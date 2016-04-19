/*
 *
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free,
 * license to use, modify and redistribute this software in 
 * source and binary code form, provided that i) this copyright
 * notice and license appear on all copies of the software; and 
 * ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty
 * of any kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS
 * AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE 
 * HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR 
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT
 * WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT
 * OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS
 * OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY
 * TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGES.

 This software is not designed or intended for use in on-line
 control of aircraft, air traffic, aircraft navigation or
 aircraft communications; or in the design, construction,
 operation or maintenance of any nuclear facility. Licensee 
 represents and warrants that it will not use or redistribute 
 the Software for such purposes.
 */

/*  The above copyright statement is included because this 
 * program uses several methods from the JavaSoundDemo
 * distributed by SUN. In some cases, the sound processing methods
 * unmodified or only slightly modified.
 * All other methods copyright Steve Potts, 2002
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;



import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;


import audio.feature.*;

/**
 * SimpleSoundCapture Example. This is a simple program to record sounds and
 * play them back. It uses some methods from the CapturePlayback program in the
 * JavaSoundDemo. For licensizing reasons the disclaimer above is included.
 * 
 * @author Steve Potts
 */
public class SimpleSoundCapture extends JPanel implements ActionListener {

  final int bufSize = 16384;

  Capture capture = new Capture();

  Playback playback = new Playback();

  AudioInputStream audioInputStream;

  JButton playB, captB;

  JTextField textField;

  String errStr;

  double duration, seconds;

  File file;

  public SimpleSoundCapture() {
    setLayout(new BorderLayout());
    EmptyBorder eb = new EmptyBorder(5, 5, 5, 5);
    SoftBevelBorder sbb = new SoftBevelBorder(SoftBevelBorder.LOWERED);
    setBorder(new EmptyBorder(5, 5, 5, 5));

    JPanel p1 = new JPanel();
    p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));

    JPanel p2 = new JPanel();
    p2.setBorder(sbb);
    p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));

    JPanel buttonsPanel = new JPanel();
    buttonsPanel.setBorder(new EmptyBorder(10, 0, 5, 0));
    playB = addButton("Play", buttonsPanel, false);
    captB = addButton("Record", buttonsPanel, true);
    p2.add(buttonsPanel);

    p1.add(p2);
    add(p1);
  }

  public void open() {
  }

  public void close() {
    if (playback.thread != null) {
      playB.doClick(0);
    }
    if (capture.thread != null) {
      captB.doClick(0);
    }
  }

  private JButton addButton(String name, JPanel p, boolean state) {
    JButton b = new JButton(name);
    b.addActionListener(this);
    b.setEnabled(state);
    p.add(b);
    return b;
  }

  public void actionPerformed(ActionEvent e) {
    Object obj = e.getSource();
    if (obj.equals(playB)) {
      if (playB.getText().startsWith("Play")) {
        playback.start();
        captB.setEnabled(false);
        playB.setText("Stop");
      } else {
        playback.stop();
        captB.setEnabled(true);
        playB.setText("Play");
      }
    } else if (obj.equals(captB)) {
      if (captB.getText().startsWith("Record")) {
        capture.start();
        playB.setEnabled(false);
        captB.setText("Stop");
      } else {
        capture.stop();
        playB.setEnabled(true);
      }

    }
  }

  /**
   * Write data to the OutputChannel.
   */
  public class Playback implements Runnable {

    SourceDataLine line;

    Thread thread;

    public void start() {
      errStr = null;
      thread = new Thread(this);
      thread.setName("Playback");
      thread.start();
    }

    public void stop() {
      thread = null;
    }

    private void shutDown(String message) {
      if ((errStr = message) != null) {
        System.err.println(errStr);
      }
      if (thread != null) {
        thread = null;
        captB.setEnabled(true);
        playB.setText("Play");
      }
    }

    public void run() {

      // make sure we have something to play
      if (audioInputStream == null) {
        shutDown("No loaded audio to play back");
        return;
      }
      // reset to the beginnning of the stream
      try {
        audioInputStream.reset();
      } catch (Exception e) {
        shutDown("Unable to reset the stream\n" + e);
        return;
      }

      // get an AudioInputStream of the desired format for playback

      AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
      float rate = 44100.0f;
      int channels = 2;
      int frameSize = 4;
      int sampleSize = 16;
      boolean bigEndian = true;

      AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8)
          * channels, rate, bigEndian);

      AudioInputStream playbackInputStream = AudioSystem.getAudioInputStream(format,
          audioInputStream);

      if (playbackInputStream == null) {
        shutDown("Unable to convert stream of format " + audioInputStream + " to format " + format);
        return;
      }

      // define the required attributes for our line,
      // and make sure a compatible line is supported.

      DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
      if (!AudioSystem.isLineSupported(info)) {
        shutDown("Line matching " + info + " not supported.");
        return;
      }

      // get and open the source data line for playback.

      try {
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format, bufSize);
      } catch (LineUnavailableException ex) {
        shutDown("Unable to open the line: " + ex);
        return;
      }

      // play back the captured audio data

      int frameSizeInBytes = format.getFrameSize();
      int bufferLengthInFrames = line.getBufferSize() / 8;
      int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
      byte[] data = new byte[bufferLengthInBytes];
      int numBytesRead = 0;

      // start the source data line
      line.start();

      while (thread != null) {
        try {
          if ((numBytesRead = playbackInputStream.read(data)) == -1) {
            break;
          }
          int numBytesRemaining = numBytesRead;
          while (numBytesRemaining > 0) {
            numBytesRemaining -= line.write(data, 0, numBytesRemaining);
          }
        } catch (Exception e) {
          shutDown("Error during playback: " + e);
          break;
        }
      }
      // we reached the end of the stream.
      // let the data play out, then
      // stop and close the line.
      if (thread != null) {
        line.drain();
      }
      line.stop();
      line.close();
      line = null;
      shutDown(null);
    }
  } // End class Playback

  /**
   * Reads data from the input channel and writes to the output stream
   */
  class Capture implements Runnable {

	  TargetDataLine line;

	  Thread thread;
	  MyWeka weka;

	  public void start() {
		  weka = BuildClassifier();
		  errStr = null;
		  thread = new Thread(this);
		  thread.setName("Capture");
		  thread.start();

	  }

	  public void stop() {
		  thread = null;
	  }

	  private void shutDown(String message) {
		  if ((errStr = message) != null && thread != null) {
			  thread = null;
			  playB.setEnabled(true);
			  captB.setText("Record");
			  System.err.println(errStr);
		  }
	  }

	  public double[] toDoubleArray(byte[] byteArray){
		  int times = Double.SIZE / Byte.SIZE;
		  double[] doubles = new double[byteArray.length / times];
		  for(int i=0;i<doubles.length;i++){
			  doubles[i] = ByteBuffer.wrap(byteArray, i*times, times).getDouble();
		  }
		  return doubles;
	  }

	  public void WriteToFile(String S) {
		  BufferedWriter writer = null;
		  try {
			  //create a temporary file
			  String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
			  File logFile = new File("MoodLog.txt");

			  // This will output the full path where the file will be written to...
			  //System.out.println(logFile.getCanonicalPath());

			  writer = new BufferedWriter(new FileWriter(logFile, true));
			  writer.write(S + '\n');
		  } catch (Exception e) {
			  e.printStackTrace();
		  } finally {
			  try {
				  // Close the writer regardless of what happens...
				  writer.close();
			  } catch (Exception e) {
			  }
		  }
	  }

	  public void run() {

		  duration = 0;
		  audioInputStream = null;

		  // define the required attributes for our line,
		  // and make sure a compatible line is supported.

		  AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
		  float rate = 16000.0f;
		  int channels = 2;
		  int frameSize = 4;
		  int sampleSize = 16;
		  boolean bigEndian = true;

		  AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8)
				  * channels, rate, bigEndian);

		  DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

		  if (!AudioSystem.isLineSupported(info)) {
			  shutDown("Line matching " + info + " not supported.");
			  return;
		  }

		  // get and open the target data line for capture.

		  try {
			  line = (TargetDataLine) AudioSystem.getLine(info);
			  line.open(format, line.getBufferSize());
		  } 
		  catch (LineUnavailableException ex) {
			  shutDown("Unable to open the line: " + ex);
			  return;
		  } 

		  catch (SecurityException ex) {
			  shutDown(ex.toString());
			  //JavaSound.showInfoDialog();
			  return;
		  } 
		  catch (Exception ex) {
			  shutDown(ex.toString());
			  return;
		  }

		  // play back the captured audio data
		  ByteArrayOutputStream out = new ByteArrayOutputStream();
		  int frameSizeInBytes = format.getFrameSize();
		  int bufferLengthInFrames = line.getBufferSize() / 8;
		  int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes * 4;
		  byte[] data = new byte[bufferLengthInBytes];
		  int numBytesRead;

		  line.start();

		  while (thread != null) {
			  if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
				  break;
			  }
			  //System.out.println("numBytesRead "+ numBytesRead);
			  //byte audioBytes[] = out.toByteArray();
			  Date dateobj = new Date();
			  FeatureExtractionAndMoodClassification(data, weka, dateobj);
			  //System.out.println("thread run new iteration...");
			  
			  out.write(data, 0, numBytesRead);
			  
			  /*try {
				  out.flush();				  
			  } 
			  catch (IOException ex) {
				  ex.printStackTrace();
			  }*/
		  }

		  // we reached the end of the stream.
		  // stop and close the line.
		  line.stop();
		  line.close();
		  line = null;

		  // stop and close the output stream
		  try {
			  out.flush();
			  out.close();
		  } catch (IOException ex) {
			  ex.printStackTrace();
		  }

		  // load bytes into the audio input stream for playback

		  byte audioBytes[] = out.toByteArray();
		  ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
		  audioInputStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);

		  long milliseconds = (long) ((audioInputStream.getFrameLength() * 1000) / format
				  .getFrameRate());
		  //  duration = milliseconds / 1000.0;

		  try {
			  audioInputStream.reset();
		  } catch (Exception ex) {
			  ex.printStackTrace();
			  return;
		  }                      

	  }

	  public MyWeka BuildClassifier(){
		  MyWeka weka = new MyWeka();
		  try {
			  weka.readDataAndBuildClassifier();
		  } 
		  catch (Exception e1) {
			  // TODO Auto-generated catch block
			  e1.printStackTrace();
		  }
		  return weka;
	  }

	  public void FeatureExtractionAndMoodClassification(byte[] audioBytes, MyWeka weka, Date dateobj){
		  /*Feature Extraction--------------by Mohsin*/
		  //System.out.println("audioByes size = " + audioBytes.length);
		  DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		  List<WindowFeature> windowFeatureList = new ArrayList<WindowFeature>();
		  double[] inputSignal = new double[audioBytes.length / 3];
		  for (int i = 0, j = 0; i != inputSignal.length; ++i, j += 3) {
			  inputSignal[i] = (double)( (audioBytes[j  ] & 0xff) | 
					  ((audioBytes[j+1] & 0xff) <<  8) |
					  ( audioBytes[j+2]         << 16));
		  }
		  int Fs = 44100; //44100
		  MFCCFeatureExtract mfccFeature = new MFCCFeatureExtract(inputSignal,Fs);
		  windowFeatureList.addAll(mfccFeature.getListOfWindowFeature());
		  //System.out.println("windowfeaturelist size = " + windowFeatureList.size());

		  for(WindowFeature w: windowFeatureList){
			  double[] featurevector = new double[351];
			  //System.out.println("window feature size " + w.windowFeature[38].length);
			  int k = 0;
			  for (int i = 0; i < 39; i++){
				  for (int j = 0; j < 9; j++){
					  featurevector[k++] = w.windowFeature[i][j];
				  }
			  }

			  if (featurevector[0] < 150){
				  System.out.println("Silent frame ...");
				  WriteToFile(df.format(dateobj)+ " " + "Silent");
				  continue;
			  }
			  try{
				  int mood = (int) weka.classify(featurevector);
				  System.out.println(mood == 0 ? "Angry" : mood == 1 ? "Happy" : mood == 2 ? "Neutral" : "Sad");
				  WriteToFile(df.format(dateobj)+ " " + (mood == 0 ? "Angry" : mood == 1 ? "Happy" : mood == 2 ? "Neutral" : "Sad"));
			  } 
			  catch (Exception e){
				  System.out.println(e);
			  }
			  //System.out.println(w.windowFeature[1].length);
		  }
	  }
  } // End class Capture

  public class MyWeka {

	  public Instances data = null;
	  public Instances testdata = null;
	  String fileName = null;
	  BufferedReader reader;
	  public Classifier classifier;
	  public Evaluation eval;
	  public int factor = 2;

	  public void setFileName(String fName) throws Exception {
		  fileName = fName;
		  // reader = new BufferedReader(new FileReader(fileName));
	  }

	  public void readDataAndBuildClassifier() throws Exception {

		  if (data != null)
			  data.delete();
		  reader = new BufferedReader(new FileReader("mood_training_set_last(1s window, 8000hz).arff"));
		  data = new Instances(reader);
		  reader.close();

		  data.setClassIndex(351);
		  //classifier = new weka.classifiers.trees.RandomForest();
		  //classifier.buildClassifier(data); // build classifier
		  File f = new File("mood_model_last.model");
		  if(f.exists() && !f.isDirectory()) { 
			  classifier = (Classifier) weka.core.SerializationHelper.read("mood_model_last.model");
			  System.out.println("Classifier loaded from saved model");
		  }
		  else{
			classifier = new weka.classifiers.trees.RandomForest();
			  classifier.buildClassifier(data); // build classifier
			  //Classifier cModel = (Classifier)new NaiveBayes();  
			  //cModel.buildClassifier(isTrainingSet);  

			  weka.core.SerializationHelper.write("mood_model_last.model", classifier);
			  System.out.println("Classifier created run time");
		  }
		  

		  //reader = new BufferedReader(new FileReader("mood_training_set_white_noise_0.04.arff"));
		  //testdata = new Instances(reader);

		  //reader.close();
		  //	UtilFunctions.close(reader);
	  }

	  public double classify(double[] featurevector) throws Exception {
		  //readData();
		  //data.setClassIndex(351);
		  //testdata.setClassIndex(0);
		  //data.setClassIndex(data.numAttributes() - 1);
		  // data=SmoteFilter.applySmote(data, factor);


		  //classifier = new weka.classifiers.trees.RandomForest(); // new instance of tree
		  /*String[] options = new String[4];
			options[0] = "-C";
			options[1] = "0.25";
			options[2] = "-M";
			options[3] = "2";
			classifier.setOptions(options);*/

		  /*String[] options = new String[6];
			options[0] = "-I";
			options[1] = "10";
			options[2] = "-K";
			options[3] = "0";
			options[4] = "-S";
			options[5] = "1";
			classifier.setOptions(options);*/

		  /*
		   * classifier = new weka.classifiers.functions.LibSVM(); // new instance
		   * of tree String[] options = new String[4]; options[0]="-K";
		   * options[1]="0"; options[2]="-H"; options[3]="0";
		   * classifier.setOptions(options);
		   */

		  //classifier.buildClassifier(data); // build classifier
		  //System.out.println("Classifier created");


		  eval = new Evaluation(data);
		  eval.setPriors(data);
		  //eval.useNoPriors();


		  //Instance i = new Instance(7.0, featurevector);
		  Instance i = new Instance(352);
		  for (int a = 0; a <=350; a++) i.setValue(a, featurevector[a]);
		  //System.out.println(i);
		  i.setDataset(data);
		  double mood = eval.evaluateModelOnce(classifier, i);
		  return mood;
		  //eval.evaluateModel(classifier, data);

		  //eval.crossValidateModel(classifier, data, 10, new Random(1));

		  //System.out.println(eval.pctCorrect());
	  }
  }

  public static void main(String s[]) {
	  File logfile = new File("MoodLog.txt");
	  if(logfile.exists() && !logfile.isDirectory()){
		  logfile.delete(); 
		  System.out.println("Previous log file deleted");
	  }
    SimpleSoundCapture ssc = new SimpleSoundCapture();
    ssc.open();
    JFrame f = new JFrame("Capture/Playback");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.getContentPane().add("Center", ssc);
    f.pack();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int w = 360;
    int h = 170;
    f.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2);
    f.setSize(w, h);
    f.show();
  }
}