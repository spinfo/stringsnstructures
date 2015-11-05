package modules.paradigmSegmenter;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.treeBuilder.Knoten;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.parallelization.CallbackReceiver;

public class ParadigmenErmittlerModul extends ModuleImpl {

	// Property keys
	public static final String PROPERTYKEY_BUFFERLENGTH = "Buffer length";
	public static final String PROPERTYKEY_DIVIDER = "Token divider";
	public static final String PROPERTYKEY_MINDESTKOSTENPROEBENE = "Minimal cost";
	public static final String PROPERTYKEY_BEWERTUNGSABFALLFAKTOR = "Bewertungsabfallfaktor";
	public static final String PROPERTYKEY_BEWERTUNGAUSGEBEN = "Bewertung mit in Ausgabe schreiben";
	public static final String PROPERTYKEY_INPUTDIVIDER = "Input divider";

	// Local variables
	private final String TEXTINPUTID = "text input";
	private final String TRIEINPUTID = "suffix trie (json)";
	private final String OUTPUTID = "output";
	private int pufferGroesse = 12;
	private String divider = "\t";
	private double mindestKostenProSymbolEbene;
	private double bewertungsAbfallFaktor;
	private boolean bewertungAusgeben = false;
	private Character inputDivider = null;

	public ParadigmenErmittlerModul(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// define I/O
		InputPort textInputPort = new InputPort(TEXTINPUTID, "Plain text character input.", this);
		textInputPort.addSupportedPipe(CharPipe.class);
		InputPort trieInputPort = new InputPort(TRIEINPUTID, "JSON-encoded suffix trie input.", this);
		trieInputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(OUTPUTID, "Plain text character output (with dividers added).", this);
		outputPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(textInputPort);
		super.addInputPort(trieInputPort);
		super.addOutputPort(outputPort);

		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_BUFFERLENGTH,
				"Size of the segmentation window (should not exceed an enforced depth maximum of the trie [if applicable])");
		this.getPropertyDescriptions().put(PROPERTYKEY_DIVIDER,
				"Divider that is inserted in between the tokens on output");
		this.getPropertyDescriptions().put(PROPERTYKEY_MINDESTKOSTENPROEBENE,
				"Minimum cost for every joining step; note that higher values significantly increase the frequency of backtracking [double]");
		this.getPropertyDescriptions().put(PROPERTYKEY_BEWERTUNGSABFALLFAKTOR,
				"Factor to modify the weight of a rating decrease between symbols [double, >0, 1=neutral]");
		this.getPropertyDescriptions().put(PROPERTYKEY_BEWERTUNGAUSGEBEN,
				"Include rating values in output");
		this.getPropertyDescriptions().put(PROPERTYKEY_INPUTDIVIDER,
				"Divider that marks the input tokens (empty for char-by-char input)");

		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"ParadigmSegmenterModule");
		this.getPropertyDefaultValues().put(PROPERTYKEY_BUFFERLENGTH, "10");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DIVIDER, "\t");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MINDESTKOSTENPROEBENE, "1");
		this.getPropertyDefaultValues().put(PROPERTYKEY_BEWERTUNGSABFALLFAKTOR, "1");
		this.getPropertyDefaultValues().put(PROPERTYKEY_BEWERTUNGAUSGEBEN, "false");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INPUTDIVIDER, "");

		// Add module description
		this.setDescription("Reads contents from a suffix tree file (JSON-encoded) and based on that data marks paradigm borders in the streamed input. Outputs segmented input data. Can handle GZIP compressed suffix tree files.");
	}

	@Override
	public boolean process() throws Exception {
		
		/*
		 * Suffixbaum einlesen
		 */

		// Instantiate JSON (de)serializer
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		// Instantiate input reader if an encoding has been set
		Reader trieReader = this.getInputPorts().get(TRIEINPUTID).getInputReader();

		// Deserialize suffix tree
		Knoten suffixbaumWurzelknoten = gson.fromJson(trieReader, Knoten.class);

		/*
		 * Segmentierung des Eingabedatenstroms
		 */
		
		// Symbolbewerter instanziieren
		SymbolBewerter symbolBewerter = new SymbolBewerter(this.mindestKostenProSymbolEbene, this.bewertungsAbfallFaktor);
		
		// Variable for input char code
		int zeichenCode;
		
		// Entscheidungsbaum starten
		SplitDecisionNode entscheidungsbaumWurzelknoten = null;

		// EntscheidungsAeffchen initialisieren
		EntscheidungsAeffchen aeffchen = new EntscheidungsAeffchen(symbolBewerter, suffixbaumWurzelknoten);
		EntscheidungsAeffchen.debug = true;
		
		// Eingabepuffer initialisieren
		List<String> puffer = new ArrayList<String>();
		
		// Initialise buffer for chars to build up to a token
		StringBuffer charBuffer = new StringBuffer();
		
		// Sekundaeren Eingabepuffer fuer nicht segmentierbare Zeichenketten initialisieren
		List<String> sekundaerPuffer = new ArrayList<String>();
		
		// HashMap zur Zwischenspeicherung von Ergebnisbaumzweigen
		//Map<Character,SplitDecisionNode> entscheidungsBaumZweige = new HashMap<Character,SplitDecisionNode>();
		
		// Daten Zeichen fuer Zeichen einlesen
		while (true) {
			
			// Read next char
			zeichenCode = this.getInputPorts().get(TEXTINPUTID).getInputReader().read();
			
			// Break if no more input available
			if (zeichenCode == -1)
				break;

			// Check for interrupt signal
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}

			// Zeichen einlesen
			Character symbol = Character.valueOf((char) zeichenCode);
			// Check whether the read symbol is an input divider
			if (this.inputDivider == null || symbol.equals(this.inputDivider)){
				if (this.inputDivider == null)
					puffer.add(symbol.toString());
				else {
					// Append char buffer to token buffer
					puffer.add(charBuffer.toString());
					charBuffer = new StringBuffer();
				}
			} else {
				// Append symbol to char buffer
				System.out.println("cs1:"+charBuffer.length());
				charBuffer.append(symbol);
				System.out.println("cs2:"+charBuffer.length());
				continue;
			}
			
			// Puffergroesse pruefen
			if (puffer.size() == this.pufferGroesse){
				// Ggf. Entscheidungsbaum beginnen
				if (entscheidungsbaumWurzelknoten == null){
					entscheidungsbaumWurzelknoten = new SplitDecisionNode(0d, suffixbaumWurzelknoten, suffixbaumWurzelknoten.getKinder().get(puffer.get(0)), null, puffer.get(0));
					//entscheidungsBaumZweige.put(symbol, entscheidungsbaumWurzelknoten);
				}
				
				// Wenn der Eingabepuffer die erforderliche Groesse erreicht hat, wird er segmentiert
				SplitDecisionNode blattBesterWeg = aeffchen.konstruiereEntscheidungsbaum(puffer, entscheidungsbaumWurzelknoten);
				
				// Erstes Segment (erster Entscheidungsknoten, der trennt) ermitteln, Entscheidungsbaum stutzen, Puffer kuerzen
				
				// Zuletzt trennenden Entscheidungsbaumknoten ermitteln
				SplitDecisionNode letzteTrennstelle = null;
				SplitDecisionNode letztesBlatt = blattBesterWeg;
				double letzteTrennstellenBewertung = 0d;
				while (letztesBlatt.getElternKnoten() != null){
					letztesBlatt = letztesBlatt.getElternKnoten();
					double trennstellenBewertung = letztesBlatt.getJoin().getAktivierungsPotential()-letztesBlatt.getSplit().getAktivierungsPotential();
					if (trennstellenBewertung>0){
						letzteTrennstelle = letztesBlatt;
						letzteTrennstellenBewertung = trennstellenBewertung;
					}
				}
				
				// Pruefen, ob eine Trennstelle gefunden wurde
				if (letzteTrennstelle == null){
					// Wenn gar keine Trennstelle gefunden wurde, wird der Puffer mit Ausnahme des letzten Zeichens in den Sekundaerpuffer uebertragen

					sekundaerPuffer.addAll(puffer);
					puffer = new ArrayList<String>();
					puffer.add(sekundaerPuffer.remove(sekundaerPuffer.size()-1));
					// Entscheidungsbaum stutzen
					entscheidungsbaumWurzelknoten = blattBesterWeg;
					entscheidungsbaumWurzelknoten.setElternKnoten(null);
				} else {
					
					// Trennstelle gefunden, Segment abloesen und ausgeben
					
					// Tiefe der letzten Trennstelle ermitteln
					int tiefe = 1;
					SplitDecisionNode entscheidungsbaumKnoten = letzteTrennstelle;
					while (entscheidungsbaumKnoten.getElternKnoten() != null){
						entscheidungsbaumKnoten = entscheidungsbaumKnoten.getElternKnoten();
						tiefe ++;
					}
					
					// Segment ermitteln (Sekundaerpuffer + Puffer bis zur ermittelten Tiefe)
					List<String> segment = new ArrayList<String>();
					segment.addAll(sekundaerPuffer);
					segment.addAll(puffer.subList(0, tiefe));
					
					// Segment aus Puffer loeschen
					puffer = puffer.subList(tiefe, puffer.size());
					
					// Sekundaerpuffer loeschen
					sekundaerPuffer.clear();
					
					// Entscheidungsbaum stutzen
					entscheidungsbaumWurzelknoten = letzteTrennstelle.getSplit();
					if (entscheidungsbaumWurzelknoten != null)
						entscheidungsbaumWurzelknoten.setElternKnoten(null);
					
					// Segment ausgeben
					Iterator<String> segmentStrings = segment.iterator();
					while (segmentStrings.hasNext()){
						this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(segmentStrings.next());
						if (this.inputDivider != null)
							this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(this.inputDivider.toString());
					}
					this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(this.divider);
					
					
					if (bewertungAusgeben)
						this.getOutputPorts().get(OUTPUTID).outputToAllCharPipes(letzteTrennstellenBewertung+this.divider);
				}
				
			}
			
		}
		
		// Close relevant I/O instances
		this.closeAllOutputs();
		// Success
		return true;
	}

	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();
			
		if (this.getProperties().containsKey(PROPERTYKEY_BUFFERLENGTH))
			this.pufferGroesse = Integer.parseInt(this.getProperties().getProperty(PROPERTYKEY_BUFFERLENGTH));
		
		if (this.getProperties().containsKey(PROPERTYKEY_DIVIDER))
			this.divider = this.getProperties().getProperty(PROPERTYKEY_DIVIDER);
		
		if (this.getProperties().containsKey(PROPERTYKEY_MINDESTKOSTENPROEBENE))
			this.mindestKostenProSymbolEbene = Double.parseDouble(this.getProperties().getProperty(PROPERTYKEY_MINDESTKOSTENPROEBENE));
	
		if (this.getProperties().containsKey(PROPERTYKEY_BEWERTUNGSABFALLFAKTOR))
			this.bewertungsAbfallFaktor = Double.parseDouble(this.getProperties().getProperty(PROPERTYKEY_BEWERTUNGSABFALLFAKTOR));
		
		if (this.getProperties().containsKey(PROPERTYKEY_BEWERTUNGAUSGEBEN))
			this.bewertungAusgeben = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_BEWERTUNGAUSGEBEN));
		
		if (this.getProperties().containsKey(PROPERTYKEY_INPUTDIVIDER) && this.getProperties().getProperty(PROPERTYKEY_INPUTDIVIDER) != null && !this.getProperties().getProperty(PROPERTYKEY_INPUTDIVIDER).isEmpty())
			this.inputDivider = Character.valueOf(this.getProperties().getProperty(PROPERTYKEY_INPUTDIVIDER).charAt(0));
			
		super.applyProperties();
	}

}
