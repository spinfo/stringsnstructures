package modules.segmentation.paradigmSegmenter;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import models.ExtensibleTreeNode;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import common.StringUnescaper;
import common.parallelization.CallbackReceiver;

public class ParadigmSegmenterModule extends ModuleImpl {

	// Property keys
	public static final String PROPERTYKEY_BUFFERSIZE = "Buffer size";
	public static final String PROPERTYKEY_OUTPUTTOKENDIVIDER = "Output token divider";
	public static final String PROPERTYKEY_OUTPUTDOCUMENTDIVIDER = "Output document divider";
	public static final String PROPERTYKEY_MINCOSTPERLAYER = "Minimal cost";
	public static final String PROPERTYKEY_SCORINGDECREASEFACTOR = "Scoring decrease factor";
	public static final String PROPERTYKEY_INCLUDESCORING = "Include scoring value in output";
	public static final String PROPERTYKEY_INPUTTOKENDIVIDER = "Input token divider";
	public static final String PROPERTYKEY_INPUTDOCUMENTDIVIDER = "Input document divider";

	// Local variables
	private final String TEXTINPUTID = "text";
	private final String TRIEINPUTID = "suffix trie";
	private final String OUTPUTID = "output";
	private int bufferSize = 12;
	private double minimalCostPerSymbolLayer;
	private double scoringDecreaseFactor;
	private boolean includeScoringInOutput = false;
	private String inputDocumentDivider = "\n";
	private Character inputTokenDivider = null;
	private String outputTokenDivider = "\t";
	private String outputDocumentDivider = "\n";

	public ParadigmSegmenterModule(CallbackReceiver callbackReceiver,
			Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// define I/O
		InputPort textInputPort = new InputPort(TEXTINPUTID,
				"Plain text character input.", this);
		textInputPort.addSupportedPipe(CharPipe.class);
		InputPort trieInputPort = new InputPort(TRIEINPUTID,
				"JSON-encoded suffix trie input.", this);
		trieInputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(OUTPUTID,
				"Plain text character output (with dividers added).", this);
		outputPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(textInputPort);
		super.addInputPort(trieInputPort);
		super.addOutputPort(outputPort);

		// Add description for properties
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_BUFFERSIZE,
						"Size of the segmentation window (should not exceed an enforced depth maximum of the trie [if applicable]).");
		this.getPropertyDescriptions().put(PROPERTYKEY_OUTPUTTOKENDIVIDER,
				"Divider that is inserted in between the tokens on output.");
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_MINCOSTPERLAYER,
						"Minimum cost for every joining step; note that higher values significantly increase the frequency of backtracking [double].");
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_SCORINGDECREASEFACTOR,
						"Factor to modify the weight of a scoring decrease between symbols [double, >0, 1=neutral].");
		this.getPropertyDescriptions().put(PROPERTYKEY_INCLUDESCORING,
				"Include scoring values in output.");
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_INPUTTOKENDIVIDER,
						"Divider that marks the input tokens (single character; empty for char-by-char input).");
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_INPUTDOCUMENTDIVIDER,
						"Divider that marks the input documents (leave empty if input does not divide into separate documents).");
		this.getPropertyDescriptions()
				.put(PROPERTYKEY_OUTPUTDOCUMENTDIVIDER,
						"Divider that will be used to mark the document borders in the output, if applicable.");

		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"Paradigm Segmenter");
		this.getPropertyDefaultValues().put(PROPERTYKEY_BUFFERSIZE, "10");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MINCOSTPERLAYER, "1");
		this.getPropertyDefaultValues().put(PROPERTYKEY_SCORINGDECREASEFACTOR,
				"1");
		this.getPropertyDefaultValues()
				.put(PROPERTYKEY_INCLUDESCORING, "false");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INPUTTOKENDIVIDER, "");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INPUTDOCUMENTDIVIDER,
				"\\n");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OUTPUTTOKENDIVIDER,
				"\\t");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OUTPUTDOCUMENTDIVIDER,
				"\\n");

		// Add module description
		this.setDescription("Reads contents from a JSON-encoded¹ atomic suffix tree (AST²) "
				+ "and based on that data determines paradigm borders in the streamed input. "
				+ "Outputs segmented input data.<br/><br/>"
				+ "------"
				+ "<p>¹: Using the <i>models.ExtensibleTreeNode</i> class</p>"
				+ "<p>²: Giegerich, Robert, and Stefan Kurtz. &quot;From Ukkonen to McCreight "
				+ "and Weiner: A unifying view of linear-time suffix tree construction.&quot; "
				+ "Algorithmica 19.3 (1997): 331-353."
				+ "<br/>Available at http://link.springer.com/article/10.1007/PL00009177</p>");

		// Add module category
		this.setCategory("Segmentation");
	}

	@Override
	public boolean process() throws Exception {

		/*
		 * Suffixbaum einlesen
		 */

		// Instantiate JSON (de)serializer
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		// Instantiate input reader if an encoding has been set
		Reader trieReader = this.getInputPorts().get(TRIEINPUTID)
				.getInputReader();

		// Deserialize suffix tree
		ExtensibleTreeNode suffixbaumWurzelknoten = gson.fromJson(trieReader,
				ExtensibleTreeNode.class);

		/*
		 * Segmentierung des Eingabedatenstroms
		 */

		if (this.inputDocumentDivider != null
				&& !this.inputDocumentDivider.isEmpty()) {
			// Initialise input scanner
			Scanner inputScanner = new Scanner(this.getInputPorts()
					.get(TEXTINPUTID).getInputReader());
			inputScanner.useDelimiter(this.inputDocumentDivider);
			while (inputScanner.hasNext()) {
				String document = inputScanner.next();
				Reader documentReader = new StringReader(document);
				this.segmentString(documentReader, suffixbaumWurzelknoten);
				documentReader.close();
				this.getOutputPorts().get(OUTPUTID)
						.outputToAllCharPipes(this.outputDocumentDivider);
			}
			inputScanner.close();

		} else {
			this.segmentString(this.getInputPorts().get(TEXTINPUTID)
					.getInputReader(), suffixbaumWurzelknoten);
		}

		// Close relevant I/O instances
		this.closeAllOutputs();
		// Success
		return true;
	}

	private void segmentString(Reader input,
			ExtensibleTreeNode atomicSuffixTreeRootNode) throws Exception {

		// Symbolbewerter instanziieren
		SymbolRater symbolRater = new SymbolRater(
				this.minimalCostPerSymbolLayer, this.scoringDecreaseFactor);

		// DecisionMonkey initialisieren
		DecisionMonkey monkey = new DecisionMonkey(
				symbolRater, atomicSuffixTreeRootNode);
		DecisionMonkey.debug = true;

		// Variable for input char code
		int charCode;

		// Entscheidungsbaum starten
		SplitDecisionNode decisionTreeRootNode = null;

		// Eingabepuffer initialisieren
		Deque<String> buffer = new ArrayDeque<String>();

		// Initialise buffer for chars to build up to a token
		StringBuffer charBuffer = new StringBuffer();

		// Sekundaeren Eingabepuffer fuer nicht segmentierbare Zeichenketten
		// initialisieren
		List<String> secondaryBuffer = new ArrayList<String>();

		// HashMap zur Zwischenspeicherung von Ergebnisbaumzweigen
		// Map<Character,SplitDecisionNode> entscheidungsBaumZweige = new HashMap<Character,SplitDecisionNode>();

		// Daten Zeichen fuer Zeichen einlesen
		while (true) {

			// Read next char
			charCode = input.read();

			// Check for interrupt signal
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}

			// Check whether there was still input to read
			if (charCode != -1) {
				// Zeichen einlesen
				Character symbol = Character.valueOf((char) charCode);
				// Check whether the read symbol is an input divider
				if (this.inputTokenDivider == null
						|| symbol.equals(this.inputTokenDivider)) {
					if (this.inputTokenDivider == null) {
						buffer.add(symbol.toString());
					} else {
						// Append char buffer to token buffer
						buffer.add(charBuffer.toString());
						charBuffer = new StringBuffer();
					}
				} else {
					// Append symbol to char buffer
					charBuffer.append(symbol);
					continue;
				}
			}

			// Puffergroesse pruefen (if there is no more input the buffer size
			// is ignored here)
			if (buffer.size() == this.bufferSize || charCode == -1) {
				// Ggf. Entscheidungsbaum beginnen
				if (decisionTreeRootNode == null) {
					decisionTreeRootNode = new SplitDecisionNode(0d,
							atomicSuffixTreeRootNode, atomicSuffixTreeRootNode
									.getChildNodes().get(buffer.peekFirst()),
							null, buffer.peekFirst());
					// entscheidungsBaumZweige.put(symbol,
					// entscheidungsbaumWurzelknoten);
				}

				// Wenn der Eingabepuffer die erforderliche Groesse erreicht
				// hat, wird er segmentiert
				List<String> bufferList = new ArrayList<String>(buffer.size());
				bufferList.addAll(buffer); // TODO: Ugly conversion. Find better
											// way to do this.
				SplitDecisionNode blattBesterWeg = monkey
						.konstruiereEntscheidungsbaum(bufferList,
								decisionTreeRootNode);

				// Erstes Segment (erster Entscheidungsknoten, der trennt)
				// ermitteln, Entscheidungsbaum stutzen, Puffer kuerzen

				// Zuletzt trennenden Entscheidungsbaumknoten ermitteln
				SplitDecisionNode letzteTrennstelle = null;
				SplitDecisionNode letztesBlatt = blattBesterWeg;
				double letzteTrennstellenBewertung = 0d;
				while (letztesBlatt.getElternKnoten() != null) {
					letztesBlatt = letztesBlatt.getElternKnoten();
					double trennstellenBewertung = letztesBlatt.getJoin()
							.getAktivierungsPotential()
							- letztesBlatt.getSplit()
									.getAktivierungsPotential();
					if (trennstellenBewertung > 0) {
						letzteTrennstelle = letztesBlatt;
						letzteTrennstellenBewertung = trennstellenBewertung;
					}
				}

				// Pruefen, ob eine Trennstelle gefunden wurde
				if (letzteTrennstelle == null) {
					// Wenn gar keine Trennstelle gefunden wurde, wird der
					// Puffer mit Ausnahme des letzten Zeichens in den
					// Sekundaerpuffer uebertragen

					secondaryBuffer.addAll(buffer);
					buffer.clear();
					buffer.add(secondaryBuffer.remove(secondaryBuffer.size() - 1));
					// Entscheidungsbaum stutzen
					decisionTreeRootNode = blattBesterWeg;
					decisionTreeRootNode.setElternKnoten(null);
				} else {

					// Trennstelle gefunden, Segment abloesen und ausgeben

					// Tiefe der letzten Trennstelle ermitteln
					int tiefe = 1;
					SplitDecisionNode entscheidungsbaumKnoten = letzteTrennstelle;
					while (entscheidungsbaumKnoten.getElternKnoten() != null) {
						entscheidungsbaumKnoten = entscheidungsbaumKnoten
								.getElternKnoten();
						tiefe++;
					}

					// Segment ermitteln (Sekundaerpuffer + Puffer bis zur
					// ermittelten Tiefe)
					List<String> segment = new ArrayList<String>();
					segment.addAll(secondaryBuffer);
					for (int i = 0; i < tiefe; i++) {
						// append the first symbol in the input buffer to the
						// segment (and remove it from the buffer in one go)
						segment.add(buffer.removeFirst());
					}

					// Sekundaerpuffer loeschen
					secondaryBuffer.clear();

					// Entscheidungsbaum stutzen
					decisionTreeRootNode = letzteTrennstelle
							.getSplit();
					if (decisionTreeRootNode != null)
						decisionTreeRootNode.setElternKnoten(null);

					// Segment ausgeben
					Iterator<String> segmentStrings = segment.iterator();
					while (segmentStrings.hasNext()) {
						this.getOutputPorts().get(OUTPUTID)
								.outputToAllCharPipes(segmentStrings.next());
						if (this.inputTokenDivider != null)
							this.getOutputPorts()
									.get(OUTPUTID)
									.outputToAllCharPipes(
											this.inputTokenDivider.toString());
					}
					this.getOutputPorts().get(OUTPUTID)
							.outputToAllCharPipes(this.outputTokenDivider);

					if (includeScoringInOutput)
						this.getOutputPorts()
								.get(OUTPUTID)
								.outputToAllCharPipes(
										letzteTrennstellenBewertung
												+ this.outputTokenDivider);
				}

			}

			/*
			 * If there is no more input available, output what's remaining in /
			 * both secondary and primary buffers and break. / TODO: Not the
			 * best way, certainly could be improved
			 */

			// Check whether last read input is invalid
			if (charCode == -1) {
				// Output secondary (backlog) buffer
				Iterator<String> secondaryBufferIterator = secondaryBuffer
						.iterator();
				while (secondaryBufferIterator.hasNext())
					this.getOutputPorts()
							.get(OUTPUTID)
							.outputToAllCharPipes(
									secondaryBufferIterator.next());

				// Output primary buffer
				Iterator<String> bufferIterator = buffer.iterator();
				while (bufferIterator.hasNext())
					this.getOutputPorts().get(OUTPUTID)
							.outputToAllCharPipes(bufferIterator.next());

				// End input read and segmentation loop
				break;
			}

		}
	}

	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();

		if (this.getProperties().containsKey(PROPERTYKEY_BUFFERSIZE))
			this.bufferSize = Integer.parseInt(this.getProperties()
					.getProperty(PROPERTYKEY_BUFFERSIZE));

		if (this.getProperties().containsKey(PROPERTYKEY_OUTPUTTOKENDIVIDER)){
			String outputTokenDividerString = this.getProperties().getProperty(
					PROPERTYKEY_OUTPUTTOKENDIVIDER);
			if (outputTokenDividerString != null)
				this.outputTokenDivider = StringUnescaper.unescape_perl_string(outputTokenDividerString); 
		}
		
		if (this.getProperties().containsKey(PROPERTYKEY_OUTPUTDOCUMENTDIVIDER)){
			String valueString = this.getProperties().getProperty(
					PROPERTYKEY_OUTPUTDOCUMENTDIVIDER);
			if (valueString != null)
				this.outputDocumentDivider = StringUnescaper.unescape_perl_string(valueString); 
		}
		
		if (this.getProperties().containsKey(PROPERTYKEY_INPUTDOCUMENTDIVIDER)){
			String valueString = this.getProperties().getProperty(
					PROPERTYKEY_INPUTDOCUMENTDIVIDER);
			if (valueString != null)
				this.inputDocumentDivider = StringUnescaper.unescape_perl_string(valueString); 
		}

		if (this.getProperties().containsKey(PROPERTYKEY_MINCOSTPERLAYER))
			this.minimalCostPerSymbolLayer = Double.parseDouble(this
					.getProperties().getProperty(PROPERTYKEY_MINCOSTPERLAYER));

		if (this.getProperties().containsKey(PROPERTYKEY_SCORINGDECREASEFACTOR))
			this.scoringDecreaseFactor = Double.parseDouble(this
					.getProperties().getProperty(
							PROPERTYKEY_SCORINGDECREASEFACTOR));

		if (this.getProperties().containsKey(PROPERTYKEY_INCLUDESCORING))
			this.includeScoringInOutput = Boolean.parseBoolean(this
					.getProperties().getProperty(PROPERTYKEY_INCLUDESCORING));

		if (this.getProperties().containsKey(PROPERTYKEY_INPUTTOKENDIVIDER)
				&& this.getProperties().getProperty(
						PROPERTYKEY_INPUTTOKENDIVIDER) != null
				&& !this.getProperties()
						.getProperty(PROPERTYKEY_INPUTTOKENDIVIDER).isEmpty())
			this.inputTokenDivider = Character.valueOf(StringUnescaper.unescape_perl_string(this.getProperties()
					.getProperty(PROPERTYKEY_INPUTTOKENDIVIDER)).charAt(0));

		super.applyProperties();
	}

}
