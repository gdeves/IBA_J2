package aifira.ui.ConvertListFiles.ActionsC;
import aifira.ui.ConvertListFiles.FrameC.FrameC;
import aifira.ui.ConvertListFiles.ADC.ADC;
import aifira.ui.ConvertListFiles.MPA3.MPA3;
import aifira.ui.ConvertListFiles.listFiles.listFiles;
import aifira.ui.Spectra.Spectra;
import javax.swing.JFileChooser;
import java.io.File;
import java.util.ArrayList;
import ij.*;
import ij.io.FileSaver;
import ij.plugin.ContrastEnhancer;
import ij.process.ImageProcessor;
import ij.process.StackProcessor;
import ij.process.ShortProcessor;
import java.awt.Component;
import java.awt.HeadlessException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import aifira.ui.Prefs.PrefsManager;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;


/**
 * class for performing various actions in data conversion
 * @author deves
 */
public class ActionsC{
  int sizeMapX=1;
  int sizeMapY=1;

  ArrayList <listFiles> listFilesArray=new ArrayList <>();
  int [] flags=new int[28];
  ArrayList <double []> pixe_stack=new ArrayList <>();
  ImageStack stimStack=new ImageStack(sizeMapX,sizeMapY);
  
  private ChartFrame currentFrame = null;
  private XYSeriesCollection dataset = null;
  private XYSeriesCollection dataset1 = null;
  private XYSeriesCollection dataset2 = null;
  private XYSeriesCollection dataset3 = null;
  
  private ChartFrame frame1 = null;
  private ChartFrame frame2 = null;
  private ChartFrame frame3 = null;

  /**
  * Constructor for ActionsC class with default initialization of flags and pixe_stack
  */
  public ActionsC(){
          initFlags();
          init_pixe_stack();
  }
  
  /**
  * Gets ADC index for scanning X direction
     * @return the index for adc corresponding the X position of the beam
  */
  public int getAdcIndexScanX(){
      int indexOfAdc=0;
      for (int i=0;i<16;i++){
          if (flags[i]==4) indexOfAdc=i;
      }
      return indexOfAdc;    
  }
  
  /**
  * Gets ADC index for scanning Y direction
     * @return the index for adc corresponding the Y position of the beam
  */
  public int getAdcIndexScanY(){
      int indexOfAdc=0;
      for (int i=0;i<16;i++){
          if (flags[i]==5) indexOfAdc=i;
      }
      return indexOfAdc;    
  }
    
  /**
  * Displays a dialog box for user to select files to be processed (multiple selection possible)
  * Is used to modify the ArrayList listFilesArray
  */    
  public void selectFiles(){     
          try{
                  listFilesArray.clear();
                  PrefsManager prefs=new PrefsManager();
                  prefs.setPreference();
                  JFileChooser jF = new JFileChooser();  // a new filechosser is created
                  File myDir=new File(prefs.getLastUsedDirectory());
                  jF.addChoosableFileFilter(new FileNameExtensionFilter("AIFIRA list file", "lst"));
                  jF.setFileFilter(jF.getChoosableFileFilters()[1]);
                  jF.setCurrentDirectory(myDir);
                  jF.setApproveButtonText("OK");         // button title
                  jF.setMultiSelectionEnabled(true);
                  
                  jF.showOpenDialog(null);               // displays the dialog box
                  
                  File [] selectedFiles = jF.getSelectedFiles(); 
                          for (File f : selectedFiles){
                                  listFiles lf=new listFiles(f.getAbsolutePath(),getAdcIndexScanX(),getAdcIndexScanY());
                                  listFilesArray.add(lf);
                                  prefs.saveDirectory(f.getAbsolutePath());
                                  
                          }
                  
          }
          catch (HeadlessException e){
          }
  }

  //Working with flags

  /**
  * Initializes flags to 1 except for Tomo projection sum and pixe map
  */
  // flags[0] to flags[15] corresponds to ADC status (1 is on)
  // flags[16] - saving XYE listfile
  // flags[17] - save gupix spectra for each ADC separately
  // flags[18] - save gupix spectra for the sum of pixe adc
  // flags[19] - save 'channel-counts' 2 columns text file (ie used for rbs)
  // flags[20] - save stim spectra as a text file
  // flags[21] - save stim maps separately as a text image readable using imageJ
  // flags[22] - plot spectra
  // flags[23] - save stim stack as TIFF
  // flags[24] - process pixe stack
  // flags[25] - display stim stack
  // flags[26] - calculate pixe map
  // flags[27] - save XYE listfile compatible with supavisio

  private void initFlags(){
          for (int i=0;i<flags.length;i++) setFlags(i,0);
          for (int i =16;i<20;i++) setFlags(i,1);//TODO remove this part of function to FrameC
  }
  /**
  * Resets all flags for ADC to 0.
  */
  private void resetJbuttonFlags(){
          for (int i=0;i<16;i++) flags[i]=0;
  }
  /**
  * Initializes arraylist containing the pixe sum spectra for all selected list files.
  */
  private void init_pixe_stack(){
      for (int i=0; i<9;i++){
          pixe_stack.add(new double[4096]);
      }
  }
  /**
  * Clears the arraylist containing projection sum spectra and initializes a new stack.
  */
  private void reset_pixe_stack(){
      //Default number of adc is 8 for AIFIRA. The 9th corresponds to pixe sum adc
      pixe_stack.clear();
      for (int i=0; i<17;i++){
          pixe_stack.add(new double[4096]);
      }
  }
  /**
  * Sets value to selected flag
      @param n index of the flag
      @param value value of the flag
      * */
  public  void setFlags(int n, int value){
      flags[n]=value;
  }
  /**
  * Sets values to flags after checking ADC status
  * flags 0 to 15 corresponding to ADC 1 to 16
  * @param f the frame containing components to be checked
  */
  public void setFlags(FrameC f){
          resetJbuttonFlags();
          int nPanel=-1; //first panel found corresponding to adc will get index 0
          
          //check for jRadioButton
          Component[] C1 = (f.jPanelB).getComponents();
          for (Component C2 : C1){
                  if (C2 instanceof JPanel){
                          nPanel+=1;
                          int nButton=0;
                          
                          Component[] C3 = ((JPanel)C2).getComponents();
                          for (Component C4 : C3) {
                                  if (C4 instanceof JRadioButton){
                                          nButton+=1;
                                          
                                          if (((JRadioButton)C4).isSelected()) flags[nPanel]=nButton;
                                  }
                          }
                  }
          }
  }
  //processing data
  /**
  * Plots a spectra in ImageJ
  * @param Yvalues double [4096] array containing values to be plotted
  * @param title Name of the plot window
  */
private void plot(double [] Yvalues, String title, int datasetType){
    double [] xValues = new double [4096];
    for (int i=0; i<4096; i++) xValues[i]=i;
    
    double minValue = 0.1;
    
    for (int i=0; i<4096; i++) {
        if (Yvalues[i] <= 0) {
            Yvalues[i] = minValue;
        }
    }
    
    // ✅ Choisir le bon dataset selon le type
    XYSeriesCollection dataset = null;
    ChartFrame currentFrame = null;
    String windowTitle = null;
    
    if (datasetType == 1) {
        dataset = dataset1;
        currentFrame = frame1;
        windowTitle = "PIXE spectra";
    } else if (datasetType == 2) {
        dataset = dataset2;
        currentFrame = frame2;
        windowTitle = "RBS spectra";
    } else if (datasetType == 3) {
        dataset = dataset3;
        currentFrame = frame3;
        windowTitle = "STIM spectra";
    }
    
    // Créer le dataset s'il n'existe pas
    if (dataset == null) {
        dataset = new XYSeriesCollection();
        if (datasetType == 1) dataset1 = dataset;
        else if (datasetType == 2) dataset2 = dataset;
        else if (datasetType == 3) dataset3 = dataset;
    }
    
    // Ajouter la série
    XYSeries series = new XYSeries(title);
    for (int i = 0; i < 4096; i++) {
        series.add(xValues[i], Yvalues[i]);
    }
    dataset.addSeries(series);
    
    // Créer le chart
    NumberAxis xAxis = new NumberAxis("x");
    NumberAxis yAxis = new LogarithmicAxis("y");
    XYPlot xyplot = new XYPlot(dataset, xAxis, yAxis, new XYLineAndShapeRenderer());
    JFreeChart chart = new JFreeChart(xyplot);
    
    // ✅ Gérer la bonne frame
    if (currentFrame == null) {
        currentFrame = new ChartFrame(windowTitle, chart);
        currentFrame.setSize(600, 450);
        currentFrame.setVisible(true);
        
        // ✅ Sauvegarder la référence
        if (datasetType == 1) frame1 = currentFrame;
        else if (datasetType == 2) frame2 = currentFrame;
        else if (datasetType == 3) frame3 = currentFrame;
    } else {
        currentFrame.getChartPanel().setChart(chart);
    }
}
  /**
  * Actions performed when ADC corresponding to RBS is detected according to flags
  * @param rbs ADC corresponding to RBS
  * @param lF name of processed list file
  * @param indexOfADC Index of RBS adc  in MPA
  */
  private void processRBS(ADC rbs, listFiles lF, int indexOfADC){
          
          if (flags[19]==1) rbs.saveChannelCountsSpectra(lF.setExtension("asc.dat"));
          if (flags[27]==1) rbs.saveXYEListFile(lF.setExtension("RBS"), (short)1);
          else if (flags[16]==1) rbs.saveXYEListFile(lF.setExtension("ADC"+Integer.toString(indexOfADC+1)+".rbs2"));
          if (flags[22]==1){
            String justName = new File(lF.getPath()).getName();
            String title=justName+" ADC: "+String.valueOf(indexOfADC+1)+": RBS - N counts = " +String.valueOf(rbs.getNEvents()-1); 
            plot(rbs.getSpectra(),title,2);
          }
          java.lang.System.gc();
  }
  /**
  * Actions performed when ADC corresponding to PIXE is detected according to flags
  * @param pixe ADC corresponding to PIXE
  * @param lF name of processed list file
  * @param indexOfADC index of PIXE adc in MPA
  */
  private void processPIXE(ADC pixe, listFiles lF, int indexOfADC){

    // ✅ Fichiers individuels - seulement si flags[17]==1 et pas l'ADC somme
    if (flags[17]==1 && indexOfADC != 16){
        pixe.saveGupixSpectra(lF.setExtension("ADC"+Integer.toString(indexOfADC+1)+".gup"));
        if (flags[16]==1) pixe.saveXYEListFile(lF.setExtension("ADC"+Integer.toString(indexOfADC+1)+".pixe2"));
    }

    // ✅ Fichier gup somme - seulement si flags[18]==1 et ADC somme
    if (flags[18]==1 && indexOfADC == 16){
        pixe.saveGupixSpectra(lF.setExtension("sumAll.gup"));
        pixe.saveXYEListFile(lF.setExtension("sumAll.pixe2"));
    }

    // ✅ Accumulation somme dans pixe_stack
    if (flags[18]==1 && indexOfADC != 16){
        double[] t = pixe.getSpectra();
        for (int i=0;i<4096;i++){
            pixe_stack.get(indexOfADC)[i] += t[i];
        }
    }

    // ✅ Affichage
    if (flags[22]==1){
        if (flags[18]==1 && indexOfADC==16){
            String justName = new File(lF.getPath()).getName();
            String title = justName+" ADC: "+String.valueOf(indexOfADC+1)+": PIXE - N counts = "+String.valueOf(pixe.getNEvents()-1);
            plot(pixe.getSpectra(), title, 1);
        }
        else if (flags[17]==1 && indexOfADC != 16){
            String title = lF.getPath()+" ADC: "+String.valueOf(indexOfADC+1)+": PIXE - N counts = "+String.valueOf(pixe.getNEvents()-1);
            plot(pixe.getSpectra(), title, 1);
        }
    }
    java.lang.System.gc();
}
  /**
  * Actions performed when ADC corresponding to STIM is detected according to flags
  * @param adc ADC corresponding to STIM
  * @param lF name of processed list file
  * @param indexOfADC index of PIXE adc in MPA
  */
  private void processSTIM(ADC adc, listFiles lF, int indexOfADC){
      try{
      adc.medianSort(); //map calculation
      if (flags[23]==1) adc.saveMedianImage(lF.setExtension("medMap.txt")); //saving map
      if (flags[25]==1) fillStack(adc,lF); //displaying map
      if (flags[20]==1) adc.saveCountsSpectra(lF.setExtension("stim_ADC" +Integer.toString(indexOfADC+1)+".asc")); // save spectra
      //save XYE list file
      if (flags[27]==1) adc.saveXYEListFile(lF.setExtension("_ADC"+Integer.toString(indexOfADC+1)+"STIM"),(short)2); // save stim
      else if (flags[16]==1) adc.saveXYEListFile(lF.setExtension("_ADC"+Integer.toString(indexOfADC+1)+".stim2"));
      //Output: display spectra
      if (flags[22]==1){
          String justName = new File(lF.getPath()).getName();
          String title=justName+" ADC: "+String.valueOf(indexOfADC+1)+": STIM - N counts = " +String.valueOf(adc.getNEvents()-1);
          plot(adc.getSpectra(),title,3);
      }
      } catch (Exception e){
          IJ.log(e.toString());
      }
      
      //Prefs ijPrefs=new Prefs();
      //ijPrefs.set(".convertListFiles.lastUsedFile", lf.getPath());
      
      java.lang.System.gc();
      
  }
  /**
  * This method adds median map to stim stack using
  * @param adc ADC to be added
  * @param lF name of the file
  */
  private void fillStack(ADC adc,listFiles lF){
      //searching for the maximum size of X and Y in adc
      this.sizeMapX=adc.getMaxX();
      this.sizeMapY=adc.getMaxY();
      
      //Creation of a map with above size and resizing existing stack created at the instanciation of ActionC
      ImageProcessor ip = new ShortProcessor(sizeMapX, sizeMapY);
      StackProcessor stakProc = new StackProcessor(stimStack,ip);
      stimStack=stakProc.resize(sizeMapX,sizeMapY);
      
      String title = lF.setExtension("");
      for (int x=0;x<sizeMapX;x++){
          for (int y=0;y<sizeMapY;y++){
              ip.set(x,y,adc.getMedianValue(x, y));
          }
      }
      stimStack.addSlice(title, ip);
      
  }

  /**
  * Processes all files in listFilesArray according to their type (RBS, STIM or PIXE)
  */
  public void process(){				
          reset_pixe_stack();
          reset_stim_stack();
          dataset = new XYSeriesCollection();
          dataset1 = new XYSeriesCollection();
          dataset2 = new XYSeriesCollection();
          dataset3 = new XYSeriesCollection();
          frame1 = null;  // ✅ Reset les frames
          frame2 = null;
          frame3 = null;
          
          for (int indexOfFile=0;indexOfFile<listFilesArray.size();indexOfFile++){
              
              processFile(indexOfFile);
          }

          if (flags[25]==1){
              try {
                  
                  ImagePlus imp = new ImagePlus("Median maps", stimStack);
                  ContrastEnhancer ce=new ContrastEnhancer();
                  ce.stretchHistogram(imp, (double)0.35);
                  imp.show();
                  if (flags[23]==1) {
                      FileSaver fs = new FileSaver( imp) ;
                      Date d= new Date();
                      fs.saveAsTiff();
                  }
              }
              catch (Exception e){
                  IJ.log(e.toString());
              }
              
          }
          java.lang.System.gc();	
          IJ.showStatus("done");
  }

  /**
  * Processes selected file in listFilesArray
  * @param indexOfFile Index of file in listFilesArray
  */
  public void processFile(int indexOfFile){
        MPA3 mpa=listFilesArray.get(indexOfFile).readListFile(getActiveADCs());
        
        boolean debug = true;
        IJ.log("Reading file: "+ listFilesArray.get(indexOfFile).getPath());
        if (debug){
          int activeADCs[]=getActiveADCs();
          for (int i=0;i<activeADCs.length;i++){
              int indexOfAdc = activeADCs[i];
              int totPeriods= mpa.getADC(indexOfAdc).getNActivationPeriods();
              int inactivePeriodsCounter=0;
              for (int j=0;j<totPeriods;j++){
                  if (mpa.getADC(indexOfAdc).getActivationPeriod(j)==0)
                      inactivePeriodsCounter++;
              }
              float inactivePeriodsCounterfl=inactivePeriodsCounter;
              float tempsMort=100*inactivePeriodsCounterfl/totPeriods;
              
              if (indexOfAdc>7) IJ.log("Dead Time - ADC "+(indexOfAdc+1) +" = "+tempsMort +"%");
          }
        }
        for (int indexOfAdc=0;indexOfAdc<16;indexOfAdc++){
                switch (flags[indexOfAdc]){
                case 1: //RBS
                        processRBS(mpa.getADC(indexOfAdc),listFilesArray.get(indexOfFile), indexOfAdc);
                        break;
                case 2: //STIM
                        processSTIM(mpa.getADC(indexOfAdc),listFilesArray.get(indexOfFile), indexOfAdc);
                        break;
                case 3: //PIXE
                        if ((flags[17]==1)||(flags[16]==1)||(flags[22]==1)||(flags[24]==1)) processPIXE(mpa.getADC(indexOfAdc),listFilesArray.get(indexOfFile), indexOfAdc);
                        if (flags[18]==1) {
                            //int offset=prefs.getOffset();
                            //IJ.log("Adding offset: "+offset);
                            mpa.addToSum(indexOfAdc,16);
                            int lastPixeADCIndex=15;
                            while (flags[lastPixeADCIndex]!=3) lastPixeADCIndex-=1;
                            if (indexOfAdc==lastPixeADCIndex) processPIXE(mpa.getADC(16),listFilesArray.get(indexOfFile),16);
                        }
                        break;
                default: //other cases
                        break;
                }
        }
        IJ.log("Processing ADC: done.");
        java.lang.System.gc();
  }
  /**
  * Resets listFilesArray, list of files to be processed
  */
  public void reset(){
          listFilesArray.clear();
  }
  /**
  * Resets STIM image stack
  */
  private void reset_stim_stack(){
      for (int i=0;i<stimStack.getSize()+1;i++) stimStack.deleteLastSlice();
      
  }
  
  /**
   * Gets an arraylist containing the active ADCs 
   * @return arrayActivatedADCs arraylist containing the active ADCs
   */
  public int[] getActiveADCs(){
    ArrayList<Integer> listActivatedADCs = new ArrayList<>();
    for (int i=0;i<16;i++){
        if (flags[i]!=6)
            listActivatedADCs.add(i);
    }
    int[] arrayActivatedADCs = new int[listActivatedADCs.size()];
    for (int i=0;i<listActivatedADCs.size();i++){
        arrayActivatedADCs[i]=listActivatedADCs.get(i);
    } 
    return arrayActivatedADCs;
  }
  
  /**
  * Writes a pixe spectra stack
  * @param path
  * @param stack 
  */
  public void writePixeStack (String path, double [] stack){
      try{
                  PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path)));
                  out.println("4096 0");
                  for (int i=0;i<4096;i++) {
                          out.println(String.valueOf((int)stack[i]));
                  }
                  out.close();
          }
          catch (IOException e){
          }
  }
  private void readAndDisplayMpaFile(File file) {
      try (BufferedReader br = new BufferedReader(new FileReader(file))) {

          // Read all lines into memory for sequential parsing
          ArrayList<String> lines = new ArrayList<>();
          String line;
          while ((line = br.readLine()) != null) {
              lines.add(line.trim());
          }

          // -----------------------------------------------------------
          // Pass 1: parse [ADCi] header sections (i is 1-based in file)
          //   adcRange[i]  = range for ADC i+1, stored at 0-based index i
          //   adcActive[i] = true when active=2
          //   ADC7 -> 0-based index 6 -> image width
          //   ADC8 -> 0-based index 7 -> image height
          // -----------------------------------------------------------
          int[]     adcRange  = new int[16];
          boolean[] adcActive = new boolean[16];
          int imgWidth  = 1;
          int imgHeight = 1;
          int currentAdc = -1; // 0-based index of ADC section being parsed

          for (String l : lines) {
              String lLower = l.toLowerCase();

              // Detect [ADCi] section header
              if (l.matches("(?i)\\[ADC(\\d+)\\]")) {
                  currentAdc = Integer.parseInt(l.replaceAll("(?i)[^0-9]", "")) - 1;
                  continue;
              }
              // Stop header parsing at first data/map section
              if (lLower.startsWith("[data") || lLower.startsWith("[cdat")) {
                  break;
              }
              // Parse key=value pairs inside [ADCi] blocks
              if (currentAdc >= 0 && currentAdc < 16 && l.contains("=")) {
                  int eqIdx  = l.indexOf('=');
                  String key = l.substring(0, eqIdx).trim().toLowerCase();
                  String val = l.substring(eqIdx + 1).trim();
                  // Strip trailing spaces or comments after the value
                  int spaceIdx = val.indexOf(' ');
                  if (spaceIdx > 0) val = val.substring(0, spaceIdx);
                  try {
                      if (key.equals("range")) {
                          adcRange[currentAdc] = Integer.parseInt(val);
                          // ADC7 (index 6) -> width, ADC8 (index 7) -> height
                          if (currentAdc == 6) imgWidth  = adcRange[6];
                          if (currentAdc == 7) imgHeight = adcRange[7];
                      } else if (key.equals("active")) {
                          adcActive[currentAdc] = (Integer.parseInt(val) == 2);
                      }
                  } catch (NumberFormatException ignored) {}
              }
          }

          IJ.log("  Image dimensions (ADC7 x ADC8): " + imgWidth + " x " + imgHeight);
          String fileName = file.getName();

          // -----------------------------------------------------------
          // Pass 2: locate [DATAi, N] and [CDATj, M] section markers
          //   and read the integer values that immediately follow.
          //
          //   [DATAi, N] : i is 0-based ADC index in file
          //                (DATA6 = ADC index 6 = "ADC7" in user terms)
          //   [CDATj, M] : j is 0-based map index
          //                values are in row-major order: pixel(x,y) = values[y*width + x]
          // -----------------------------------------------------------
          int mapCount = 0; // number of CDAT sections encountered

          for (int li = 0; li < lines.size(); li++) {
              String l      = lines.get(li);
              String lLower = l.toLowerCase();

              // --- Spectrum section: [DATAi, N] ---
              if (lLower.matches("\\[data\\d+,\\s*\\d+\\s*\\]")) {
                  String inner   = l.substring(1, l.length() - 1); // strip [ ]
                  String[] parts = inner.split(",");
                  // adcIdx is 0-based: DATA6 -> user ADC7
                  int adcIdx  = Integer.parseInt(parts[0].trim().replaceAll("(?i)[^0-9]", ""));
                  int nValues = Integer.parseInt(parts[1].trim());

                  double[] spectrum = new double[nValues];
                  int read = 0;
                  while (read < nValues && li + 1 < lines.size()) {
                      li++;
                      String v = lines.get(li).trim();
                      if (v.isEmpty()) continue;
                      if (v.startsWith("[")) { li--; break; } // next section reached
                      try { spectrum[read++] = Double.parseDouble(v); }
                      catch (NumberFormatException ignored) {}
                  }

                  // Build an ADC from the counts array, then a Spectra for FrameSpectra display
                  // adcIdx is 0-based (DATA6 = ADC index 6 = "ADC7" in user terms)
                  String title = fileName + "  ADC" + (adcIdx + 1)
                                 + "  [" + nValues + " ch]";
                  IJ.log("  Plotting spectrum: " + title);
                  ADC adcFromFile = new ADC(spectrum, nValues);
                  Spectra sp = new Spectra(adcFromFile, file.getAbsolutePath());
                  sp.plot(title, title).setVisible(true);
              }

              // --- Map section: [CDATj, M] ---
              else if (lLower.matches("\\[cdat\\d+,\\s*\\d+\\s*\\]")) {
                  String inner   = l.substring(1, l.length() - 1);
                  String[] parts = inner.split(",");
                  int nValues    = Integer.parseInt(parts[1].trim());

                  int[] pixels = new int[nValues];
                  int read = 0;
                  while (read < nValues && li + 1 < lines.size()) {
                      li++;
                      String v = lines.get(li).trim();
                      if (v.isEmpty()) continue;
                      if (v.startsWith("[")) { li--; break; }
                      try { pixels[read++] = Integer.parseInt(v); }
                      catch (NumberFormatException ignored) {}
                  }

                  String title = fileName + "  Map" + mapCount;
                  IJ.log("  Displaying map: " + title
                         + "  (" + imgWidth + " x " + imgHeight + ")");
                  displayMap(pixels, imgWidth, imgHeight, title);
                  mapCount++;
              }
          }

      } catch (IOException e) {
          IJ.log("readAndDisplayMpaFile - IOException: " + e.getMessage());
      } catch (Exception e) {
          IJ.log("readAndDisplayMpaFile - Error: " + e.getMessage());
      }
  }
    /**
   * Builds a ShortProcessor image from a flat pixel array and displays it
   * as an ImagePlus window in FIJI with automatic contrast enhancement (0.35%).
   * The array is assumed to be row-major: pixel(x,y) = pixels[y * width + x].
   *
   * @param pixels   flat array of integer pixel values (length = width * height)
   * @param width    image width  in pixels (from ADC7 range, 0-based index 6)
   * @param height   image height in pixels (from ADC8 range, 0-based index 7)
   * @param title    window title shown in FIJI
   */
  private void displayMap(int[] pixels, int width, int height, String title) {
      try {
          // Safety fallback if header dimensions were not found
          if (width  <= 0) width  = (int) Math.sqrt(pixels.length);
          if (height <= 0) height = (int) Math.sqrt(pixels.length);

          ImageProcessor ip = new ShortProcessor(width, height);
          for (int y = 0; y < height; y++) {
              for (int x = 0; x < width; x++) {
                  int idx = y * width + x;
                  if (idx < pixels.length) ip.set(x, y, pixels[idx]);
              }
          }
          ImagePlus imp = new ImagePlus(title, ip);
          ContrastEnhancer ce = new ContrastEnhancer();
          ce.stretchHistogram(imp, 0.35);
          imp.show();
      } catch (Exception e) {
          IJ.log("displayMap - Error: " + e.getMessage());
      }
  }
  public void selectAndDisplayMpaTextFiles() {
      try {
          PrefsManager localPrefs = new PrefsManager();
          localPrefs.setPreference();

          JFileChooser jF = new JFileChooser();
          File myDir = new File(localPrefs.getLastUsedDirectory());
          jF.addChoosableFileFilter(new FileNameExtensionFilter("MPA text file", "mpa"));
          jF.setFileFilter(jF.getChoosableFileFilters()[1]);
          jF.setCurrentDirectory(myDir);
          jF.setApproveButtonText("OK");
          jF.setMultiSelectionEnabled(true);
          jF.showOpenDialog(null);

          File[] selectedFiles = jF.getSelectedFiles();
          if (selectedFiles == null || selectedFiles.length == 0) return;

          for (File f : selectedFiles) {
              localPrefs.saveDirectory(f.getAbsolutePath());
              IJ.log("Reading MPA file: " + f.getAbsolutePath());
              readAndDisplayMpaFile(f);
          }

          IJ.showStatus("MPA files loaded.");

      } catch (HeadlessException e) {
          IJ.log("selectAndDisplayMpaTextFiles - HeadlessException: " + e.getMessage());
      }
  }
}


