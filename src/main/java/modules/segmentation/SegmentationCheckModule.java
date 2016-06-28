package modules.segmentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import common.StringUtil;
import common.parallelization.CallbackReceiver;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

/**
 * A module to check the morphological segmentation of words against the output
 * of a stemmer that would be produced for the same words.
 */
public class SegmentationCheckModule extends ModuleImpl {

	private static final Logger LOGGER = Logger.getLogger(SegmentationCheckModule.class.getName());

	private static final String MODULE_DESC = "Check for a list of segmented words, how many of those words would be segmented in the same way by a stemmer with linguistic background information.";

	private static final String INPUT_ID = "input";
	private static final String INPUT_DESC = "A list with words/tokens on each line separated by a single segmentation char.";

	private static final String OUTPUT_ID = "output";
	private static final String OUTPUT_DESC = "Outputs a log showing valid and invalid segmentations as well as a single last line with a count of total recognized lines";

	// variables for the user-set property of the separator
	private static final String PROPERTYKEY_SEPARATOR_ID = "Segment separator";
	private static final String PROPERTYKEY_SEPARATOR_DESC = "On what single char to split the segments.";
	private static final String DEFAULT_SEPARATOR = "|";

	// varibales for the user set property of the input language
	private static final String PROPERTYKEY_LANG_ID = "Input language";
	private static final String PROPERTYKEY_LANG_DESC = "The input language to use. Either 'EN', 'DE' or 'ES'";

	public SegmentationCheckModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// set module name and description
		this.setName("Segmentatation Check Module");
		this.setDescription(MODULE_DESC);
		this.setCategory("Segmentation");

		// setup i/o
		InputPort in = new InputPort(INPUT_ID, INPUT_DESC, this);
		in.addSupportedPipe(CharPipe.class);
		super.addInputPort(in);

		OutputPort out = new OutputPort(OUTPUT_ID, OUTPUT_DESC, this);
		out.addSupportedPipe(CharPipe.class);
		super.addOutputPort(out);

		// setup properties with default values
		this.getPropertyDescriptions().put(PROPERTYKEY_SEPARATOR_ID, PROPERTYKEY_SEPARATOR_DESC);
		this.getPropertyDefaultValues().put(PROPERTYKEY_SEPARATOR_ID, DEFAULT_SEPARATOR);
		this.getPropertyDescriptions().put(PROPERTYKEY_LANG_ID, PROPERTYKEY_LANG_DESC);
		this.getPropertyDefaultValues().put(PROPERTYKEY_LANG_ID, "EN");
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, this.getName());

		this.setDefaultsIfMissing();
	}

	@Override
	public boolean process() throws Exception {
		boolean result = true;

		// a reader to get input line by line
		BufferedReader reader = null;

		// the output port
		OutputPort out = super.getOutputPorts().get(OUTPUT_ID);

		// processing should fail on any error
		try {
			// The stemmer to use for checking
			MultipleLanguageStemmer stemmer = getStemmerByLanguage(
					this.getProperties().getProperty(PROPERTYKEY_LANG_ID));

			// variables to get at total accuracy
			int hits = 0;
			int misses = 0;

			// A regex to split each line on
			String pattern = this.getProperties().getProperty(PROPERTYKEY_SEPARATOR_ID);
			final Pattern separator = Pattern.compile(Pattern.quote(pattern));
			String[] split = null;

			// we will proceed line by line
			reader = new BufferedReader(super.getInputPorts().get(INPUT_ID).getInputReader());
			String line;
			String word;
			List<String> stems;
			String stem;
			while ((line = reader.readLine()) != null) {
				boolean isValid = false;

				if (StringUtil.isBlank(line)) {
					continue;
				}

				split = separator.split(line);

				if (split == null || split.length != 2) {
					LOGGER.warning("Bad input line: " + line);
					continue;
				}

				word = split[0] + split[1];

				// try porter stemming first as it should be faster
				stem = stemmer.porterStem(word);
				if (split[0].equals(stem) || split[1].equals(stem)) {
					hits += 1;
					outputHit(out, line, stem);
					continue; // for a valid word move directly to the next line
				}

				// try hunspell stemming if porter didn't work
				stems = stemmer.hunspellStem(word);
				// hunspell may offer multiple stems. A single hit counts as a
				// valid segmentation.
				for (String s : stems) {
					if (split[0].equals(s) || split[1].equals(s)) {
						hits += 1;
						outputHit(out, line, s);
						isValid = true;
						break;
					}
				}

				if (!isValid) {
					misses += 1;
					out.outputToAllCharPipes(
							"\"" + line + "\": INVALID segmentation not recognized" + System.lineSeparator());
				}
			}

			// Output info on total hits/misses
			int total = hits + misses;
			double percentage = ((double) hits / total) * 100;
			out.outputToAllCharPipes("TOTAL: recognized " + hits + "/" + total + "("
					+ String.format("%.2f", percentage) + " %)" + System.lineSeparator());

		} catch (Exception e) {
			result = false;
			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
			this.closeAllOutputs();
		}

		return result;
	}

	private void outputHit(OutputPort out, String line, String stem) throws IOException {
		out.outputToAllCharPipes("\"" + line + "\": VALID segmentation for stem: " + stem + System.lineSeparator());
	}

	private MultipleLanguageStemmer getStemmerByLanguage(String lang) {
		switch (lang) {
		case "EN":
			return new MultipleLanguageStemmer(MultipleLanguageStemmer.LANGUAGE.EN);
		case "DE":
			return new MultipleLanguageStemmer(MultipleLanguageStemmer.LANGUAGE.DE);
		case "ES":
			return new MultipleLanguageStemmer(MultipleLanguageStemmer.LANGUAGE.ES);
		default:
			throw new IllegalArgumentException("Not a valid language choice: " + lang);
		}
	}

}
