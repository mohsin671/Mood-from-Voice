package audio.feature;

import java.util.Arrays;

public class WindowFeature {
				
	public double[][] windowFeature;
	int numStat = 9;
	//int numStat = 1; //changed by Mohsin
	int numFeature;
	int numFrame;
	
	//stats
	private static final int MEAN = 0;
	private static final int GEOMEAN = 1;
	private static final int HARMEAN = 2;
	private static final int STDEV = 3;
	private static final int RANGE = 4;
	private static final int MOMENT = 5;
	private static final int ZSCORE = 6;
	private static final int SKEW = 7;
	private static final int KURT = 8;
	
	
	
	WindowFeature(double[][] featureTable){
		numFrame = featureTable.length;
		numFeature = featureTable[0].length;
		computeFeatureStats(featureTable);
	}
	
	
	//featureTable : table of 39 Feature Vector of each frame within this window
	//each column represents a frame 
	//each row represents a feature
	
	private void computeFeatureStats(double[][] featureTable){
		windowFeature = new double[numFeature][numStat];
		//for each feature
		for(int j = 0; j < numFeature ; j++){
			//in each frame
			double[] temp = new double[numFrame];	//get values of a single feature across all frames in this window;
			for(int i = 0; i < numFrame; i++){
				temp[i] = featureTable[i][j];
				//Log.d("FRAMES","MFCC 1" + ", Frame " + i + ": " + featureTable[i][1]);
			}
			// compute statistics for the j th feature
			
			
			windowFeature[j][0] = Statistics.mean(temp);				//mean
			windowFeature[j][1] = Statistics.geoMean(temp);				//geometric mean
			windowFeature[j][2] = Statistics.harMean(temp);				//harmonic mean
			windowFeature[j][3] = Statistics.stdDev(temp);				//standard deviation
			windowFeature[j][4] = Statistics.range(temp);				//range
			windowFeature[j][5] = Statistics.moment(temp);				//moment
			windowFeature[j][6] = Statistics.zscoreAvg(temp);			//average Z-score
			windowFeature[j][7] = Statistics.skewness(temp);			//skewness
			windowFeature[j][8] = Statistics.kurtosis(temp);			//kurtosis 
			
		}
		
		//print
//		for(double[] feature:windowFeature){
//			System.out.println(Arrays.toString(feature));
//		}
//		System.out.println(Arrays.deepToString(windowFeature));
		
	}
	
	public String toString(){
		//1:stat1_feature1 2:stat2_feature1 3:stat3_feature1 ... 9:stat9_feature1
		//10:stat1_feature2 11:stat1_feature2 ...
		//20:stat1_feature3 ...
		StringBuilder result = new StringBuilder();
	    String NEW_LINE = System.getProperty("line.separator");
	    
		int i = 0;
		for(double[] stats : windowFeature){	//set of statistics of each feature
			for(double value: stats){
				result.append(i+":"+(float)value+" ");
				i++;
			}
			
		}
		return result.toString();
		
	}
	
	
	
	
	
//	public static void main(String argv[]){
//		double[][] fTable = new double[][]{{1,2,3},{4,5,6},{7,8,9},{10,11,12},{13,14,15},{16,17,18}};
//		WindowFeature wf = new WindowFeature(fTable);
//		wf.generateSVMInput();
//	}

}
