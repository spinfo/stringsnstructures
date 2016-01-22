package modules.suffixTree.node;

import java.util.logging.Logger;

/**
 * For nodes in a generalised suffix tree.
 * 
 * If a node has a TextStartPosInfo, it is a leaf node. The TextStartPosInfo
 * object holds information about the suffix read, that produced this leaf node.
 * 
 * (text corpora may consist of units, units may consist of texts. In the case of
 * structure detection the corpus consists of units of types, the units consist
 * of texts, which are kwip tokens (sentences) (of this types) information for
 * leaf nodes about text in which suffix occurs and position of start in text)
 **/
public class TextStartPosInfo {
	private static final Logger LOGGER = Logger.getGlobal();
	// .getLogger(TextStartPosInfo.class.getName());

	public int unit, text, startPositionOfSuffix;

	// cstr
	public TextStartPosInfo(int unit, int text, int startPos) {
		this.unit = unit;
		this.text = text;
		this.startPositionOfSuffix = startPos;
		LOGGER.finest("TextStartPosInfo: unit: " + unit + " text: " + text + " startPos: " + startPos);
	}
}