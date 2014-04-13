/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui.view.impl.menu.hierarchy;

import java.util.ArrayList;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.DataType.DataTypeDescription;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

public class HierarchyWizardPageOrder<T> extends HierarchyWizardPageBuilder<T> {

    private final HierarchyWizardModelOrder<T> model;
    private final Controller controller;
    private List list;
    private Combo combo;
    
    public HierarchyWizardPageOrder(final Controller controller,
                                       final HierarchyWizardModel<T> model, 
                                       final HierarchyWizardPageFinal<T> finalPage) {
        super(model.getOrderModel(), finalPage);
        this.model = model.getOrderModel();
        this.controller = controller;
        setTitle("Create a hierarchy by ordering and grouping items");
        setDescription("Specify the parameters");
        setPageComplete(true);
    }
    
    public void createControl(final Composite parent) {
        
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(SWTUtil.createGridLayout(2, false));
        createOrder(composite);
        createGroups(composite);
        setControl(composite);
    }

    private void createGroups(Composite parent){
        Group composite = new Group(parent, SWT.NONE);
        composite.setText("Groups");
        composite.setLayout(SWTUtil.createGridLayout(1, false));
        composite.setLayoutData(SWTUtil.createFillGridData());
        
        HierarchyWizardGroupingEditor<Long> component = 
                new HierarchyWizardGroupingEditor<Long>(composite, 
                        (HierarchyWizardModelGrouping<Long>) model);
        component.setLayoutData(SWTUtil.createFillGridData());
    }
    
    private void createOrder(Composite parent){
        Group composite = new Group(parent, SWT.NONE);
        composite.setText("Order");
        composite.setLayout(SWTUtil.createGridLayout(1, false));
        composite.setLayoutData(SWTUtil.createFillVerticallyGridData());
        
        list = new List(composite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        list.setLayoutData(SWTUtil.createFillGridData());
        for (String s : model.getData()) {
            list.add(s);
        }

        final Button up = new Button(composite, SWT.NONE);
        up.setText(Resources.getMessage("HierarchyWizardPageOrder.3")); //$NON-NLS-1$
        up.setImage(controller.getResources().getImage("arrow_up.png")); //$NON-NLS-1$
        up.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        up.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                actionUp();
            }
        });

        final Button down = new Button(composite, SWT.NONE);
        down.setText(Resources.getMessage("HierarchyWizardPageOrder.5")); //$NON-NLS-1$
        down.setImage(controller.getResources().getImage("arrow_down.png")); //$NON-NLS-1$
        down.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        down.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                actionDown();
            }
        });

        final Composite bottom1 = new Composite(composite, SWT.NONE);
        bottom1.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        bottom1.setLayout(SWTUtil.createGridLayout(2, false));

        final Label text = new Label(bottom1, SWT.NONE);
        text.setText(Resources.getMessage("HierarchyWizardPageOrder.7")); //$NON-NLS-1$

        combo = new Combo(bottom1, SWT.NONE);
        combo.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        combo.add(Resources.getMessage("HierarchyWizardPageOrder.8")); //$NON-NLS-1$
        for (String type : getDataTypes()){
            combo.add(type);
        }
        combo.select(0);
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                int index = combo.getSelectionIndex();
                if (index >=0 ){
                    combo.select(actionSort(index));
                }
            }
        });
    }
    

    /**
     * Returns a description for the given label
     * @param label
     * @return
     */
    private DataTypeDescription<?> getDataType(String label){
        for (DataTypeDescription<?> desc : DataType.list()){
            if (label.equals(desc.getLabel())){
                return desc;
            }
        }
        throw new RuntimeException("Unknown data type: "+label);
    }
    
    /**
     * Returns the labels of all available data types
     * @return
     */
    private String[] getDataTypes(){
        ArrayList<String> list = new ArrayList<String>();
        for (DataTypeDescription<?> desc : DataType.list()){
            list.add(desc.getLabel());
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Returns the index of a given data type
     * @param type
     * @return
     */
    private int getIndexOfDataType(DataType<?> type){
        int idx = 0;
        for (DataTypeDescription<?> desc : DataType.list()){
            if (desc.getLabel().equals(type.getDescription().getLabel())) {
                return idx;
            }
            idx++;
        }
        throw new RuntimeException("Unknown data type: "+type.getDescription().getLabel());
    }

    /**
     * Checks whether the data type is valid
     * @param type
     * @param values
     * @return
     */
    private boolean isValid(DataType<?> type, String[] values){
        for (String value : values){
            if (!type.isValid(value)) {
                return false;
            }
        }
        return true;
    }

    private void actionDown() {
        int index = list.getSelectionIndex();
        model.moveDown(index);
        
        // After moving in the array, move in the list
        if (index>=list.getItemCount()-1 || index<0) return;
        list.setRedraw(false);
        String temp = list.getItem(index+1);
        list.setItem(index+1, list.getItem(index));
        list.setItem(index, temp);
        list.setSelection(index+1);
        list.setRedraw(true);
        update();
    }
    private void actionUp() {
        int index = list.getSelectionIndex();
        model.moveUp(index);

        // After moving in the array, move in the list
        if (index<=0) return;
        list.setRedraw(false);
        String temp = list.getItem(index-1);
        list.setItem(index-1, list.getItem(index));
        list.setItem(index, temp);
        list.setSelection(index-1);
        list.setRedraw(true);
        update();

    }
    
    private int actionSort(int index) {
        
        // Initial data type
        DataType<?> type = model.getDataType();
        int returnIndex = index;
 
        // If not default
        if (index>0) {
            
            // Extract chosen type
            String label = combo.getItem(combo.getSelectionIndex());
            DataTypeDescription<?> description = getDataType(label);
    
            // Open format dialog
            if (description.getLabel().equals("OrderedString")) {
                final String text1 = Resources.getMessage("AttributeDefinitionView.9"); //$NON-NLS-1$
                final String text2 = Resources.getMessage("AttributeDefinitionView.10"); //$NON-NLS-1$
                String[] array = controller.actionShowOrderValuesDialog(text1, text2,
                                                                        DataType.STRING,
                                                                        model.getData());
                if (array == null) {
                    type = DataType.STRING;
                } else {
                    try {
                        type = DataType.createOrderedString(array);
                        if (!isValid(type, model.getData())) {
                            type = DataType.STRING;
                        }
                    } catch (Exception e) {
                        controller.actionShowInfoDialog("Error", "Cannot create data type: " + e.getMessage());
                        type = DataType.STRING;
                    }
                }
            } else if (description.hasFormat()) {
                final String text1 = Resources.getMessage("AttributeDefinitionView.9"); //$NON-NLS-1$
                final String text2 = Resources.getMessage("AttributeDefinitionView.10"); //$NON-NLS-1$
                final String format = controller.actionShowFormatInputDialog(getShell(),
                                                                             text1,
                                                                             text2,
                                                                             description,
                                                                             model.getData());
                if (format == null) {
                    type = DataType.STRING;
                } else {
                    type = description.newInstance(format);
                }
            } else {
                type = description.newInstance();
                if (!isValid(type, model.getData())) {
                    type = DataType.STRING;
                }
            }
            returnIndex = getIndexOfDataType(type) + 1;
        }
        if (!model.sort(type)) {
            model.sort(DataType.STRING);
        }
        list.setRedraw(false);
        list.removeAll();
        for (String s : model.getData()) {
            list.add(s);
        }
        list.setRedraw(true);
        return returnIndex;
    }
}
