/*
  Please feel free to use/modify this class. 
  If you give me credit by keeping this information or
  by sending me an email before using it or by reporting bugs , i will be happy.
  Email : gtiwari333@gmail.com,
  Blog : http://ganeshtiwaridotcomdotnp.blogspot.com/ 
 */
package audio.feature;

/**
 * calculates energy from given PCM of a frame
 * 
 * @author Madhav Pandey
 * @reference Spectral Features for Automatic Text-Independent Speaker
 *            Recognition @author Tomi Kinnunen, @fromPage ##
 */
public class Energy {

	/**
     *
     */
	private int samplePerFrame;

	/**
	 * 
	 * @param samplePerFrame
	 */
	public Energy(int samplePerFrame) {
		this.samplePerFrame = samplePerFrame;
	}

	/**
	 * 
	 * @param framedSignal
	 * @return energy of given PCM frame
	 */
	public double[] calcEnergy(double[][] framedSignal) {
		double[] energyValue = new double[framedSignal.length];
		for (int i = 0; i < framedSignal.length; i++) {
			float sum = 0;
			for (int j = 0; j < samplePerFrame; j++) {
				// sum the square
				sum += Math.pow(framedSignal[i][j], 2);
			}
			// find log
			energyValue[i] = Math.log(sum);
		}
		return energyValue;
	}
	
	
//	public static void main(String argv[]){
//		double[][] frames = new double[][]{{1,2,3,5},{4,5,6,5},{7,8,9,5}};
//		double[] result;
//		Energy en = new Energy(3);
//		
//		result = en.calcEnergy(frames);
//		for(double e:result){
//			System.out.println(e);
//		}
//	}
}
