 package modules.matrix;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import models.NamedFieldMatrix;



public class MatrixBitwiseOperationTreeNodeCodeGenerationElement {
        String code;
        int count;
        MatrixBitWiseOperationTreeNodeElement nodeElement;
       
       
        // sorts list by code which is generated in
        // MatrixBitWiseOperationTreeNodeElement.walkForCodeGeneration
        static public void sort
        (ArrayList<MatrixBitwiseOperationTreeNodeCodeGenerationElement> list){
               
                // Sorting
                Collections.sort(list,
                new Comparator<MatrixBitwiseOperationTreeNodeCodeGenerationElement>() {
                        @Override
                    public int compare(MatrixBitwiseOperationTreeNodeCodeGenerationElement
                            el1,MatrixBitwiseOperationTreeNodeCodeGenerationElement el2)
                        {
                                        return  el1.code.compareTo(el2.code);
                        }
                    });
               
        }//sort


        static public void printList
        (ArrayList<MatrixBitwiseOperationTreeNodeCodeGenerationElement> list,
                        PrintWriter p,NamedFieldMatrix namedFieldMatrix,String message){
               
                // 
                p.println("********************PrintList: "+message);
                for (int i=0;i<list.size();i++){
                        MatrixBitwiseOperationTreeNodeCodeGenerationElement el=list.get(i);
                        if (el.nodeElement !=null)
                                if (el.nodeElement.child1==null){
                                        p.print(el.code+";  ");
                       
                       
                                        String deselect="";
                                        if(el.nodeElement.deselect) deselect=" deselected";
                                       
                                        p.println(namedFieldMatrix.getRowName(el.nodeElement.fromNamedFieldMatrixRow)
                                                +deselect);
                        }       
                        //else p.println();
                       
                }
               
        }//printList
       
       
       

}