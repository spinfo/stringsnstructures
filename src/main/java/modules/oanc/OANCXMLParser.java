package modules.oanc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import modules.CharPipe;
import modules.ModuleImpl;

import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.parallelization.CallbackReceiver;

public class OANCXMLParser extends ModuleImpl {

	public static final String STARTSYMBOL = "^";
	public static final String TERMINIERSYMBOL="$";
	public static final String SATZGRENZENDATEISUFFIX="-s";
	public static final String ANNOTATIONSDATEISUFFIX="-hepple";
	public static final String WORTTRENNERREGEX = "[\\ \\n\\t\u200B]+";
	public static final Pattern WORTTRENNERREGEXMUSTER = Pattern.compile(WORTTRENNERREGEX);
	//public static final String ZEICHENSETZUNGSREGEX = "((?<=[\\[\\]\\(\\)\\?\\!\\-\\/\\.\\,\\;\\:\\\"\\'\\…])|(?=[\\[\\]\\(\\)\\?\\!\\-\\/\\.\\,\\;\\:\\\"\\'\\…]))"; // <String>.split() trennt hiermit Zeichen ab und behaelt sie als Elemente
	public static final String SATZZEICHENABTRENNERREGEX = "((?<=[\\[\\(\\]\\)]|[^(\\p{L}\\p{M}*+)])|(?=[\\[\\(\\]\\)]|[^(\\p{L}\\p{M}*+)]))";
	public static final Pattern SATZZEICHENABTRENNERREGEXMUSTER = Pattern.compile(SATZZEICHENABTRENNERREGEX);
	public static final String ZUENTFERNENDEZEICHENREGEX = "[^(\\p{L}\\p{M}*+)]*";
	public static final Pattern ZUENTFERNENDEZEICHENREGEXMUSTER = Pattern.compile(ZUENTFERNENDEZEICHENREGEX);
	// Annotationsbezeichner
	public static final String XML_PENNTAG_BEZEICHNER = "msd";
	public static final String XML_BEGRIFF_BEZEICHNER = "base";
	// Property keys
	public static final String PROPERTYKEY_ADDSTARTSYMBOL = "fuegeStartSymbolHinzu";
	public static final String PROPERTYKEY_ADDTERMINALSYMBOL = "fuegeTerminierSymbolHinzu";
	public static final String PROPERTYKEY_CONVERTTOLOWERCASE = "wandleInKleinbuchstaben";
	public static final String PROPERTYKEY_KEEPPUNCTUATION = "behaltePunktuation";
	public static final String PROPERTYKEY_OUTPUTANNOTATEDJSON = "outputAnnotatedJson";
	public static final String PROPERTYKEY_JSONOUTPUT_ONEOBJECTPERLINE = "oneJSONObjectPerLine";
	public static final String PROPERTYKEY_OUTPUTJSON = "output JSON";
	public static final String PROPERTYKEY_SPACE = "word divider";
	// local variables
	private File quellDatei;
	private File satzGrenzenXMLDatei;
	private File annotationsXMLDatei;
	private boolean fuegeStartSymbolHinzu;
	private boolean fuegeTerminierSymbolHinzu;
	private boolean wandleInKleinbuchstaben;
	private boolean behaltePunktuation;
	private boolean outputAnnotatedJson;
	private boolean oneJSONObjectPerLine;
	private boolean outputJson;
	private String wortTrennzeichen;
	
	public OANCXMLParser(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);
		
		// Define I/O
		this.getSupportedInputs().add(CharPipe.class);
		this.getSupportedOutputs().add(CharPipe.class);
		
		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_ADDSTARTSYMBOL, "Set to 'true' if '"+STARTSYMBOL+"' should be added as start symbol to each sentence");
		this.getPropertyDescriptions().put(PROPERTYKEY_ADDTERMINALSYMBOL, "Set to 'true' if '"+TERMINIERSYMBOL+"' should be added as end symbol to each sentence");
		this.getPropertyDescriptions().put(PROPERTYKEY_CONVERTTOLOWERCASE,"If set to 'true' the output will be all lowercase");
		this.getPropertyDescriptions().put(PROPERTYKEY_KEEPPUNCTUATION,"If set to 'true' punctuation will not be discarded");
		this.getPropertyDescriptions().put(PROPERTYKEY_OUTPUTANNOTATEDJSON,"If this and \'"+PROPERTYKEY_OUTPUTJSON+"\' is set to 'true' the output will be annotated JSON.");
		this.getPropertyDescriptions().put(PROPERTYKEY_JSONOUTPUT_ONEOBJECTPERLINE,"If this and \'"+PROPERTYKEY_OUTPUTJSON+"\' is set to 'true' the output will be one JSON object per line.");
		this.getPropertyDescriptions().put(PROPERTYKEY_OUTPUTJSON,"If set to 'true' the output will be JSON instead of plain text");
		this.getPropertyDescriptions().put(PROPERTYKEY_SPACE,"symbol or string to divide words from each other");
		
		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "OANC-XML-Parser");
		this.getPropertyDefaultValues().put(PROPERTYKEY_ADDSTARTSYMBOL, "true");
		this.getPropertyDefaultValues().put(PROPERTYKEY_ADDTERMINALSYMBOL, "true");
		this.getPropertyDefaultValues().put(PROPERTYKEY_CONVERTTOLOWERCASE, "true");
		this.getPropertyDefaultValues().put(PROPERTYKEY_KEEPPUNCTUATION, "true");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OUTPUTANNOTATEDJSON, "true");
		this.getPropertyDefaultValues().put(PROPERTYKEY_JSONOUTPUT_ONEOBJECTPERLINE, "true");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OUTPUTJSON, "true");
		this.getPropertyDefaultValues().put(PROPERTYKEY_SPACE, " ");
	}
	
	/**
	 * Guesses the location of the sentence borders file in relation to the
	 * source file (if existent) and sets the internal variable accordingly.
	 * @return true if file is found & readable
	 */
	private boolean guessSentenceBordersFile() {
		if (quellDatei != null) {
			this.satzGrenzenXMLDatei = new File(quellDatei.getPath().substring(0, quellDatei.getPath().lastIndexOf('.'))+SATZGRENZENDATEISUFFIX+".xml");
			if (this.satzGrenzenXMLDatei.canRead())
				return true;
		}
		return false;
	}

	/**
	 * Guesses the location of the annotation file in relation to the
	 * source file (if existent) and sets the internal variable accordingly.
	 * @return true if file is found & readable
	 */
	private boolean guessAnnotationsFile() {
		if (quellDatei != null) {
			this.annotationsXMLDatei = new File(quellDatei.getPath().substring(0, quellDatei.getPath().lastIndexOf('.'))+ANNOTATIONSDATEISUFFIX+".xml");
			if (this.annotationsXMLDatei.canRead())
				return true;
		}
		return false;
	}

	/**
	 * Parst die Quell- und Satzgrenzendatei und gibt eine Liste von (Roh)Saetzen zurueck
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private List<String> parseQuellDatei() throws SAXException, IOException, ParserConfigurationException{
		
		// Zugriff auf Dateien pruefen
		if (!this.quellDatei.canRead()){
			throw new IOException("Kann Quelldatei nicht lesen: "+this.quellDatei.getAbsolutePath());
		}
		if (!this.satzGrenzenXMLDatei.canRead()){
			throw new IOException("Kann Satzgrenzendatei nicht lesen: "+this.satzGrenzenXMLDatei.getAbsolutePath());
		}
		
		// Liste fuer Ergebnis
		ArrayList<String> ergebnisListe = new ArrayList<String>();
		
		// XML-Satzgrenzendatei parsen
		SAXParserFactory parserFactor = SAXParserFactory.newInstance();
	    SAXParser parser = parserFactor.newSAXParser();
	    OANCSatzgrenzenXMLHandler handler = new OANCSatzgrenzenXMLHandler();
	    InputStream satzgrenzenInputStream = new FileInputStream(satzGrenzenXMLDatei);
	    parser.parse(satzgrenzenInputStream, handler);

	    // Quelldatei oeffnen
	    FileReader datei = new FileReader(this.quellDatei);
	    
	    // Markierung fuer Leselposition in der Quelldatei
    	int position = 0;
	    
	    // Liste der Satzgrenzen durchlaufen
	    Iterator<OANCXMLSatzgrenze> satzgrenzen = handler.getSatzgrenzen().iterator();
	    while(satzgrenzen.hasNext()){
	    	
	    	// Naechste Satzgrenze ermitteln
	    	OANCXMLSatzgrenze satzgrenze = satzgrenzen.next();
	    	
	    	// Laenge des zu lesenden Satzes ermitteln
	    	int satzlaenge = satzgrenze.getBis() - satzgrenze.getVon();
	    	
	    	// Zeichenarray mit entsprechender Laenge erstellen
	    	char[] satzZeichenArray = new char[satzlaenge];
	    	
	    	//System.out.println("Lese Zeichen "+satzgrenze.getVon()+" bis "+satzgrenze.getBis() +", Laenge:"+satzlaenge);
	    	
	    	// Ggf. in der Quelldatei zum naechsten Satzanfang springen
	    	if (satzgrenze.getVon()>position){
	    		datei.skip(satzgrenze.getVon()-position);
	    		position = satzgrenze.getVon();
	    	}
	    	
	    	// Zeichen aus Quelldatei in ZeichenArray einlesen
	    	if (datei.ready() && position<satzgrenze.getBis()){
	    		datei.read(satzZeichenArray, 0, satzlaenge);
	    		position = satzgrenze.getBis();
	    	}
	    	
	    	
	    	// Zeichenarray in String umwandeln und in Ergebnisliste speichern
	    	ergebnisListe.add(String.copyValueOf(satzZeichenArray).intern());
	    	
	    }
	    
	    
	    // Quelldatei schliessen
		datei.close();
	    
		// Ergebnisliste zurueckgeben
		return ergebnisListe;
	}
	
	/**
	 * Bereinigt und segmentiert den uebergebenen Satz. Entfernt Zeilenumbrueche, Tabulatoren, Leerzeichen, Punktuation.
	 * Fuegt ggf. am Ende das Terminiersymbol ein.
	 * @param rohsatz
	 * @return Wortliste
	 */
	private List<String> bereinigeUndSegmentiereSatz(String rohsatz, boolean fuegeStartSymbolEin, boolean fuegeTerminierSymbolEin, boolean wandleZuKleinbuchstaben, boolean behalteSatzzeichenAlsToken){
		List<String> ergebnisListe = new ArrayList<String>();
		
		// Satz segmentieren
		String[] segmente = OANCXMLParser.WORTTRENNERREGEXMUSTER.split(rohsatz);
		
		// Ggf. Startsymbol einfuegen
		if (fuegeStartSymbolEin){
			ergebnisListe.add(OANCXMLParser.STARTSYMBOL);
		}
		
		// Segmente durchlaufen
		for (int i=0; i<segmente.length; i++){
			
			// Wort, ggf. mit Zeichensetzung, daher als Array
			String[] segment;
			
			// Satzzeigen als Token behalten oder entfernen?
			if (behalteSatzzeichenAlsToken){
				// Zeichensetzung trennen
				segment = SATZZEICHENABTRENNERREGEXMUSTER.split(segmente[i]);
			} else {
				// Segment bereinigen und in Ergebnis speichern
				segment = new String[]{segmente[i].replaceAll(ZUENTFERNENDEZEICHENREGEX, "").trim()};
			}
			
			// Schleife ueber Token des Segments
			for (int j=0; j<segment.length; j++){
				// Ggf. zu Kleinbuchstaben wandeln
				if (wandleZuKleinbuchstaben){
					segment[j] = segment[j].toLowerCase().intern();
				}
				if (!segment[j].isEmpty())
					ergebnisListe.add(segment[j].intern());
			}
			
			
		}
		
		// Ggf. Terminiersymbol einfuegen
		if (fuegeTerminierSymbolEin){
			ergebnisListe.add(OANCXMLParser.TERMINIERSYMBOL);
		}
		
		// Ergebnisliste zurueckgeben
		return ergebnisListe;
	}
	
	/**
	 * Parst die Quell-, Annotations- und Satzgrenzendatei und gibt eine Liste von Saetzen mit annotierten Worten zurueck.
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	/*private List<List<WortAnnotationTupel>> parseQuellDateiMitAnnotationen() throws SAXException, IOException, ParserConfigurationException {
		return this.parseQuellDateiMitAnnotationen(false);
	}*/
	
	/**
	 * Parst die Quell-, Annotations- und Satzgrenzendatei und gibt eine Liste von Saetzen mit annotierten Worten zurueck.
	 * @param wandleZuKleinbuchstaben Zeigt an, ob die eingelesenen Worte in Kleinbuchstaben gewandelt werden sollen
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	private List<List<WortAnnotationTupel>> parseQuellDateiMitAnnotationen(boolean wandleZuKleinbuchstaben) throws SAXException, IOException, ParserConfigurationException{
		
		// Zugriff auf Dateien pruefen
		if (!this.quellDatei.canRead()){
			throw new IOException("Kann Quelldatei nicht lesen: "+this.quellDatei.getAbsolutePath());
		}
		if (!this.satzGrenzenXMLDatei.canRead()){
			throw new IOException("Kann Satzgrenzendatei nicht lesen: "+this.satzGrenzenXMLDatei.getAbsolutePath());
		}
		
		// Liste fuer Ergebnis
		ArrayList<List<WortAnnotationTupel>> ergebnisListe = new ArrayList<List<WortAnnotationTupel>>();
		
		// XML-Satzgrenzendatei parsen
		SAXParserFactory satzgrenzenParserFactory = SAXParserFactory.newInstance();
	    SAXParser satzgrenzenParser = satzgrenzenParserFactory.newSAXParser();
	    OANCSatzgrenzenXMLHandler satzgrenzenHandler = new OANCSatzgrenzenXMLHandler();
	    InputStream satzgrenzenInputStream = new FileInputStream(satzGrenzenXMLDatei);
	    try {
	    	satzgrenzenParser.parse(satzgrenzenInputStream, satzgrenzenHandler);
	    } catch (Exception e){
	    	Logger.getLogger(this.getClass().getSimpleName()).warning("Fehler beim Parsen der Satzgrenzendatei \'"+this.satzGrenzenXMLDatei.getAbsolutePath()+"\': "+e.getMessage());
	    	return ergebnisListe;
	    }
	    
		
		// XML-Annotationsdatei parsen
		SAXParserFactory annotationsParserFactory = SAXParserFactory.newInstance();
	    SAXParser annotationsParser = annotationsParserFactory.newSAXParser();
	    OANCAnnotationsXMLHandler annotationsHandler = new OANCAnnotationsXMLHandler();
	    InputStream annotationsInputStream = new FileInputStream(annotationsXMLDatei);
	    try {
		    annotationsParser.parse(annotationsInputStream, annotationsHandler);
	    } catch (Exception e){
	    	Logger.getLogger(this.getClass().getSimpleName()).warning("Fehler beim Parsen der Annotationsdatei \'"+this.annotationsXMLDatei.getAbsolutePath()+"\': "+e.getMessage());
	    	return ergebnisListe;
	    }
	    
	    // Liste der geparsten Wortannotationen (mit deren Position im Text!)
	    List<OANCXMLAnnotation> annotationsListe = annotationsHandler.getAnnotationen();

	    // Quelldatei oeffnen
	    FileReader datei = new FileReader(this.quellDatei);
	    
	    // Markierung fuer Leselposition in der Quelldatei
    	int position = 0;

    	// Iterator fuer Wortannotationen
    	Iterator<OANCXMLAnnotation> annotationen = annotationsListe.iterator();
    	
    	// Falls keine Wortannotation vorhanden ist, wird die leere Ergebnisliste zurueckgegeben
    	if (!annotationen.hasNext()){
    		// Quelldatei schliessen
    		datei.close();
    		// Leere Liste zurueckgeben
    		return ergebnisListe;
    	}
    	
    	// Erste Wortannotation ermitteln
    	OANCXMLAnnotation annotation = annotationen.next();
    	
	    // Liste der Satzgrenzen durchlaufen
	    Iterator<OANCXMLSatzgrenze> satzgrenzen = satzgrenzenHandler.getSatzgrenzen().iterator();
	    while(satzgrenzen.hasNext()){
	    	
	    	// Liste fuer neuen Satz
	    	List<WortAnnotationTupel> satz = new ArrayList<WortAnnotationTupel>();
	    	
	    	// Naechste Satzgrenze ermitteln
	    	OANCXMLSatzgrenze satzgrenze = satzgrenzen.next();
	    	
	    	while (annotation != null && annotation.getVon() < satzgrenze.getBis()){
	    		
	    		// Laenge des zu lesenden Wortes ermitteln
		    	int wortlaenge = annotation.getBis() - annotation.getVon();
		    	
		    	// Zeichenarray mit entsprechender Laenge erstellen
		    	char[] wortZeichenArray = new char[wortlaenge];
	    		
	    		// Ggf. in der Quelldatei zum Wortanfang springen
		    	if (annotation.getVon()>position){
		    		datei.skip(annotation.getVon()-position);
		    		position = annotation.getVon();
		    	}
	    		
		    	// Zeichen aus Quelldatei in ZeichenArray einlesen
		    	if (datei.ready() && position<annotation.getBis()){
		    		datei.read(wortZeichenArray, 0, wortlaenge);
		    		position = annotation.getBis();
		    	}
		    	
		    	// Zeichenkette umwandeln und internalisieren (letzteres wichtig fuer Minimierung der Speicherauslastung)
		    	String wortString;
		    	if (wandleZuKleinbuchstaben){
		    		wortString = String.copyValueOf(wortZeichenArray).toLowerCase().intern();
		    	} else {
		    		wortString = String.copyValueOf(wortZeichenArray).intern();
		    	}
	    		
		    	// Annotiertes Wort zum Satz hinzufuegen
	    		satz.add(new WortAnnotationTupel(wortString,annotation.getAnnotationswerte().get(XML_PENNTAG_BEZEICHNER),annotation.getAnnotationswerte().get(XML_BEGRIFF_BEZEICHNER)));
	    		
	    		// Naechstes Wort ermitteln
	    		if (annotationen.hasNext()){
	    			annotation = annotationen.next();
	    		} else {
	    			annotation = null;
	    		}
	    	}
	    	
	    	// Abgeschlossenen Satz in Ergebnisliste speichern
	    	ergebnisListe.add(satz);
	    	
	    }
	    
	    // Quelldatei schliessen
		datei.close();
	    
		// Ergebnisliste zurueckgeben
		return ergebnisListe;
	}

	@Override
	public boolean process() throws Exception {
		
		// Instantiate JSON converter
		Gson gson;
		
		if (this.oneJSONObjectPerLine)
			gson = new Gson();
		else
			gson = new GsonBuilder().setPrettyPrinting().create();
		
		// Read list of files from input
		File[] inputFileList;
		try {
			inputFileList = gson.fromJson(this.getInputCharPipe().getInput(), new File[0].getClass());
		} catch (Exception e) {
			throw new Exception("Error parsing the input -- it does not seem to be the expected list of files.", e);
		}
				
		for (int i=0; i<inputFileList.length; i++){
			
			// Auf Unterbrechersignal pruefen
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			// Determine the next input file
			File inputFile = inputFileList[i];
			
			// Write log message
			Logger.getLogger(this.getClass().getSimpleName()).log(Level.FINEST,"Parser is processing "+inputFile.getPath());
			
			// Aktuelle Korpusdatei als Quelle fuer Parser setzen
			this.quellDatei = inputFile;

			// Satzgrenzendatei auf ermitteln
			boolean sentenceBordersArePresent = this.guessSentenceBordersFile();
			
			// If the sentence borders are missing, throw an exception
			if (!sentenceBordersArePresent)
				throw new Exception("I'm very sorry indeed, but I must stop processing for I could not find the file containing the sentence borders.");
			else
				Logger.getLogger(this.getClass().getSimpleName()).log(Level.FINEST,"Found sentence borders in "+this.satzGrenzenXMLDatei.getPath());
			
			// If the output format is set to annotated JSON, the method used for parsing differs
			if (this.outputAnnotatedJson){
				
				// The output format is annotated JSON; first we need to get the annotations
				boolean annotationsArePresent = this.guessAnnotationsFile();
				
				// If the annotations are missing, throw an exception
				if (!annotationsArePresent)
					throw new Exception("I'm very sorry indeed, but I must stop processing for I could not find the file containing the annotation data.");
				else
					Logger.getLogger(this.getClass().getSimpleName()).log(Level.FINEST,"Found annotations in "+this.annotationsXMLDatei.getPath());
				
				// Parse the source text with annotations
				List<List<WortAnnotationTupel>> annotatedTupelList = this.parseQuellDateiMitAnnotationen(this.wandleInKleinbuchstaben);
				
				// Convert tupel list to JSON
				String annotatedTupelListJson = gson.toJson(annotatedTupelList);
				
				// Output the result
				this.outputToAllCharPipes(annotatedTupelListJson+"\n");
				
			} else {
				// The output format is plain sentences, cleaned up a bit
				
				// Datei parsen und Rohsaetze ermitteln
				List<String> rohsatzListe = this.parseQuellDatei();

				// Liste der Rohsaetze durchlaufen
				Iterator<String> rohsaetze = rohsatzListe.iterator();
				while (rohsaetze.hasNext()) {
					
					// Clean up raw sentence
					List<String> bereinigterSatz = this.bereinigeUndSegmentiereSatz(
							rohsaetze.next(), fuegeStartSymbolHinzu,
							fuegeTerminierSymbolHinzu, wandleInKleinbuchstaben,
							behaltePunktuation);

					// Variable for the data that gets sent to output
					String outputString;
					
					// Check whether we need to convert the output into JSON
					if (this.outputJson){

						// Convert to JSON
						outputString = gson.toJson(bereinigterSatz);
						
					} else {
						
						// Create a stringbuilder for better performance
						StringBuilder stringBuilder = new StringBuilder();
						
						// Loop over words of the parsed sentence to construct a single output string
						Iterator<String> worte = bereinigterSatz.iterator();
						while(worte.hasNext()){
							stringBuilder.append(worte.next());
							if (worte.hasNext())
								stringBuilder.append(this.wortTrennzeichen);
						}
						
						// Set the output variable
						outputString = stringBuilder.toString();
						
					}

					// Output the result
					this.outputToAllCharPipes(outputString);
				}
			}
		}

		// Close outputs
		this.closeAllOutputWriters();
		
		return true;
	}

	/* (non-Javadoc)
	 * @see modularization.ModuleImpl#applyProperties()
	 */
	@Override
	public void applyProperties() throws Exception {
		if (this.getProperties().containsKey(PROPERTYKEY_ADDSTARTSYMBOL))
			this.fuegeStartSymbolHinzu = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_ADDSTARTSYMBOL));
		if (this.getProperties().containsKey(PROPERTYKEY_ADDTERMINALSYMBOL))
			this.fuegeTerminierSymbolHinzu = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_ADDTERMINALSYMBOL));
		if (this.getProperties().containsKey(PROPERTYKEY_CONVERTTOLOWERCASE))
			this.wandleInKleinbuchstaben = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_CONVERTTOLOWERCASE));
		if (this.getProperties().containsKey(PROPERTYKEY_KEEPPUNCTUATION))
			this.behaltePunktuation = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_KEEPPUNCTUATION));
		if (this.getProperties().containsKey(PROPERTYKEY_OUTPUTANNOTATEDJSON))
			this.outputAnnotatedJson = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_OUTPUTANNOTATEDJSON));
		if (this.getProperties().containsKey(PROPERTYKEY_JSONOUTPUT_ONEOBJECTPERLINE))
			this.oneJSONObjectPerLine = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_JSONOUTPUT_ONEOBJECTPERLINE));
		if (this.getProperties().containsKey(PROPERTYKEY_OUTPUTJSON))
			this.outputJson = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_OUTPUTJSON));
		if (this.getProperties().containsKey(PROPERTYKEY_SPACE))
			this.wortTrennzeichen = this.getProperties().getProperty(PROPERTYKEY_SPACE);
			
		super.applyProperties();
	}

}
