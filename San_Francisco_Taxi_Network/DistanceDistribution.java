/**
* Description of this file can be found in San_Francisco_Taxi_Network.pdf
*/


import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.storage.StorageLevel;

import scala.Tuple2;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;

public class DistanceDistribution {
    
	//Co-ordinate boundary for San francisco city
	private static double north=38.2033  ;
	private static double south=37.1897  ;
	private static double east=-122.6445 ;
	private static double west=-121.5871 ;
	
	//Function to change degree coordinate to Radian
	public static double toRadian(double value) {
		return value * (Math.PI/ 180); 
	}
	

	public static void main(String[] args) throws Exception  {
		if (args.length < 2) {
			System.err.println("Usage: DistanceDistribution <inputfile> <outputdirectory>");
			System.exit(1);
		 }
		 String inputPath = args[0];
		 System.out.println("args[0]: <file>="+args[0]);
		 
		 String outputDirectory= args[1];
		 System.out.println("args[1]: <directory>="+args[1]);
		 
		 
		 JavaSparkContext jsc = new JavaSparkContext(new SparkConf().setMaster("local").setAppName("Spark Count"));

	        JavaRDD<String> inputLines = jsc.textFile(args[0]);
	        
	        //Map to compute distance and discarding the rest
	        JavaRDD<Double> distance=inputLines.map(s -> {
	        	String[] lineSplit = s.split(" ");
                
                double R= 6371.009;
                
                double startLat= Double.parseDouble(lineSplit[2]);
                double startLong= Double.parseDouble(lineSplit[3]);
                double endLat=Double.parseDouble(lineSplit[5]);
                double endLong=Double.parseDouble(lineSplit[6]);
                
                double distances;
                
                if((south<startLat && startLat <north) && (south<endLat && endLat<north)
                		&& (east<startLong && startLong<west) && (east<endLong && endLong<west) ) {
                	double deltaLat= toRadian(startLat-endLat);
	                double deltaLong= toRadian(startLong- endLong);
	                double MeanLat= (startLat+endLat)/2;
	                
	                distances= R * Math.sqrt(Math.pow(deltaLat, 2)+ Math.pow((Math.cos(MeanLat)*deltaLong), 2));

	                if(distances>25) {
	                	System.out.println("Distance is too big: "+ distances);
	                	distances= -1.0;
	                	
	                }
                }else {
                	System.out.println("Invalid for San Francisco city: "+ s);
                	distances= -1.0;
                }
                return distances;
	        });
	        JavaRDD cachedRdd = distance.persist(StorageLevel.MEMORY_ONLY());
	        
	        //Filter potential distances
	        JavaRDD<Double> potentialDistance =distance.filter(values -> values!=-1);
	        long total= potentialDistance.count();// Count total distance
	        System.out.println("Total: "+ total);
	        
	        //Rounding up the distance to it's ceiling with their occurance count
	        JavaPairRDD<Double, Integer> pairs=potentialDistance.mapToPair(d -> {
	            return new Tuple2<Double, Integer>(Math.ceil(d),1);
	        });
	        
	        //Adding up the occurance count for each integer distance
	        JavaPairRDD<Double, Integer> results= pairs.reduceByKey((a, b) -> a+b);
	        
	        //Soring distances in ascending
	        JavaPairRDD<Double, Integer> sortedPairRDD = results.sortByKey(true);
	        
	        //Normalizing counts
	        JavaPairRDD<Double, Object> sortedPairRDDNormalized = sortedPairRDD.mapValues(aDouble -> (double)aDouble/total);
	        
	       
	        sortedPairRDDNormalized.foreach(p -> System.out.println(p));
	        sortedPairRDDNormalized.saveAsTextFile(args[1]);
	        
	        System.exit(0);

	}

}
