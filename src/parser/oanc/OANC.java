package parser.oanc;

import helpers.RegAusdruckDateiFilter;
import helpers.VerzeichnisFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import modularization.ModuleImpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Gibt zentralen Zugriff auf die lokal gespeicherten Daten des OANC
 * @author marcel
 *
 */
public class OANC extends ModuleImpl {

	public static final String PROPERTYKEY_OANCLOCATION = "oanc-location";
	public static final String PROPERTYKEY_OANCLOCATION_REGEX = PROPERTYKEY_OANCLOCATION+"[0-9]+";
	private String[] oancSpeicherorte = new String[]{"/Users/marcel/Downloads/OANC/data/written_1/","/Users/marcel/Downloads/OANC/data/written_2/"};
	private FileFilter verzeichnisFilter = new VerzeichnisFilter();
	private FileFilter quellDateiFilter = new RegAusdruckDateiFilter(".+\\.txt$");
	
	public OANC() throws Exception {
		super();
		
		// Define I/O
		super.setInputReader(null);
		super.setInputStream(null);
		super.setOutputStream(null);
		
		// Set default module name
		this.setName("OANC-Korpus");
		
		// update properties
		this.updateProperties();
	}

	public String[] getOancSpeicherorte() {
		return oancSpeicherorte;
	}

	public void setOancSpeicherorte(String[] oancSpeicherorte) throws Exception {
		this.oancSpeicherorte = oancSpeicherorte;
		this.updateProperties();
	}

	public FileFilter getVerzeichnisFilter() {
		return verzeichnisFilter;
	}

	public void setVerzeichnisFilter(FileFilter verzeichnisFilter) {
		this.verzeichnisFilter = verzeichnisFilter;
	}

	public FileFilter getQuellDateiFilter() {
		return quellDateiFilter;
	}

	public void setQuellDateiFilter(FileFilter quellDateiFilter) {
		this.quellDateiFilter = quellDateiFilter;
	}

	/**
	 * Findet alle Textdateien, die sich am oder unterhalb der OANC-Speicherpfade befinden und gibt sie als Liste zurueck.
	 * @return Liste mit Textdateien
	 * @throws Exception Falls Dateien o. Verzeichnissse nicht gefunden werden oder nicht lesbar sind
	 */
	public List<File> sucheQuellDateien() throws Exception {
		
		// Liste fuer Ergebnis anlegen
		List<File> quellDateiListe = new ArrayList<File>();
		
		// Speicherorte durchlaufen
		for (int i=0; i<this.oancSpeicherorte.length; i++){
			
			// Speicherort-Verzeichnis ermitteln
			File verzeichnis = new File(oancSpeicherorte[i]);
			
			// Rekursive Variante dieser Methode aufrufen
			quellDateiListe.addAll(this.sucheQuellDateien(verzeichnis));
			
		}
		
		// Ergebnisliste zurueckgeben
		return quellDateiListe;
	}
	
	/**
	 * Findet alle Textdateien, die sich im oder unterhalb des uebergebenen Verzeichnis befinden und gibt sie als Liste zurueck.
	 * @param verzeichnis
	 * @return
	 * @throws Exception
	 */
	public List<File> sucheQuellDateien(File verzeichnis) throws Exception {
		
		// Liste fuer Ergebnis anlegen
		List<File> quellDateiListe = new ArrayList<File>();
			
		// Pruefen, ob existent, Verzeichnis und lesbar
		if (!(verzeichnis.isDirectory() && verzeichnis.canRead())) {
			throw new Exception("Der Pfad " + verzeichnis.getAbsolutePath() + " ist nicht lesbar bzw. kein Verzeichnis.");
		}
		
		// Quelldateien suchen
		File[] quelldateien = verzeichnis.listFiles(this.quellDateiFilter);
		for (int i=0; i<quelldateien.length; i++){
			// Fund zur Ergebnisliste hinzufuegen
			quellDateiListe.add(quelldateien[i]);
		}

		// Unterverzeichnisse durchlaufen
		File[] unterverzeichnisse = verzeichnis.listFiles(this.verzeichnisFilter);
		for (int i=0; i<unterverzeichnisse.length; i++){
			// Funktion rekursiv aufrufen
			quellDateiListe.addAll(this.sucheQuellDateien(unterverzeichnisse[i]));
		}
		
		return quellDateiListe;
	}

	@Override
	public boolean process() throws Exception {
		if (this.oancSpeicherorte == null || this.oancSpeicherorte.length<1)
			return false;
		
		// Search for corpus files
		List<File> fileList = this.sucheQuellDateien();
		
		// Instanciate JSON converter
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		// Convert file list to JSON and write to output
		gson.toJson(fileList, this.getOutputWriter());
		
		// Close the output writer
		this.getOutputWriter().close();
		
		// Processing ended successfully
		return true;
	}

	@Override
	protected void applyProperties() throws Exception {
		List<String> oancLocationList = new ArrayList<String>();
		Set<Object> propertyKeySet = this.getProperties().keySet();
		Iterator<Object> propertyKeys = propertyKeySet.iterator();
		while(propertyKeys.hasNext()){
			Object propertyKey = propertyKeys.next();
			if (propertyKey.toString().matches(PROPERTYKEY_OANCLOCATION_REGEX))
				oancLocationList.add(this.getProperties().getProperty(propertyKey.toString()));
				
		}
		super.applyProperties();
	}

	@Override
	protected void updateProperties() {
		for (int i=0; i<oancSpeicherorte.length; i++)
			this.getProperties().setProperty(PROPERTYKEY_OANCLOCATION+i, oancSpeicherorte[i]);
		super.updateProperties();
	}
}
