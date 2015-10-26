package modules.treeBuilder;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.TreeSet;

import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.Pipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.parallelization.CallbackReceiver;

public class AtomicRangeSuffixTrieBuilder extends ModuleImpl {
	
	// Property keys
	public static final String PROPERTYKEY_MAXLENGTH = "Maximum length of branches";
	public static final String PROPERTYKEY_REVERSE = "Reverse the trie";

	// Local variables
	private final String INPUTID = "input";
	private final String OUTPUTID = "output";
	private int maxLaenge; // Maximale Laenge des zu bauenden Baums
	private boolean umgekehrt; // Maximale Laenge des zu bauenden Baums

	public AtomicRangeSuffixTrieBuilder(CallbackReceiver callbackReceiver, Properties properties)
			throws Exception {
		super(callbackReceiver, properties);

		// Define I/O
		InputPort inputPort = new InputPort(INPUTID, "Plain text character input.", this);
		inputPort.addSupportedPipe(CharPipe.class);
		OutputPort outputPort = new OutputPort(OUTPUTID, "JSON-encoded suffix trie.", this);
		outputPort.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputPort);
		super.addOutputPort(outputPort);
		
		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_MAXLENGTH,"Define the maximum length of any branch of the trie.");
		this.getPropertyDescriptions().put(PROPERTYKEY_REVERSE,"Reverse the building of the trie (results in a prefix trie).");
		
		// Add default values
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, "AtomicRangeSuffixTrieBuilder");
		this.getPropertyDefaultValues().put(PROPERTYKEY_MAXLENGTH, "10");
		this.getPropertyDefaultValues().put(PROPERTYKEY_REVERSE, "false");
		
		// Add module description
		this.setDescription("Iterates over a raw and unsegmented string input, building a suffix trie from the data of limited range with each step. Keeps track of how often each node of the suffix trie gets triggered.");
	}

	@Override
	public boolean process() throws Exception {
		
		// Wurzelknoten des zu erstellenden Baumes erstellen
		Knoten wurzelKnoten = new Knoten("^");
		
		LinkedList<Character> buffer = new LinkedList<Character>();
		
		// Read first characters
		int charCode = this.getInputPorts().get(INPUTID).getInputReader().read();

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
			if (buffer.size()>this.maxLaenge)
				buffer.removeFirst();
			
			// Construct trie from buffer and attach it to the root node (skip this until the buffer is full)
			if (buffer.size()==this.maxLaenge)
				this.baueTrie(buffer, wurzelKnoten, this.umgekehrt, -1);
			
			// Read next char
			charCode = this.getInputPorts().get(INPUTID).getInputReader().read();
		}
		
		// Read remaining buffer
		if (!buffer.isEmpty() && buffer.size()==this.maxLaenge)
			buffer.removeFirst();
		while (!buffer.isEmpty()){
			// Construct trie from buffer and attach it to the root node
			this.baueTrie(buffer, wurzelKnoten, this.umgekehrt, -1);
			buffer.removeFirst();
		}
		
		// Letztlich wird der Wurzelknoten (und damit der gesamte erstellte Baum) in JSON umgewandelt und ausgegeben
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
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
		if (this.getProperties().containsKey(PROPERTYKEY_MAXLENGTH))
			this.maxLaenge = Integer.parseInt(this.getProperties().getProperty(PROPERTYKEY_MAXLENGTH));
		if (this.getProperties().containsKey(PROPERTYKEY_REVERSE))
			this.umgekehrt = Boolean.parseBoolean(this.getProperties().getProperty(PROPERTYKEY_REVERSE));
		super.applyProperties();
	}
	
    /**
     * Erzeugt einen Suffixtrie im uebergebenen Knoten anhand der uebergebenen Token.
     * Inkrementiert die Zaehlvariable eines jeden Knotens um eins fuer jede "Beruehrung".
     * @param token LinkedList mit Token
     * @param rootnode Startknoten Wurzelknoten des zu konstruierenden Baumes
     * @param umgekehrt Zeigt an, ob der Baum umgekehrt erstellt werden soll (quasi als "Praefixbaum")
     * @param maxLaenge Die maximale Anzahl an Token, die dem Trie hinzugefuegt werden soll (&lt;0 = ignorieren).
     * @return Die Anzahl der neu erstellten Knoten
     */
    public int baueTrie(LinkedList<Character> token, Knoten rootnode, boolean umgekehrt, int maxLaenge) {

    	// Variable zum Mitzaehlen der erstellten Knoten
		int knotenerstellt = 0;
		
		// "Beruehrung" des Knotens mitzaehlen
		rootnode.setZaehler(rootnode.getZaehler() + 1);

		// Wenn keine Token mehr vorhanden sind bzw. die maximal hinzuzufuegende Anzahl an Token ueberschritten wird, wird abgebrochen
		if (token == null || token.size() == 0 || maxLaenge==0) {
			return knotenerstellt;
		}

		// Index des als naechstes zu vergleichenden Tokens ermitteln
		int vergleichsTokenIndex = 0;
		if (umgekehrt) {
			vergleichsTokenIndex = token.size() - 1;
		}

		// Variable fuer Kindknoten definieren
		Knoten kindKnoten;

		// Ggf. neuen Knoten erstellen
		if (!rootnode.getKinder().containsKey(String.valueOf(token.get(vergleichsTokenIndex)))) {
			// passender Knoten NICHT vorhanden - neuen erstellen
			kindKnoten = new Knoten();
			
			// Zaehler fuer erstellte Knoten inkrementieren
			knotenerstellt++;
			
			// Den Namen der Kante in der Node speichern .. um spaeter bei Bedarf die Knoten geordnet ausgeben zu koennen (debug)
			kindKnoten.setName(String.valueOf(token.get(vergleichsTokenIndex)));
			
			// Kind dem Elternknoten anfuegen
			rootnode.getKinder().put(String.valueOf(token.get(vergleichsTokenIndex)), kindKnoten);

		} else {
			// passender Knoten vorhanden
			kindKnoten = rootnode.getKinder().get(String.valueOf(token.get(vergleichsTokenIndex)));
		}

		// Eingabeliste klonen (da sie im weiteren Schritt gekuerzt wird)
		@SuppressWarnings("unchecked")
		LinkedList<Character> restListe = (LinkedList<Character>)token.clone();
		
		// Pruefen, ob der Baum "umgekehrt" erstellt werden soll
		if (umgekehrt) {
			// Rekursiver Aufruf mit Token 0 bis n-1
			restListe.removeLast();
			knotenerstellt += this.baueTrie( restListe,
					kindKnoten, umgekehrt, maxLaenge-1);
		} else {
			// Rekursiver Aufruf mit Token 1 bis n
			restListe.removeFirst();
			knotenerstellt += this.baueTrie( restListe,
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
	 * @param wurzel Wurzelknoten
	 * @param treeSet Treeset
	 */
	public void fuegeNodesInTreeSetEin(Knoten wurzel, TreeSet<Knoten> treeSet) {
		Iterator<String> kinder = wurzel.getKinder().keySet().iterator();
		while (kinder.hasNext()) {
			fuegeNodesInTreeSetEin(wurzel.getKinder().get(kinder.next()),
					treeSet);
		}
		treeSet.add(wurzel);
	}

}
