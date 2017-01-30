package modules.matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import common.parallelization.CallbackReceiver;
import models.NamedFieldMatrix;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;

/**
 * Module interprets either rows or columns of an input matrix as binary bitsets
 * and performs symmetrical operations (AND, OR, XOR) on these bitsets. Output
 * is again a matrix showing how many bits are set for each combination of
 * rows/columns after the operation is performed.
 */
import base.workbench.ModuleRunner;




public class MatrixBitwiseOperationModule extends ModuleImpl {
	
	//-----test for Check JR-------------------------------------------------------------------------
	
	private static String best_n1, best_n2;
	private static BitSet best_BitSet=null;
	private static int best_nr=0;
	private static int difference=0;
	
	
	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(MatrixBitwiseOperationModule.class, args);
	}
	
	
	//---------- JR-----xtensions---------------------------------------------------------
	private class Best {
		void selectBest(BitSet product, BitSet nrBits,String name1,String name2) {
			if (product.cardinality()==difference) {
				int nr=nrBits.cardinality();
				if (nr>best_nr) {
					best_nr=nr;
					best_n1=name1;
					best_n2=name2;
					best_BitSet=nrBits;
				}
			}
		}// selectBest
		
		void printBest(NamedFieldMatrix matrix) {
			System.out.println("Best: "+best_n1+ "  " +best_n2+" "+best_BitSet.cardinality());
			for (int i=0;i<best_BitSet.length();i++){
				if(best_BitSet.get(i)){
					String colName=matrix.getColumnName(i);
					System.out.println(colName);
				}
			}
		}
		
		
	}//Best
	
	
	private class Concurrency {
		HashMap<String,Occurrencies> concurrHashMap;
		
		void readEvalHashMap(BufferedReader r) throws Exception{
			
			concurrHashMap=new HashMap<String,Occurrencies>();
			String line;
			int lineNr=0;
			while((line=r.readLine())!=null){
				if (line.length()>0){
					String[] concurrStr= line.split("\\$;?");
					int nrDollar=concurrStr.length;
					System.out.print("redEvalMatrixProposals: "+line+" nrDollars: "+nrDollar+" ");
					nrDollar--;
					
					int nrConcurr=0;
					while(nrDollar>=0) {
						String[] partOfConcurr=concurrStr[nrConcurr].split("\\|");
						Occurrencies occ= 
						concurrHashMap.get(partOfConcurr[0]);
						if (occ==null) {
							concurrHashMap.put(partOfConcurr[0], new Occurrencies(lineNr));
						}
						else {occ.lineNrs.add(lineNr);
							
						}
						
						nrDollar--;
						nrConcurr++;
					}
					lineNr++;
					System.out.println();
				}
				
			}			
			
		}// readEvalHashMap
		
		boolean checkConcurrency(String name1,String name2) throws Exception{
			
			if(name2.startsWith(name1)) {
				System.out.print("name1 "+name1 +" is prefix of "+name2+"  ");
				Occurrencies occ1= concurrHashMap.get(name1);
				/*if (occ1!=null ){
					for (int index=0;index<occ1.lineNrs.size();index++){
						System.out.print(occ1.lineNrs.get(index)+ "  ");
					}
				}
				System.out.print(" : ");
				*/
				Occurrencies occ2= concurrHashMap.get(name2);
				/*if (occ2!=null ){
					for (int index=0;index<occ2.lineNrs.size();index++){
						System.out.print(occ2.lineNrs.get(index)+ "  ");
					}
				}
				System.out.println();
				*/
				
				if ((occ1!=null) && (occ2!=null)){					
					for (int index1=0;index1<occ1.lineNrs.size();index1++){
						for (int index2=0;index2<occ2.lineNrs.size();index2++){									
							if (occ1.lineNrs.get(index1).equals(occ2.lineNrs.get(index2))){
								System.out.println(" concurrency "+
								occ1.lineNrs.get(index1)+"  "+occ2.lineNrs.get(index2));
										
							}
						}
					}
				}
				
			}
			else if (name1.startsWith(name2)){
				System.out.println("name2 "+name2 +" is prefix of "+name1);
				Exception e= new Exception();
				throw e;
			}
			
			return false;
		}//checkConcurrency
	}// class Concurrency
	
	private class Occurrencies {
		
		ArrayList<Integer> lineNrs;
		
		Occurrencies(int lineNr){
			lineNrs=new ArrayList<Integer>();
			lineNrs.add(lineNr);
			System.out.print("Occurrencies lineNr: "+lineNr+" ");
		}
	}
	
	
	
	private class MorphResult {
		
		private ArrayList<Integer> columnSum;
		private ArrayList<Integer> rowSum;
		private ArrayList <Double>resultList=null;
		//NamedFieldMatrix resultMatrix;
		private int nrBitsInRow=0;
		
		private void columnSum(NamedFieldMatrix namedFieldMatrix){
		// the sum of all bits set in a column, for all columns
			columnSum= new ArrayList<Integer>();
			int columnsAmount=namedFieldMatrix.getColumnsAmount();
			for (int col=0;col<columnsAmount;col++){
				double[] values=namedFieldMatrix.getColumn(col);
				// create the BitSet from the matrix' values
				BitSet result = new BitSet(values.length);
				for (int i = 0; i < values.length; i++) {
					if (!ZERO_D.equals(values[i])) {
						result.set(i);
					}
					
				}// for int i
				int nrBitsInColumn=result.cardinality();
				columnSum.add(nrBitsInColumn);
			}// for int col
			
		}// columnSum();
		
		private void rowSum(NamedFieldMatrix namedFieldMatrix){
			rowSum= new ArrayList<Integer>();
			// the sum of all bits set in a row, for all rows
			
			int rowsAmount=namedFieldMatrix.getRowAmount();
			for (int row=0;row<rowsAmount;row++){
				double[] values=namedFieldMatrix.getRow(row);
				// create the BitSet from the matrix' values
				BitSet result = new BitSet(values.length);
				for (int i = 0; i < values.length; i++) {
					if (!ZERO_D.equals(values[i])) {
						result.set(i);
					}
					
				}// for int i
				nrBitsInRow=result.cardinality();
				rowSum.add(nrBitsInRow);
			}// for int row
			
			
		}// rowSum
		
		/*private double calcVal(int colVal,int rowVal){
			// TODO
			return (double) colVal+rowVal/nrBitsInRow;
		}//calcVal
		*/
		
		private void calculatemorphVectorMatrix(NamedFieldMatrix namedFieldMatrix) {
			final Double ZERO_D = new Double(0.0);
			resultList=new ArrayList<Double>();
			int rowsAmount=namedFieldMatrix.getRowAmount();
			int columnsAmount=namedFieldMatrix.getColumnsAmount();
			double resVal;
			for (int row=0;row<rowsAmount;row++){
				resVal=0;
				for (int col=0;col<columnsAmount;col++){
					// get non zero values of namedFieldMatrix
					double val=namedFieldMatrix.getValue(row, col);
					if(val!=ZERO_D){
						resVal=resVal+this.columnSum.get(col);//=calcVal(this.columnSum.get(col),this.rowSum.get(row))+resVal;
						//resultMatrix.setValue(row, col, resVal);
					}
					
				}
				
				resultList.add(resVal);
			}
		}//calculatemorphVectorMatrix
		
		void morphProcess(NamedFieldMatrix namedFieldMatrix,Set<String> names) {
			//columnSum(namedFieldMatrix);
			//rowSum(namedFieldMatrix);
			//calculatemorphVectorMatrix(namedFieldMatrix);
			Operation operation=Operation.valueOf("AND");
			
			for (String name1:names){
				
				int val=0;
				int res=0;
				BitSet operand1 = getOrCreateBitSet(name1);
				int cardName1=operand1.cardinality();
				for (String name2:names){
					if (name1 != name2) {
						BitSet operand2 = getOrCreateBitSet(name2);
						BitSet product = 
						performOperation(operand1, operand2, operation);
						val=(int)(Math.pow(product.cardinality(),3) * 
								Math.pow(cardName1,3));
						res=res+val*name2.length();
					}
				}
				System.out.println("morphProcess "+name1+" \t "+ res);
			}
			
			// output, test
			/*for (String name:names){
				System.out.println(name+"  "+ this.resultList.get(row) *
						this.rowSum.get(name));
			}*/
			
		}//morphProcess
		
	}// class MorphResult
//---------End jr---------------------------------------------------------------------------
	
	private static final String MODULE_DESC = "Module interprets either rows or columns of an input matrix as binary bitsets"
			+ " and performs symmetrical operations (AND, OR, XOR) on these bitsets. Output"
			+ " is again a matrix showing how many bits are set for each combination of"
			+ " rows/columns after the operation is performed.";

	private final static String INPUT_ID = "Input Matrix";
	private final static String INPUT_DESC = "[text/csv] An input csv table. Header row and first column are expected to contain Strings as labels. All other fields are assumed to be blank or contain numerical values.";

	private final static String INPUTConcurrent_ID="Input Concurrent";
	private final static String INPUTConcurrent_DESC="Concurrent entries \';\' separated";
	
	private final static String OUTPUT_MATRIX_ID = "Output Matrix";
	private final static String OUTPUT_MATRIX_DESC = "[text/csv] A symmetrical csv table mapping row/column headings to each other and containing the amounts of bits sets after the the operation specified was applied.";

	private final static String OUTPUT_LIST_ID = "List Output";
	private final static String OUTPUT_LIST_DESC = "[text/plain] A list of row/column mappings with the amount of bits set after the operation was applied, sorted by that count.";

	// An enum, and property to specify the operation to apply
	// Note: For these operations the order of operands is unimportant, adding
	// an asymmetrical operation would require some changes in the processing
	private static enum Operation {
		AND, OR, XOR
	};

	private static final String PROPERTYKEY_OPERATION = "operation";
	private Operation operation;

	// A boolean deciding whether to operate on columns or rows of a table and a
	// property for the user to choose that
	private static final String PROPERTYKEY_USE_ROWS = "Operate on rows";
	private boolean useRows;

	// Properties for the input and output separator
	private static final String PROPERTYKEY_INPUT_SEPARATOR = "Input separator";
	private static final String PROPERTYKEY_OUTPUT_SEPARATOR = "Output separator";
	private String inputSeparator;
	private String outputSeparator;

	// Properties for controlling which rows/columns are compared
	private static final String PROPERTYKEY_OPERATE_REFLEXIVE = "Reflexive";
	private boolean reflexive;

	// The value zero as a double
	private static final Double ZERO_D = new Double(0.0);

	// Bitsets are initialised lazily and kept in this map
	private Map<String, BitSet> bitsets;

	// The input matrix may be accessed from some private methods
	NamedFieldMatrix inMatrix;
	
	

	public MatrixBitwiseOperationModule(CallbackReceiver callbackReceiver, Properties properties) throws Exception {
		super(callbackReceiver, properties);

		// set module name and description
		this.setName("Matrix Bitwise Operation Module");
		this.getPropertyDefaultValues().put(ModuleImpl.PROPERTYKEY_NAME, this.getName());
		this.setDescription(MODULE_DESC);

		// setup i/o
		InputPort input = new InputPort(INPUT_ID, INPUT_DESC, this);
		input.addSupportedPipe(CharPipe.class);
		super.addInputPort(input);
		
		InputPort inputConcurrent = new InputPort(INPUTConcurrent_ID, INPUTConcurrent_DESC, this);
		inputConcurrent.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputConcurrent);

		OutputPort matrixOutput = new OutputPort(OUTPUT_MATRIX_ID, OUTPUT_MATRIX_DESC, this);
		matrixOutput.addSupportedPipe(CharPipe.class);
		super.addOutputPort(matrixOutput);

		OutputPort listOutput = new OutputPort(OUTPUT_LIST_ID, OUTPUT_LIST_DESC, this);
		listOutput.addSupportedPipe(CharPipe.class);
		super.addOutputPort(listOutput);

		// setup properties
		this.getPropertyDescriptions().put(PROPERTYKEY_OPERATION, "Which operation to perform, one of: AND, OR, XOR.");
		this.getPropertyDescriptions().put(PROPERTYKEY_USE_ROWS,
				"Whether to operate on rows (true) or columns (false).");
		this.getPropertyDescriptions().put(PROPERTYKEY_INPUT_SEPARATOR,
				"Which input separator to use for the input csv table.");
		this.getPropertyDescriptions().put(PROPERTYKEY_OUTPUT_SEPARATOR,
				"Which Output separator to use for the csv table output.");
		this.getPropertyDescriptions().put(PROPERTYKEY_OPERATE_REFLEXIVE,
				"Whether the operation should be applied to a row/col with itself.");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OPERATION, "AND");
		this.getPropertyDefaultValues().put(PROPERTYKEY_USE_ROWS, "true");
		this.getPropertyDefaultValues().put(PROPERTYKEY_INPUT_SEPARATOR, ";");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OUTPUT_SEPARATOR, ";");
		this.getPropertyDefaultValues().put(PROPERTYKEY_OPERATE_REFLEXIVE, "false");

		this.setDefaultsIfMissing();
	}

	public boolean process() throws Exception {
		boolean result = true;
		//--------------JR----------
		Best best=new Best();
		//--------------End JR------
		// a reader to read input line by line
		BufferedReader inputReader = new BufferedReader(getInputPorts().get(INPUT_ID).getInputReader());

		// set the bitsets member which will be used to store bitsets when
		// created
		bitsets = new HashMap<>();

		try {
			// read the input matrix to operate on and determine whether row or
			// column names will be operated on
			inMatrix = NamedFieldMatrix.parseCSV(inputReader, inputSeparator);
			Set<String> names;
			if (useRows) {
				names = inMatrix.getRowNames();
			} else {
				names = inMatrix.getColumnNames();
			}
			
			//JR  test concurrency
			Concurrency concurrency=null;
			if (getInputPorts().get(INPUTConcurrent_ID)!=null){			
				concurrency= new Concurrency();
				concurrency.readEvalHashMap(
				new BufferedReader(getInputPorts().get(INPUTConcurrent_ID).getInputReader()));
			}
			//JR End Concurrency
			
			// build a matrix containing the result of applying the operation to
			// each pair of BitSets
			NamedFieldMatrix outMatrix = new NamedFieldMatrix();
			BitSet operand1 = null;
			BitSet operand2 = null;
			BitSet product = null;
			Double value = null;
			
		    
			//evalMatrixProposals eval=null;
			for (String name1 : names) {
				System.out.print("Name: "+name1);
				//eval=evalHashMap.get(name1);    //get(index);
				//if(eval !=null) {
				//	
				//	System.out.print("\t "+" evalHashMap: "+eval.concurrent);
				//}
				System.out.println();
				
				operand1 = getOrCreateBitSet(name1);
			
				for (String name2 : names) {
					// If this combination was already calculated in a
					// previous iteration, just copy the value
					// this works as long as all possible operations are
					// symmetrical
					value = outMatrix.getValue(name2, name1);
					if (value != null) {
						outMatrix.setValue(name1, name2, value);
						continue;
					}
					// don't compare a BitSet to itself unless instructed
					if (!reflexive && name1.equals(name2)) {
						// set to zero to get a symmetrical matrix
						outMatrix.setValue(name1, name2, 0.0);
						continue;
					}

					// actually compare the two bitsets and save the amount of
					// bits set in the result to the output matrix
					operand2 = getOrCreateBitSet(name2);
					product = performOperation(operand1, operand2, operation);
					outMatrix.setValue(name1, name2, (double) product.cardinality());
					
					//---------------JR--------------------------
					best.selectBest(product,operand1,name1,name2);
					//---------------JR--------------------------
					
					//----test jr
					if (concurrency!=null)
						if (concurrency.checkConcurrency(name1, name2)) {
							System.out.println("name1 "+name1 +" is concurrent to "+name2+"  ");
						}
					
					
				}//for (String name2 : names) 
			}//for (String name1 : names)
			
			//test jr------------
			 best.printBest(inMatrix);
			 MorphResult morphResult=new MorphResult();
			 morphResult.morphProcess(inMatrix,names);
			 
			//----------End test
			// these data structures might have gotten big and may be
			// harvested directly after processing finished.
			inMatrix = null;
			bitsets = null;

			// write the output
			OutputPort matrixOut = this.getOutputPorts().get(OUTPUT_MATRIX_ID);
			if (matrixOut.isConnected()) {
				writeMatrixOutput(outMatrix, matrixOut, outputSeparator);
			}

			OutputPort listOut = this.getOutputPorts().get(OUTPUT_LIST_ID);
			if (listOut.isConnected()) {
				writeListOutput(outMatrix, listOut);
			}

		} catch (Exception e) {
			result = false;
			throw e;
		} finally {
			if (inputReader != null) {
				inputReader.close();
			}
			this.closeAllOutputs();
		}

		return result;
	}

	// performs the operation on both BitSets and returns a new BitSet
	// containing the result
	private static BitSet performOperation(BitSet op1, BitSet op2, Operation op) {
		// we need a clone for the operation because it is destructive
		BitSet result = (BitSet) op1.clone();

		switch (op) {
		case AND:
			result.and(op2);
			break;
		case OR:
			result.or(op2);
			break;
		case XOR:
			result.xor(op2);
			break;
		default:
			throw new IllegalStateException("Unknown bitwise operation: " + op);
		}

		return result;
	}

	// get the BitSet associated with the given row or column name. If it
	// doesn't exist already create it from the given matrix
	private BitSet getOrCreateBitSet(String name) {
		// try to find the BitSet in those already created
		BitSet result = bitsets.get(name);

		// if it doesn't exist, create it
		if (result == null) {
			double[] values;

			// decide on whether to use rows or columns
			if (useRows) {
				values = inMatrix.getRow(name);
			} else {
				values = inMatrix.getColumn(name);
			}

			// create the BitSet from the matrix' values
			result = new BitSet(values.length);
			for (int i = 0; i < values.length; i++) {
				if (!ZERO_D.equals(values[i])) {
					result.set(i);
				}
			}

			// save the new BitSet
			bitsets.put(name, result);
		}

		return result;
	}

	private static void writeMatrixOutput(NamedFieldMatrix matrix, OutputPort out, String separator)
			throws IOException {
		matrix.setDelimiter(separator);
		out.outputToAllCharPipes(matrix.csvHeader());
		for (int i = 0; i < matrix.getRowAmount(); i++) {
			out.outputToAllCharPipes(matrix.csvLine(i));
		}
	}

	private static void writeListOutput(NamedFieldMatrix matrix, OutputPort out) throws IOException {
		TreeMap<Integer, List<String>> items = new TreeMap<Integer, List<String>>(Collections.reverseOrder());
		StringBuilder sb = new StringBuilder();

		Double value = null;
		String rowName = null;
		List<String> tokens = null;
		for (int i = 0; i < matrix.getRowAmount(); i++) {
			rowName = matrix.getRowName(i);

			for (int j = 0; j < matrix.getColumnsAmount(); j++) {
				value = matrix.getValue(i, j);

				if (value != null && value > 0) {
					tokens = items.getOrDefault(value.intValue(), new LinkedList<String>());

					sb.append(rowName);
					sb.append('-');
					sb.append(matrix.getColumnName(j));

					tokens.add(sb.toString());
					items.put(value.intValue(), tokens);
					sb.setLength(0);
				}
			}
		}

		for (int count : items.keySet()) {
			for (String nameCombination : items.get(count)) {
				out.outputToAllCharPipes(nameCombination + ": " + count);
				out.outputToAllCharPipes("\n");
			}
		}
	}

	@Override
	public void applyProperties() throws Exception {

		// Set defaults for properties not yet set
		super.setDefaultsIfMissing();

		// A string for testing user inputs on
		String value;

		// set own properties
		value = this.getProperties().getProperty(PROPERTYKEY_USE_ROWS,
				this.getPropertyDefaultValues().get(PROPERTYKEY_USE_ROWS));
		useRows = Boolean.parseBoolean(value);

		value = this.getProperties().getProperty(PROPERTYKEY_OPERATE_REFLEXIVE,
				this.getPropertyDefaultValues().get(PROPERTYKEY_OPERATE_REFLEXIVE));
		reflexive = Boolean.parseBoolean(value);

		value = this.getProperties().getProperty(PROPERTYKEY_OPERATION,
				this.getPropertyDefaultValues().get(PROPERTYKEY_OPERATION));
		if (!(value == null) && !value.isEmpty()) {
			operation = Operation.valueOf(value);
		}

		inputSeparator = this.getProperties().getProperty(PROPERTYKEY_INPUT_SEPARATOR,
				this.getPropertyDefaultValues().get(PROPERTYKEY_INPUT_SEPARATOR));
		outputSeparator = this.getProperties().getProperty(PROPERTYKEY_OUTPUT_SEPARATOR,
				this.getPropertyDefaultValues().get(PROPERTYKEY_OUTPUT_SEPARATOR));

		// Apply parent object's properties (just the name variable actually)
		super.applyProperties();
	}

}
