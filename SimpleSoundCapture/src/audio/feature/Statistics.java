package audio.feature;

//credit: Sharier Nirjon from SpeakerSense

public class Statistics {
	public static double geoMean(double[] input){
		double sum = 0;
		for(int i = 0; i < input.length; i++) {
			sum = sum + Math.log(Math.abs(input[i]));
		}
		return Math.exp(sum / input.length);
	}
	
	public static double harMean(double[] input){
		double sum = 0;
		double ret = 0;
		for(int i = 0; i < input.length; i++) {
			if(Math.abs(input[i]) > 0){
				sum = sum + 1.0 / input[i];
			}
		}
		if(Math.abs(sum) > 1e-8) {
			ret = input.length / sum;
		}
		return ret;
	}
	
	public static double mean(double[] input){
		double sum = 0;
		double ret = 0;
		for(int i = 0; i < input.length; i++) {
			sum = sum + input[i];
		}
		ret = sum / input.length;
		
		return ret;
	}
	
	public static double range(double[] input){
		double min, max;
		double ret;
		min = max = input[0];
		for(int i = 1; i < input.length; i++) {
			if(min > input[i]) min = input[i];
			if(max < input[i]) max = input[i];
		}
		ret = max - min;
		
		return ret;
	}
	
	public static double skewness(double[] input){
		double mean = 0;
		for(int i = 0; i < input.length; i++) {
			mean += input[i];
		}
		mean /= input.length;
		
		double m2 = 0, m3 = 0 , dx = 0;
		for(int j = 0; j < input.length; j++) {
			dx = input[j] - mean;
			m2 = m2 + dx*dx;
			m3 = m3 + dx*dx*dx;
		}
		m3 = m3 / input.length;
		m2 = m2 / input.length;
		
		double g1 = m3 / Math.sqrt(m2 * m2 * m2);
		double n = input.length;
		double fn = Math.sqrt(n * (n-1)) * g1 / n;
		
		return fn;
		
	}
	
	public static double stdDev(double[] input){
		
		double sum = 0, sum2 = 0;
		double ret;
		for(int i = 0; i < input.length; i++) {
			sum = sum + input[i];
			sum2 = sum2 + input[i] * input[i];
		}
		double n = input.length;
		ret = Math.sqrt((sum2/n) - (sum/n)*(sum/n));
		
		return ret;
	}
	
	public static double zscoreAvg(double[] input){
		double sum = 0, sum2 = 0;
		double ret;
		for(int i = 0; i < input.length; i++) {
			sum = sum + input[i];
			sum2 = sum2 + input[i] * input[i];
		}
		double n = input.length;
		double mean, std; 
		mean = sum/n;
		std  = Math.sqrt((sum2/n) - (sum/n)*(sum/n));	
		
		double zmean = 0;
		for(int j = 0; j < input.length; j++) {
			zmean = zmean + (input[j] - mean)/(std+1e-10);
		}
		ret = zmean / input.length;
		
		return ret;
	}
	
	public static double moment(double[] input){
		
		double sum = 0;
		double ret;
		
		for(int i = 0; i < input.length; i++) {
			sum = sum + input[i];
		}
		sum = sum / input.length;
		
		double mom = 0;
		for(int j = 0; j < input.length; j++) {
			mom = mom + Math.pow((input[j] - sum), 3);
		}
		ret = mom / input.length;
		
		return ret;
	}
	
	public static double kurtosis(double[] input){
		double mean = 0;
		for(int i = 0; i < input.length; i++) {
			mean += input[i];
		}
		mean /= input.length;
		
		double m2 = 0, m4 = 0 , dx = 0;
		for(int j = 0; j < input.length; j++) {
			dx = input[j] - mean;
			m2 = m2 + dx*dx;
			m4 = m4 + dx*dx*dx*dx;
		}
		m4 = m4 / input.length;
		m2 = m2 / input.length;
		
		double n = input.length;
		
		double g2 = m4 / (m2 * m2) - 3;
		
		double fn = (n-1) * (g2 * (n+1) + 6) / ((n-2) * (n-3));
		
		return fn;
	}
	
	
	public static void main(String argv[]){
		double[] a = new double[]{1, 5, -3, 4};
		double result = Statistics.kurtosis(a);
		System.out.println(result);
	}
}
