package audio.feature;

import java.util.Arrays;

public class PreProcess {
	
	/** 
	 * get frames from an array of signal
	 * @param vec input signal (array of samples)
	 * @param Nw frame width (samples)
	 * @param Ns frame shift or overlap (samples)
	 * @return frames 2d array; each row represents a frame
	 */
	public static double[][] vec2frames(double[] vec,int Nw, int Ns){			
		int L = vec.length;							// length of the input vector
		
	    int M = (int) Math.floor((L-Nw)/Ns+1);             // number of frames 
	    // figure out if the input vector can be divided into frames exactly
        int E = (L-((M-1)*Ns+Nw));
        // see if padding is actually needed
        double[] padded_vec = null;
       
        if( E>0 ) {
        	// how much padding will be needed to complete the last frame?
        	int P = Nw-E;
        	padded_vec = Arrays.copyOf(vec, (int) (L+P));
        }
        else{
        	padded_vec = vec;
        }
        // update number of frames after padding
        L = padded_vec.length;
        M = (int) Math.floor((L-Nw)/Ns+1);	//number of frames
        
        // overlapping
        double[][] frames = new double[M][Nw];
    	//System.out.println(Arrays.toString(padded_vec));
    	
    	int i = 0;
    	int j = 0;
    	int k = 0;
    	while(k < M){
    		double[] f = new double[Nw];
    		j = Ns*k;
    		for(i = 0; i < Nw; i++){
    			f[i] = padded_vec[j+i];
    		}
    		
    		frames[k] = f;
    		
    		j = j + Ns;
    		i = j;
    		k++;
    	}
    	
    	//windowing
		WindowFunction window = new WindowFunction();
		window.setWindowType("Hamming");
		double[] win = window.generate(Nw);		//generate window samples
		for(int m = 0; m < M; m++){
			if(win.length == Nw){
				for(int s = 0; s < Nw; s++){
					frames[m][s] =frames[m][s]*win[s];
				}

			}
		
		}
		return frames;
		
	}
	
	public static double log2( double a){
		return Math.log(a) / Math.log(2);
	}
	
	public static double nextpow2(double num){
		return Math.ceil(log2(num));
	}
	
	
//	private static void main(String[] args) {
//		double[] input = new double[]{1,2,3,4,5,6,7,8};
//		double[][] result = vec2frames(input,5,2);
//
//	}
}

	
