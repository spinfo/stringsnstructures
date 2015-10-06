package modules.treeBuilder;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.Logger;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.Pipe;
import modules.oanc.WortAnnotationTupel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import common.parallelization.CallbackReceiver;

public class TreeBuilder extends ModuleImpl {
	
	// Property keys
	public static final String PROPERTYKEY_BUILDTRIE = "Build trie instead of tree";
	public static final String PROPERTYKEY_MAXLENGTH = "Maximum length of branches";
	public static final String PROPERTYKEY_REVERSE = "Reverse order";

	// Local variables
	private final String INPUTID = "input";
	private final String OUTPUTID = "output";
	private boolean baueTrie; // Zeigt an, ob ein Trie oder ein Tree gebaut werden soll
	private int maxLaenge; // Maximale Laenge des zu bauenden Baums
	private boolean umgekehrt; // Zeigt an, ob der Baum umgekehrt (als Praefix-Baum) konstruiert werden soll

	public TreeBuilder(CallbackReceiver callbackReceiver, Properties properties)
			throws Exception {
		super(callbackReceiver, properties);

		// Define I/O
		InputPort inputPort = new InputPort(INPUTID, "JSON-encoded OANC data.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(OUTPUTID, "JSON-encoded suffix trie.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
		
		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_BUILDTRIE,"Set to true if you want to construct a trie instead of a tree.");
		this.getPropertyDescriptions().put(PROPERTYKEY_MAXLENGTH,"Define the maximum length of any branch of the tree.");
		this.getPropertyDescriptions().put(PROPERTYKEY_REVERSE,"Reverse the order, construct a prefix-tree.");
		
		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "Treebuilder");
		this.getPropertyDefaultValues().put(PROPERTYKEY_BUILDTRIE, "true");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MAXLENGTH, "-1");
		this.getPropertyDefaultValues().put(PROPERTYKEY_REVERSE, "false");
		
		// Add module description
		this.setDescription("Builds a suffixtree from the JSON output of OANCXMLParser (expects annotated JSON, one object per line). Can be configured to build other forms.");
	}

	@Override
	public boolean process() throws Exception {
		
		// Wurzelknoten des zu erstellenden Baumes erstellen
		Knoten wurzelKnoten = new Knoten();
		if (this.umgekehrt)
			wurzelKnoten.setName("$");
		else
			wurzelKnoten.setName("^");
		
		// JSON-Parser instanziieren
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		// Eingabe puffern
		BufferedReader eingabe = new BufferedReader(this.getInputPorts().get(INPUTID).getInputReader());
		
		// Eingabe einlesen
		String jsonObjekt = eingabe.readLine();
		while (jsonObjekt != null){

			// Check for interrupt signal
			if (Thread.interrupted()) {
				this.closeAllOutputs();
				throw new InterruptedException("Thread has been interrupted.");
			}
			
			// JSON-Objekt parsen
			WortAnnotationTupel[][] saetze = gson.fromJson(jsonObjekt, new WortAnnotationTupel[0][0].getClass());
			
			// Saetze durchlaufen
			for (int i=0; i<saetze.length; i++){
			
				WortAnnotationTupel[] satz = saetze[i];
				
				// Satz in String-Array fassen
				String[] satzArray = new String[satz.length];
				for (int j=0; j<satz.length; j++){
					satzArray[j] = satz[j].getWort();
				}
				
				// Mit dem ermittelten Satz wird der Baum weiter konstruiert
				if (this.baueTrie)
					this.baueTrie(satzArray, wurzelKnoten, this.umgekehrt, this.maxLaenge);
				else
					this.baueBaum(satzArray, wurzelKnoten, this.umgekehrt, this.maxLaenge);
				
			}
			
			// Naechstes Objekt einlesen
			jsonObjekt = eingabe.readLine();
			
		}
		
		// Letztlich wird der Wurzelknoten (und damit der gesamte erstellte Baum) in JSON umgewandelt und ausgegeben
		Iterator<Pipe> charPipes = this.getOutputPorts().get(OUTPUTID).getPipes(CharPipe.class).iterator();
		while (charPipes.hasNext()){
			gson.toJson(wurzelKnoten, ((CharPipe)charPipes.next()).getOutput());
		}
		
		// Ausgabekanaele schliessen
		this.closeAllOutputs();
		
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see modularization.ModuleImpl#applyProperties()
	 */
	@Override
	public void applyProperties() throws Exception {
		super.setDefaultsIfMissing();
		if (this.getProperties().containsKey(PROPERTYKEY_BUILDTRIE))
			this.baueTrie = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_BUILDTRIE));
		if (this.getProperties().containsKey(PROPERTYKEY_MAXLENGTH))
			this.maxLaenge = Integer.parseInt(this.getProperties().getProperty(PROPERTYKEY_MAXLENGTH));
		if (this.getProperties().containsKey(PROPERTYKEY_REVERSE))
			this.umgekehrt = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_REVERSE));
		super.applyProperties();
	}
	
	/**
     * Erzeugt einen Suffixbaum im uebergebenen Knoten anhand der uebergebenen Token.
     * Inkrementiert die Zaehlvariable eines jeden Knotens um eins fuer jede "Beruehrung".
     * @param token String-Array mit Token (Woerter, Buchstaben, Symbole, ... egal was)
     * @param rootnode Startknoten Wurzelknoten des zu konstruierenden Baumes
     * @param umgekehrt Zeigt an, ob der Baum umgekehrt erstellt werden soll (quasi als "Praefixbaum")
     * @return Die Anzahl der neu erstellten Knoten
     */
	public int baueBaum(String[] token, Knoten rootnode, boolean umgekehrt) {
		return this.baueBaum(token, rootnode, umgekehrt, -1);
	}
	
	/**
     * Erzeugt einen Suffixbaum im uebergebenen Knoten anhand der uebergebenen Token.
     * Inkrementiert die Zaehlvariable eines jeden Knotens um eins fuer jede "Beruehrung".
     * @param token String-Array mit Token (Woerter, Buchstaben, Symbole, ... egal was)
     * @param rootnode Startknoten Wurzelknoten des zu konstruierenden Baumes
     * @param umgekehrt Zeigt an, ob der Baum umgekehrt erstellt werden soll (quasi als "Praefixbaum")
     * @param maxLaenge Die maximale Tiefe des zu erstellenden Baumes, inklusive des Wurzelknotens (<0 = ignorieren)
     * @return Die Anzahl der neu erstellten Knoten
     */
	public int baueBaum(String[] token, Knoten rootnode, boolean umgekehrt, int maxLaenge) {
		
		// Zaehler fuer eingefuegte Knoten
		int knotenEingefuegt = 0;
		
		// Tokenarray durchlaufen
		for (int i=0; i<token.length; i++){
			
			// Bereich fuer naechsten Suffix-Trie ermitteln
			int von = i;
			int bis = token.length;
			if (umgekehrt){
				von = 0;
				bis = token.length -i;
			}
			
			// Naechsten Trie in Baum einfuegen
			knotenEingefuegt += this.baueTrie(Arrays.copyOfRange(token, von, bis), rootnode, umgekehrt, maxLaenge);
			
		}
		
		// Anzahl der eingefuegten Knoten zurueckgeben
		return knotenEingefuegt;
		
	}
	
    /**
     * Erzeugt einen Suffixtrie im uebergebenen Knoten anhand der uebergebenen Token.
     * Inkrementiert die Zaehlvariable eines jeden Knotens um eins fuer jede "Beruehrung".
     * @param token String-Array mit Token (Woerter, Buchstaben, Symbole, ... egal was)
     * @param rootnode Startknoten Wurzelknoten des zu konstruierenden Baumes
     * @param umgekehrt Zeigt an, ob der Baum umgekehrt erstellt werden soll (quasi als "Praefixbaum")
     * @return Die Anzahl der neu erstellten Knoten
     */
    public int baueTrie(String[] token, Knoten rootnode, boolean umgekehrt) {
    	return this.baueTrie(token, rootnode, umgekehrt, -1);
    }
	
    /**
     * Erzeugt einen Suffixtrie im uebergebenen Knoten anhand der uebergebenen Token.
     * Inkrementiert die Zaehlvariable eines jeden Knotens um eins fuer jede "Beruehrung".
     * @param token String-Array mit Token (Woerter, Buchstaben, Symbole, ... egal was)
     * @param rootnode Startknoten Wurzelknoten des zu konstruierenden Baumes
     * @param umgekehrt Zeigt an, ob der Baum umgekehrt erstellt werden soll (quasi als "Praefixbaum")
     * @param maxLaenge Die maximale Anzahl an Token, die dem Trie hinzugefuegt werden soll (<0 = ignorieren).
     * @return Die Anzahl der neu erstellten Knoten
     */
    public int baueTrie(String[] token, Knoten rootnode, boolean umgekehrt, int maxLaenge) {

    	// Variable zum Mitzaehlen der erstellten Knoten
		int knotenerstellt = 0;
		
		// "Beruehrung" des Knotens mitzaehlen
		rootnode.setZaehler(rootnode.getZaehler() + 1);

		// Wenn keine Token mehr vorhanden sind bzw. die maximal hinzuzufuegende Anzahl an Token ueberschritten wird, wird abgebrochen
		if (token == null || token.length == 0 || maxLaenge==0) {
			return knotenerstellt;
		}

		// Index des als naechstes zu vergleichenden Tokens ermitteln
		int vergleichsTokenIndex = 0;
		if (umgekehrt) {
			vergleichsTokenIndex = token.length - 1;
		}

		// Variable fuer Kindknoten definieren
		Knoten kindKnoten;

		// Ggf. neuen Knoten erstellen
		if (!rootnode.getKinder().containsKey(token[vergleichsTokenIndex])) {
			// passender Knoten NICHT vorhanden - neuen erstellen
			kindKnoten = new Knoten();
			
			// Zaehler fuer erstellte Knoten inkrementieren
			knotenerstellt++;
			
			// Den Namen der Kante in der Node speichern .. um spaeter bei Bedarf die Knoten geordnet ausgeben zu koennen (debug)
			kindKnoten.setName(token[vergleichsTokenIndex]);
			
			// Kind dem Elternknoten anfuegen
			rootnode.getKinder().put(token[vergleichsTokenIndex], kindKnoten);

		} else {
			// passender Knoten vorhanden
			kindKnoten = rootnode.getKinder().get(token[vergleichsTokenIndex]);
		}

		// Pruefen, ob der Baum "umgekehrt" erstellt werden soll
		if (umgekehrt) {
			// Rekursiver Aufruf mit Token 0 bis n-1
			knotenerstellt += this.baueTrie(
					Arrays.copyOfRange(token, 0, token.length - 1),
					kindKnoten, umgekehrt, maxLaenge-1);
		} else {
			// Rekursiver Aufruf mit Token 1 bis n
			knotenerstellt += this.baueTrie(
					Arrays.copyOfRange(token, 1, token.length),
					kindKnoten, umgekehrt, maxLaenge-1);
		}

		// Anzahl der neu erstellten Knoten zurueckgeben
		return knotenerstellt;

    }

	/**
	 * Gibt eine Kopie des uebergebenen Baumes zurueck, aber ohne die Knoten, die nicht als Treffer
	 * markiert waren.
	 * @param knoten Wurzelknoten des zu kopierenden Baumes
	 * @param ignoriereErstenKnoten Gibt vor, ob der Wurzelknoten ignoriert werden soll (nuetzlich fuer Kontextvergleichsbaeume)
	 * @return Wurzel des neu erstellten Baumes
	 */
	public Knoten entferneNichtTrefferKnoten(Knoten knoten, boolean ignoriereErstenKnoten) {

		// Bearbeitung ggf. Abbrechen
		if (knoten == null || !(knoten.isMatch() || ignoriereErstenKnoten)) {
			return null;
		}

		// Neuen Knoten erstellen und Werte uebertragen
		Knoten neuerKnoten = new Knoten();
		neuerKnoten.setName(knoten.getName());
		neuerKnoten.setZaehler(knoten.getZaehler());
		neuerKnoten.setMatch(true);

		// Aufruf fuer Kindknoten rekursiv wiederholen
		Iterator<String> kinder = knoten.getKinder().keySet().iterator();
		while (kinder.hasNext()) {
			String kindName = kinder.next();
			Knoten kind = entferneNichtTrefferKnoten(knoten.getKinder().get(
					kindName),false);
			if (kind != null)
				neuerKnoten.getKinder().put(kindName, kind);
		}

		// Neuen Knoten zurueckgeben
		return neuerKnoten;

	}

	/**
	 * Fuegt alle Elemente und Unterelemente des uebergebenen Baumes dem uebergebenen TreeSet hinzu. 
	 * @param wurzel
	 * @param treeSet
	 */
	public void fuegeNodesInTreeSetEin(Knoten wurzel, TreeSet<Knoten> treeSet) {
		Iterator<String> kinder = wurzel.getKinder().keySet().iterator();
		while (kinder.hasNext()) {
			fuegeNodesInTreeSetEin(wurzel.getKinder().get(kinder.next()),
					treeSet);
		}
		treeSet.add(wurzel);
	}
	
	/**
	 * Filtert eine Satzliste (Liste einer Liste von Strings) anhand eines Wortfilters und erstellt im uebergebenen Knoten einen Baum.
	 * @param wortTyp
	 * @param satzListe
	 * @param wf
	 * @param wurzel
	 * @param praefixwurzel
	 * @param vergleichAufVergleichswortzweigBeschraenken
	 * @param praefixBaumErstellen
	 * @return Anzahl der in den Saetzen gefundenen Wortvorkommen. 
	 */
	public int baueTrieAusSaetzenMitWorttyp(String wortTyp,
			List<List<String>> satzListe, WortFilter wf, Knoten wurzel, Knoten praefixwurzel,
			boolean vergleichAufVergleichswortzweigBeschraenken,
			boolean praefixBaumErstellen, boolean ausfuehrlicheFortschrittsMeldungen) {
		// Saetze aus Korpus durchlaufen, Treffer mitzaehlen (fuer Anzeige)
		int saetzeDurchlaufen = 0;
		int saetzeGefunden = 0;
		int vorkommenGefunden = 0;
		Iterator<List<String>> saetze = satzListe.iterator();
		while (saetze.hasNext()) {

			// Naechsten Satz ermitteln
			List<String> satz = saetze.next();

			// Pruefen, ob WortFilter greift
			if (wf.hatWort(satz)) {

				// Ggf. nur Trie ab dem Vergleichswort bauen
				if (vergleichAufVergleichswortzweigBeschraenken) {

					// Ermitteln, an welchen Stellen im Satz das Vergleichswort
					// vorkommt
					Integer[] vergleichsWortIndices = wf.getWortIndices(satz);
					
					// Anzahl der gefundenen Vorkommen mitzaehlen
					vorkommenGefunden += vergleichsWortIndices.length;

					// Indices durchlaufen
					for (int j = 0; j < vergleichsWortIndices.length; j++) {
						// Satz in Array konvertieren
						String[] satzArray = satz.toArray(new String[satz
								.size()]);
						// Satz in den Baum/Graphen hineinbauen
						this.baueTrie(Arrays.copyOfRange(satzArray,
								vergleichsWortIndices[j], satzArray.length),
								wurzel, false);
						// Ggf. Satz ebenfalls in den Praefixbaum/-graphen
						// hineinbauen
						if (praefixwurzel != null && praefixBaumErstellen) {
							this.baueTrie(Arrays.copyOfRange(satzArray, 0,
									vergleichsWortIndices[j] + 1), praefixwurzel, true);
						}
					}

				} else {
					// Satz in den Baum/Graphen hineinbauen
					this.baueBaum(satz.toArray(new String[satz.size()]),
							wurzel, false);
				}

				// Treffer mitzaehlen
				saetzeGefunden++;
			}

			// Durchlaufenen Satz mitzaehlen
			saetzeDurchlaufen++;

			// ggf. Meldung ausgeben
			if (ausfuehrlicheFortschrittsMeldungen){
				double prozentFertig = Math
						.ceil(((double) saetzeDurchlaufen / (double) satzListe
								.size()) * 100);
				if ((satzListe.size() / 20) != 0
						&& saetzeDurchlaufen % (satzListe.size() / 20) == 0) {
					Logger.getLogger(
							TreeBuilder.class.getCanonicalName())
							.info("Ermittle Saetze, die Wort '" + wortTyp
									+ "' beinhalten: " + saetzeDurchlaufen + "/"
									+ satzListe.size() + " (" + saetzeGefunden
									+ ") " + prozentFertig + "%");
				}
			}
		}
		
		// Anzahl der gefundenen Wortvorkommen zurueckgeben
		return vorkommenGefunden;
	}
	
	/**
	 * Filtert eine Satzliste (Liste einer Liste von Strings) anhand eines Wortfilters und erstellt im uebergebenen Knoten einen Baum.
	 * @param wortTyp
	 * @param satzListe
	 * @param wf
	 * @param wurzel
	 * @param praefixwurzel
	 * @param praefixBaumErstellen
	 * @param maxLaenge Die maximale Tiefe des zu erstellenden Baumes, inklusive des Wurzelknotens (<0 = ignorieren)
	 * @param vergleichsworteNichtInBaumMitAufnehmen Schliesst die Vergleichsworte von den konstruierten Baeumen aus (Eingabesaetze werden entsprechend gekuerzt). 
	 * @return Anzahl der in den Saetzen gefundenen Wortvorkommen. 
	 */
	public int baueBaumAusSaetzenMitWorttyp(String wortTyp,
			List<List<String>> satzListe, WortFilter wf, Knoten wurzel, Knoten praefixwurzel,
			boolean praefixBaumErstellen, boolean ausfuehrlicheFortschrittsMeldungen, int maxLaenge, boolean vergleichsworteNichtInBaumMitAufnehmen) {
		// Saetze aus Korpus durchlaufen, Treffer mitzaehlen (fuer Anzeige)
		int saetzeDurchlaufen = 0;
		int saetzeGefunden = 0;
		int vorkommenGefunden = 0;
		Iterator<List<String>> saetze = satzListe.iterator();
		while (saetze.hasNext()) {

			// Naechsten Satz ermitteln
			List<String> satz = saetze.next();

			// Pruefen, ob WortFilter greift
			if (wf.hatWort(satz)) {

				// Ermitteln, an welchen Stellen im Satz das Vergleichswort
				// vorkommt
				Integer[] vergleichsWortIndices = wf.getWortIndices(satz);

				// Anzahl der gefundenen Vorkommen mitzaehlen
				vorkommenGefunden += vergleichsWortIndices.length;

				// Indices durchlaufen
				for (int j = 0; j < vergleichsWortIndices.length; j++) {
					// Satz in Array konvertieren
					String[] satzArray = satz.toArray(new String[satz.size()]);
					// Satz in den Baum/Graphen hineinbauen
					int index = vergleichsWortIndices[j];
					if (vergleichsworteNichtInBaumMitAufnehmen)
						index = vergleichsWortIndices[j];
					if (index<satzArray.length)
						this.baueBaum(Arrays.copyOfRange(satzArray,
								index, satzArray.length),
								wurzel, false, maxLaenge);
					// Ggf. Satz ebenfalls in den Praefixbaum/-graphen
					// hineinbauen
					index = vergleichsWortIndices[j]+1;
					if (vergleichsworteNichtInBaumMitAufnehmen)
						index = vergleichsWortIndices[j];
					if (index>=0)
						if (praefixwurzel != null && praefixBaumErstellen) {
							this.baueBaum(Arrays.copyOfRange(satzArray, 0,
									index), praefixwurzel,
									true, maxLaenge);
						}
				}

				// Treffer mitzaehlen
				saetzeGefunden++;
			}

			// Durchlaufenen Satz mitzaehlen
			saetzeDurchlaufen++;

			// ggf. Meldung ausgeben
			if (ausfuehrlicheFortschrittsMeldungen){
				double prozentFertig = Math
						.ceil(((double) saetzeDurchlaufen / (double) satzListe
								.size()) * 100);
				if ((satzListe.size() / 20) != 0
						&& saetzeDurchlaufen % (satzListe.size() / 20) == 0) {
					Logger.getLogger(
							TreeBuilder.class.getCanonicalName())
							.info("Ermittle Saetze, die Wort '" + wortTyp
									+ "' beinhalten: " + saetzeDurchlaufen + "/"
									+ satzListe.size() + " (" + saetzeGefunden
									+ ") " + prozentFertig + "%");
				}
			}
		}
		
		// Anzahl der gefundenen Wortvorkommen zurueckgeben
		return vorkommenGefunden;
	}

}
