package modules.segmentation;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.hunspell.HunspellDictionary;
import org.apache.lucene.analysis.hunspell.HunspellStemmer;
import org.apache.lucene.analysis.hunspell.HunspellStemmer.Stem;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.German2Stemmer;
import org.tartarus.snowball.ext.SpanishStemmer;

/**
 * A class to abstract stemming algorithms from multiple languages.
 */
public class MultipleLanguageStemmer {

	/**
	 * Languages available to the <code>MultipleLanguageStemmer</code>
	 */
	public static enum LANGUAGE {
		DE, EN, ES
	};

	// The language to use for stemming
	private final LANGUAGE lang;

	// A dictionary and stemmer for hunspell
	private HunspellDictionary hunspellDict = null;
	private HunspellStemmer hunspellStemmer = null;

	// A porter stemmer
	private SnowballProgram porterStemmer = null;

	/**
	 * Sets the language to <code>lang</code> and initialises the stemmers to
	 * use for that language.
	 * 
	 * @param lang
	 *            The language to stem.
	 */
	public MultipleLanguageStemmer(LANGUAGE lang) {
		this.lang = lang;

		initStemmers();
	}

	/**
	 * Stem the word with a porter stemmer and return the determined stem.
	 * 
	 * @param word
	 *            The word to stem.
	 * @return A list of stems.
	 */
	public String porterStem(String word) {
		final String result;

		if (StringUtils.isBlank(word)) {
			result = word;
		} else {
			porterStemmer.setCurrent(word);
			porterStemmer.stem();
			result = porterStemmer.getCurrent();
		}

		return result;
	}

	/**
	 * Stem the word with hunspell and return a list of possible stems.
	 * 
	 * @param word
	 *            The word to stem.
	 * @return A list of stems.
	 */
	public List<String> hunspellStem(String word) {
		final List<String> result;
		final List<Stem> stems = hunspellStemmer.stem(word);

		if (stems.isEmpty()) {
			result = Collections.<String> emptyList();
		} else {
			result = stems.stream().map(stem -> stem.getStemString()).collect(Collectors.toList());
		}

		return result;
	}

	private void initStemmers() {
		// setup Hunspell dictionary and stemmer
		try {
			InputStream affixIn = getHunspellAffixFileStream(this.lang);
			InputStream dictionaryIn = getHunspellDictionaryFileStream(this.lang);
			hunspellDict = new HunspellDictionary(affixIn, dictionaryIn, Version.LUCENE_46, true);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			throw new IllegalStateException(
					"Packaged ressource files for hunspell should always be readable and parseable.");
		}
		hunspellStemmer = new HunspellStemmer(hunspellDict);

		// setup the porter stemmer
		this.porterStemmer = getPorterStemmer(this.lang);
	}

	// Get the resource files for hunspell dictionaries of the specified language
	private InputStream getHunspellDictionaryFileStream(LANGUAGE lang) {
		ClassLoader loader = getClass().getClassLoader();
		switch (lang) {
		case DE:
			return loader.getResourceAsStream("hunspell/de_DE.dic");
		case EN:
			return loader.getResourceAsStream("hunspell/en_US.dic");
		case ES:
			return loader.getResourceAsStream("hunspell/es_ES.dic");
		default:
			throw new IllegalStateException("Unknown language set.");
		}
	}

	// Get the resource files for hunspell affixes of the specified language
	private InputStream getHunspellAffixFileStream(LANGUAGE lang) {
		ClassLoader loader = getClass().getClassLoader();
		switch (lang) {
		case DE:
			return loader.getResourceAsStream("hunspell/de_DE.aff");
		case EN:
			return loader.getResourceAsStream("hunspell/en_US.aff");
		case ES:
			return loader.getResourceAsStream("hunspell/es_ES.aff");
		default:
			throw new IllegalStateException("Unknown language set.");
		}
	}

	private SnowballProgram getPorterStemmer(LANGUAGE lang) {
		final Class<? extends SnowballProgram> stemmerClass;
		switch (lang) {
		case DE:
			stemmerClass = German2Stemmer.class;
			break;
		case EN:
			stemmerClass = EnglishStemmer.class;
			break;
		case ES:
			stemmerClass = SpanishStemmer.class;
			break;
		default:
			throw new IllegalStateException("Unknown language set.");
		}
		try {
			return (SnowballProgram) stemmerClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("Snowball stemmer classes not accessible. Did the tartarus library change?");
		}
	}

}
