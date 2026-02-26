package aifira.ui.resources.lib;

import java.util.ArrayList;

public class RoiProperties {
    public ArrayList<String> names;
    public ArrayList<float[]> limits;
    
    public RoiProperties(ArrayList<String> names, ArrayList<float[]> limits) {
        this.names = names;
        this.limits = limits;
    }
}