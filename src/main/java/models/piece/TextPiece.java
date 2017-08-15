package models.piece;

public class TextPiece extends Piece{
	int pos, len;
	
	String stringOfPieces (Piece lastPiece){
		StringBuffer res=new StringBuffer();
		TextPiece current=this;
		while((current!=null) && (current!=lastPiece.next)){
			for (int pos= current.pos;pos<current.pos+len;pos++){
				res.append(text[pos]);
			}
		}
		return res.toString();
	}

}
