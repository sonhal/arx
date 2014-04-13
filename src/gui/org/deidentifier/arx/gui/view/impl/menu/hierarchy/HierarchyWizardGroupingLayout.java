package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import java.util.Arrays;
import java.util.List;

import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyWizardModelGrouping.HierarchyWizardGroupingGroup;
import org.deidentifier.arx.gui.view.impl.menu.hierarchy.HierarchyWizardModelGrouping.HierarchyWizardGroupingInterval;

/**
 * 
 * Layouts the tree shown in the wizard
 * @author Fabian Prasser
 *
 * @param <T>
 */
public class HierarchyWizardGroupingLayout<T> {

    /** Constant */
    public static final int                       PRETTY_THRESHOLD = 100;

    /** Var */
    private final HierarchyWizardModelGrouping<T> model;
    /** Var */
    private boolean                               pretty           = true;

    /**
     * Creates a new instance
     * @param model
     */
    public HierarchyWizardGroupingLayout(HierarchyWizardModelGrouping<T> model){
        this.model = model;
    }

    /**
     * Is the layout pretty
     * @return
     */
    public boolean isPretty(){
        return pretty;
    }
    
    /**
     * Computes the cardinalities of the optimal number of elements per hierarchy level
     * @return
     */
    public int[] layout() {
        
        // Init
        boolean showIntervals = model.isShowIntervals();
        List<HierarchyWizardGroupingInterval<T>> intervals = model.getIntervals();
        List<List<HierarchyWizardGroupingGroup<T>>> groups = model.getModelGroups();

        // Size of the solution
        int size = showIntervals ? 1 + groups.size() : groups.size();
        
        // Init elements, sum, cardinality
        int[] sum = new int[size];
        int[] pointer = new int[size];
        int[] cardinality = new int[size];
        int[] base = new int[size];
        int[][] elements = new int[size][];
        
        // Init from intervals
        if (showIntervals) {
            elements[0] = new int[intervals.size()];
            Arrays.fill(elements[0], 1);
            cardinality[0] = intervals.size();
            sum[0] = intervals.size();
        } 
        
        // Init from groups
        for (int i=0; i < groups.size(); i++){
            
            // Prepare
            int index = showIntervals ? i +1 : i;
            List<HierarchyWizardGroupingGroup<T>> groups2 = groups.get(i);
            
            // Prepare cardinality
            cardinality[index] = groups2.size();
            
            // Prepare elements and sum
            int[] array = new int[groups2.size()];
            elements[index] = array;
            for (int j=0; j<array.length; j++){
                array[j] = groups2.get(j).size;
                sum[index]+=groups2.get(j).size;
            }
        }
        
        // Prepare base cardinality
        System.arraycopy(cardinality, 0, base, 0, base.length);
        
        // Prepare flags
        boolean repeat = true;
        pretty = true;
        
        // Repeat
        while (repeat) {
            
            // Do not repeat
            repeat = false;
            
            // Sweep right to left
            for (int i=cardinality.length-1; i>0; i--){
                while (cardinality[i-1] < sum[i]) {
                    repeat = true;
                    sum[i-1]+=elements[i-1][pointer[i-1]++];
                    if (pointer[i-1]==base[i-1]) pointer[i-1]=0;
                    cardinality[i-1]++;
                }
            }
            
            // Sweep left to right
            for (int i=0; i<cardinality.length-1; i++){
                while (cardinality[i] > sum[i+1]) {
                    repeat = true;
                    sum[i+1]+=elements[i+1][pointer[i+1]++];
                    if (pointer[i+1]==base[i+1]) pointer[i+1]=0;
                    cardinality[i+1]++;
                }
            }
            
            // Check if still pretty
            for (int i=0; i<cardinality.length; i++){
                if (cardinality[i] > PRETTY_THRESHOLD) {
                    pretty = false;
                    repeat = false;
                }
            }
        }
        
        // Return
        if (!pretty) {
            return base;
        } else {
            return cardinality;
        }
    }
}
