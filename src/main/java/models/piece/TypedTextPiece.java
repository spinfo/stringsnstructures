package models.piece;

public class TypedTextPiece extends TextPiece{
	
	public static final char PIPE = '|';
	public static final char GR = '>';
	public static final char LE = '<';
	
	public char type;
	
	// cstr
	public TypedTextPiece(int pos,char type){
		this.len=0; // TypedTextPieces are always of length 0;
		this.pos=pos;
		this.type=type;
	}

}
