import java.io.*;
import java.util.*;

/**
* This class is a stub for VFDT(Very Fast Decision Tree):a decision-tree learning system based on Hoeffding trees learner
* This class can be use for building the tree
* Each Node of this tree is managed using VfdtNode class; see VfdtNode.java
* Incremental update of the model is done by another class which is discarded to keep the originality of code
* Data instance "Example" class is not present here; has two property-- attributeValues & classValues
*/
public class Vfdt extends IncrementalLearner<Integer>{

   private int[] nbFeatureValues;
   private double delta;
   private double tau;
   private double nmin;
   
   private VfdtNode root;
   
   List<VfdtNode> tree = new ArrayList<>();
   
   /**
      Vfdt constructor
      @param nbFeatureValues are nb of values of each feature.
		  e.g. nbFeatureValues[3]=5 means that feature 3 can have values 0,1,2,3 and 4.
      @param delta is the parameter used for the Hoeffding bound
      @param tau is the parameter that is used to deal with ties
      @param nmin is the parameter that is used to limit the G computations
    */

   public Vfdt(int[] nbFeatureValues, double delta, double tau, int nmin){
       this.nbFeatureValues = nbFeatureValues;
       this.delta = delta;
       this.tau = tau;
       this.nmin = nmin;

       nbExamplesProcessed = 0;
       int[] possibleFeatures = new int[nbFeatureValues.length];
       for(int i=0; i < nbFeatureValues.length; i++) possibleFeatures[i]=i;
       root = new VfdtNode(nbFeatureValues, possibleFeatures);

       try {
		this.readModel("./models/vfdt.model",0);
	} catch (IOException e) {
		e.printStackTrace();
	}
   }


   /**
    This method will update the parameters of the model using the given example.
    @param example is a training example
    */

   @Override
   public void update(Example<Integer> example){
       super.update(example);
       
       VfdtNode leaf=root.sortExample(example.attributeValues);

       leaf.incrementnijk(example);
       
       if((leaf.getLeafTotal() % nmin)==0 && (!leaf.isPure())) {
    	   
    	    double max=0.0;
    	   	double secondmax=0.0;
    	   	
    	   	double [] G= leaf.computeGainofPossibleFeature();
    	   	double [] Gduplicate= leaf.computeGainofPossibleFeature();
    	   	
    	   	Arrays.sort(G);
    	   	
    		int indexA=find(Gduplicate,G[G.length-1]);
    		
    		max=G[G.length-1];
    		secondmax=G[G.length-2];
    		
    		double e=calculateEpsilon(nbFeatureValues[indexA], leaf.getLeafTotal());
    		
    		if(max!=leaf.gainTheta() && (max-secondmax)>e || e<tau ) {
    			VfdtNode [] child =new VfdtNode [nbFeatureValues[indexA]];
        		for(int i=0; i<child.length; i++) {
        			int [] possibleSplitFeatures= leaf.reducedPossibleSplitFeatures(indexA);
        			child[i]= new VfdtNode(nbFeatureValues, possibleSplitFeatures);
        		}
        		leaf.addChildren(indexA, child);
    		}
    		
    		
       }

   }
   
   public double calculateEpsilon(int R, int n) {
       return ((Math.pow(R, 2)*Math.log(1/delta))/(2*n));
   }



   /**
      Uses the current model to calculate the probability that an attributeValues belongs to class "1";

      @param example is a the test instance to classify
      @return the probability that attributeValues belongs to class "1"
    */
   @Override

   public double makePrediction(Integer[] example){

	double prediction = 0;

	VfdtNode leaf=root.sortExample(example);
	
    prediction=leaf.computeProbability();
	return prediction;
   }

   /**
    * Writes the current model to a file.
    *
    * The written file can be read in with readModel.
    *
    * @param path the path to the file
    * @throws IOException
    */
   @Override
   public void writeModel(String path) throws IOException {
	    BufferedWriter br = new BufferedWriter(new FileWriter(path));
  		StringBuilder sb = new StringBuilder();
  		
  		int node=0;
  		
  		Stack<VfdtNode> stack =printLevelOrder();
  		int total=stack.size();
  		
  		sb.append(total);
  		sb.append("\n");
  		
	   	while(!stack.isEmpty()) {
	   		VfdtNode tempNode= stack.pop();
	   		if(tempNode.isLeaf()) {
	   			tempNode.id=node;
	   			sb.append(tempNode.id);
	   			sb.append(" ");
	   			
	   			sb.append("L");
	   			sb.append(" ");
	   			
	   			sb.append("pf:[");
	   			for(int i=0; i<tempNode.possibleSplitFeatures.length; i++) {
	   				sb.append(tempNode.possibleSplitFeatures[i]);
	   				sb.append(",");
	   			}
	   			sb.append("] ");
	   			
	   			sb.append("nijk:");
	   			sb.append(tempNode.print_nijk());
	   		
	   			node++;
	   		}
	   		else {
	 
	   			tempNode.id=node;
	   			sb.append(tempNode.id);
	   			sb.append(" ");
	   			
	   			sb.append("D");
	   			sb.append(" ");
	   			
	   			sb.append("f:");
	   			sb.append(tempNode.getSplitFeatures());
	   			sb.append(" ");
	   			
	   			sb.append("ch:[");
	   			VfdtNode [] children= tempNode.getChildren();
	        	for(int i=0; i<children.length; i++) {
	        		sb.append(children[i].id);
	        		sb.append(",");
	        	}
	   			sb.append("]");
	   			
	   			node++;
	   			
	   		}
	   		sb.append("\n");
	   	}
	   	br.write(sb.toString());
	   	br.close();
   }


   /**
    * Reads in the model in the file and sets it as the current model. Sets the number of examples processed.
    *
    * @param path the path to the model file
    * @param nbExamplesProcessed the nb of examples that were processed to get to the model in the file.
    * @throws IOException
    */
   @Override
   public void readModel(String path, int nbExamplesProcessed) throws IOException {
       super.readModel(path, nbExamplesProcessed);
       
       
       BufferedReader objReader = null;
       try {
	        String strCurrentLine;
	        objReader = new BufferedReader(new FileReader(path));
	        
	        objReader.readLine(); // skip header
	       
	        
	        while ((strCurrentLine = objReader.readLine()) != null) {
	        	String[] splitLine = strCurrentLine.split(" ");
	        	
	        	if(splitLine[1].equals("L")) {
	        		
	        		int [] possibleFeatures=convertStringPF(splitLine[2]);
	        		VfdtNode leaf= new VfdtNode(nbFeatureValues,possibleFeatures);
	        		
     		
	        		int [][] nijk= convertStringNIJK(splitLine[3]);
	        		
	        		for(int i=0; i<nijk.length; i++) {
	        			int [] value= new int [4];
	        			value= nijk[i];
	        			if(value.length==4)
	        				leaf.setnijk(value);
	        		}
	        		tree.add(leaf);
	        		
	        		root=leaf;
	        		
	        	}
	        	else if(splitLine[1].equals("D")) {
	        		
	        		int [] possibleFeature= null;
	        		VfdtNode decision= new VfdtNode(nbFeatureValues,possibleFeature);
	        		
	        		int splitFeature = Integer.parseInt(stripcolon(splitLine[2]));
	        		
	        		int [] childIndex=convertStringPF(splitLine[3]);
	        		VfdtNode [] child =new VfdtNode [childIndex.length];
	        		
	        		for(int i=0; i<child.length; i++) {
	        			child[i]= tree.get(childIndex[i]);
	        		}
	        		decision.addChildren(splitFeature, child);
	        		
	        		tree.add(decision);
	        		root=decision;
	        	}
	        }

       } catch (IOException e) {
    	   System.out.println("No file to read");
       } finally {
	        try {
	        	if (objReader != null)
	        		objReader.close();
	        } catch (IOException ex) {
	        	ex.printStackTrace();
	        }
       
       }
   }
   
  
   /**
    * Return the visualization of the tree.
    * @return Visualization of the tree
    */
   public String getVisualization() {
       return root.getVisualization("");
   }
   
   public Stack<VfdtNode> printLevelOrder()  
   { 
       Queue<VfdtNode> queue = new LinkedList<VfdtNode>(); 
       Stack<VfdtNode> stack = new Stack<VfdtNode>(); 
       queue.add(root); 
       while (!queue.isEmpty())  
       { 
 
    	   VfdtNode tempNode = queue.poll(); 
           stack.push(tempNode);
    	   
           /*Enqueue children */
           if (tempNode.isLeaf() != true) {
        	   VfdtNode [] children= tempNode.getChildren();
        	   for(int i=0; i<children.length; i++) {
        		   queue.add(children[i]);
        	   }
           } 
       } 
       return stack;
   } 

  
  /**
   *@category HELPER METHODS
   */
  
  public int find(double[] array, double value) {
	    for(int i=0; i<array.length; i++) 
	         if(array[i] == value)
	             return i;
	    return -1;
	}
  

  public static int[] convertStringPF(String str) {
	   	String pf = stripNonDigits(str);
		
		String [] possibleFeatures= pf.split(",");
		int [] result= new int [possibleFeatures.length];
		
		
		for(int i=0; i<possibleFeatures.length; i++) {
			String temp=stripcolon(possibleFeatures[i]);
			if(temp.matches(".*\\d.*")) {
				result[i]=Integer.parseInt(temp);
			}
			else
				possibleFeatures= removeTheElement(possibleFeatures, i);
		}
	   return result;
  }
  
  
  public static int[][] convertStringNIJK(String str) {
	   	String pf = stripNonDigits(str);
	   	pf= pf.substring(1);
		
		String [] possibleFeatures= pf.split(",");
		int [][] result= new int [possibleFeatures.length][4];
		
		for(int i=0; i<possibleFeatures.length; i++) {
				String [] tempArray= possibleFeatures[i].split(":");
				for(int j=0; j<tempArray.length; j++) {
					String temp2=stripcolon(tempArray[j]);
					if(temp2.matches(".*\\d.*")) {
						result[i][j]=Integer.parseInt(temp2);
					}
					else {
						tempArray= removeTheElement(tempArray, j);
					}
				}
		}
	   return result;
 }
  
  
  public static String stripNonDigits(
       final CharSequence input /* inspired by seh's comment */){
	   	final StringBuilder sb = new StringBuilder(input.length() /* also inspired by seh's comment */);
	   	for(int i = 0; i < input.length(); i++){
	       final char c = input.charAt(i);
	       if((c > 47 && c < 58) || (c==44) || (c==58)){
	           sb.append(c);
	       }
	   	}
  return sb.toString();
  }
  
  
  public static String stripcolon(
	        final CharSequence input /* inspired by seh's comment */){
		   	final StringBuilder sb = new StringBuilder(input.length() /* also inspired by seh's comment */);
		   	for(int i = 0; i < input.length(); i++){
		       final char c = input.charAt(i);
		       if((c > 47 && c < 58)){
		           sb.append(c);
		       }
	   }
	   return sb.toString();
  }
  
  public static String[] removeTheElement(String[] nijk,  int index) {
	   
		if (nijk == null || index < 0 || index >= nijk.length) 
			return nijk; 
		
		String  [] anotherArray = new String [nijk.length - 1]; 
		for (int i = 0, k = 0; i < nijk.length; i++) { 
			if (i == index) 
				continue; 
			anotherArray[k++] = nijk[i]; 
		} 
		return anotherArray; 
	} 

}




