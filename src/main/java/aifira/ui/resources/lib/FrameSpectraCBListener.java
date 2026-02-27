package aifira.ui.resources.lib;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;



import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.ui.RectangleInsets;

import java.awt.Color;
import java.awt.Font;

/**
 * CheckBoxListener is the class responsible for the events related
 * with the checked / unchecked state of the checkboxes indicating the Min and MlastCheckBoxActivatedax spectra values.
 * It will draw lines on the Spectra where the user has choosed energies
 */
public class FrameSpectraCBListener implements ItemListener,DocumentListener{
     private final FrameSpectra sourceFrame;
     private final ArrayList<JCheckBox> checkBoxList = new ArrayList<>();
     private final ArrayList<int[]> colorList = new ArrayList<>(); //color corresponding to the checkboxes

     private JCheckBox lastUsedCheckBox; 

    private class ROIEntry {
        IntervalMarker marker;
        int[] color;
        float position;
        JCheckBox checkBox; // ✅ lier directement la checkbox
    
        ROIEntry(IntervalMarker marker, int[] color, float position, JCheckBox checkBox){
            this.marker = marker;
            this.color = color;
            this.position = position;
            this.checkBox = checkBox;
        }
    }
    private ArrayList<ROIEntry> roiEntries = new ArrayList<>(); 
    /**
     *
     * @param sourceFrame
     */
    public FrameSpectraCBListener(FrameSpectra sourceFrame) {
      this.sourceFrame= sourceFrame;    
   }
   
    /**
     *
     * @return
     */
    public JCheckBox getLastUsedCheckBox(){
       return lastUsedCheckBox;
   }
  
    /**
     * Method to check if the generated color is available to be used as marker
     * @param R
     * @param G
     * @param B
     * @param numberOfCall
     * @return
     */
public boolean isColorAvailable(int R, int G, int B, int numberOfCall){
    if(numberOfCall > 50) return true; // éviter boucle infinie
    
    for(ROIEntry entry : roiEntries){
        int[] c = entry.color;
        if(c[0] == R && c[1] == G && c[2] == B) return false;
    }
    return true;
}
   
   
   public void drawROIZone(float minPosition, float maxPosition, JCheckBox CurrentCheckbox, String roiName){
    IntervalMarker marker = new IntervalMarker(minPosition, maxPosition);
    
    Color color;
    int R=0, G=0, B=0;
    
    if(checkBoxList.contains(CurrentCheckbox)){
        int index = checkBoxList.indexOf(CurrentCheckbox);
        int[] colorRGB = colorList.get(index);
        R = colorRGB[0];
        G = colorRGB[1];
        B = colorRGB[2];
    } else {
        int numberOfCall=0;
        while(!isColorAvailable(R,G,B,numberOfCall)){
            R = randomInteger(100, 200);
            G = randomInteger(120, 220);
            B = randomInteger(120, 220);
            numberOfCall++;
        }
    }
    
    color = new Color(R, G, B);
    marker.setPaint(color);
    marker.setAlpha(0.2f);
    marker.setLabel(roiName.isEmpty() ? null : roiName);
    marker.setLabelAnchor(RectangleAnchor.TOP);
    marker.setLabelTextAnchor(TextAnchor.CENTER);
    marker.setLabelOffset(new RectangleInsets(10, 0, 0, 0)); // top, left, bottom, right
    marker.setLabelFont(new Font("Arial", Font.PLAIN, 12));
    
    JFreeChart chart = sourceFrame.getChart();
    XYPlot XYPlotOfChart = (XYPlot) chart.getPlot();
    
    // ✅ Sans Layer
    XYPlotOfChart.addDomainMarker(marker);
    
    int[] usedColor = new int[3];
    usedColor[0] = R;
    usedColor[1] = G;
    usedColor[2] = B;
    
    roiEntries.add(new ROIEntry(marker, usedColor, minPosition, CurrentCheckbox));
    
    if (!checkBoxList.contains(CurrentCheckbox)){
        checkBoxList.add(CurrentCheckbox);
        colorList.add(usedColor);            
    }
}
 public void removeMarker(Marker marker, JCheckBox checkBox){
    JFreeChart chart = sourceFrame.getChart();
    XYPlot XYPlotOfChart = (XYPlot) chart.getPlot();
    
    if (marker instanceof IntervalMarker) {
        XYPlotOfChart.removeDomainMarker((IntervalMarker) marker); // ✅ domain pas range
    } else if (marker instanceof ValueMarker) {
        XYPlotOfChart.removeDomainMarker((ValueMarker) marker);
    }
    // ✅ Plus besoin de supprimer des listes ici, c'est géré par removeIf dans update
}
    /**
     * this method removes the marker used as bouding line on the dispayed spectra's front layer.
     * @param position
     * @param checkBoxCurrent 
     */
    
    
    
    /**
     * This method is called each time that a checkbox is selected/unselected 
     * @param e
     */
     @Override
    public void itemStateChanged(ItemEvent e) {
        update(e.getSource());
    }
        
    /**
     *
     * @param objectActivated
     */
    public void update(Object objectActivated){
    ArrayList<JComponent[]> vectButtons = sourceFrame.getVectButtonsSupp();
    
    for (int i = 0; i < vectButtons.size(); i++) {
        JComponent[] tabJCompToCheck = (JComponent[]) vectButtons.get(i);
        JCheckBox checkBoxCurrent = (JCheckBox) tabJCompToCheck[0];
        
        if(objectActivated == checkBoxCurrent){
            System.out.println("DEBUG: Update ROI " + (i+1));
            
            if(checkBoxCurrent.isSelected())
                lastUsedCheckBox = checkBoxCurrent;
            
            // Supprimer les anciens markers de cette couleur
              roiEntries.removeIf(entry -> {
                  if(entry.checkBox == checkBoxCurrent){
                      removeMarker(entry.marker, checkBoxCurrent);
                      return true;
                  }
                  return false;
              });
            
            // ✅ Récupérer min ET max
            float minValue = -1;
            float maxValue = -1;
            
            for (int j = 0; j < 2; j++){
                JTextField textFieldMinMax = (JTextField) tabJCompToCheck[j+2];
                if ( !(textFieldMinMax.getText().equals("Min") || textFieldMinMax.getText().equals("Max")) ) {
                    try {
                        float value = Float.valueOf(textFieldMinMax.getText());
                        if (j == 0) {
                            minValue = value;  // Min
                        } else {
                            maxValue = value;  // Max
                        }
                    } catch(NumberFormatException e2){}
                }
            }
            String roiName = "";
            JComponent nameComp = tabJCompToCheck[1];
            if(nameComp instanceof JTextField){
                roiName = ((JTextField) nameComp).getText();
            } else if(nameComp instanceof JTextField){
                roiName = ((JTextField) nameComp).getText();
}
            // ✅ Afficher la zone si les deux valeurs sont valides
            if (minValue > -1 && maxValue > -1 && checkBoxCurrent.isSelected()) {
                if (minValue < maxValue) {
                    drawROIZone(minValue, maxValue, checkBoxCurrent, roiName);
                }
            }
        }
    }
}
    
    /**
     * @param min
     * @param max
     * @return a randomly generated number between min and max (max included)
     */
    public static int randomInteger(int min, int max) {
        // Usually this can be a field rather than a method variable
        Random rand = new Random();
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    /**
     *
     * @param dEvt
     */
    public void handlesDocumentEvt(DocumentEvent dEvt){
        int lengthText = dEvt.getDocument().getLength();
        try{
            String text = dEvt.getDocument().getText(0,lengthText);
            JCheckBox checkBoxToUpdate = sourceFrame.getCheckBoxRelatedToField(text);
            if(checkBoxToUpdate!=null && checkBoxToUpdate.isSelected()){
                update(checkBoxToUpdate);
            }
        }
        catch(BadLocationException e){}
    }
    
     @Override
    public void insertUpdate(DocumentEvent dEvt) {
        handlesDocumentEvt(dEvt);
    }

     @Override
    public void removeUpdate(DocumentEvent dEvt) {
        handlesDocumentEvt(dEvt);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        //not used
    }
    
}
