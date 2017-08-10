package modules.matrix;

public interface IMatrixBitWiseOperationTreeWalkerListener {

	public void entryAction(MatrixBitWiseOperationTreeNodeElement node,IMatrixBitWiseOperationTreeWalkerListener l,int level);
	public void exitAction(MatrixBitWiseOperationTreeNodeElement node,IMatrixBitWiseOperationTreeWalkerListener l,int level);
	
}
