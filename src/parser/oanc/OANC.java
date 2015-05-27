package parser.oanc;

import helpers.RegAusdruckDateiFilter;
import helpers.VerzeichnisFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import modularization.CharPipe;
import modularization.ModuleImpl;
import parallelization.CallbackReceiver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Gibt zentralen Zugriff auf die lokal gespeicherten Daten des OANC
 * @author marcel
 *
 */
public class OANC extends ModuleImpl {

	public static final String PROPERTYKEY_OANCLOCATION = "oanc-location";
	private String[] oancSpeicherorte;
	private FileFilter verzeichnisFilter = new VerzeichnisFilter();
	private FileFilter quellDateiFilter = new RegAusdruckDateiFilter(".+\\.txt$");
	
	public OANC(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);
		
		// Define I/O
		this.getSupportedOutputs().add(CharPipe.class);
		
		// Add description for properties
		this.getPropertyDescriptions().put(PROPERTYKEY_OANCLOCATION, "The directory containing OANC-Files (subdirectories are used, too)");
		
		Logger.getLogger(this.getClass().getSimpleName()).log(Level.INFO, "Initialized module "+this.getProperties().getProperty(ModuleImpl.PROPERTYKEY_NAME));
	}

	/**
	 * Findet alle Textdateien, die sich am oder unterhalb der OANC-Speicherpfade befinden und gibt sie als Liste zurueck.
	 * @return Liste mit Textdateien
	 * @throws Exception Falls Dateien o. Verzeichnissse nicht gefunden werden oder nicht lesbar sind
	 */
	private List<File> sucheQuellDateien() throws Exception {
		
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
	private List<File> sucheQuellDateien(File verzeichnis) throws Exception {
		
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
		
		// Ergebnisliste zurueckgeben
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
		
		// Convert file list to JSON
		String fileListJson = gson.toJson(fileList);
		
		// Write the file list JSON to all output character pipes
		this.outputToAllCharPipes(fileListJson);
		
		// Close outputs
		this.closeAllOutputWriters();
		
		// Processing ended successfully
		return true;
	}

	@Override
	protected void applyProperties() throws Exception {
		
		if (this.getProperties().containsKey(PROPERTYKEY_OANCLOCATION))
			this.oancSpeicherorte = new String[]{this.getProperties().get(PROPERTYKEY_OANCLOCATION).toString()};
		
		// Call apply for super class
		super.applyProperties();
	}

}
