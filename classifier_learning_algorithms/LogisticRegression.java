import java.io.*;
import java.text.DecimalFormat;


/**
* This class is a stub for incrementally building a Logistic Regression model. 
* This class can be used for learning weights for regression model, reading/writing model weigts from/to a file
* Incremental update of the model is done by another class "IncrementalLearner" which is discarded to keep the originality of code
* Data instance "Example" class is not present here; has two property-- attributeValues & classValues
*/

public class LogisticRegression extends IncrementalLearner<Double>{

   private double learningRate;
   private double regularizationParameter;
   double [] weights;


   /**
      LogisticRegression constructor. 

    @param numFeatures is the number of features, user input
    @param learningRate is the learning rate, user input
    @param regularizationParameter is the regularization parameter, user input
    */

   public LogisticRegression(int numFeatures, double learningRate, double regularizationParameter){
       this.learningRate = learningRate;
       this.regularizationParameter = regularizationParameter;
       nbExamplesProcessed = 0;
       
       weights= new double [numFeatures+1]; 
       
       for(int i=0; i<weights.length; i++) {
		   	weights[i]= 0;
  		}
       
       try {
		this.readModel("./models/lr.model", nbExamplesProcessed);
		} catch (IOException e) {
			e.printStackTrace();
	}

   }


   /**
    This method will update the parameters of you model using the given example.

    @param example is a training example of two attributes: 
    example.attributeValues is an array for all the attribute value for example
    example.classValue is an integer for class indicator
    */

   @Override
   public void update(Example<Double> example){
       super.update(example);
      
       double [] temp= new double [weights.length];
       double probability=this.makePrediction(example.attributeValues);
       
       for(int j=0; j<weights.length-1; j++) {
	    	double x= example.attributeValues[j];
	    	DecimalFormat df = new DecimalFormat("#.####");      
	    	temp[j]=Double.valueOf(df.format(weights[j]-learningRate*((probability-example.classValue)*x-(regularizationParameter*weights[j])))) ;
       }
       
       temp[weights.length-1]= weights[weights.length-1]- learningRate*((probability-example.classValue));
       this.weights= temp;

   }


   /**
      Uses the current model to calculate the probability that an attributeValues belongs to class "1";

      @param example is a test attributeValues
      @return the probability that attributeValues belongs to class "1"
    */
   @Override
   public double makePrediction(Double[] example){
	
	   
	   double prediction=0.0;
	   double z=0;
	   for(int i=0; i<weights.length-1; i++) {
			z+= weights[i]*example[i]; 
		}
		
	   z+= weights[weights.length-1];
		prediction= sigmoid(z);
	  
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
       
	   	for(int i=0; i< this.weights.length; i++) {
	   		sb.append(this.weights[i]);
	   		sb.append(" ");
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
	        while ((strCurrentLine = objReader.readLine()) != null) {
	        	String[] splitLine = strCurrentLine.split(" ");
	        	
	        	for(int i=0; i<this.weights.length; i++) {
	        		this.weights[i]= Double.parseDouble(splitLine[i]);
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
    * HELPER METHOD: calculate the sigmoid value
    * @param x
    * @return sigmoid value of x
    */
   
   public static double sigmoid(double x) {
	    return (1/( 1 + Math.pow(Math.E,(-1*x))));
	}
	
}

