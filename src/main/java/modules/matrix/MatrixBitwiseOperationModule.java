
package modules.matrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
//import java.util.TreeSet;

import common.parallelization.CallbackReceiver;
import models.NamedFieldMatrix;
import modules.CharPipe;
import modules.InputPort;
import modules.ModuleImpl;
import modules.OutputPort;
import modules.matrix.morph.RestructMorphologicalClasses;
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
	private static int best_nr=0;// greatest nr of following (adjacent) strings
	//private static int difference=0;
	static HashMap<String,Integer> classesHashMap;
	static HashMap<String, Integer> competitionHashMap;
	// list of classes generated
	static ArrayList<MatrixBitwiseOperationClassSelection> listOfClasses=
			new ArrayList<MatrixBitwiseOperationClassSelection>();
	// competitionlist is a list with competing elements, s. class competition and its 
	// method readEvalHashMap
	static ArrayList<MatrixBitwiseOperationHelpCompetingElementGroup> competitionList=
			new ArrayList<MatrixBitwiseOperationHelpCompetingElementGroup>();
	
	
	// Main method for stand-alone execution
	public static void main(String[] args) throws Exception {
		ModuleRunner.runStandAlone(MatrixBitwiseOperationModule.class, args);
	}
	
	
	
	
	//---------- JR-----xtensions---------------------------------------------------------
	private class Best {
		void selectBest(BitSet product, /*BitSet nrBits,*/ String name1,String name2) {
			//if (product.cardinality()==difference) {
				//int nr=nrBits.cardinality();
				int nr=product.cardinality();
				if (nr>best_nr) {
					best_nr=nr;
					best_n1=name1;
					best_n2=name2;
					//best_BitSet=nrBits;
					best_BitSet=product;
				}
			//}
		}// selectBest
		
		// for test only; prints out best (following (adjacent)) string(s)
		void printBest(NamedFieldMatrix matrix,PrintWriter writer) {
			writer.println();
			writer.println("Best: "+best_n1+ "  " +best_n2+" "+best_BitSet.cardinality());
			for (int i=0;i<best_BitSet.length();i++){
				if(best_BitSet.get(i)){
					String colName=matrix.getColumnName(i);
					writer.println(colName);
				}
			}
		}
		
		
	}//Best
	
	/******************************
	private class Competition {
		
		
		void readEvalHashMap(BufferedReader r,PrintWriter writer) throws Exception{
			// produces a map which consists of a prefix string for competing strings 
			// competing strings are strings which might be separated differently: Belg|ae vs Belga|e
			competitionHashMap=new HashMap<String,Integer>();
			String line;
			// competitions are identified by competitionIdent; competing elements
			// elements have equal competitionIdent, e.g. ama+re, amar+e
			int competitionIdent=0;
			MatrixBitwiseOperationHelpCompetingElementGroup competitionElement;
			while((line=r.readLine())!=null){
				// skip empty lines (i.e. lines which don't contain pipes("|" or dollars ("$")
				if (line.length()>0){
					// '$' marks end of String. ';' separates competing strings
					String[] competionStr= line.split("\\$;?");
					int nrCompetitions=competionStr.length;
					if(nrCompetitions>1){
						writer.println
						("readEvalHashMap: "+line+" nrCompetitions: "+nrCompetitions+" ");
						competitionIdent++;
						
						competitionElement=new MatrixBitwiseOperationHelpCompetingElementGroup();
						for (int i=0;i<nrCompetitions;i++){
							// binary separation of competing string(s)
							// save (different) prefixes
							String prefix[]=competionStr[i].split("\\|");
							//System.out.println
							//("readEvalHashMap prefix: "+prefix[0]);
							competitionHashMap.put(prefix[0], new Integer(competitionIdent));
							competitionElement.add(prefix[0]);
						}
						competitionList.add(competitionElement);
					}					
					
				}
				
			}			
			
		}// readEvalHashMap
		
		
		private boolean checkcompetition(String name1,String name2,PrintWriter writer) throws Exception
		{
			// for key name1 and key name2 get value and compare value with 'equals'
			if ((competitionHashMap.get(name1)!=null) &&
					(competitionHashMap.get(name1).equals(competitionHashMap.get(name2))))
				{
					writer.println("checkcompetition competition name1: "+name1+ " name2: "+name2);
					return true;
				}
				else return false;	
				
		}//checkcompetition
		
	}// class competition
	
	*/
		
	
	
	private class MorphResult {
		
		private ArrayList<Integer> columnSum;
		private ArrayList<Integer> rowSum;
		private ArrayList <Double>resultList=null;
		//NamedFieldMatrix resultMatrix;
		private int nrBitsInRow=0;
		private int numberOfClasses=0;
		Set<String> morphemes;
		
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
		
		
		
		void morphProcess(NamedFieldMatrix namedFieldMatrix,Set<String> names,
				PrintWriter writer) {
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
				writer.println("morphProcess "+name1+" \t "+ res);
			}
			
			// output, test
			//for (String name:names){
			//	System.out.println(name+"  "+ this.resultList.get(row) *
			//			this.rowSum.get(name));
			//}
			
		}//morphProcess
		
		*/
		
		/*
		private boolean checkDifferenceElementToClass(Set<String> names, String name2check, NamedFieldMatrix distanceMatrix, int numberOfClasses, int maxDif) {
			// if an element name2check is found which is similar to an element of class classNr then name2check must not be more 
			// different from the other members of class classNr than defined by maxDif
			// By this a certain similarity of all class members is maintained
			
			//search all elements of class classNr
			for (String name:names){
				if((Integer)classesHashMap.get(name) ==numberOfClasses){
					// detect difference of name and name2check
					if (distanceMatrix.getValue(name,name2check)>maxDif) return false;					
				}
				
			}
			
			return true;
		}
		
		*/
		
	/*	private boolean checkDifferenceClass1Class2(Set<String> names, NamedFieldMatrix distanceMatrix, int classNr1,
				int classNr2, int maxDif){
			
			for (String name:names){
				if((Integer)classesHashMap.get(name) == classNr1){
					if(!checkDifferenceElementToClass(names,name,distanceMatrix,classNr2,maxDif)) return false;
				}
			
			}
			return true;
		}

	*/	
	
	/*	void classBuilding(NamedFieldMatrix distanceMatrix,Set<String> names,
			int maxSimilarity// maxSimilarity is greatest nmber of following (adjacent)strings;
			//it is best_nr in calling method 
			)
		
			{			
			classesHashMap = new HashMap<String,Integer>();
			
				int minSimilarity=maxSimilarity/3;
				// init HashMap classes: value 0, i.e. no class at begin
				for (String name:names){
					classesHashMap.put(name, numberOfClasses//0
					);			
				}
			
				for (int sim=maxSimilarity;sim>=minSimilarity;sim--) {				
					for (String name1:names){
						for (String name2:names){
							if(distanceMatrix.getValue(name1, name2)==sim){
								// no class for name1
								if((Integer)classesHashMap.get(name1) ==0){
									// no class for name2
									if (classesHashMap.get(name2)==0){
									// new class, new numberOfClasses
										numberOfClasses++;
										//System.out.println("classBuilding: "+numberOfClasses);
										classesHashMap.put(name1,numberOfClasses);
										classesHashMap.put(name2,numberOfClasses);
									} else {
										// class found for name2, numberOfClasses of name2 is given to name1
										classesHashMap.put(name1,classesHashMap.get(name2));
									}									
									
								} else if (classesHashMap.get(name2)==0){
									// class found for name1, numberOfClasses of name1 is given to name2
									classesHashMap.put(name2,classesHashMap.get(name1));
								
								} else {
									// set all numberOfClassess from name2 to name1
									//int nrName1=classes.get(name1);
									int nrName2=classesHashMap.get(name2);
									// get all name2 and change to nrName1
									for (String name:names){
										if(classesHashMap.get(name)==nrName2) {
											classesHashMap.put(name2,nrName2);
										}
									}
								}
								
							}//if(distanceMatrix.getValue(name1, name2)==sim){
						}//for (String name1:names){				
					}//for (String name2:names){
				}//for (int sim=maxSimilarity;sim<=minSimilarity;sim--)
			
			
			
		}//classBuilding
*/		
		
/*
		private void selectionOfClasses(PrintWriter writer){
			Iterator<MatrixBitwiseOperationClassSelection> classIterator = 
			listOfClasses.iterator();
			while (classIterator.hasNext()) {
				MatrixBitwiseOperationClassSelection clss=classIterator.next();
				clss.competition(listOfClasses,competitionList,
				classesHashMap/ *<String,Integer>* /,
				// s.above: String is prefix, Integer identifies different prefixes which are
				// competing
				competitionHashMap/ *<String, Integer>* /,
				writer,this.morphemes);
			}
		}
*/		
		
/*		void result(HashMap <String,Integer> resultMap,Set<String> names,
				PrintWriter writer){
			
			writer.println();
			writer.println("result numberOfClasses: "+numberOfClasses);
			writer.println();
			// classes found
			listOfClasses=new ArrayList<MatrixBitwiseOperationClassSelection>();
			
			for (int classNr=1;classNr<=numberOfClasses;classNr++){
				MatrixBitwiseOperationClassSelection classElement=
						new MatrixBitwiseOperationClassSelection(classNr);
				//boolean notWritten=true;
				for (String name:names){
					// select elements of class
					if(resultMap.get(name)==classNr) {
						classElement.members.add(name);
						//if(notWritten){System.out.print("-----classIndex "+
						//		classIndex+"-----morphfollowing ");
						//	notWritten=false;
							// morphemes following: Caveat:ALL morph, not only the subset of common
							// morphemes
							double[]row=inMatrix.getRow(name);
							for (int i=0;i<row.length;i++){
								if(row[i]!=0){
									String colName=
									inMatrix.getColumnName(i).replaceAll("\\<|\\>|\\|", "");
									//System.out.print(colName+" ");
									classElement.adjacentMembers.add(colName);
								}//if
							}//for
							//System.out.println();
						//}// notWritten
						// write name 
						//System.out.println(name);
					}
				}//for (String name:names)
				classElement.countWriteClass(writer,"result",this.morphemes,true);
				listOfClasses.add(classElement);
			}//for (int classIndex=1;classndex<=classes;classIndex++)
			
			selectionOfClasses(writer);			
		
			writer.println();
			writer.println(" no Class found");
			// no classification
			for (String name:names){
				if(resultMap.get(name)==0) {
					// write name, no class 
					writer.println(name);
				}
			}//for (String name:names){
			
		}// result
		
		// makeLex reads a dictionary of morphemes (e.g. LateinLexikonList) as HashSet
		// it may be used to evaluate results of morphologic analysis
		void makeLex(PrintWriter writer){
			writer.println("makeLex");
			String pathFile="c:/users/rols/workspace/LateinLexikonList.txt";
			String line="";
			this.morphemes = new HashSet<String>();
			try {
				BufferedReader in= new BufferedReader(new FileReader(pathFile));
				while ((line=in.readLine()) !=null)//&&(i<100)) 
				 {
					//i++;
					String parts[]=line.split("\\+");
					writer.println("line "+line+ " parts: "+parts[0]);
					this.morphemes.add(parts[0]);					
					
				}
				in.close();	   
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			if (morphemes.contains("abac"))System.out.println("abac ok");
		}//matchLex
		
	*/
		
	}// class MorphResult
	
//---------End jr---------------------------------------------------------------------------
	
	private static final String MODULE_DESC = "Module interprets either rows or columns of an input matrix as binary bitsets"
			+ " and performs symmetrical operations (AND, OR, XOR) on these bitsets. Output"
			+ " is again a matrix showing how many bits are set for each combination of"
			+ " rows/columns after the operation is performed.";

	private final static String INPUT_ID = "Input Matrix";
	private final static String INPUT_DESC = "[text/csv] An input csv table. Header row and first column are expected to contain Strings as labels. All other fields are assumed to be blank or contain numerical values.";

	private final static String INPUTCompetition_ID="Input Competition";
	private final static String INPUTCompetition_DESC="Competition entries \';\' separated";
	
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
		
		InputPort inputCompetition = new InputPort(INPUTCompetition_ID, INPUTCompetition_DESC, this);
		inputCompetition.addSupportedPipe(CharPipe.class);
		super.addInputPort(inputCompetition);

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
		PrintWriter writer=null;
		try{
		    writer = new PrintWriter("C:\\Users\\rols\\Help.txt", "UTF-8");
			   
		} catch (IOException e) {
			   e.printStackTrace();
		}
		
		
		// jr
		Operation operation=Operation.valueOf("AND");
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
			
			//JR  test competition
			// aims at selection of elements differently separated,
			// e.g. latin Belg|ae or Belga|e
			MatrixBitWiseOperationCompetition competition=null;
			if (getInputPorts().get(INPUTCompetition_ID)!=null){			
				competition= new MatrixBitWiseOperationCompetition();
				competition.readEvalHashMap(
				new BufferedReader(getInputPorts().get(INPUTCompetition_ID).getInputReader()),
				writer);
			}
			//JR End competition
			
			// build a matrix containing the result of applying the operation to
			// each pair of BitSets
			NamedFieldMatrix outMatrix = new NamedFieldMatrix();
			BitSet operand1 = null;
			BitSet operand2 = null;
			BitSet resultBitSet = null;
			Double value = null;
			// 2 nestested loops (for (String name1 : names), for (String name2 : names) 
			// in order to find best adjacent pair (name1, name2)
			// comparision is done by selectBest
		    
			//evalMatrixProposals eval=null;
			// write competition; write name1 (outer for loop) only once,
			// so name1ForCompetition notes that name1 was already written
			String name1ForCompetition="";
			for (String name1 : names) {
				writer.print("process name1: "+name1+ " ");
				//eval=evalHashMap.get(name1);    //get(index);
				//if(eval !=null) {
				//	
				//	System.out.print("\t "+" evalHashMap: "+eval.competition);
				//}
				//System.out.println();
				
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
					// to do operation should be AND
					resultBitSet = performOperation(operand1, operand2, operation);
					outMatrix.setValue(name1, name2, (double) resultBitSet.cardinality());
					if(resultBitSet.cardinality()>0){
						//System.out.println(name1+" "+name2+" "+ resultBitSet.cardinality());
					}
					
					
					//---------------JR--------------------------
					best.selectBest(resultBitSet,/*operand1,*/name1,name2);
					//---------------JR--------------------------
					
					//----test only jr
					if (competition!=null)
						//System.out.println("competition !=null");
						
						if (competition.checkcompetition(name1, name2,writer)) {
							if(!name1ForCompetition.equals(name1)) {
								
								name1ForCompetition=name1;
								writer.print("competition competition:"+name1+"  ");
							}
							writer.print(name2+" ");
						};
						
					
					
				} //for (String name2 : names) 
				if(name1ForCompetition.equals(name1))writer.println();
			}//for (String name1 : names)
			
			
			
			//test jr------------
			 best.printBest(inMatrix,writer);
			//-----------------------------------------------
			 
			
			 MatrixDynamicMorphClustering matrixDynamicMorphClustering =
					 new MatrixDynamicMorphClustering();
			 try {
				 NamedFieldMatrix restructMatrix=
						 matrixDynamicMorphClustering.restruct(inMatrix,competition, writer);
			 }
			 catch(Exception e){
				 writer.close();
				 System.out.println
				 ("error after catch Exception in MatrixBitwiseOperationModule.process");
				 throw e;
			 }
			//
			 
			
			/*
			MorphResult morphResult=new MorphResult();
			morphResult.makeLex(writer);
			//HashMap<String,Integer>resultMap=
			morphResult.classBuilding(outMatrix,names,best_nr);
			morphResult.result(classesHashMap, names,writer); 
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
			
			*/
			 
		} catch (Exception e) {
			result = false;
			throw e;
		} finally {
			if (inputReader != null) {
				inputReader.close();
			}
			this.closeAllOutputs();
		}
		writer.close();
		return result;
	}

	// performs the operation on both BitSets and returns a new BitSet
	// containing the result
	private static BitSet performOperation(BitSet op1, BitSet op2, Operation op) {
		// we need a clone for the operation because it is destructive
		BitSet result = (BitSet) op1.clone();

		switch (op) {
		case AND:
			result.and(op2);	//System.out.print("AND ");
			break;
		case OR:
			result.or(op2); //System.out.print("OR ");
			break;
		case XOR:
			result.xor(op2);//System.out.print("XOR  ");
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
