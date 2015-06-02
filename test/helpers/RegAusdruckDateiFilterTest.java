package helpers;

import java.io.File;

import org.junit.Test;

public class RegAusdruckDateiFilterTest {

	@Test
	public void test() {
		File verzeichnis = new File("/Users/marcel/Downloads/OANC/data/written_1/journal/slate/43");
		RegAusdruckDateiFilter r = new RegAusdruckDateiFilter(".+\\.txt$"); 
		File[] quelldateien = verzeichnis.listFiles(r);
		for (int i=0; i<quelldateien.length; i++){
			// Fund zur Ergebnisliste hinzufuegen
			System.out.println(quelldateien[i].getName());
		}
	}

}
