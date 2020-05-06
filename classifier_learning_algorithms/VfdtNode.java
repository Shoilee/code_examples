import java.util.Arrays;


/**
* This class is a stub for VFDT. 
*/
public class VfdtNode{

   private VfdtNode[] children; /* child children (null if node is a leaf) */

   public final int[] possibleSplitFeatures; /* The features that this node can split on */

   private int splitFeature; /* splitting feature */

   private int[][][] nijk; /* instance counts (see paper) */
   
   private int [] nbFeatureValues;
   
   int nbSplits;
   
   public int id;
   
   private int nl;
   

   /**
      Create and initialize a leaf node.

      THIS METHOD IS REQUIRED.
      
    @param nbFeatureValues are the nb of values for each feature in this node.
                           If a feature has k values, then the values are [0:k-1].
   */
   public VfdtNode(int[] nbFeatureValues, int[] possibleSplitFeatures){
       this.possibleSplitFeatures = possibleSplitFeatures;
       this.nbFeatureValues=nbFeatureValues;
       children = null;
       
       nbSplits=0;

       //initializing nijk
       nijk = new int[nbFeatureValues.length][][];
       for(int i=0; i<nbFeatureValues.length; i++) {
    	   nijk[i] = new int[nbFeatureValues[i]][];
    	   for(int j=0; j<nbFeatureValues[i]; j++) {
    		   nijk[i][j]=new int[2];
    	   }
       }
       //System.out.println("Number of feature"+ nijk.length+"; Number of value"+ nijk[0].length);
       
       for(int i=0; i<nijk.length; i++) {
    	   for(int j=0; j<nijk[i].length; j++) {
    		   for(int k=0; k<nijk[i][j].length; k++) {
    			   nijk[i][j][k]=0;
    		   }
    	   }
       }
       
       
       
   }
   
   public void setnijk(int[] value) {
	   nijk[value[0]][value[1]][value[2]]=value[3];
   }
   
   public void incrementnijk(Example<Integer> example) {
	   for(int i=0; i<possibleSplitFeatures.length; i++) {
		   int feature=possibleSplitFeatures[i];
		   nijk[feature][example.attributeValues[feature]][example.classValue]++;
	   }
   }
   

   /** 
	Turn a leaf node into a internal node.
	
	@param splitFeature is the feature to test on this node.
	@param nodes are the children (the index of the node is the value of the splitFeature).
   */
   public void addChildren(int splitFeature, VfdtNode[] nodes){
       if (nodes==null) throw new IllegalArgumentException("null children");
       nbSplits++;
       
       this.splitFeature= splitFeature;
       this.children=nodes;
       
   }
   
   public boolean isLeaf() {
	   if(children==null)
		   return true;
	   return false;
   }
   
   public VfdtNode [] getChildren() {
	   return children;
   }
   
   public int getSplitFeatures() {
	   return splitFeature;
   }
   
   public static int[] removeTheElement(int[] arr,  int index) {
		if (arr == null || index < 0 || index >= arr.length) 
			return arr; 
	
		int[] anotherArray = new int[arr.length - 1]; 
		for (int i = 0, k = 0; i < arr.length; i++) { 
			if (i == index) 
				continue; 
			anotherArray[k++] = arr[i]; 
		} 
		
		return anotherArray; 
	} 

   /** 
	Returns the leaf node corresponding to the test attributeValues.

	@param example is the test attributeValues to sort.
   */
   public VfdtNode sortExample(Integer[] example){

	VfdtNode leaf; // change this
	
	if(children==null) {
		leaf=this;
		//System.out.println("I am leaf");
		return leaf;
	}else {
		//System.out.println("I am root");
		int value= example[this.splitFeature];
		leaf=children[value];
		leaf=leaf.sortExample(example);
	}
	return leaf;
   }

   
   /**
    * Determine is leaf has only one class or not
    * @return boolean value 
    */
   
   public boolean isPure() {
	     int [] classCount= new int [] {0 ,0};
		 
		 int featureValue=this.possibleSplitFeatures[0];
		 for(int k=0;k<classCount.length; k++) {
			 for(int j=0; j<this.nijk[featureValue].length; j++) {
				 classCount[k]+=this.nijk[featureValue][j][k];
			 }
		 }
		 
		 if(classCount[0]==0 || classCount[1]==0) 
			 return true;
		 return false;
   }


   /**
    * Determine total leaf nodes for two classes
    * @return an interger 
    */
   
   public int getLeafTotal() {
	     int [] classCount= new int [] {0 ,0};
		 
		 int featureValue=this.possibleSplitFeatures[0];
		 for(int k=0;k<classCount.length; k++) {
			 for(int j=0; j<this.nijk[featureValue].length; j++) {
				 classCount[k]+=this.nijk[featureValue][j][k];
			 }
		 }
		 
		 int total= classCount[0]+classCount[1];
		 return total;
 }
   

   /**
    * Compute G for all possibleFeatures
    * @return array of information gain value 
    */
   
   double [] computeGainofPossibleFeature() {
	   double [] gainArray= new double [nbFeatureValues.length];
	   
	   for(int i=0; i<gainArray.length; i++) {
		   gainArray[i]=0.0;
	   }
	   
	   for(int i=0; i<possibleSplitFeatures.length; i++) {
		   gainArray[possibleSplitFeatures[i]]=splitEval(possibleSplitFeatures[i]);
	   }
	   
	   return gainArray;
   }
   

   /**
      Split evaluation method (function G in the paper)
      
      Compute a splitting score for the feature featureId.
      For now, we'll use information gain, but this may be changed.
      You can test your code with other split evaluations, but be sure to change it 
      back to information gain in the submitted code and for the experiments with default values.
      
      @param featureId is the feature to be considered. 
   */

   public double splitEval(int featureId){
	return informationGain(featureId, nijk);
   }

   /**
      Compute the information gain of a feature for this leaf node. 
      @param featureId is the feature to be considered. 
      @param nijk are the instance counts.
   */ 
      
   public static double informationGain(int featureId, int[][][] nijk){
	     
	     int total=0;
	     int [] value= new int [nijk[featureId].length];
	     int [] classes= new int [nijk[featureId][0].length];
	     
	     for(int j=0; j<nijk[featureId].length; j++) {
	  	   value[j]=0;
	     }
	     
	     for(int k=0; k<nijk[featureId][0].length; k++) {
	  	   value[k]=0;
	     }
	     
	     for(int j=0; j<nijk[featureId].length; j++) {
	  	   for(int k=0; k<nijk[featureId][j].length; k++) {
	  		   value[j]+=nijk[featureId][j][k];
	  		   total+=nijk[featureId][j][k];
	  		   classes[k]+=nijk[featureId][j][k];
	  	   }
	     }
	    if(total==0){
		   return 0.0;
		}else{ 
	     double ig = 0.0;
	     for(int k=0; k<classes.length; k++) {
	    	 double prob=(double)classes[k]/total;
	  	     ig-=(prob*log(prob, 2));
	     }
	     
	     
	     for(int j=0; j<nijk[featureId].length; j++) {
	  	   double entropy=0.0;
	  	   for(int k=0; k<nijk[featureId][j].length; k++) {
	  		   double prob;
	  		   if(value[j]==0) {
	  			   prob=0.0;
	  		   }else {
	  			 prob=((double)nijk[featureId][j][k]/value[j]);
	  		   }
	  		   //System.out.println("Feature value:"+ j+ "class-"+k+ " probability:"+ prob);
	  		   entropy-=prob*log(prob, 2);
	  		 //System.out.println("Entropy:"+ entropy);
	  	   }
	  	   
	  	   
	  	   entropy= entropy*((double)value[j]/total);
	  	   ig=ig-entropy;
	     }

	     return ig;
		}
	 }
   
	 
	 static double log(double x, int base){
		 if(x==0) 
			 return 0;
	     return (Math.log(x) / Math.log(base));
	 }
	 
	 
	 
	 /**
     Compute the probability of class "1" on this node/leaf

     @return probability value; 
     */ 
	 
	 public double computeProbability() {
		 double probability=0.0;
		 
		 int [] classCount= new int [] {0 ,0};
		 
		 int featureValue=this.possibleSplitFeatures[0];
		 
		 for(int k=0;k<classCount.length; k++) {
			 for(int j=0; j<this.nijk[featureValue].length; j++) {
				 classCount[k]+=this.nijk[featureValue][j][k];
			 }
		 }
		 
		 if((classCount[1]+classCount[0])==0) 
			 probability=0.5;
		 else 
			 probability=(double)classCount[1]/(double)(classCount[1]+classCount[0]); 

		 return probability;
	 }
	 
	 /**
     Compute the gain for "null" attribute

     @return G value for the current leaf; 
     */ 
	 
	 public double gainTheta() {
		 //double probability=0.0;
		 
		 int [] classCount= new int [] {0 ,0};
		 
		 int featureValue=this.possibleSplitFeatures[0];
		 
		 for(int k=0;k<classCount.length; k++) {
			 for(int j=0; j<this.nijk[featureValue].length; j++) {
				 classCount[k]+=this.nijk[featureValue][j][k];
			 }
		 }
		 
		 int total=classCount[0]+classCount[1];
		 double ig = 0.0;
		 
	     for(int k=0; k<classCount.length; k++) {
	    	 double prob=(double)classCount[k]/total;
	  	     ig-=(prob*log(prob, 2));
	     }
		
		 return ig;
	 }
	 
	 
	 
	 /**
	  * 
	  * @param index
	  * @return possibleSplitFeatures for children
	  */
	 public int [] reducedPossibleSplitFeatures(int index) {
		 return removeTheElement(possibleSplitFeatures, index);
	 }
	 
	 
   /**
    * Return the visualization of the tree.
    * @return Visualization of the tree
    */
   public String getVisualization(String indent){
       if (children==null){
           return indent+"Leaf: "+print_nijk()+"\n";
       }
       else {
           String visualization= "";
           for (int v = 0; v<children.length; v++) {
               visualization += indent + splitFeature + "="+v+":\n";
               visualization += children[v].getVisualization(indent + "| ");
           }
           return visualization;
       }
   }
   
   public String print_nijk(){
		 String str="[";
		 for(int i=0; i<nijk.length; i++) {
			 for(int j=0; j<nijk[i].length; j++) {
				 for(int k=0; k<nijk[i][j].length; k++) {
					 if(nijk[i][j][k]!=0) {
						 str+=i+":"+j+":"+k+":"+nijk[i][j][k]+",";
					 }
				 }
			 }
		 }
		 
		 str+="] ";
	       return str;
	   }

}
