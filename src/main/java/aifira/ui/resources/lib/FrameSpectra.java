package aifira.ui.resources.lib;

import aifira.ui.GeneratedMap.GeneratedMap;
import aifira.ui.mainframe.MainFrame;
import aifira.ui.Spectra.Spectra;
import aifira.ui.Prefs.PrefsManager;
import ij.*;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Class FrameSpectra is the class representing the spectra management window
 */
public class FrameSpectra extends JFrame {
    
    private final String graphTitle;
    private final Spectra spectra;
    private final JFreeChart chart;
    private static MainFrame parentWindow;
    private boolean yIsLog=false;
    private FrameSpectraCBListener checkBoxListener;
    private final CustomChartPanel chartPanel;
    private PrefsManager prefsManager;
    
    /**
     *
     * @param spectra
     * @param windowTitle
     * @param graphTitle
     * @param energiesX
     * @param dataY
     * @param nROI
     */
    public FrameSpectra(Spectra spectra, final String windowTitle,final String graphTitle, double[] energiesX, double[] dataY,int nROI) {
        super(windowTitle);
        prefsManager = new PrefsManager();
        this.parentWindow=spectra.getParentWindowS();
        this.graphTitle=graphTitle;
        this.spectra=spectra;
        //create the dataset
        final XYDataset dataset = createDataset(energiesX,dataY);
        //create the chart
        final JFreeChart myChart = createChart(dataset,graphTitle,getEnergyLabel());
        this.chart = myChart;
        this.chartPanel= new CustomChartPanel(myChart,this);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        jButtonLogLinActionPerformed(null);
        initComponents(nROI);
    }
    
    /**
     *
     * @return
     */
public ArrayList<JComponent[]> getVectButtonsSupp(){
    return vectButtonsSupp;
}
public ArrayList<JButton> getButtonList(){
    return buttonList;
}
    
    
    /**
     *
     * @return
     */
    public JFreeChart getChart(){
        return chart;
    }
    /**
     * Creates the dataset of the Spectra
     * @returns a dataset.
     */
    private static XYDataset createDataset(double[] energiesX,double[] dataY) {
        final XYSeries series1 = new XYSeries(translate("Spectra")+" 1");
        for(int i=0;i<energiesX.length;i++){
            if(dataY[i]>0)
                series1.add(energiesX[i],dataY[i]);
        }
        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        return dataset;
    }
    
    /**
     * Creates a chart.
     * @param dataset  the data for the chart.
     * @returns a chart.
     */
    private static JFreeChart createChart(final XYDataset dataset, final String titleGraph2, final String titleAxisX) {
        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
            titleGraph2,      // chart title
translate(titleAxisX),                      // x axis label
translate("Events number"),              // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL, // plot orientation (pretty obvious this one)
            false,                    // if true show the legeng (useful for several lignes)
            true,                     // tooltips
            false                     // URLs
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);
        //final StandardLegend legend = (StandardLegend) chart.getLegend();
        //legend.setDisplaySeriesShapes(true);
        
        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        //plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);
        
        //code for points and no lines on the chart
        //final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        //renderer.setSeriesLinesVisible(0, false);
        //renderer.setSeriesShapesVisible(1, false);
        //plot.setRenderer(renderer);

        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        // OPTIONAL CUSTOMISATION COMPLETED.
        return chart;
    }
    
    /**
     * Use this method to show the window to the user
     */
public void showVisible() {
    pack();
    setLocationRelativeTo(null);  // 
    setVisible(true);
}
    
    /**
     *
     * @param evt
     */
    public void closeWindow(final WindowEvent evt){
        if(evt.getWindow() == this){
            dispose();
        }
    }
    
    /**
     * This method will link each CheckBox to FrameSpectraCBListener
     * @see FrameSpectraCBListener
     */
    private void initComponents(int nROI) {
        
        int nAddedField=vectButtonsSupp.size()+1;
        checkBoxListener = new FrameSpectraCBListener(this);
        int nbFieldsToAdd=nROI-vectButtonsSupp.size();
        if (nbFieldsToAdd>0){
            if((vectButtonsSupp.size()%3)!=0){
                nbFieldsToAdd=3-(vectButtonsSupp.size()%3);
                for(int j=0;j<nbFieldsToAdd;j++){
                    addField(nAddedField);
                    nAddedField+=1;
                }
            }
            nbFieldsToAdd=nROI-vectButtonsSupp.size();
            int nbLinesToAdd=nbFieldsToAdd/3;
            if((nbFieldsToAdd%3)!=0)
                nbLinesToAdd+=1;
            int nbFieldsPerLine=3;
            for (int i=0; i<nbLinesToAdd;i++){
                if (i==nbLinesToAdd-1)  nbFieldsPerLine=(nbFieldsToAdd%3);
                if (nbFieldsPerLine==0) nbFieldsPerLine=3;
                for(int j=0;j<nbFieldsPerLine;j++){
                    addField(nAddedField);
                    nAddedField+=1;
                }
            }
        }
        //setSelectedCheckBox();
        jButtonPlus = new JButton();
        jButtonPlus.setText(translate("+ ROI ..."));
        
        jButtonPlus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMoreActionPerformed(evt);
            }
        });
        jButtonGenImg = new JButton();
        jButtonGenImg.setText(translate("Generate maps"));
        jButtonGenImg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGenImgActionPerformed(evt);
            }
        });
        
        jButtonLogLin = new JButton();
        jButtonLogLin.setText(translate("Log/Lin"));
        jButtonLogLin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLogLinActionPerformed(evt);
            }
        });
        
        jButtonSave = new JButton();
        jButtonSave.setText("Save prefs");
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });
        
        jButtonSaveGup = new JButton();
        jButtonSaveGup.setText(translate("Save Gupix"));
        jButtonSaveGup.addActionListener(new java.awt.event.ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveGupActionPerformed(evt);
        }
        });
        JPanel panel = new JPanel();
        remove(getContentPane());
        setContentPane(panel);
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        
        // horizontal Layout
        ParallelGroup paralGroupHor=layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        SequentialGroup grpChartPanel=layout.createSequentialGroup();
        grpChartPanel.addComponent(chartPanel);
        paralGroupHor.addGroup(grpChartPanel);
        
        //panel for fields
       
        JPanel panelFields = new JPanel();
        GroupLayout layoutPanelFields = new GroupLayout(panelFields);
        panelFields.setLayout(layoutPanelFields);
        ParallelGroup paralGroupHorPanelFields=layoutPanelFields.createParallelGroup(GroupLayout.Alignment.LEADING);
        int nbLines=nROI/3;
        if (nROI%3>0) nbLines+=1;
        int nbFieldsPerLine=3;
        //IJ.log("N retrieved ROI from Prefs : "+String.valueOf(nROI));
        for (int i=0; i<nbLines;i++){
            
            SequentialGroup grp1 = layoutPanelFields.createSequentialGroup();
            grp1.addContainerGap();
            if (i==nbLines-1)
                nbFieldsPerLine=(nROI%3);
            if (nbFieldsPerLine==0)
                nbFieldsPerLine=3;
            for(int j=0;j<nbFieldsPerLine;j++){        
                int currentField=i*3+j;
                JComponent[] tablJComp = vectButtonsSupp.get(currentField);
                JCheckBox checkBoxCurrent = (JCheckBox) tablJComp[0];
                JTextField textFieldCurrentName= (JTextField) tablJComp[1];
                JTextField textFieldCurrentMin= (JTextField) tablJComp[2];
                JTextField textFieldCurrentMax=(JTextField) tablJComp[3];
                grp1.addComponent(checkBoxCurrent, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE);
                grp1.addPreferredGap(ComponentPlacement.RELATED);
                grp1.addComponent(textFieldCurrentName, 20, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
                grp1.addPreferredGap(ComponentPlacement.RELATED);
                grp1.addComponent(textFieldCurrentMin, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE);
                grp1.addPreferredGap(ComponentPlacement.RELATED);
                grp1.addComponent(textFieldCurrentMax, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE);
                if (j!=3){
                    grp1.addGap(40,40,40);
                }
                else{
                    grp1.addContainerGap(25, Short.MAX_VALUE);
                }
            }
            paralGroupHorPanelFields.addGroup(grp1);
        }
        JScrollPane scroll = new JScrollPane(panelFields,JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        if(nROI>15){//so enable scrolling
            scroll = new JScrollPane(panelFields,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);    
            scroll.setPreferredSize(new Dimension(320,260));
        }
        scroll.setBorder(null);
        SequentialGroup grpPanelFields=layout.createSequentialGroup();
        grpPanelFields.addComponent(scroll);
        paralGroupHor.addGroup(grpPanelFields);
        //layout horizontal for buttons
        SequentialGroup grpButton = layout.createSequentialGroup();
        grpButton.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        //grpButton.addGap(130);
        grpButton.addComponent(jButtonPlus, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE);
        grpButton.addPreferredGap(ComponentPlacement.RELATED);
        grpButton.addComponent(jButtonGenImg, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE);
        grpButton.addPreferredGap(ComponentPlacement.RELATED);
        grpButton.addComponent(jButtonLogLin, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE);
        grpButton.addPreferredGap(ComponentPlacement.RELATED);
        grpButton.addComponent(jButtonSave, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE);
        grpButton.addPreferredGap(ComponentPlacement.RELATED);
        grpButton.addComponent(jButtonSaveGup, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE);
        grpButton.addGap(70,70,70);
        paralGroupHor.addGroup(GroupLayout.Alignment.TRAILING, grpButton);
        
        
        //vertical Layout  
        ParallelGroup paralGroup2Vert=layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        SequentialGroup grpAll = layout.createSequentialGroup();
        ParallelGroup gpChart = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
        gpChart.addComponent(chartPanel);
        grpAll.addGroup(gpChart);
        
        ParallelGroup paralGroup2VertPanFields=layoutPanelFields.createParallelGroup(GroupLayout.Alignment.LEADING);
        SequentialGroup grpAllFields = layoutPanelFields.createSequentialGroup();
        nbFieldsPerLine=3;
        for (int i=0; i<nbLines;i++){
            ParallelGroup grpIntChannel = layoutPanelFields.createParallelGroup(GroupLayout.Alignment.BASELINE);
            if (i==nbLines-1)
                nbFieldsPerLine=(nROI%3);
            if (nbFieldsPerLine==0)
                nbFieldsPerLine=3;
            for(int j=0;j<nbFieldsPerLine;j++){
                int currentField=i*3+j;
                JComponent[] tablJComp = (JComponent[]) vectButtonsSupp.get(currentField);
                JCheckBox checkBoxCurrent = (JCheckBox) tablJComp[0];
                JTextField textFieldCurrentName= (JTextField) tablJComp[1];
                JTextField textFieldCurrentMin= (JTextField) tablJComp[2];
                JTextField textFieldCurrentMax= (JTextField) tablJComp[3];
                grpIntChannel.addComponent(checkBoxCurrent);
                grpIntChannel.addComponent(textFieldCurrentName,GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE);
                grpIntChannel.addComponent(textFieldCurrentMin,GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE);
                grpIntChannel.addComponent(textFieldCurrentMax,GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE);
            }
            grpAllFields.addContainerGap(10, Short.MAX_VALUE).addGroup(grpIntChannel);
            grpAllFields.addPreferredGap(ComponentPlacement.RELATED);
        }
        grpAllFields.addGap(6, 6, 6);
        paralGroup2VertPanFields.addGroup(GroupLayout.Alignment.TRAILING,grpAllFields);
        
        ParallelGroup grpPanFields = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
        grpPanFields.addComponent(scroll);
        grpAll.addGroup(grpPanFields);
     
        ParallelGroup grpButtons = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
        grpButtons.addComponent(jButtonPlus, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE);
        grpButtons.addComponent(jButtonGenImg, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE);
        grpButtons.addComponent(jButtonLogLin, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE);
        grpButtons.addComponent(jButtonSave, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE);
        grpButtons.addComponent(jButtonSaveGup, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE);
        grpAll.addContainerGap(10, Short.MAX_VALUE).addGroup(grpButtons);
        grpAll.addGap(6, 6, 6);
      
        paralGroup2Vert.addGroup(GroupLayout.Alignment.TRAILING,grpAll);
        
        layoutPanelFields.setHorizontalGroup(paralGroupHorPanelFields);
        layoutPanelFields.setVerticalGroup(paralGroup2VertPanFields);
        layout.setHorizontalGroup(paralGroupHor);
        layout.setVerticalGroup(paralGroup2Vert);
        pack();
    }                 
    
    private void addField(int ROI){
        
        
        JComponent[] buttonsToAdd= new JComponent[4];
        buttonsToAdd[0] = new JCheckBox();
        buttonsToAdd[1] = new JTextField();
        buttonsToAdd[2] = new JTextField();
        buttonsToAdd[3] = new JTextField();
        vectButtonsSupp.add(buttonsToAdd);
        JCheckBox checkBoxCurrent = (JCheckBox) buttonsToAdd[0];
        checkBoxCurrent.setSelected(prefsManager.getROIState(ROI));
        JTextField textFieldCurrentName= (JTextField) buttonsToAdd[1];
        JTextField textFieldCurrentMin= (JTextField) buttonsToAdd[2];
        JTextField textFieldCurrentMax=(JTextField) buttonsToAdd[3];
        checkBoxCurrent.setText(translate("NA"));
        textFieldCurrentName.setText(prefsManager.ijGetValue("IBA.roi"+String.valueOf(ROI)+".name","?"));
        textFieldCurrentMin.setText(prefsManager.ijGetValue("IBA.roi"+String.valueOf(ROI)+".min",Integer.toString(1)));
        textFieldCurrentMax.setText(prefsManager.ijGetValue("IBA.roi"+String.valueOf(ROI)+".max",Integer.toString(4095)));
        checkBoxCurrent.addItemListener(checkBoxListener);
        textFieldCurrentMin.getDocument().addDocumentListener(checkBoxListener);
        textFieldCurrentMax.getDocument().addDocumentListener(checkBoxListener);
        updateLinesOnChart(checkBoxCurrent);
    }
    private void jButtonMoreActionPerformed(java.awt.event.ActionEvent evt) {                                           
        initComponents(vectButtonsSupp.size()+1);
    }   
    
    private void jButtonGenImgActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // generates images for all  selected checkbox
        //First which checkbox is selected
        // number of checkboxes = vectButtonsSupp.size()
        // the whole is taken if JComp[0] is selected and 1,2 et 3 are corrects
        try {
            GeneratedMap[] roiMap;
            RoiProperties roiProperties = getRoiProperties(false,true,true);
            ArrayList<String> mapNames = roiProperties.names;
            ArrayList<float[]> roiLimits = roiProperties.limits;
            float[][] roiLimitsArray = roiLimits.toArray(new float[roiLimits.size()][2]);
            if(roiLimitsArray.length>0){
                roiMap = spectra.elementMaps(roiLimitsArray);
                GeneratedMap defaultsMap=roiMap[0];
                defaultsMap.setTitle(mapNames.get(0));
                int stackSize=roiMap.length-1;
                if(stackSize>0){//check if more than one image to show
                    GeneratedMap[] mapStack = new GeneratedMap[stackSize];
                    for (int i=0;i<stackSize;i++){
                        mapStack[i]=roiMap[i+1];
                        mapStack[i].setTitle(mapNames.get(i+1));
                    }
                    defaultsMap.showMaps(mapStack);
                }
                else{
                    defaultsMap.show();
                }
            }
        }
        catch(NullPointerException e){}
    }
    
    /**
     *
     * @return
     */
    public ArrayList<JCheckBox> getSelectedCheckBox(){
        ArrayList<JCheckBox> selectedCheckboxes = new ArrayList<>();
        for (Object vectButtonsSupp1 : vectButtonsSupp) {
            JComponent[] jComponentTable;
            jComponentTable = (JComponent[]) vectButtonsSupp1;
            JCheckBox currentCheckbox = (JCheckBox) jComponentTable[0];
            if(currentCheckbox.isSelected()){
                selectedCheckboxes.add(currentCheckbox);
            }
        }
        return selectedCheckboxes;
    }

    /**
     *
     * @return
     */
    public JCheckBox getLastActivatedCheckBox(){
        return checkBoxListener.getLastUsedCheckBox();
    }
    
    /**
     *
     * @param checkBoxToUpdate
     */
    public void updateLinesOnChart(JCheckBox checkBoxToUpdate){
        checkBoxListener.update(checkBoxToUpdate);
    }
    
    /**
     * Gets the min, max and name values if checkboxs are selected
     * @param minOrMax if false min AND max have to be correct 
     * @param nameIsImportant if false the method will not check the name
     * @param showError
     * @return a vector containing the arraylist of the names and the arraylist of the [min,max]
     */
    public RoiProperties getRoiProperties(boolean minOrMax, boolean nameIsImportant, boolean showError) {
        ArrayList<String> roiNames = new ArrayList<>();
        ArrayList<float[]> roiLimits = new ArrayList<>();
        boolean errorShowed1=false, errorShowed2=false, errorShowed3=false, errorShowed4=false;
        for (Object vectButtonsSupp1 : vectButtonsSupp) {
            JComponent[] tabJCompToCheck = (JComponent[]) vectButtonsSupp1;
            JCheckBox checkBoxCurrent = (JCheckBox) tabJCompToCheck[0];
            if(checkBoxCurrent.isSelected()){
                JTextField name= (JTextField) tabJCompToCheck[1];
                JTextField min= (JTextField) tabJCompToCheck[2];
                JTextField max=(JTextField) tabJCompToCheck[3];
                if(!( (min.getText().equals(translate("Min")) && max.getText().equals(translate("Max"))) )){
                    if(minOrMax|| !((min.getText().equals(translate("Min")) || max.getText().equals(translate("Max"))))){
                        if ( !nameIsImportant || !(name.getText().equals(translate("Name"))) ) {
                            float start=-1;
                            float end=-1;
                            try {
                                start = Float.valueOf(min.getText());
                            }
                            catch(NumberFormatException e){
                                if(showError && !errorShowed1){
                                    errorShowed1=true;
                                    IJ.error(translate("Put numbers in the Min and Max text fields"));
                                }
                            }
                            try {
                                end = Float.valueOf(max.getText());
                            }
                            catch(NumberFormatException e){}
                            if(minOrMax||start<end){
                                if (spectra.isAvailable(start) || spectra.isAvailable(end)){
                                    if (!spectra.isAvailable(start)){
                                        start=spectra.getEnergies()[0];
                                    }
                                    else if(!spectra.isAvailable(end)) {
                                        int length = spectra.getEnergies().length;
                                        end=spectra.getEnergies()[length-1];
                                    }
                                    if( !nameIsImportant || !roiNames.contains(name.getText()) ){//useful to avoid saving problems
                                        if( !nameIsImportant || !(name.getText().contains("_")) ){//useful to avoid restoring problems
                                            roiNames.add(name.getText());
                                            float[] tabMinMax = new float[2];
                                            tabMinMax[0]=start;
                                            tabMinMax[1]=end;
                                            roiLimits.add(tabMinMax);
                                        }
                                        else{
                                            if(showError && !errorShowed1 && !errorShowed2){
                                                IJ.error(translate("The character '_' is not allowed"));
                                                errorShowed2=true;
                                            }
                                        }
                                    }
                                    else{
                                        if(showError && !errorShowed1 && !errorShowed2 && !errorShowed3){
                                            IJ.error(translate("Give different name for roi please"));
                                            errorShowed3=true;
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            if(showError && !errorShowed1 && !errorShowed2 && !errorShowed3 && !errorShowed4){
                                IJ.error(translate("Give names please (other that Name)"));
                                errorShowed4=true;
                            }
                        }
                    }
                }
            }
        }
return new RoiProperties(roiNames, roiLimits);
 }
   
    public ArrayList<String> getAllProperties(){
    ArrayList<String> properties = new ArrayList<>();
    
    for (int i = 0; i < vectButtonsSupp.size(); i++) {
        JComponent[] tabJCompToCheck = (JComponent[]) vectButtonsSupp.get(i);
        
        JCheckBox checkBoxCurrent = (JCheckBox) tabJCompToCheck[0];
        JTextField name = (JTextField) tabJCompToCheck[1];
        JTextField min = (JTextField) tabJCompToCheck[2];
        JTextField max = (JTextField) tabJCompToCheck[3];
        
        String state = checkBoxCurrent.isSelected() ? "true" : "false";
        String nameText = name.getText();
        String minText = min.getText();
        String maxText = max.getText();
        
        // ✅ Convertir en double pour accepter les floats
        try {
            double minVal = Double.valueOf(minText);
            minText = String.valueOf((int) minVal);  // Convertir en int
        } catch (NumberFormatException e) {
            System.out.println("ERREUR ROI " + (i+1) + ": min invalide = " + minText);
            minText = "1";
        }
        
        try {
            double maxVal = Double.valueOf(maxText);
            maxText = String.valueOf((int) maxVal);  // Convertir en int
        } catch (NumberFormatException e) {
            System.out.println("ERREUR ROI " + (i+1) + ": max invalide = " + maxText);
            maxText = "4095";
        }
        
        properties.add(state);
        properties.add(nameText);
        properties.add(minText);
        properties.add(maxText);
    }
    
    return properties;
}
    
    private String selectDirectory(){
        File selectedFile = null;
        PrefsManager prefs=new PrefsManager();
        prefs.setPreference();
        File myDir=new File(prefs.getLastUsedDirectory());
        JFileChooser fileChooser = new JFileChooser(myDir.getAbsolutePath());
        IJ.log("start "+prefs.getLastUsedDirectory());
        fileChooser.setCurrentDirectory(myDir);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fileChooser.showDialog(null,translate("Select directory"));
        if (option == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
             // if the user accidently clicks on a file, the parent directory is selected.
            if (!selectedFile.isDirectory()) {
                selectedFile = selectedFile.getParentFile();
                }
        }
        if (selectedFile!=null){
            prefs.saveDirectory(selectedFile.getAbsolutePath());
            return selectedFile.getAbsolutePath()+"/";}
        return myDir.getAbsolutePath();
    }
    
    private void jButtonLogLinActionPerformed(java.awt.event.ActionEvent evt) {                                         
        XYPlot plot = (XYPlot) chart.getPlot();
        if (!yIsLog){
            plot.setRangeAxis(new LogarithmicAxis("Yield (counts)"));
            yIsLog=true;
        }
        else{
            plot.setRangeAxis(new NumberAxis("Yield (counts)"));
            yIsLog=false;
        }
    }
    
private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {
    System.out.println("\n=== AVANT getAllProperties() ===");
    for (int i = 0; i < vectButtonsSupp.size(); i++) {
        JComponent[] tab = (JComponent[]) vectButtonsSupp.get(i);
        JCheckBox cb = (JCheckBox) tab[0];
        JTextField name = (JTextField) tab[1];
        JTextField min = (JTextField) tab[2];
        JTextField max = (JTextField) tab[3];
        System.out.println("ROI " + (i+1) + ": [" + cb.isSelected() + "] [" + name.getText() + "] [" + min.getText() + "] [" + max.getText() + "]");
    }
    
    try {
        ArrayList<String> roiProperties = getAllProperties();
        System.out.println("\n=== APRÈS getAllProperties() ===");
        System.out.println("Size: " + roiProperties.size());
        for (int i = 0; i < roiProperties.size(); i++) {
            System.out.println("[" + i + "] = " + roiProperties.get(i));
        }
        
        PrefsManager prefs = new PrefsManager();
        int nRoi = 1;
        for (int index = 0; index < roiProperties.size(); index += 4){
            System.out.println("SAVING ROI " + nRoi + ": " + roiProperties.get(index) + " " + roiProperties.get(index+1) + " " + roiProperties.get(index+2) + " " + roiProperties.get(index+3));
            prefs.saveRoiState(roiProperties.get(index), nRoi);
            prefs.saveRoiName(roiProperties.get(index+1), nRoi);
            prefs.saveRoiMin(roiProperties.get(index+2), nRoi);
            prefs.saveRoiMax(roiProperties.get(index+3), nRoi);
            nRoi += 1;
        }
        prefs.savePrefsNow();
        System.out.println("✓ Sauvegarde réussie !");
        
    } catch (Exception e) {
        System.out.println("❌ ERREUR: " + e.getMessage());
        e.printStackTrace();
    }
}
    
    /**
     * @see ADC.saveGupixSpectra 
     */
    private void jButtonSaveGupActionPerformed(java.awt.event.ActionEvent evt) {                                         
        String directory=selectDirectory();
        //IJ.log("Saving: ");
        if(directory!=null){
            File f=new File(spectra.getPath());
            String nameToSave=f.getName()+".gup";
            IJ.log("File saved: "+nameToSave);
            //IJ.log("Path: "+directory+nameToSave);
            spectra.getADC().saveGupixSpectra(directory+nameToSave);
        }
    }
    
    /**
     *
     * @param sentence
     * @return
     */
    public static String translate(String sentence){
        return MainFrame.translate(sentence);
    }
    
    /**
     *
     * @param value
     * @param rightIncluded
     * @return
     */
    public float getRealXValue(double value, boolean rightIncluded){
        if(spectra.isAvailable((float) value)){
            int indiceNearValue=spectra.getIndex((float)value,rightIncluded);
            return spectra.getEnergies()[indiceNearValue];
        }
        return -1;
    }
    
    /**
     *
     * @param checkBoxOfInterest
     * @param nameField
     * @return
     */
    public JTextField getField(JCheckBox checkBoxOfInterest,String nameField){
        for (Object vectButtonsSupp1 : vectButtonsSupp) {
            JComponent[] tabJComp = (JComponent[]) vectButtonsSupp1;
            JCheckBox checkBoxCurrent = (JCheckBox) tabJComp[0];
            if(checkBoxCurrent==checkBoxOfInterest){
                int indexOfInterest=-1;
                switch(nameField){
                    case "Name" : indexOfInterest=1;break;
                    case "Min" : indexOfInterest=2; break;
                    case "Max" : indexOfInterest=3;break;
                }
                if (indexOfInterest!=-1){
                    return (JTextField) tabJComp[indexOfInterest];
                }
            }
        }
        return null;
    }
    
    /**
     *
     * @param text
     * @return
     */
    public JCheckBox getCheckBoxRelatedToField(String text){
        for (Object vectButtonsSupp1 : vectButtonsSupp) {
            JComponent[] tabJComp = (JComponent[]) vectButtonsSupp1;
            for(int j=2;j<4;j++){
                JTextField currentTextField = (JTextField) tabJComp[j];
                if(currentTextField.getText().equals(text))
                    return (JCheckBox) tabJComp[0];
            }
        }
        return null;
    }
    
    /**
     *
     * @return
     */
    public String getEnergyLabel(){
        if(spectra.isScaled())
            return "Energy (keV)";
        return "Energy (channels)";
    }
        
    // Variables declaration - do not modify 
    private ArrayList<JComponent[]> vectButtonsSupp = new ArrayList<>();
    private ArrayList<JButton> buttonList = new ArrayList<>();
    private JButton jButtonPlus;
    private JButton jButtonGenImg;
    private JButton jButtonLogLin;
    private JButton jButtonSave;
    private JButton jButtonSaveGup;
    // End of variables declaration               
}

