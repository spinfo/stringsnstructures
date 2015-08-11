package module;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import modularization.CharPipe;
import modularization.ModuleImpl;
import module.common.EntscheidungsAeffchen;
import module.common.SplitDecisionNode;
import module.common.SymbolBewerter;
import parallelization.CallbackReceiver;
import treeBuilder.Knoten;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ParadigmenErmittlerModul extends ModuleImpl {

	// Property keys
	public static final String PROPERTYKEY_INPUTFILE = "Suffix tree file";
	public static final String PROPERTYKEY_USEGZIP = "Suffix tree is GZIP encoded";
	public static final String PROPERTYKEY_ENCODING = "Encoding";
	public static final String PROPERTYKEY_BUFFERLENGTH = "Buffer length";
	public static final String PROPERTYKEY_DIVIDER = "Token divider";

	// Local variables
	private File file;
	private boolean useGzip = false;
	private String encoding = "UTF-8";
	private int pufferGroesse = 12;
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
		this.getPropertyDescriptions().put(PROPERTYKEY_BUFFERLENGTH,
				"Groesse des Eingabepuffers (sollte nicht die Tiefe des Suffixbaumes ueberschreiten!)");
		this.getPropertyDescriptions().put(PROPERTYKEY_DIVIDER,
				"Divider that is inserted in between the tokens on output");

		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME,
				"ParadigmSegmenterModule");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INPUTFILE,
				homedir + fs + "suffixtree.txt.gz");
		this.getPropertyDefaultValues().put(PROPERTYKEY_USEGZIP, "true");
		this.getPropertyDefaultValues().put(PROPERTYKEY_ENCODING, "UTF-8");
		this.getPropertyDefaultValues().put(PROPERTYKEY_BUFFERLENGTH, "10");
		this.getPropertyDefaultValues().put(PROPERTYKEY_DIVIDER, "\t");

		// Add module description
		this.setDescription("Reads contents from a suffix tree file (JSON-encoded) and based on that data marks paradigm borders in the streamed input. Outputs segmented input data. Can handle GZIP compressed suffix tree files.");
	}

	@Override
	public boolean process() throws Exception {
		
		/*
		 * Suffixbaum einlesen
		 */

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
		Knoten suffixbaumWurzelknoten = gson.fromJson(fileReader, Knoten.class);

		// Close relevant I/O instances
		fileReader.close();
		fileInputStream.close();

		/*
		 * Segmentierung des Eingabedatenstroms
		 */
		
		// Symbolbewerter instanziieren
		SymbolBewerter symbolBewerter = new SymbolBewerter();
		
		// Erstes Zeichen einlesen
		int zeichenCode = this.getInputCharPipe().getInput().read();
		
		// Entscheidungsbaum starten
		SplitDecisionNode entscheidungsbaumWurzelknoten = new SplitDecisionNode(0d);

		// EntscheidungsAeffchen initialisieren
		EntscheidungsAeffchen aeffchen = new EntscheidungsAeffchen(symbolBewerter, suffixbaumWurzelknoten);
		
		// Eingabepuffer initialisieren
		StringBuffer puffer = new StringBuffer();
		
		// Daten Zeichen fuer Zeichen einlesen
		while (zeichenCode != -1) {

			// Check for interrupt signal
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}

			// Zeichen einlesen
			Character symbol = Character.valueOf((char) zeichenCode);
			
			// Eingelesenes Zeichen an Puffer anfuegen
			puffer.append(symbol);
			
			// Puffergroesse pruefen
			if (puffer.length() == this.pufferGroesse){
				
				// Wenn der Eingabepuffer die erforderliche Groesse erreicht hat, wird er segmentiert
				SplitDecisionNode blattBesterWeg = aeffchen.konstruiereEntscheidungsbaum(puffer, entscheidungsbaumWurzelknoten);
				
				// TODO erstes Segment (erster Entscheidungsknoten, der trennt) ermitteln, Entscheidungsbaum stutzen, Puffer kuerzen
				
				// Zuletzt trennenden Entscheidungsbaumknoten ermitteln
				SplitDecisionNode letzteTrennstelle = blattBesterWeg;
				while (blattBesterWeg.getElternKnoten() != null){
					blattBesterWeg = blattBesterWeg.getElternKnoten();
					if (blattBesterWeg.getSplit().getValue()<blattBesterWeg.getJoin().getValue())
						letzteTrennstelle = blattBesterWeg;
				}
				
				// Tiefe der letzten Trennstelle ermitteln
				int tiefe = 0;
				SplitDecisionNode entscheidungsbaumKnoten = letzteTrennstelle;
				while (entscheidungsbaumKnoten.getElternKnoten() != null){
					entscheidungsbaumKnoten = entscheidungsbaumKnoten.getElternKnoten();
					tiefe ++;
				}
				
				// Segment ermitteln
				String segment = puffer.substring(0, tiefe);
				
				// Segment aus Puffer loeschen
				puffer.delete(0, tiefe);
				
				// Entscheidungsbaum stutzen
				letzteTrennstelle.setElternKnoten(null);
				entscheidungsbaumWurzelknoten = letzteTrennstelle;
				
				// Segment ausgeben
				this.outputToAllCharPipes(segment.concat(this.divider));
				
			}
			
			
			// Read next char
			zeichenCode = this.getInputCharPipe().getInput().read();
		}
		
		// Close relevant I/O instances
		this.closeAllOutputs();

		// Success
		return true;
	}

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
			
		if (this.getProperties().containsKey(PROPERTYKEY_BUFFERLENGTH))
			this.pufferGroesse = Integer.parseInt(this.getProperties().getProperty(PROPERTYKEY_BUFFERLENGTH));
		else if (this.getPropertyDefaultValues() != null && this.getPropertyDefaultValues().containsKey(PROPERTYKEY_BUFFERLENGTH))
			this.pufferGroesse = Integer.parseInt(this.getPropertyDefaultValues().get(PROPERTYKEY_BUFFERLENGTH));
		
		if (this.getProperties().containsKey(PROPERTYKEY_DIVIDER))
			this.divider = this.getProperties().getProperty(PROPERTYKEY_DIVIDER);
		else if (this.getPropertyDefaultValues() != null && this.getPropertyDefaultValues().containsKey(PROPERTYKEY_DIVIDER))
				this.divider = this.getPropertyDefaultValues().get(PROPERTYKEY_DIVIDER);
			
		super.applyProperties();
	}

}
