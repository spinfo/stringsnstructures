package module;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import modularization.CharPipe;
import modularization.ModuleImpl;
import module.common.SplitDecisionNode;
import parallelization.CallbackReceiver;
import treeBuilder.Knoten;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ParadigmenErmittlerModul extends ModuleImpl {

	// Property keys
	public static final String PROPERTYKEY_INPUTFILE = "Suffix tree file";
	public static final String PROPERTYKEY_USEGZIP = "Suffix tree is GZIP encoded";
	public static final String PROPERTYKEY_ENCODING = "Encoding";
	public static final String PROPERTYKEY_DECISIONTREEDEPTH = "Depth of the decision tree";
	public static final String PROPERTYKEY_BUFFERLENGTH = "Buffer length";
	public static final String PROPERTYKEY_DIVIDER = "Token divider";
	public static final String PROPERTYKEY_DEPTHFACTOR = "Tree depth factor";

	// Local variables
	private File file;
	private boolean useGzip = false;
	private String encoding = "UTF-8";
	private int decisiontreeDepth = 10;
	private int bufferLength = 10;
	private String divider = "\t";
	private double depthFactor = 2d;
	
	private int anzahlDerSymbole;

	public ParadigmenErmittlerModul(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// Determine system properties (for setting default values that make
		// sense)
		String fs = System.getProperty("file.separator");
		String homedir = System.getProperty("user.home");

		// define I/O
		this.getSupportedInputs().add(CharPipe.class);
		this.getSupportedOutputs().add(CharPipe.class);

		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_INPUTFILE,
				"Path to the suffix tree file");
		this.getPropertyDescriptions().put(PROPERTYKEY_USEGZIP,
				"Set to 'true' if the suffix tree file is compressed using GZIP");
		this.getPropertyDescriptions().put(PROPERTYKEY_ENCODING,
				"The text encoding of the suffix tree file (if applicable, else set to empty string)");
		this.getPropertyDescriptions().put(PROPERTYKEY_DECISIONTREEDEPTH,
				"The depth to which the decision tree is built (NOT YET IMPLEMENTED)");
		this.getPropertyDescriptions().put(PROPERTYKEY_BUFFERLENGTH,
				"Length of the I/O buffer (streamed input) and minimal depth of the decision tree");
		this.getPropertyDescriptions().put(PROPERTYKEY_DIVIDER,
				"Divider that is inserted in between the tokens on output");
		this.getPropertyDescriptions().put(PROPERTYKEY_DEPTHFACTOR,
				"Factor to multiply the tree depth with for decision tree node score calculation (double precision)");

		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"ParadigmSegmenterModule");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INPUTFILE,
				homedir + fs + "suffixtree.txt.gz");
		this.getPropertyDefaultValues().put(PROPERTYKEY_USEGZIP, "true");
		this.getPropertyDefaultValues().put(PROPERTYKEY_ENCODING, "UTF-8");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DECISIONTREEDEPTH, "10");
		this.getPropertyDefaultValues().put(PROPERTYKEY_BUFFERLENGTH, "10");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DIVIDER, "\t");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DEPTHFACTOR, "2.0");

		// Add module description
		this.setDescription("Reads contents from a suffix tree file (JSON-encoded) and based on that data marks paradigm borders in the streamed input. Outputs segmented input data. Can handle GZIP compressed suffix tree files.");
	}

	@Override
	public boolean process() throws Exception {

		// Instantiate a new input stream
		InputStream fileInputStream = new FileInputStream(this.file);

		// Use GZIP if requested
		if (this.useGzip)
			fileInputStream = new GZIPInputStream(fileInputStream);

		// Instantiate JSON (de)serializer
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		if (this.encoding == null || this.encoding.isEmpty()) {
			// close relevant I/O instances
			fileInputStream.close();
			this.closeAllOutputs();
			throw new Exception(
					"There is no text encoding set, thus I am unable to read the suffix tree.");
		}

		// Instantiate input reader if an encoding has been set
		Reader fileReader = new InputStreamReader(fileInputStream, this.encoding);

		// Deserialize suffix tree
		Knoten suffixTreeRootNode = gson.fromJson(fileReader, Knoten.class);

		// Close relevant I/O instances
		fileReader.close();
		fileInputStream.close();
		
		// Anzahl der vorhandenen Symbole (Types) ermitteln (entspricht der Anzahl der Suffixbaum-Kindknoten der ersten Ebene)
		this.anzahlDerSymbole = suffixTreeRootNode.getKinder().size();
		
		// Read first characters
		int charCode = this.getInputCharPipe().getInput().read();
		
		// Variable for the head
		StringBuffer head = new StringBuffer();
		
		// Aktuellen Knoten merken
		Knoten aktuellerKnoten = suffixTreeRootNode;
		
		// Letzte Bewertung merken
		double letzteBewertung = 0d;

		// Loop until no more data can be read
		while (charCode != -1) {

			// Check for interrupt signal
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}

			// Token als Zeichenkette einlesen
			Character symbol = Character.valueOf((char) charCode);
			
			// Bewertung ermitteln
			double bewertung = this.symbolBewerten(symbol, aktuellerKnoten);
			
			// DEBUG
			System.out.println(head+" "+symbol+" "+bewertung);
			Thread.sleep(500l);
			
			// Trennen oder Verbinden?
			if (bewertung > 0 && bewertung > letzteBewertung){
				// Verbinden
				letzteBewertung = bewertung;
				aktuellerKnoten = aktuellerKnoten.getKinder().get(symbol.toString());
				head.append(symbol);
			//} else if (bewertung > 0){
				// Pruefen, ob der Abfall in der Bewertung groesser ist, als
			} else {
				// Trennen
				aktuellerKnoten = suffixTreeRootNode;
				letzteBewertung = 0d;
				this.outputToAllCharPipes(head.toString().concat(this.divider));
				System.out.println(); // DEBUG
				head.delete(0, head.length());
				head.append(symbol);
			}
			
			
			
			// Read next char
			charCode = this.getInputCharPipe().getInput().read();
		}
		
		// Close relevant I/O instances
		this.closeAllOutputs();

		// Success
		return true;
	}
	
	/**
	 * Gibt an, ob ein Entscheidungsbaum die Abtrennung des momentanen Kopfes der Zeichenfolge vorgibt. 
	 * @param entscheidungsbaumWurzelknoten
	 * @return Wahr, wenn abgetrennt werden soll
	 */
	private boolean trennen(SplitDecisionNode entscheidungsbaumWurzelknoten) {
		double splitValue = this.hoechsteZweigBewertungsErmitteln(entscheidungsbaumWurzelknoten.getSplit());
		double joinValue = this.hoechsteZweigBewertungsErmitteln(entscheidungsbaumWurzelknoten.getJoin());
		System.out.println(Math.round(joinValue*10000d)+":"+Math.round(splitValue*10000d));
		return splitValue>joinValue;
	}
	
	/**
	 * Gibt die hoechste Bewertung eines Zweiges zurueck.
	 * @param entscheidungsbaumWurzelknoten
	 * @return
	 */
	private double hoechsteZweigBewertungsErmitteln(SplitDecisionNode entscheidungsbaumWurzelknoten) {

		// Falls der Entscheidungsbaumknoten null ist, wird 1 zurueckgegeben
		if (entscheidungsbaumWurzelknoten == null || entscheidungsbaumWurzelknoten.getSplit() == null || entscheidungsbaumWurzelknoten.getJoin() == null)
			return 1d;

		// Bewertungsvariable festlegen
		double bewertung = entscheidungsbaumWurzelknoten.getValue();

		// Entscheidungswert der Kindzweige ermitteln
		double trennWert = hoechsteZweigBewertungsErmitteln(entscheidungsbaumWurzelknoten.getSplit());
		double bindeWert = hoechsteZweigBewertungsErmitteln(entscheidungsbaumWurzelknoten.getJoin());

		// Hoechsten Wert ermitteln (bei Gleichstand wird der Trennwert
		// bevorzugt)
		if (trennWert >= bindeWert)
			bewertung = bewertung*trennWert;
		else
			bewertung = bewertung*bindeWert;

		// Hoechste gefundene Kantenbewertung zurueckgeben
		return bewertung;
	}
	
	private SplitDecisionNode entscheidungsBaumKonstruieren(Character kopf, String rumpf, Knoten wurzelKnoten, Knoten elternKnoten, int ebenenTiefe){
		
		// Neuen Entscheidungsknoten beginnen
		SplitDecisionNode entscheidungsKnoten = new SplitDecisionNode();
		
		// Bewertung ermitteln
		entscheidungsKnoten.setValue(this.symbolBewerten(kopf, elternKnoten));
		
		// Notiz anfuegen (nur zur Information)
		if (elternKnoten != null && kopf != null)
			entscheidungsKnoten.setNotiz(elternKnoten.getName()+"-"+kopf.toString());
		
		if (!rumpf.isEmpty()){
			// Kindknoten fuer Trennaktion
			entscheidungsKnoten.setSplit(this.entscheidungsBaumKonstruieren(rumpf.charAt(0), rumpf.substring(1), wurzelKnoten, wurzelKnoten, ebenenTiefe));
			// Kindknoten fuer Bindeaktion
			if (elternKnoten != null)
				entscheidungsKnoten.setJoin(this.entscheidungsBaumKonstruieren(rumpf.charAt(0), rumpf.substring(1), wurzelKnoten, elternKnoten.getKinder().get(kopf.toString()), ebenenTiefe+1));
			else
				entscheidungsKnoten.setJoin(new SplitDecisionNode(0d,kopf.toString()));
		}
		
		// Rueckgabe des erstellten Entscheidungsknotens
		return entscheidungsKnoten;
	}
	
	/**
	 * Bewertet ein einzelnes Symbol
	 * @param symbol Symbol (Zeichenkette mit Laenge eins)
	 * @param elternKnoten Elternknoten im Suffixbaum
	 * @return Bewertung 0 <= X <= ebenenFaktor
	 */
	private double symbolBewerten(Character symbol, Knoten elternKnoten){
		// Variable fuer das Gesamtergebnis
		double bewertung = 0d;
		
		// Pruefen, ob der aktuelle Knoten des Suffixbaumes unter dem aktuellen Symbol der Zeichenkette einen Kindknoten fuehrt.
		if (symbol != null && elternKnoten != null && elternKnoten.getKinder().containsKey(symbol.toString())){
			
			// Knoten ermitteln
			Knoten kindKnoten = elternKnoten.getKinder().get(symbol.toString());
			
			// Ermitteln, welchen Wert der aktuelle Knoten hat
			int gesamtwert = elternKnoten.getZaehler();
			
			// Ermitteln, welchen Wert der Kindknoten hat
			int teilwert = kindKnoten.getZaehler();
			
			// Anteil des Kindknotenzaehlers am Zaehler seines Elternknoten ermitteln
			double anteil = new Double(teilwert)/new Double(gesamtwert); // 0 < anteil <= 1
			
			// Bewertung fuer diesen Kindknoten errechnen und mit Anzahl der Symbole justieren
			bewertung = anteil*new Double(this.anzahlDerSymbole);
			
		}
		
		// Ergebnis zurueckgeben
		return bewertung;
	}
	
	/*private double zeichenketteBewerten(Knoten suffixbaumWurzelknoten, String... zeichenketten){
		
		// Variable fuer das Gesamtergebnis
		double bewertung = 0d;
		
		// Schleife ueber Zeichenketten
		for (int i=0; i<zeichenketten.length; i++){
			
			// Erster aktueller Knoten des Suffixbaumes ist dessen Wurzel
			Knoten aktuellerSuffixbaumKnoten = suffixbaumWurzelknoten;
			
			// Schleife ueber die einzelnen Symbole der aktuellen Zeichenkette
			for (int j=0; j<zeichenketten[i].length(); j++){
				
				// Pruefen, ob der aktuelle Knoten des Suffixbaumes unter dem aktuellen Symbol der Zeichenkette einen Kindknoten fuehrt.
				if (aktuellerSuffixbaumKnoten.getKinder().containsKey(zeichenketten[i].substring(j, j+1))){
					
					// Knoten ermitteln
					Knoten kindKnoten = aktuellerSuffixbaumKnoten.getKinder().get(zeichenketten[i].substring(j, j+1));
					
					// Ermitteln, welchen Wert der aktuelle Knoten hat
					int gesamtwert = aktuellerSuffixbaumKnoten.getZaehler();
					
					// Ermitteln, welchen Wert der Kindknoten hat
					int teilwert = kindKnoten.getZaehler();
					
					// Anteil des Kindknotenzaehlers am Zaehler seines Elternknoten ermitteln
					double anteil = new Double(teilwert)/new Double(gesamtwert); // 0 < anteil <= 1
					
					// Bewertung fuer diesen Kindknoten errechnen und auf das Gesamtergebnis addieren
					bewertung += (new Double(j)+1d)*anteil;
					
					// Kindknoten als aktuellen Knoten definieren
					aktuellerSuffixbaumKnoten = kindKnoten;
					
				} else
					// Kein Kindknoten gefunden, Schleife abbrechen
					break;
			}
		}
		
		// Ergebnis zurueckgeben
		return bewertung;
	}*/

	@Override
	public void applyProperties() throws Exception {
		
		if (this.getProperties().containsKey(PROPERTYKEY_INPUTFILE))
			this.file = new File(this.getProperties().getProperty(PROPERTYKEY_INPUTFILE));
		
		if (this.getProperties().containsKey(PROPERTYKEY_USEGZIP))
			this.useGzip = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_USEGZIP));
		
		if (this.getProperties().containsKey(PROPERTYKEY_ENCODING))
			this.encoding = this.getProperties().getProperty(PROPERTYKEY_ENCODING);
		else if (this.getPropertyDefaultValues() != null && this.getPropertyDefaultValues().containsKey(PROPERTYKEY_ENCODING))
				this.encoding = this.getPropertyDefaultValues().get(PROPERTYKEY_ENCODING);
		
		if (this.getProperties().containsKey(PROPERTYKEY_DECISIONTREEDEPTH))
			this.decisiontreeDepth = Integer.parseUnsignedInt(this.getProperties().getProperty(PROPERTYKEY_DECISIONTREEDEPTH));
		else if (this.getPropertyDefaultValues() != null && this.getPropertyDefaultValues().containsKey(PROPERTYKEY_DECISIONTREEDEPTH))
			this.decisiontreeDepth = Integer.parseUnsignedInt(this.getPropertyDefaultValues().get(PROPERTYKEY_DECISIONTREEDEPTH));
			
		if (this.getProperties().containsKey(PROPERTYKEY_BUFFERLENGTH))
			this.bufferLength = Integer.parseInt(this.getProperties().getProperty(PROPERTYKEY_BUFFERLENGTH));
		else if (this.getPropertyDefaultValues() != null && this.getPropertyDefaultValues().containsKey(PROPERTYKEY_BUFFERLENGTH))
			this.bufferLength = Integer.parseInt(this.getPropertyDefaultValues().get(PROPERTYKEY_BUFFERLENGTH));
		
		if (this.getProperties().containsKey(PROPERTYKEY_DIVIDER))
			this.divider = this.getProperties().getProperty(PROPERTYKEY_DIVIDER);
		else if (this.getPropertyDefaultValues() != null && this.getPropertyDefaultValues().containsKey(PROPERTYKEY_DIVIDER))
				this.divider = this.getPropertyDefaultValues().get(PROPERTYKEY_DIVIDER);
		
		if (this.getProperties().containsKey(PROPERTYKEY_DEPTHFACTOR))
			this.depthFactor = Double.parseDouble(this.getProperties().getProperty(PROPERTYKEY_DEPTHFACTOR));
		else if (this.getPropertyDefaultValues() != null && this.getPropertyDefaultValues().containsKey(PROPERTYKEY_DEPTHFACTOR))
			this.depthFactor = Double.parseDouble(this.getPropertyDefaultValues().get(PROPERTYKEY_DEPTHFACTOR));
			
		super.applyProperties();
	}

}
