package parser.oanc;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import parallelization.CallbackReceiverImpl;

public class OANCXMLParserTest {
	
	/*private static String DATEIPFAD = "/home/marcel/Daten/OANC/OANC-1.0.1-UTF8/data/written_1/journal/verbatim/VOL15_1.txt";
	private static String SGDATEIPFAD = "/home/marcel/Daten/OANC/OANC-1.0.1-UTF8/data/written_1/journal/verbatim/VOL15_1-s.xml";
	private static CallbackReceiverImpl cbr = new CallbackReceiverImpl(){};
	
	@Test
	public void testOANCXMLParserFile() {
		System.out.println("Satzgrenzendatei ermitteln.");
		OANCXMLParser p = null;
		Properties pp = new Properties();
		pp.setProperty(OANCXMLParser.PROPERTYKEY_NAME, "OANC-Parser");
		try {
			p = new OANCXMLParser(cbr,pp);
		} catch (Exception e) {
			fail(e.toString());
		}
		
		System.out.println("Satzgrenzendatei: "+p.getSatzGrenzenXMLDatei().getAbsolutePath());
		assertTrue(p.getSatzGrenzenXMLDatei().getAbsolutePath().equals(SGDATEIPFAD));
		System.out.println();
		
	}

	@Test
	public void testParseQuellDatei() {
		OANCXMLParser p = null;
		try {
			System.out.println("Normaler Parser. Erwartet werden 713 Saetze.");
			p = new OANCXMLParser(new File(DATEIPFAD));
			List<String> saetze = p.parseQuellDatei();
			
			// Es sind in der Satzgrenzendatei 713 Saetze ausgewiesen.
			assertTrue(saetze.size()==713);
			
			System.out.println("Beispiele (Max 10):");
			Iterator<String> s = saetze.iterator();
			int limit = 10;
			while (limit >0 && s.hasNext()){
				System.out.println("\t"+s.next());
				limit --;
			}
			System.out.println();
			
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	@Test
	public void testParseQuellDateiAlternativ() {
		OANCXMLParser p = null;
		try {
			System.out.println("Parser+Annotationen: Erwartet werden 713 Saetze.");
			p = new OANCXMLParser(new File(DATEIPFAD));
			
			List<List<WortAnnotationTupel>> saetze = p.parseQuellDateiMitAnnotationen(true);
			
			// Es sind in der Satzgrenzendatei 713 Saetze ausgewiesen.
			assertTrue(saetze.size()==713);
			
			System.out.println("Beispiele (Max 10):");
			Iterator<List<WortAnnotationTupel>> s = saetze.iterator();
			int limit = 10;
			while (limit >0 && s.hasNext()){
				List<WortAnnotationTupel> l = s.next();
				Iterator<WortAnnotationTupel> w = l.iterator();
				System.out.print("\t");
				while (w.hasNext()){
					WortAnnotationTupel t = w.next();
					System.out.print(t.getWort()+"("+t.getAnnotation()+") ");
				}
				System.out.println();
				limit --;
			}
			System.out.println();
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	@Test
	public void testBereinigeUndSegmentiereSatz() {
		OANCXMLParser p = null;
		try {
			System.out.println("Rohsaetze bereinigen + segmentieren.");
			p = new OANCXMLParser(new File(DATEIPFAD));
			
			String rohsatz = " WortA-WortB: \n\t\t (WortC, WortD),\nWortE  WortF.  ";
			System.out.println("\t"+rohsatz);
			List<String> wortliste = p.bereinigeUndSegmentiereSatz(rohsatz, true, true, true, true);
			
			System.out.println("Anzahl ermittelter Worte: "+wortliste.size());
			assertTrue(wortliste.size()==15);
			
			Iterator<String> worte = wortliste.iterator();
			System.out.print("\t");
			while(worte.hasNext()){
				System.out.print("["+worte.next()+"] ");
			}
			System.out.println();
			
			List<String> saetze = p.parseQuellDatei();
			
			// Es sind in der Satzgrenzendatei 713 Saetze ausgewiesen.
			assertTrue(saetze.size()==713);
			
			System.out.println("Beispiele (Max 10):");
			Iterator<String> s = saetze.iterator();
			int limit = 10;
			while (limit >0 && s.hasNext()){
				String satz = s.next();
				System.out.println("\t"+satz);
				wortliste = p.bereinigeUndSegmentiereSatz(satz, true, true, true, true);
				worte = wortliste.iterator();
				System.out.print("\t");
				while(worte.hasNext()){
					System.out.print("["+worte.next()+"] ");
				}
				System.out.println();
				limit --;
			}
			System.out.println();
			
		} catch (Exception e) {
			fail(e.toString());
		}
	}
*/
}
