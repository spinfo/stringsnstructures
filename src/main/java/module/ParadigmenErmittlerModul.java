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

	// Local variables
	private File file;
	private boolean useGzip = false;
	private String encoding = "UTF-8";
	private int decisiontreeDepth = 10;
	private int bufferLength = 8192;
	private SplitDecisionNode splitDecisionTreeRootNode;
	private String divider = "\t";

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
				"The depth to which the decision tree is built");
		this.getPropertyDescriptions().put(PROPERTYKEY_BUFFERLENGTH,
				"Length of the I/O buffer (streamed input)");
		this.getPropertyDescriptions().put(PROPERTYKEY_DIVIDER,
				"Divider that is inserted in between the tokens on output");

		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"ParadigmSegmenterModule");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INPUTFILE,
				homedir + fs + "suffixtree.txt.gz");
		this.getPropertyDefaultValues().put(PROPERTYKEY_USEGZIP, "true");
		this.getPropertyDefaultValues().put(PROPERTYKEY_ENCODING, "UTF-8");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DECISIONTREEDEPTH, "10");
		this.getPropertyDefaultValues().put(PROPERTYKEY_BUFFERLENGTH, "8192");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DIVIDER, "\t");

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
		
		// Initialize stream input buffer
		LinkedList<Character> buffer = new LinkedList<Character>();
		
		// Read first characters
		int charCode = this.getInputCharPipe().getInput().read();
		
		// Variable for the head
		StringBuffer head = new StringBuffer();

		// Loop until no more data can be read
		while (charCode != -1) {

			// Check for interrupt signal
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}

			// Add read char code to buffer
			buffer.add(Character.valueOf((char) charCode));
			
			// If the buffer exceeds the set maximum length, remove the oldest (first) element
			if (buffer.size()>this.bufferLength){
				Character removed = buffer.removeFirst();
				
				// Append it to the head
				head.append(removed);
			}
				
			
			// Construct trie from buffer and attach it to the root node (skip this until the buffer is full)
			if (buffer.size()==this.bufferLength){
				
				// Puffer in Zeichenkette umwandeln (TODO Leistungsfaehigere Loesung implementieren)
				StringBuffer tail = new StringBuffer();
				Iterator<Character> characters = buffer.iterator();
				while (characters.hasNext()){
					tail.append(characters.next());
				}
				
				// Entscheidungsbaum konstruieren
				SplitDecisionNode entscheidungsbaumWurzelknoten = this.entscheidungsBaumKonstruieren(head.toString()+tail.toString(), head.length(), suffixTreeRootNode);
				
				String test = entscheidungsbaumWurzelknoten.toString();
				
				// Entscheidungsbaum auswerten
				boolean trennen = this.trennen(entscheidungsbaumWurzelknoten);
				
				if (trennen){
					this.outputToAllCharPipes(head.toString().concat(this.divider));
					head.delete(0, head.length());
				}
				
			}
			
			// Read next char
			charCode = this.getInputCharPipe().getInput().read();
		}
		
		// Read remaining buffer
		if (!buffer.isEmpty())
			buffer.removeFirst();
		while (!buffer.isEmpty()){
			// TODO process rest of data
			
			
			buffer.removeFirst();
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
		return this.hoechsteKantenbewertungErmitteln(entscheidungsbaumWurzelknoten.getSplit())>this.hoechsteKantenbewertungErmitteln(entscheidungsbaumWurzelknoten.getJoin());
	}
	
	/**
	 * Gibt die hoechste Bewertung einer Kante zurueck.
	 * @param entscheidungsbaumWurzelknoten
	 * @return
	 */
	private double hoechsteKantenbewertungErmitteln(SplitDecisionNode entscheidungsbaumWurzelknoten) {

		// Bewertungsvariable festlegen
		double hoechsteBewertung = 0d;

		// Falls Kindknoten vorhanden sind, wird deren Wert verwendet
		if (entscheidungsbaumWurzelknoten != null)
			if (entscheidungsbaumWurzelknoten.getJoin() != null) {
				hoechsteBewertung = this
						.hoechsteKantenbewertungErmitteln(entscheidungsbaumWurzelknoten
								.getJoin());
			} else if (entscheidungsbaumWurzelknoten.getSplit() != null) {
				double trennenBewertung = this
						.hoechsteKantenbewertungErmitteln(entscheidungsbaumWurzelknoten
								.getSplit());
				if (trennenBewertung > hoechsteBewertung)
					hoechsteBewertung = trennenBewertung;
			} else {
				// Keine Kindknoten vorhanden, wir verwenden die Bewertung des
				// aktuellen Knotens
				hoechsteBewertung = entscheidungsbaumWurzelknoten.getValue();
			}

		// Hoechste gefundene Kantenbewertung zurueckgeben
		return hoechsteBewertung;
	}
	
	private SplitDecisionNode entscheidungsBaumKonstruieren(String eingabe, int rumpfIndex, Knoten suffixbaumWurzelknoten){
		
		// Falls die Eingabe leer oder die Maximallaenge zu gering ist, wird abgebrochen
		if (eingabe == null || eingabe.isEmpty())
			return null;
		
		// Neuen Entscheidungsknoten beginnen
		SplitDecisionNode entscheidungsKnoten = new SplitDecisionNode();
		
		// Rumpfteil der Eingabe abtrennen
		String kopf = eingabe.substring(0, rumpfIndex);
		String rumpf = eingabe.substring(rumpfIndex);
		
		// Knoten des letzten Kopfsymbols ermitteln
		Knoten aktuellerKnoten = suffixbaumWurzelknoten;
		for (int i=0; i<kopf.length(); i++){
			
			// Kindknoten des naechsten Symbols ermitteln
			Knoten kindKnoten = aktuellerKnoten.getKinder().get(kopf.substring(i, i+1));
			
			// Falls kein Kindknoten gefunden wurde, wird der Entscheidungsknoten mit Wert null zurueckgegeben
			if (kindKnoten == null){
				entscheidungsKnoten.setValue(0d);
				return entscheidungsKnoten;
			}
			
			// Kindknoten als aktuellen Knoten einsetzen
			aktuellerKnoten = kindKnoten;
		}
		
		// Ebenenfaktor fuer die Bewertung festlegen
		double ebenenFaktor = new Double(rumpfIndex);
		
		// Bewertung des ersten Symbols errechnen
		double bewertung = this.symbolBewerten(rumpf.substring(0, 1), aktuellerKnoten, ebenenFaktor);
		
		// Bewertung in Entscheidungsknoten einfuegen
		entscheidungsKnoten.setValue(bewertung);
		
		// Falls der Rumpf der Eingabezeichenkette noch weitere (>2) Symbole hat, werden rekursiv Kindknoten erstellt
		if (rumpf.length()>2){
			// Teilen
			entscheidungsKnoten.setSplit(this.entscheidungsBaumKonstruieren(eingabe, rumpfIndex+1, suffixbaumWurzelknoten));
			
			// Nicht teilen
			entscheidungsKnoten.setJoin(this.entscheidungsBaumKonstruieren(eingabe, rumpfIndex+2, suffixbaumWurzelknoten));
		}
		
		// Rueckgabe des erstellten Entscheidungsknotens
		return entscheidungsKnoten;
	}
	
	/**
	 * Bewertet ein einzelnes Symbol
	 * @param symbol Symbol (Zeichenkette mit Laenge eins)
	 * @param elternKnoten Elternknoten im Suffixbaum
	 * @param ebenenFaktor Faktor, mit welchem die Bewertung multipliziert werden soll (=Baumtiefe des Kindknotens)
	 * @return Bewertung 0 <= X <= ebenenFaktor
	 */
	private double symbolBewerten(String symbol, Knoten elternKnoten, double ebenenFaktor){
		// Variable fuer das Gesamtergebnis
		double bewertung = 0d;
		
		// Pruefen, ob der aktuelle Knoten des Suffixbaumes unter dem aktuellen Symbol der Zeichenkette einen Kindknoten fuehrt.
		if (elternKnoten.getKinder().containsKey(symbol)){
			
			// Knoten ermitteln
			Knoten kindKnoten = elternKnoten.getKinder().get(symbol);
			
			// Ermitteln, welchen Wert der aktuelle Knoten hat
			int gesamtwert = elternKnoten.getZaehler();
			
			// Ermitteln, welchen Wert der Kindknoten hat
			int teilwert = kindKnoten.getZaehler();
			
			// Anteil des Kindknotenzaehlers am Zaehler seines Elternknoten ermitteln
			double anteil = new Double(teilwert)/new Double(gesamtwert); // 0 < anteil <= 1
			
			// Bewertung fuer diesen Kindknoten errechnen und auf das Gesamtergebnis addieren
			bewertung += ebenenFaktor*anteil;
			
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
		
		super.applyProperties();
	}

}
