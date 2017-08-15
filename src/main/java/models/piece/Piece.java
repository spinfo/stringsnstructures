package models.piece;

/*
 * All pieces are extended from an abstract base Piece class Piece which
 * represents elements of a doubly linked list. All doubly linked lists contain
 * a sentinel element for entry etc.
 * 
 * Piece is extended to an abstract class TreeNodePiece with a reference
 * to a mother TreeNode, to a class AltTerminalNodePiece and to TextPiece.
 * 
 * TreeNodePiece is extended to two classes, TermnalTreeNodePiece and
 * NonTerminalTreeNodePiece.
 * 
 * TerminalTreeNodes (with reference to lexical information) are linked
 * by two PieceLists of type AltTerminalNodePiece. These lists represent alternatives.
 * Each terminal node points to the sentinel element of two AltPiece lists,
 * at start (left side) and at the end (right side).
 * 
 * Thus, all (typed) nodes in a tree are accessible.
 * 
 * TextPiece is used for (all) references to text, e.g. in morphological components.
 * (As its components pos and len are repeated in TerminalTreeNodePiece,
 * it may be more convenient to integrate an Interface for multiple inheritance.
 * TypedTextPiece is an empty string type (len 0) which represents potential
 * borders like '|', '>' and '<' as a result of (morphological) suffix trees
 * 
 *  
 * 
 * 						  Piece
 							Piece prev,next;
 							char[] text
 							|
 			---------------------------------------------------------
			|							|							|
		AltTerminalNodePiece	  TreeNodePiece					TextPiece
TerminalTreeNodePiece 					TreeNodePiece mother		int pos,len		
	terminalTreeNode					|							|
										|						TypedTextPiece
							------------------------
							|	 					|
					
							|						|
					TerminalTreeNodePiece	NonTerminalTreeNodePiece
						AltPiece left,			TreeNodePiece children
						right (sentinel)			(sentinel)
 						int pos, len
 
 
 */


public abstract class Piece {
	Piece prev, next;
	char[] text;
}
