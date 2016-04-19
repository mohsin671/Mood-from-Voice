package audio.feature;

import java.util.ArrayList;
import java.util.Arrays;
/*
Please feel free to use/modify this class. 
If you give me credit by keeping this information or
by sending me an email before using it or by reporting bugs , i will be happy.
Email : gtiwari333@gmail.com,
Blog : http://ganeshtiwaridotcomdotnp.blogspot.com/ 
*/
import java.util.List;

public class MFCCFeatureExtract {
	private double preEmphasisAlpha = 0.97; //preemphasis coefficient
	private double inputSignal[];
	private double[][] mag;		//Magnitude spectrum of each frame
	private double Tw,Ts,Fs;
	private double winLength = 1;	// window length (second)
	private int Nw,Ns, nfft,K;
	private int totalFrame;
	private int numCoeffs = 12;		// 12 MFCC + 1 Energy = 13; the number of cepstral coefficients (including the 0th coefficient)
	private int M = 20; 			//number of filterbank channels (MelBands) 
	
	
	double[][] framedSignal_IM;		// padded framed signal imaginary part for FFT
	double[][] framedSignal_RE;		// padded framed signal real part for FFT
	
	private double[][] CC;		//mel-scale cepstral coefficients for each frame
	private double[] energy;	//log energy
	private double[][] deltaMfcc;	//delta-cepstrum
	private double[][] deltaDeltaMfcc;	//delta-delta
	private double[] deltaEnergy;
	private double[] deltaDeltaEnergy;
	private double[][] featureVector;	//39 FeatureVector
	double[][][] windows;	//set of frames to form a window
	
	/**
	 * Constructor
	 * @param tw is the analysis frame duration (ms)       
	 * @param ts is the analysis frame shift (ms)
	 * @param fs is the sampling frequency (Hz)
	 * @param wl is the window length (s)         
	 */
	public MFCCFeatureExtract(double signal[], double tw, double ts, double fs, double wl){
		Tw = tw;
		Ts = ts;
		Fs = fs;
		winLength = wl;
		inputSignal = signal;
		processSignal();
	}
	
	public MFCCFeatureExtract(double[] signal, double fs){
		Tw = 25;		//default setting
		Ts = 10;		//default setting
		Fs = fs;		//default setting
		winLength = 1;  //changed by Mohsin
		inputSignal = signal;
		processSignal();
	}
	
	public MFCCFeatureExtract(double tw, double ts, double fs, double wl){
		Tw = tw;
		Ts = ts;
		Fs = fs;
		winLength = wl;
	}
	
	public void setByteSignal(double[] signal){
		inputSignal = signal;
	}
	
	public void processSignal(){
		preEmphasis();
		frameBlocking();
		MFCC();
		makeMfccFeatureVector();
	}
	
	
	private void preEmphasis() {	//s2(n) = s(n) - a*s(n-1)
		double outputSignal[] = new double[inputSignal.length];
		// apply pre-emphasis to each sample
		outputSignal[0] = inputSignal[0]; // handle boundary case
		for (int n = 1; n < inputSignal.length; n++) {
			outputSignal[n] = (double) (inputSignal[n] - preEmphasisAlpha * inputSignal[n - 1]);
		}
		
		inputSignal = outputSignal;
//		return outputSignal;
	}
	
	private void frameBlocking() {
		
//		PRELIMINARIES 
	    Nw = (int)Math.round( 0.001*Tw*Fs);    // frame duration (samples)
	    Ns = (int)Math.round( 0.001*Ts*Fs );   // frame shift (samples)
	   
	    nfft = (int)Math.pow(2, PreProcess.nextpow2(Nw)); // length of FFT analysis 
	    K = nfft/2+1;                	 // length of the unique part of the FFT 
		
	    double[][] frames = PreProcess.vec2frames(inputSignal, Nw, Ns);
	    totalFrame = frames.length;
	    
	  //padded with zeros if the frame length or the number of samples in a frame != nfft
    	double[][] paddedFramedSignal = new double[totalFrame][nfft];
		
		for(int fi = 0; fi < totalFrame; fi++){
			paddedFramedSignal[fi] = Arrays.copyOf(frames[fi], nfft);
		}
		
		framedSignal_RE = paddedFramedSignal;
		
	 // generate imaginary part of the signal for each frame
		framedSignal_IM = new double[totalFrame][nfft];
		
	  		
//	    System.out.println("--");
//	    for(double[] f : paddedFramedSignal){
//	    	for(double sam: f){
//	    		System.out.println((float)sam);
//	    	}
//	    	System.out.println("-----------------");
//	    	System.out.println(Arrays.toString(f));
//	    }
	}
	
	/**
	 * extract MFCC of each frame
	 */
	private void MFCC(){
		FFT mFFT = new FFT(nfft);
		MFCC mMFCC = new MFCC(nfft, numCoeffs, M, Fs);
		//mag = new double[totalFrame][nfft];
		CC = new double[totalFrame][numCoeffs];
		
		for(int fi = 0; fi < totalFrame; fi++){
			//apply FFT
			mFFT.fft(framedSignal_RE[fi], framedSignal_IM[fi]);
			CC[fi] =  mMFCC.cepstrum(framedSignal_RE[fi], framedSignal_IM[fi]);
			
			
//			//Magnitude spectrum computation
//			for(int si = 0; si < nfft; si++){
//				mag[fi][si] = Math.sqrt(framedSignal_RE[fi][si]*framedSignal_RE[fi][si] + im[si]*im[si]);
//			}
		}
		
//		for(double[] frame : CC){
//			for(double coef : frame){
//				System.out.println(coef);
//			}
//			System.out.println("---------------------------------");
//		}
		
/*
		////Display
		for(double[] mframe : mag){	
			System.out.println(Arrays.toString(mframe));
		}
		
		for(double[] f : mag){
	    	for(double sam: f){
	    		System.out.println(sam);
	    	}
	    	System.out.println("-------------------------------------------------------");
	    }
*/
			
	}
	
	public void makeMfccFeatureVector() {
		featureVector = new double[totalFrame][3*numCoeffs+3];
		
		// delta cepstrum
		Delta delta = new Delta();
		delta.setRegressionWindow(2);// 2 for delta
		deltaMfcc = delta.performDelta2D(CC);
		// delta delta
		delta.setRegressionWindow(1);// 1 for delta delta
		deltaDeltaMfcc = delta.performDelta2D(deltaMfcc);
		
		//calculate log energy feature (13th feature)
		Energy en = new Energy(Nw);		//Nw : Samples per frame not including zero padding
		energy = en.calcEnergy(framedSignal_RE);
		
//		// energy delta
		delta.setRegressionWindow(1);
		deltaEnergy = delta.performDelta1D(energy);
//		// energy delta delta
		delta.setRegressionWindow(1);
		deltaDeltaEnergy = delta.performDelta1D(deltaEnergy);
		
		//combine 39 features per frame into one 2D array
		for (int i = 0; i < totalFrame; i++) {
			for (int j = 0; j < numCoeffs; j++) {
				featureVector[i][j] = CC[i][j];
			}
			for (int j = numCoeffs; j < 2 * numCoeffs; j++) {
				featureVector[i][j] = deltaMfcc[i][j - numCoeffs];
			}
			for (int j = 2 * numCoeffs; j < 3 * numCoeffs; j++) {
				featureVector[i][j] = deltaDeltaMfcc[i][j - 2 * numCoeffs];
			}
			featureVector[i][3 * numCoeffs] = energy[i];
			featureVector[i][3 * numCoeffs + 1] = deltaEnergy[i];
			featureVector[i][3 * numCoeffs + 2] = deltaDeltaEnergy[i];
		}
		
//		for(double[] f : featureVector){
//			for(double i : f){
//				System.out.println(i);
//			}
//			System.out.println("");
//		}
		
	}
	
	private List<WindowFeature> makeWindowFeature(){
		int nFrames = (int)Math.floor(winLength*1000/Tw);		//number of frames within a window
		//Log.d("FRAMES: ",""+nFrames);
		int numWindows = (int)Math.floor(totalFrame/nFrames);	//number of windows for this audio file
//test
//		double[][] featureVector = new double[][]{{0,1,2},{2,3,4},{3,4,5},{5,6,7},{7,8,9},{9,8,7},{7,6,5},{5,4,3}};
//		nFrames = 4;
//		numWindows = 2;
//		numCoeffs =3;
		
		windows = new double[numWindows][nFrames][numCoeffs];
		
		for(int n = 0; n < numWindows; n++){
			double[][] frames = new double[nFrames][numCoeffs];
			for(int m = n*nFrames; m < (n+1)*nFrames; m++){
				frames[m % nFrames] = featureVector[m];
			}
			
			windows[n] = frames;
		}
		
		List<WindowFeature> windowFeatureList = new ArrayList<WindowFeature>();	//array of windows
		for(int wi = 0; wi < numWindows; wi++){
			windowFeatureList.add(new WindowFeature(windows[wi]));
		}
		return windowFeatureList;
	}
	public List<WindowFeature> getListOfWindowFeature(){
		List<WindowFeature> windowFeatureList =  makeWindowFeature();
		return windowFeatureList;
		
	}
	
	public String toString(){
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");
		for(int ci = 0; ci < numCoeffs; ci++){
			for(int fi = 0; fi < totalFrame; fi++){
				result.append(CC[fi][ci]+",");
			}
			result.append(NEW_LINE);
		}
		return result.toString();
	}

	/*
	public static void main(String[] args) {
		
		String path = "sp10.wav";
      	Wave wave = new Wave(path);
     	double[] inputSignal = wave.getSampleAmplitudes();
     	int Fs = wave.getWaveHeader().getSampleRate();
     	double Tw = 25;                // analysis frame duration (ms)
        double Ts = 10;                // analysis frame shift (ms)
        double Wl = 1;				   // window duration (second)
        
		MFCCFeatureExtract mfccFeatures = new MFCCFeatureExtract(inputSignal, Tw, Ts, Fs, Wl);
		System.out.println("12 MFCCs of each frame");
		System.out.println(mfccFeatures);	//Display 12MFCC Features for each frame
		
		List<WindowFeature> lst = mfccFeatures.getListOfWindowFeature();
		System.out.println("WindowFeatures");
		for(WindowFeature wf:lst){
			System.out.println(wf);
		}
	}
	*/

}
