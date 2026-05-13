/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Presentation_Layer;

/**
 *
 * @author emimo
 */

import Backend.Tag;
import java.awt.*;
import java.util.function.*;
import java.util.concurrent.*;
import java.util.*;
import javax.swing.table.*;
import javax.swing.*;
import java.sql.*;
import org.postgresql.util.*;
import org.postgresql.*;
import javax.swing.event.*;
import java.awt.event.*;
import javax.swing.border.Border;
import javax.swing.text.MaskFormatter;
public class UI extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(UI.class.getName());
    private String selected_table;
    private Backend.DAO access = new Backend.DAO("jdbc:postgresql://localhost:5432/ProjectDB");
    private Backend.Table tableObj;
    private DefaultTableModel model = new DefaultTableModel(){
        @Override
        public Class<?> getColumnClass(int columnIndex){
            Backend.Tag colType = (Backend.Tag) tableObj.getColData(columnIndex, "TYPE");
            switch(colType){
                case Backend.Tag.NUMERICAL ->{
                    return Double.class;
                }
                case Backend.Tag.STRING -> {
                    return String.class;
                }
                case Backend.Tag.DATE->{
                    return java.sql.Date.class;
                }
                case Backend.Tag.DATETIME -> {
                    return java.sql.Timestamp.class;
                }
                case Backend.Tag.BOOLEAN -> {
                    return Boolean.class;
                }
                case Backend.Tag.DEFAULT ->{
                    return Object.class;
                }
                default ->{
                    return Object.class;
                }
            }
        }
    };
    private class CustomRenderer extends DefaultTableCellRenderer{
        
        private final Border customBorder = BorderFactory.createLineBorder(Color.getHSBColor(0.13f, 0.75f, 0.95f));
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col){
            super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,col);
            
            if((boolean)tableObj.getColData(col, "PRIMARY")){
                setForeground(Color.white);
                setBackground(Color.getHSBColor(TOP_ALIGNMENT, TOP_ALIGNMENT, TOP_ALIGNMENT));
                setBorder(customBorder);
            }
            else{
                setForeground(table.getForeground());
                setBackground(table.getBackground());
                setBorder(UIManager.getBorder("Table.cellNoFocusBorder"));
            }
            
            
            return this;
        }
    }
    private CustomRenderer renderer = new CustomRenderer();
    private Map<String,JComponent> fields = new HashMap<>();
    private Map<String,Object> rowData = new HashMap<>();
    private boolean loaded=false;
    private boolean table_inUse=false;
    
    /**
     * Creates new form UI
     */
    public UI() {
        initComponents();
        model = (DefaultTableModel) Table.getModel();
        this.inputPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        access.start(tableSelect);
        loaded=true;
    }
    
    public abstract class SimpleDocListener implements DocumentListener{
        public abstract void update();
        
        @Override
        public void insertUpdate(DocumentEvent e){
            update();
        }
        
        @Override
        public void removeUpdate(DocumentEvent e){
            update();
        }
        
        @Override
        public void changedUpdate(DocumentEvent e){
            update();
        }
    }
    
    public  void rendererIntegrator(JTable table, DefaultTableModel model,String columnName,TableCellRenderer renderer,JTableHeader header){
        model.addColumn(columnName);
        TableColumn col = table.getColumnModel().getColumn(table.getColumnCount() - 1);
        col.setCellRenderer(renderer);
        header.setDefaultRenderer(renderer);
    }
    
    
    public void loadTable(){
        try{
        this.selected_table=this.tableSelect.getSelectedItem().toString();
        tableObj = access.fetch(selected_table);
        model.setRowCount(0);
        model.setColumnCount(0);
        this.inputPanel.removeAll();
        this.colSelect.removeAllItems();
        this.colSFSelector.removeAllItems();
        this.colSortSelector.removeAllItems();
        for(Backend.Col c: tableObj.getColList()){
            this.colSelect.addItem(c.getName());
            this.colSFSelector.addItem(c.getName());
            this.colSortSelector.addItem(c.getName());
            if(c.getPrKey()){
                this.rendererIntegrator(Table, model, c.getName(), renderer,Table.getTableHeader());
            }
            else{
                model.addColumn(c.getName());
            }
            if(!c.getIncrementState()){
                JPanel cell = new JPanel();
                cell.setLayout(new BoxLayout(cell,BoxLayout.Y_AXIS));
                cell.add(new JLabel(c.getName()));
                if(c.getState()){
                    JComboBox<String> box = new JComboBox<>(c.getValues().toArray(String[]::new));
                    box.addActionListener(e ->{
                        rowData.put(c.getName(),box.getSelectedItem());
                    });
                    fields.put(c.getName(), box);
                    cell.add(box);
                }
                else{
                    switch(c.getFormat()){
                        case "NONE" -> {
                            JTextField input = new JTextField();
                            input.getDocument().addDocumentListener(new SimpleDocListener(){
                                @Override
                                public void update(){
                                    rowData.put(c.getName(),input.getText());
                                }
                                
                            });
                            fields.put(c.getName(), input);
                            cell.add(input);
                        }
                        case "PHONE" -> {
                            try{
                                MaskFormatter phoneMask = new MaskFormatter("##-###-###-####");
                                JFormattedTextField input = new JFormattedTextField(phoneMask);
                                input.getDocument().addDocumentListener(new SimpleDocListener(){
                                    @Override
                                    public void update(){
                                        rowData.put(c.getName(),input.getText());
                                    }
                                });
                                fields.put(c.getName(), input);
                                cell.add(input);
                            }catch(Exception e){
                                System.out.println("error:" + e.getMessage());
                            }
                        }
                        case "DATE" -> {
                            try{
                                MaskFormatter dateMask = new MaskFormatter("####-##-##");
                                JFormattedTextField input = new JFormattedTextField(dateMask);
                                input.getDocument().addDocumentListener(new SimpleDocListener(){
                                    @Override
                                    public void update(){
                                        rowData.put(c.getName(),input.getText());
                                    }
                                });
                                fields.put(c.getName(), input);
                                cell.add(input);
                            } catch (Exception e){
                                System.out.println("Error: " + e.getMessage());
                            }
                        }
                        case "DATETIME" -> {
                            try{
                                MaskFormatter dateTimeMask = new MaskFormatter("####-##-## ##:##:##");
                                JFormattedTextField input = new JFormattedTextField(dateTimeMask);
                                input.getDocument().addDocumentListener(new SimpleDocListener(){
                                    @Override
                                    public void update(){
                                        rowData.put(c.getName(),input.getText());
                                    }
                                });
                                fields.put(c.getName(), input);
                                cell.add(input);
                            } catch (Exception e){
                                System.out.println("Error: " + e.getMessage());
                            }
                        }
                        case "EMAIL" -> {
                            JTextField input = new JTextField();
                            input.getDocument().addDocumentListener(new SimpleDocListener(){
                                    @Override
                                    public void update(){
                                        rowData.put(c.getName(),input.getText());
                                    }
                                });
                            fields.put(c.getName(), input);
                            cell.add(input);
                        }
                        case "BOOLEAN" -> {
                            JCheckBox check = new JCheckBox();
                            check.addActionListener(e ->{
                                rowData.put(c.getName(),check.isSelected());
                            });
                            fields.put(c.getName(), check);
                            cell.add(check);
                        }
                        default ->{
                            JOptionPane.showMessageDialog(this, "Warning, unknown format error for: {" + c.getName() + "}" + ", using JTextFiled as default.");
                            JTextField input = new JTextField();
                            input.getDocument().addDocumentListener(new SimpleDocListener(){
                                    @Override
                                    public void update(){
                                        rowData.put(c.getName(),input.getText());
                                    }
                                });
                            fields.put(c.getName(), input);
                            cell.add(input);
                        }
                            
                    }
                }
                inputPanel.add(cell);
            }
            
            access.consult(model, selected_table);
            this.table_inUse=true;
        }
        
    } catch (Exception e){
        System.out.println("LoadTable error");
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        e.printStackTrace();
    }
    }
    
    
    
    
    public void retrieveOP(){
        
    }
    
    
    
    
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        SFWindow = new javax.swing.JDialog();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        colSFSelector = new javax.swing.JComboBox<>();
        operatorSelect = new javax.swing.JComboBox<>();
        connectSelect = new javax.swing.JComboBox<>();
        filterBar = new javax.swing.JTextField();
        colSortSelector = new javax.swing.JComboBox<>();
        sortOperatorSelect = new javax.swing.JComboBox<>();
        sADD = new javax.swing.JButton();
        fUpdateBtn = new javax.swing.JButton();
        fDeleteBtn = new javax.swing.JButton();
        sDelete = new javax.swing.JButton();
        fAddBtn = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        sfBar = new javax.swing.JTextField();
        sfObjSelector = new javax.swing.JComboBox<>();
        sfCreateBtn = new javax.swing.JButton();
        sfDelete = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel10 = new javax.swing.JPanel();
        sortLabel = new javax.swing.JLabel();
        sortShowcasetxt = new javax.swing.JTextField();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        inputPanel = new javax.swing.JPanel();
        btnPanel = new javax.swing.JPanel();
        createBtn = new javax.swing.JButton();
        updateBtn = new javax.swing.JButton();
        clearBtn = new javax.swing.JButton();
        deleteBtn = new javax.swing.JButton();
        sfMenuBtn = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        searchBar = new javax.swing.JTextField();
        colSelect = new javax.swing.JComboBox<>();
        tableOpPanel = new javax.swing.JPanel();
        tableSelect = new javax.swing.JComboBox<>();
        resetBtn = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        addValueBtn = new javax.swing.JButton();
        valueBar = new javax.swing.JTextField();
        colAddSelect = new javax.swing.JComboBox<>();
        jPanel6 = new javax.swing.JPanel();
        func1Btn = new javax.swing.JButton();
        lowStockTrigger = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        Table = new javax.swing.JTable();

        jPanel7.setBackground(new java.awt.Color(0, 153, 204));

        connectSelect.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "---", "AND", "OR" }));

        sADD.setText("SADD");

        fUpdateBtn.setText("FUPDATE");

        fDeleteBtn.setText("FDELETE");

        sDelete.setText("SDELETE");

        fAddBtn.setText("FADD");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(filterBar)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(colSFSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(operatorSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(connectSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fAddBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(fUpdateBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fDeleteBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(67, 67, 67)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(sADD, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(colSortSelector, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(sortOperatorSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(sDelete)))
                .addContainerGap(42, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filterBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sADD)
                    .addComponent(fDeleteBtn)
                    .addComponent(sDelete)
                    .addComponent(fAddBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(colSFSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(operatorSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connectSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(colSortSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sortOperatorSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fUpdateBtn))
                .addGap(25, 25, 25))
        );

        sfCreateBtn.setText("SFCREATE");

        sfDelete.setText("SFDELETE");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sfBar, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(sfCreateBtn)
                .addGap(18, 18, 18)
                .addComponent(sfDelete)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(sfObjSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sfBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sfObjSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sfCreateBtn)
                    .addComponent(sfDelete))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Arguments", "Connector"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jTable1);

        jPanel10.setBackground(new java.awt.Color(0, 153, 204));

        sortLabel.setText("Sorter");

        sortShowcasetxt.setEditable(false);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sortLabel)
                    .addComponent(sortShowcasetxt, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(sortLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(sortShowcasetxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout SFWindowLayout = new javax.swing.GroupLayout(SFWindow.getContentPane());
        SFWindow.getContentPane().setLayout(SFWindowLayout);
        SFWindowLayout.setHorizontalGroup(
            SFWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SFWindowLayout.createSequentialGroup()
                .addGroup(SFWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SFWindowLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(SFWindowLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        SFWindowLayout.setVerticalGroup(
            SFWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SFWindowLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTabbedPane1.setBackground(new java.awt.Color(0, 102, 255));

        jPanel1.setBackground(new java.awt.Color(51, 153, 255));

        javax.swing.GroupLayout inputPanelLayout = new javax.swing.GroupLayout(inputPanel);
        inputPanel.setLayout(inputPanelLayout);
        inputPanelLayout.setHorizontalGroup(
            inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 971, Short.MAX_VALUE)
        );
        inputPanelLayout.setVerticalGroup(
            inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 109, Short.MAX_VALUE)
        );

        createBtn.setText("Agregar");

        updateBtn.setText("Actualizar");

        clearBtn.setText("Clear");

        deleteBtn.setText("Delete");

        sfMenuBtn.setText("S&F");
        sfMenuBtn.addActionListener(this::sfMenuBtnActionPerformed);

        javax.swing.GroupLayout btnPanelLayout = new javax.swing.GroupLayout(btnPanel);
        btnPanel.setLayout(btnPanelLayout);
        btnPanelLayout.setHorizontalGroup(
            btnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(btnPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(createBtn)
                .addGap(18, 18, 18)
                .addComponent(updateBtn)
                .addGap(17, 17, 17)
                .addComponent(clearBtn)
                .addGap(18, 18, 18)
                .addComponent(deleteBtn)
                .addGap(18, 18, 18)
                .addComponent(sfMenuBtn)
                .addContainerGap(35, Short.MAX_VALUE))
        );
        btnPanelLayout.setVerticalGroup(
            btnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(btnPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(btnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createBtn)
                    .addComponent(updateBtn)
                    .addComponent(clearBtn)
                    .addComponent(deleteBtn)
                    .addComponent(sfMenuBtn))
                .addGap(152, 152, 152))
        );

        jLabel1.setText("Search");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(searchBar, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(colSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(colSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        tableSelect.addActionListener(this::tableSelectActionPerformed);

        resetBtn.setText("Reset");

        javax.swing.GroupLayout tableOpPanelLayout = new javax.swing.GroupLayout(tableOpPanel);
        tableOpPanel.setLayout(tableOpPanelLayout);
        tableOpPanelLayout.setHorizontalGroup(
            tableOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableOpPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(tableSelect, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(resetBtn)
                .addGap(7, 7, 7))
        );
        tableOpPanelLayout.setVerticalGroup(
            tableOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableOpPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tableOpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tableSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resetBtn))
                .addContainerGap(11, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tableOpPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGap(18, 18, 18)
                            .addComponent(btnPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(inputPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(12, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tableOpPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inputPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15))
        );

        jTabbedPane1.addTab("EDIT & VIEW", jPanel1);

        jPanel4.setBackground(new java.awt.Color(51, 153, 255));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 998, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 238, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("REPORTS", jPanel4);

        jPanel3.setBackground(new java.awt.Color(51, 153, 255));

        addValueBtn.setText("Add Value");
        addValueBtn.addActionListener(this::addValueBtnActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(valueBar, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(addValueBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(colAddSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(17, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(valueBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addValueBtn)
                    .addComponent(colAddSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        func1Btn.setText("func1");

        lowStockTrigger.setText("Low Stock Alert");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(func1Btn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                .addComponent(lowStockTrigger)
                .addGap(17, 17, 17))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(69, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lowStockTrigger)
                    .addComponent(func1Btn))
                .addGap(64, 64, 64))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(437, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(62, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("BUSINESS", jPanel3);

        Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Empty"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(Table);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jTabbedPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tableSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tableSelectActionPerformed
        if(loaded){
            loadTable();
        }
    }//GEN-LAST:event_tableSelectActionPerformed

    private void addValueBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addValueBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addValueBtnActionPerformed

    private void sfMenuBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sfMenuBtnActionPerformed
        if(loaded && table_inUse){
            SFWindow.pack();
            SFWindow.setLocationRelativeTo(this);
            SFWindow.setVisible(true);
        }
    }//GEN-LAST:event_sfMenuBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new UI().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog SFWindow;
    private javax.swing.JTable Table;
    private javax.swing.JButton addValueBtn;
    private javax.swing.JPanel btnPanel;
    private javax.swing.JButton clearBtn;
    private javax.swing.JComboBox<String> colAddSelect;
    private javax.swing.JComboBox<String> colSFSelector;
    private javax.swing.JComboBox<String> colSelect;
    private javax.swing.JComboBox<String> colSortSelector;
    private javax.swing.JComboBox<String> connectSelect;
    private javax.swing.JButton createBtn;
    private javax.swing.JButton deleteBtn;
    private javax.swing.JButton fAddBtn;
    private javax.swing.JButton fDeleteBtn;
    private javax.swing.JButton fUpdateBtn;
    private javax.swing.JTextField filterBar;
    private javax.swing.JButton func1Btn;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JRadioButton lowStockTrigger;
    private javax.swing.JComboBox<String> operatorSelect;
    private javax.swing.JButton resetBtn;
    private javax.swing.JButton sADD;
    private javax.swing.JButton sDelete;
    private javax.swing.JTextField searchBar;
    private javax.swing.JTextField sfBar;
    private javax.swing.JButton sfCreateBtn;
    private javax.swing.JButton sfDelete;
    private javax.swing.JButton sfMenuBtn;
    private javax.swing.JComboBox<String> sfObjSelector;
    private javax.swing.JLabel sortLabel;
    private javax.swing.JComboBox<String> sortOperatorSelect;
    private javax.swing.JTextField sortShowcasetxt;
    private javax.swing.JPanel tableOpPanel;
    private javax.swing.JComboBox<String> tableSelect;
    private javax.swing.JButton updateBtn;
    private javax.swing.JTextField valueBar;
    // End of variables declaration//GEN-END:variables
}
