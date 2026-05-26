/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Presentation_Layer;

/**
 *
 * @author emimo
 */

import Backend.*;
import Data.SortAndFilter;
import Data.*;
import java.awt.*;
import java.util.function.*;
import java.util.concurrent.*;
import java.util.*;
import java.util.regex.Pattern;
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
    private String filterInput="";
    private boolean UIloading=true;
    private boolean SFUIloading=true;
    private String filterSQLSF="";
    private JComponent inputSF;
    private Map<String,LinkedHashMap<String,SortAndFilter>> listSF = new HashMap<>();
    private String currentSF = "";
    private DefaultTableModel SFmodel = new DefaultTableModel();
    private DefaultTableModel model = new DefaultTableModel(){
        @Override
        public Class<?> getColumnClass(int columnIndex){
            Tag colType = (Data.Tag) tableObj.getColData(columnIndex, "TYPE");
            switch(colType){
                case Tag.NUMERICAL ->{
                    return Integer.class;
                }
                case Tag.NUMERICAL_PRECISION->{
                    return Double.class;
                }
                case Tag.STRING -> {
                    return String.class;
                }
                case Tag.DATE->{
                    return java.sql.Date.class;
                }
                case Tag.DATETIME -> {
                    return java.sql.Timestamp.class;
                }
                case Tag.BOOLEAN -> {
                    return Boolean.class;
                }
                case Tag.DEFAULT ->{
                    return Object.class;
                }
                default ->{
                    return Object.class;
                }
            }
        }
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private class CustomRenderer extends DefaultTableCellRenderer{
        
        private final Border customBorder = BorderFactory.createLineBorder(Color.getHSBColor(0.13f, 0.75f, 0.95f));
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col){
            super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,col);
            int modelCol = table.convertColumnIndexToModel(col);
            if((boolean)tableObj.getColData(modelCol, "PRIMARY")){
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
    private TableRowSorter<DefaultTableModel> search;
    private boolean loaded=false;
    private boolean table_inUse=false;
    private Color titleColor=Color.BLACK;
    private PDFgen pdf = new PDFgen();
    
    /**
     * Creates new form UI
     */
    public UI() {
        initComponents();
        Table.setModel(model);
        search = new TableRowSorter<>(model);
        Table.setRowSorter(search);
        SFmodel = (DefaultTableModel) SFTable.getModel();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = ge.getAvailableFontFamilyNames();
        fontSel.setModel(new DefaultComboBoxModel<>(fonts));
        titleFontSel.setModel(new DefaultComboBoxModel<>(fonts));
        textSizeSpinnor.setModel(new SpinnerNumberModel(12,6,72,1));
        titleSizeSpinnor.setModel(new SpinnerNumberModel(24,12,84,1));
        this.inputPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.SFbarPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        pdf.genFonts();
        access.start(tableSelect);
        loaded=true;
        UIloading = false;
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
        currentSF="";
        filterInput="";
        filterSQLSF="";
        clearSF();
        model.setRowCount(0);
        model.setColumnCount(0);
        fields.clear();
        rowData.clear();
        this.inputPanel.removeAll();
        this.colSelect.removeAllItems();
        this.colSFSelector.removeAllItems();
        this.colSortSelector.removeAllItems();
        this.colAddSelect.removeAllItems();
        this.fixedListSelect.removeAllItems();
        for(Map.Entry<String,Backend.Col> entry: tableObj.getColList().entrySet()){
            Backend.Col c = entry.getValue();
            this.colSelect.addItem(c.getName());
            this.colSFSelector.addItem(c.getName());
            this.colSortSelector.addItem(c.getName());
            if(c.getPrKey()){
                this.rendererIntegrator(Table, model, c.getName(), renderer,Table.getTableHeader());
            }
            else{
                model.addColumn(c.getName());
            }
            if(c.getState()){
                this.colAddSelect.addItem(c.getName());
            }
            JPanel cell = new JPanel();
            cell.setLayout(new BoxLayout(cell,BoxLayout.Y_AXIS));
            cell.add(new JLabel(c.getName()));
            cell.setOpaque(false);
            if(!c.getIncrementState()){
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
            else{
                JTextField autoKey = new JTextField();
                autoKey.setEditable(false);
                autoKey.getDocument().addDocumentListener(new SimpleDocListener(){
                                @Override
                                public void update(){
                                    rowData.put(c.getName(),autoKey.getText());
                                }
                                
                });
                fields.put(c.getName(), autoKey);
                cell.add(autoKey);
                inputPanel.add(cell);
            }
        }
            inputPanel.revalidate();
            inputPanel.repaint();
            access.consult(model, selected_table,filterSQLSF);
            this.table_inUse=true;
            this.resetBtn.setEnabled(table_inUse);
            Tcheck();
            Table.setAutoCreateRowSorter(false);
        
    } catch (Exception e){
        System.out.println("LoadTable error");
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        e.printStackTrace();
    }
    }
    
    
    
    
    public void search(){
        String searchTxt = searchBar.getText();
        int col=Table.getColumnModel().getColumnIndex(colSelect.getSelectedItem().toString());
        
        if(searchTxt == null || searchTxt.trim().isEmpty()){
            search.setRowFilter(null);
        }
        else{
            search.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(searchTxt),col));
        }
    }
    
    
    public void getData(int row){
        int col=-1;
       for(Map.Entry<String,JComponent> entry: this.fields.entrySet()){
             System.out.println("Looking for: " + entry.getKey());

             for(int i=0;i<Table.getColumnCount();i++){
                 System.out.println(
                     "Column " + i + ": " + Table.getColumnName(i)
                 );
             }
           try{
                col = Table.getColumnModel().getColumnIndex(entry.getKey());
                Object value=model.getValueAt(row,col);
                if(value==null) continue;
            }
            catch(IllegalArgumentException e){
                continue;
            }
           if(entry.getValue() instanceof JFormattedTextField txt){
               txt.setValue(model.getValueAt(row, col));
           }
           else if(entry.getValue() instanceof JTextField textfield){
               textfield.setText(model.getValueAt(row, col).toString());
           }
           else if(entry.getValue() instanceof JComboBox combo){
               combo.setSelectedItem(model.getValueAt(row, col).toString());
           }
           else if(entry.getValue() instanceof JCheckBox check){
               try{
               check.setSelected(Boolean.parseBoolean(model.getValueAt(row, col).toString()));
               }
               catch(Exception e){
                   JOptionPane.showMessageDialog(this,"Error, algo a salido mal en columna: " + entry.getKey());
                   e.printStackTrace();
               }
           }
       }
    }
    
    
    public void clearData(){
        for(Map.Entry<String,JComponent> entry: this.fields.entrySet()){
            if(entry.getValue() instanceof JFormattedTextField ftxt){
                ftxt.setValue(null);
            }
            else if(entry.getValue() instanceof JTextField txt){
                txt.setText("");
            }
            else if(entry.getValue() instanceof JComboBox box){
                box.setSelectedIndex(0);
            }
            else if(entry.getValue() instanceof JCheckBox cbox){
                cbox.setSelected(false);
            }
        }
    }
    
    public void colShiftAction(java.awt.event.ItemEvent evt){
        if (evt.getStateChange() != java.awt.event.ItemEvent.SELECTED) return;
        if(!UIloading){   
            boolean fixed=false;
            Tag opType= Tag.DEFAULT;
            fixed=tableObj.getColList().get(colSFSelector.getSelectedItem().toString()).getState();
            opType=tableObj.getColList().get(colSFSelector.getSelectedItem().toString()).getType();
            String format = tableObj.getColList().get(colSFSelector.getSelectedItem().toString()).getFormat();
            this.SFbarPanel.removeAll();
            System.out.println("State: " + fixed);
            if(!fixed){
                operatorSelect.removeAllItems();
                System.out.println("OpTAG: " +opType);
                System.out.println("Op: " + opType.getOperators() );
                if(opType.getOperators() != null){
                    for(String s : opType.getOperators()){
                        System.out.println("Adding: " + s);
                        operatorSelect.addItem(s);
                    }
                }
                operatorSelect.revalidate();
                operatorSelect.repaint();
                switch(format){
                    case "NONE" -> {
                                JTextField input = new JTextField();
                                input.getDocument().addDocumentListener(new SimpleDocListener(){
                                    @Override
                                    public void update(){
                                        filterInput=input.getText();
                                    }

                                });
                                SFbarPanel.add(input);
                                inputSF = input;
                            }
                            case "PHONE" -> {
                                try{
                                    MaskFormatter phoneMask = new MaskFormatter("##-###-###-####");
                                    JFormattedTextField input = new JFormattedTextField(phoneMask);
                                    input.getDocument().addDocumentListener(new SimpleDocListener(){
                                        @Override
                                        public void update(){
                                            filterInput=input.getText();
                                        }
                                    });
                                    SFbarPanel.add(input);
                                    inputSF = input;
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
                                            filterInput=input.getText();
                                        }
                                    });
                                    SFbarPanel.add(input); 
                                    inputSF = input;
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
                                            filterInput=input.getText();
                                        }
                                    });
                                    SFbarPanel.add(input);
                                    inputSF = input;
                                } catch (Exception e){
                                    System.out.println("Error: " + e.getMessage());
                                }
                            }
                            case "EMAIL" -> {
                                JTextField input = new JTextField();
                                input.getDocument().addDocumentListener(new SimpleDocListener(){
                                        @Override
                                        public void update(){
                                            filterInput=input.getText();
                                        }
                                    });
                                SFbarPanel.add(input);
                                inputSF = input;
                            }
                            case "BOOLEAN" -> {
                                JCheckBox check = new JCheckBox();
                                check.addActionListener(e ->{
                                    filterInput=String.valueOf(check.isSelected());
                                });
                                SFbarPanel.add(check);
                                inputSF = check;
                            }
                            default ->{
                                JOptionPane.showMessageDialog(this, "Warning, unknown format error for: {" + colSFSelector.getSelectedItem().toString() + "}" + ", using JTextFiled as default.");
                                JTextField input = new JTextField();
                                input.getDocument().addDocumentListener(new SimpleDocListener(){
                                        @Override
                                        public void update(){
                                            filterInput=input.getText();
                                        }
                                    });
                                SFbarPanel.add(input);
                                inputSF = input;
                            }
                }
                
            }
            else{
                JComboBox<String> box = new JComboBox<>(tableObj.getColList().get(colSFSelector.getSelectedItem().toString()).getValues().toArray(String[]::new));
                    box.addActionListener(e ->{
                        filterInput=box.getSelectedItem().toString();
                    });
                    SFbarPanel.add(box);
                    inputSF = box;
            }
                Dimension inputSize = new Dimension(160, 25);
                inputSF.setPreferredSize(inputSize);
                inputSF.setMinimumSize(inputSize);
                inputSF.setMaximumSize(inputSize);
                SFbarPanel.revalidate();
                SFbarPanel.repaint();
        }
    }
    
    
    public void create(){
        if(this.rowData.isEmpty()){
            JOptionPane.showMessageDialog(this,"Error, la informacion no puede estar vacia");
            return;
        }
        LinkedHashMap <String,ValueTag> arg = new LinkedHashMap<>();
        for(Map.Entry<String,Backend.Col> entry : tableObj.getColList().entrySet()){
            Backend.Col c = entry.getValue();
            if(!c.getIncrementState()){
                Object value = rowData.get(c.getName());
                if(value != null){
                    arg.put(
                        c.getName(),
                        new ValueTag(value,c.getType())
                    );
                }
            }
        }
        try{
            access.create(selected_table, arg);
            access.consult(model, selected_table, filterSQLSF);
            rowData.clear();
            Table.clearSelection();
            clearData();
        }
        catch(Exception e){
            String errorMSG="";
            if(e instanceof PSQLException ePSQL){
                ServerErrorMessage error = ePSQL.getServerErrorMessage();
                errorMSG = "Error de Columna: " + error.getColumn() + ". Detalles: " + error.getDetail();
            }
            else{
                errorMSG="Error: " + e.getMessage();
            }
            JOptionPane.showMessageDialog(this, errorMSG);
            e.printStackTrace();
            return;
        }
    }
    public void update(){
        if(rowData.isEmpty() || Table.getSelectedRow() == -1){
        JOptionPane.showMessageDialog(this,"Error, la informacion no puede estar vacia y ocupa selecionar una fila.");
        return;
        }
        LinkedHashMap <String,ValueTag> arg = new LinkedHashMap<>();
        LinkedHashMap <String,ValueTag> position = new LinkedHashMap<>();
        for(Map.Entry<String,Backend.Col> entry : tableObj.getColList().entrySet()){
            Backend.Col c = entry.getValue();
            Object value = rowData.get(c.getName());
            if(c.getPrKey()){
                    if(value == null){
                        JOptionPane.showMessageDialog(
                            this,
                            "Error: Primary key missing -> " + c.getName()
                        );
                        return;
                    }
                    position.put(c.getName(),new ValueTag(value,c.getType()));
            }
            else{
                if(value != null){
                arg.put(
                    c.getName(),
                    new ValueTag(value,c.getType())
                );
                }
            }
        }
        try{
                access.update(selected_table,arg,position);
                access.consult(model, selected_table, filterSQLSF);
                rowData.clear();
                Table.clearSelection();
                clearData();
            }
        catch(Exception e){
            String errorMSG="";
            if(e instanceof PSQLException ePSQL){
                ServerErrorMessage error = ePSQL.getServerErrorMessage();
                errorMSG = "Error de Columna: " + error.getColumn() + ". Detalles: " + error.getDetail();
            }
            else{
                errorMSG="Error: " + e.getMessage();
            }
            JOptionPane.showMessageDialog(this, errorMSG);
            e.printStackTrace();
            return;
        }
    }

    public void delete(){
        if(rowData.isEmpty() || Table.getSelectedRow() == -1){
            JOptionPane.showMessageDialog(this,"Error, la informacion no puede estar vacia y ocupa selecionar una fila.");
            return;
       }
       LinkedHashMap <String,ValueTag> position = new LinkedHashMap<>();
       for(Map.Entry<String,Backend.Col> entry : tableObj.getColList().entrySet()){
            Backend.Col c = entry.getValue();
            if(c.getPrKey()){
                Object value = rowData.get(c.getName());
                if(value == null){
                    JOptionPane.showMessageDialog(
                        this,
                        "Error: Primary key missing -> " + c.getName()
                    );
                    return;
                }
                position.put(c.getName(),new ValueTag(value,c.getType()));
            }
       }
       try{
            access.delete(selected_table,position);
            access.consult(model, selected_table, filterSQLSF);
            rowData.clear();
            Table.clearSelection();
            clearData();
        }
        catch(Exception e){
            String errorMSG="";
            if(e instanceof PSQLException ePSQL){
                ServerErrorMessage error = ePSQL.getServerErrorMessage();
                errorMSG = "Error de Columna: " + error.getColumn() + ". Detalles: " + error.getDetail();
            }
            else{
                errorMSG="Error: " + e.getMessage();
            }
            JOptionPane.showMessageDialog(this, errorMSG);
            e.printStackTrace();
            return;
        }
    }
    public void SFopen(){
        if(loaded && table_inUse){
            this.SFUIloading=true;
            SFWindow.pack();
            SFWindow.setLocationRelativeTo(this);
            loadSFSelector();
            SFWindow.setVisible(true);
            this.SFUIloading=false;
        }
    }
    public void addSF(){
        String sfCol = this.colSFSelector.getSelectedItem().toString();
        String sfOp = this.operatorSelect.getSelectedItem().toString();
        String sfCon = this.connectSelect.getSelectedItem().toString();
        String processed_input = this.filterInput.replace(" ","(_)");
        String tSFOp = translator(sfOp);
        this.SFmodel.addRow(new Object[]{
            sfCol + " " + tSFOp + " " + processed_input,
            sfCon
        });
        if(currentSF != null && !currentSF.isBlank()){
            this.listSF.get(selected_table).get(currentSF).addArgument(sfCol + " " + tSFOp + " " + processed_input + " " + sfCon);
        }
        clearDataSFT();
    }
    public void deleteSF(int sfRow) throws Exception{
        if(sfRow>=0){
            SFmodel.removeRow(sfRow);
            if(currentSF != null && !currentSF.isBlank()){
                this.listSF.get(selected_table).get(currentSF).removeArguments(sfRow);
            }
            clearDataSFT();
        }
        else{
            throw new Exception("Error, porfavor selecione una fila.");
        }
    }
    public void updateSF(int sfRow) throws Exception{
        if(sfRow>=0){
            String sfCol = this.colSFSelector.getSelectedItem().toString();
            String sfOp = this.operatorSelect.getSelectedItem().toString();
            String sfCon = this.connectSelect.getSelectedItem().toString();
            String processed_input = this.filterInput.replace(" ","(_)");
            String tSFOp = translator(sfOp);
            SFmodel.setValueAt(sfCol + " " + tSFOp + " " + processed_input, sfRow, 0);
            SFmodel.setValueAt(sfCon,sfRow,1);
            if(currentSF != null && !currentSF.isBlank()){
                this.listSF.get(selected_table).get(currentSF).updateArgument(sfRow,sfCol + " " + tSFOp + " " + processed_input + " " + sfCon);
            }
            clearDataSFT();
        }
        else{
            throw new Exception("Error, porfavor selecione una fila.");
        }
    }
    
    public String translator(String op){
        switch(op){
            case "EQUAL" ->{
                return "=";
            }
            case "NEQUAL" ->{
                return "!=";
            }
            case "GREATER" ->{
                return ">";
            }
            case "LESSER" ->{
                return "<";
            }
            case "GRTEQ" ->{
                return ">=";
            }
            case "LESEQ" ->{
                return "<=";
            }
            case "LIKE" ->{
                return "LIKE";
            }
            case "ILIKE" ->{
                return "ILIKE";
            }
            case "NLIKE" ->{
                return "NOT LIKE";
            }
            case "NILIKE" ->{
                return "NOT ILIKE";
            }
            case "DEFAULT" ->{
                return "=";
            }
            case "NOT" ->{
                return "NOT";
            }
            default ->{
                return "=";
            }
        }
    }
    
    public String invTranslator(String opSym) {
        switch (opSym) {

            case "=" -> {
                return "EQUAL";
            }

            case "!=" -> {
                return "NEQUAL";
            }

            case ">" -> {
                return "GREATER";
            }

            case "<" -> {
                return "LESSER";
            }

            case ">=" -> {
                return "GRTEQ";
            }

            case "<=" -> {
                return "LESEQ";
            }

            case "LIKE" -> {
                return "LIKE";
            }

            case "ILIKE" -> {
                return "ILIKE";
            }

            case "NOT LIKE" -> {
                return "NLIKE";
            }

            case "NOT ILIKE" -> {
                return "NILIKE";
            }

            case "NOT" -> {
                return "NOT";
            }

            default -> {
                return "EQUAL"; 
            }
        }
    }
    public void clearDataSFT(){
        if(this.inputSF instanceof JTextField textfield){
            textfield.setText("");
        }
        else if(this.inputSF instanceof JComboBox combo){
            combo.setSelectedItem("");
        }
        else if(this.inputSF instanceof JFormattedTextField txt){
            txt.setValue("");
        }
        else if(this.inputSF instanceof JCheckBox check){
            try{
            check.setSelected(Boolean.parseBoolean(""));
            }
            catch(Exception e){
                JOptionPane.showMessageDialog(this,"Error, algo a salido mal en limpiar inputSF.");
                e.printStackTrace();
            }
        }
    }
    public void getDataSFT(int sfRow) throws Exception{
        String[] SFdata = SFmodel.getValueAt(sfRow,0).toString().split(" ");
        if (SFdata.length < 3) {
            throw new IllegalStateException("Corrupted SF row format");
        }
        String processed_data = SFdata[2].replace("(_)", " ");
        if(this.inputSF instanceof JTextField textfield){
            textfield.setText(processed_data);
        }
        else if(this.inputSF instanceof JComboBox combo){
            combo.setSelectedItem(processed_data);
        }
        else if(this.inputSF instanceof JFormattedTextField txt){
            txt.setValue(processed_data);
        }
        else if(this.inputSF instanceof JCheckBox check){
            try{
            check.setSelected(Boolean.parseBoolean(processed_data));
            }
            catch(Exception e){
                JOptionPane.showMessageDialog(this,"Error, algo a salido mal en valor: " + SFdata[2]);
                e.printStackTrace();
            }
        }
        String iTOP = invTranslator(SFdata[1]);
        this.operatorSelect.setSelectedItem(iTOP);
        this.colSFSelector.setSelectedItem(SFdata[0]);
        this.connectSelect.setSelectedItem(SFmodel.getValueAt(sfRow, 1).toString());
    }
    public void createSFTable(){
        String SFName = sfBar.getText();
        if(this.listSF.get(selected_table) == null){
            this.listSF.put(
                selected_table,
                new LinkedHashMap<>()
            );
        }
        if(this.listSF.get(selected_table).containsKey(SFName)){
            JOptionPane.showMessageDialog(
                this,
                "Error, no puedes poner un nombre en uso."
            );
            return;
        }
        ArrayList<String> arguments = new ArrayList<>();
        for (int row = 0; row < SFTable.getRowCount(); row++) {
            String arg = SFmodel.getValueAt(row, 0) + " " + SFmodel.getValueAt(row, 1);
            arguments.add(arg);
        }
        this.listSF.get(selected_table).put(SFName, new SortAndFilter(SFName,arguments,this.sortShowcasetxt.getText()));
        this.currentSF=SFName;
        UIloading = true;
        loadSFSelector();
        sfObjSelector.setSelectedItem(currentSF);
        UIloading = false;
    }
    
    
    public void deleteSFTable(){
        if(currentSF != null && !currentSF.isBlank() && this.listSF.get(selected_table) != null){
            this.listSF.get(selected_table).remove(currentSF);
            currentSF="";
            loadSFSelector();
        }
    }
    
    
    public void loadSFTable(){
        if (selected_table == null) return;
        SFmodel.setRowCount(0);
        LinkedHashMap<String, SortAndFilter> map = listSF.get(selected_table);
        if (map == null) return;

        if (currentSF == null || currentSF.isBlank()) return;

        SortAndFilter sf = map.get(currentSF);
        if (sf == null) return;
        for(String row : sf.getArguments()){
            String[] rowData = row.split(" ",4);
            String args = rowData[0] + " " + rowData[1] + " " + rowData[2]; 
            SFmodel.addRow(new Object[]{
                args,
                rowData[3]
            });
        }
        this.sortShowcasetxt.setText(sf.getSorter());
    }
    public void loadSFSelector(){
        sfObjSelector.removeAllItems();
        try{
            LinkedHashMap<String,SortAndFilter> map =
                this.listSF.get(this.selected_table);

            if(map == null){
                return;
            }
            for(Map.Entry<String,SortAndFilter> entry : this.listSF.get(this.selected_table).entrySet()){
                this.sfObjSelector.addItem(entry.getKey());
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void applySF() throws Exception{
        if(currentSF == null || currentSF.isBlank()){
            return;
        }
        LinkedHashMap<String,SortAndFilter> map =this.listSF.get(selected_table);
        if(map == null){
            return;
        }
        SortAndFilter sf = map.get(currentSF);
        if(sf == null){
            return;
        }
        StringBuilder sql = new StringBuilder();
        for(int i = 0; i < sf.getArguments().size(); i++){
            String s = sf.getArg(i);
            String[] rowData = s.split(" ",4);
            String pInputData = rowData[2].replaceAll("(_)", " ");
            if(i!=0){
                String as = sf.getArg(i-1);
                String[] prowData = as.split(" ",4);
                if(!prowData[3].equals("OR") && !prowData[3].equals("AND")){
                    throw new Exception("Error, falta de connector en fila: " + i);
                }  
            }
            if(i==sf.getArguments().size()-1){
                if(rowData[3].equals("OR") || rowData[3].equals("AND")){
                    throw new Exception("Error, conector extra en argumento final.");
                }
            }
            sql.append(rowData[0])
                .append(" ")
                .append(rowData[1])
                .append(" ")
                .append(pInputData)
                .append(" ")
                .append(rowData[3])
                .append(" ");
        }
        sql.append(sf.getSorter());
        filterSQLSF = sql.toString();
        this.access.consult(model, selected_table, filterSQLSF);
    }
    
    public void undoSF(){
        filterSQLSF="";
        this.access.consult(model, selected_table, filterSQLSF);
    }
    
    public void Tcheck(){
        transactionBtn.setEnabled(loaded && "sales_transactions".equals(selected_table));
    }
    public void clearSF(){
        this.currentSF="";
        SFmodel.setRowCount(0);
        clearDataSFT();
        this.sortShowcasetxt.setText("");
        this.sfBar.setText("");
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        SFWindow = new javax.swing.JDialog();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        colSFSelector = new javax.swing.JComboBox<>();
        operatorSelect = new javax.swing.JComboBox<>();
        connectSelect = new javax.swing.JComboBox<>();
        colSortSelector = new javax.swing.JComboBox<>();
        sortOperatorSelect = new javax.swing.JComboBox<>();
        sMODIFY = new javax.swing.JButton();
        fUpdateBtn = new javax.swing.JButton();
        fDeleteBtn = new javax.swing.JButton();
        sDelete = new javax.swing.JButton();
        fAddBtn = new javax.swing.JButton();
        SFbarPanel = new javax.swing.JPanel();
        filterBar = new javax.swing.JTextField();
        jPanel9 = new javax.swing.JPanel();
        sfBar = new javax.swing.JTextField();
        sfObjSelector = new javax.swing.JComboBox<>();
        sfCreateBtn = new javax.swing.JButton();
        sfDelete = new javax.swing.JButton();
        applyBtn = new javax.swing.JButton();
        unapplyBtn = new javax.swing.JButton();
        sfClearBtn = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        SFTable = new javax.swing.JTable();
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
        searchBtn = new javax.swing.JButton();
        tableOpPanel = new javax.swing.JPanel();
        tableSelect = new javax.swing.JComboBox<>();
        resetBtn = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        fontSel = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        textSizeSpinnor = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        boldOpt = new javax.swing.JCheckBox();
        italicOpt = new javax.swing.JCheckBox();
        titleTxtInput = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        boldTitleOpt = new javax.swing.JCheckBox();
        italicTitleOpt = new javax.swing.JCheckBox();
        colorBtn = new javax.swing.JButton();
        titleFontSel = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        titleSizeSpinnor = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        pdfPreviewBtn = new javax.swing.JButton();
        pdfExportBtn = new javax.swing.JButton();
        PDFgenBtn = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        addValueBtn = new javax.swing.JButton();
        valueBar = new javax.swing.JTextField();
        colAddSelect = new javax.swing.JComboBox<>();
        fixedListSelect = new javax.swing.JComboBox<>();
        eraseFixedBtn = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        transactionBtn = new javax.swing.JButton();
        lowStockTrigger = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        Table = new javax.swing.JTable();

        jPanel7.setBackground(new java.awt.Color(0, 153, 204));

        jPanel8.setOpaque(false);

        colSFSelector.addItemListener(this::Colshift);

        connectSelect.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "---", "AND", "OR" }));

        sortOperatorSelect.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ASC", "DESC" }));

        sMODIFY.setBackground(new java.awt.Color(102, 0, 102));
        sMODIFY.setText("SMODIFY");
        sMODIFY.addActionListener(this::sMODIFYActionPerformed);

        fUpdateBtn.setBackground(new java.awt.Color(153, 0, 204));
        fUpdateBtn.setText("FUPDATE");
        fUpdateBtn.addActionListener(this::fUpdateBtnActionPerformed);

        fDeleteBtn.setBackground(new java.awt.Color(255, 0, 0));
        fDeleteBtn.setText("FDELETE");
        fDeleteBtn.addActionListener(this::fDeleteBtnActionPerformed);

        sDelete.setBackground(new java.awt.Color(255, 0, 0));
        sDelete.setText("SDELETE");
        sDelete.addActionListener(this::sDeleteActionPerformed);

        fAddBtn.setBackground(new java.awt.Color(0, 153, 0));
        fAddBtn.setText("FADD");
        fAddBtn.addActionListener(this::fAddBtnActionPerformed);

        SFbarPanel.setMinimumSize(new java.awt.Dimension(64, 22));
        SFbarPanel.setOpaque(false);
        SFbarPanel.setPreferredSize(new java.awt.Dimension(64, 22));

        filterBar.addActionListener(this::filterBarActionPerformed);

        javax.swing.GroupLayout SFbarPanelLayout = new javax.swing.GroupLayout(SFbarPanel);
        SFbarPanel.setLayout(SFbarPanelLayout);
        SFbarPanelLayout.setHorizontalGroup(
            SFbarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SFbarPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(filterBar)
                .addContainerGap())
        );
        SFbarPanelLayout.setVerticalGroup(
            SFbarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SFbarPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(filterBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(colSFSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(operatorSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(connectSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addComponent(SFbarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(fAddBtn)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(fUpdateBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fDeleteBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addComponent(sMODIFY)
                        .addGap(18, 18, 18)
                        .addComponent(sDelete))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addComponent(colSortSelector, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(sortOperatorSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sMODIFY)
                            .addComponent(fDeleteBtn)
                            .addComponent(sDelete)
                            .addComponent(fAddBtn))
                        .addGap(12, 16, Short.MAX_VALUE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(SFbarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(operatorSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(connectSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(fUpdateBtn))
                    .addComponent(colSFSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(sortOperatorSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(colSortSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(9, 9, 9))
        );

        jPanel9.setOpaque(false);

        sfObjSelector.addItemListener(this::tableShiftSF);

        sfCreateBtn.setBackground(new java.awt.Color(51, 255, 255));
        sfCreateBtn.setText("SFCREATE");
        sfCreateBtn.addActionListener(this::sfCreateBtnActionPerformed);

        sfDelete.setBackground(new java.awt.Color(255, 0, 0));
        sfDelete.setText("SFDELETE");
        sfDelete.addActionListener(this::sfDeleteActionPerformed);

        applyBtn.setText("Apply");
        applyBtn.addActionListener(this::applyBtnActionPerformed);

        unapplyBtn.setText("Unapply");
        unapplyBtn.addActionListener(this::unapplyBtnActionPerformed);

        sfClearBtn.setText("SFCLEAR");
        sfClearBtn.addActionListener(this::sfClearBtnActionPerformed);

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
                .addGap(18, 18, 18)
                .addComponent(sfClearBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(sfObjSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(applyBtn)
                .addGap(18, 18, 18)
                .addComponent(unapplyBtn)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sfBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sfObjSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sfCreateBtn)
                    .addComponent(sfDelete)
                    .addComponent(applyBtn)
                    .addComponent(unapplyBtn)
                    .addComponent(sfClearBtn))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        SFTable.setModel(new javax.swing.table.DefaultTableModel(
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
        SFTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                interactSFTable(evt);
            }
        });
        jScrollPane2.setViewportView(SFTable);

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
                .addGap(6, 6, 6)
                .addGroup(SFWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(SFWindowLayout.createSequentialGroup()
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
            .addGroup(SFWindowLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 796, Short.MAX_VALUE)
                .addContainerGap())
        );
        SFWindowLayout.setVerticalGroup(
            SFWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SFWindowLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 388, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTabbedPane1.setBackground(new java.awt.Color(0, 102, 255));

        jPanel1.setBackground(new java.awt.Color(51, 153, 255));

        inputPanel.setOpaque(false);

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

        btnPanel.setOpaque(false);

        createBtn.setBackground(new java.awt.Color(0, 153, 0));
        createBtn.setText("Agregar");
        createBtn.addActionListener(this::createBtnActionPerformed);

        updateBtn.setBackground(new java.awt.Color(153, 0, 204));
        updateBtn.setText("Actualizar");
        updateBtn.addActionListener(this::updateBtnActionPerformed);

        clearBtn.setBackground(new java.awt.Color(0, 153, 153));
        clearBtn.setText("Clear");
        clearBtn.addActionListener(this::clearBtnActionPerformed);

        deleteBtn.setBackground(new java.awt.Color(255, 0, 0));
        deleteBtn.setText("Delete");
        deleteBtn.addActionListener(this::deleteBtnActionPerformed);

        sfMenuBtn.setBackground(new java.awt.Color(255, 255, 0));
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
                .addGap(188, 188, 188))
        );

        jPanel5.setOpaque(false);

        jLabel1.setText("Search");

        searchBtn.setText("Search");
        searchBtn.addActionListener(this::searchBtnActionPerformed);

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
                        .addComponent(searchBar, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                        .addComponent(searchBtn)
                        .addGap(26, 26, 26)
                        .addComponent(colSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(searchBtn)
                        .addComponent(colSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        tableOpPanel.setOpaque(false);

        tableSelect.addItemListener(this::tableSelectItemStateChanged);

        resetBtn.setText("Reset");
        resetBtn.setEnabled(false);
        resetBtn.addActionListener(this::resetBtnActionPerformed);

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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        jPanel11.setOpaque(false);

        jLabel2.setText("Fonts");

        jLabel3.setText("Text size");

        boldOpt.setText("Bold");

        italicOpt.setText("Italic");

        jLabel4.setText("Title");

        boldTitleOpt.setText("Bold");

        italicTitleOpt.setText("Italic");

        colorBtn.setText("Color Picker");
        colorBtn.addActionListener(this::colorChoice);

        jLabel5.setText("Title Fonts");

        jLabel6.setText("Title size");

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        pdfPreviewBtn.setText("Preview");
        pdfPreviewBtn.addActionListener(this::pdfPreviewBtnActionPerformed);

        pdfExportBtn.setText("Export");
        pdfExportBtn.addActionListener(this::pdfExportBtnActionPerformed);

        PDFgenBtn.setText("Generate");
        PDFgenBtn.addActionListener(this::PDFgenBtnActionPerformed);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addGroup(jPanel11Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(162, 162, 162)
                                .addComponent(jLabel3))
                            .addGroup(jPanel11Layout.createSequentialGroup()
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel11Layout.createSequentialGroup()
                                        .addComponent(fontSel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(71, 71, 71)
                                        .addComponent(textSizeSpinnor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(titleTxtInput, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(57, 57, 57)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel11Layout.createSequentialGroup()
                                        .addComponent(boldTitleOpt)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(italicTitleOpt))
                                    .addGroup(jPanel11Layout.createSequentialGroup()
                                        .addComponent(boldOpt)
                                        .addGap(27, 27, 27)
                                        .addComponent(italicOpt)))
                                .addGap(39, 39, 39)
                                .addComponent(colorBtn))
                            .addGroup(jPanel11Layout.createSequentialGroup()
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                                        .addComponent(titleFontSel, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(75, 75, 75))
                                    .addGroup(jPanel11Layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addGap(136, 136, 136)))
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(jPanel11Layout.createSequentialGroup()
                                        .addComponent(titleSizeSpinnor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 648, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PDFgenBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                .addComponent(pdfPreviewBtn)
                .addGap(34, 34, 34)
                .addComponent(pdfExportBtn)
                .addGap(15, 15, 15))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel11Layout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(fontSel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(textSizeSpinnor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(boldOpt)
                                    .addComponent(italicOpt))
                                .addGap(18, 18, 18)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(titleTxtInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(boldTitleOpt)
                                    .addComponent(italicTitleOpt)
                                    .addComponent(colorBtn))
                                .addGap(4, 4, 4)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel6))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(titleFontSel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(titleSizeSpinnor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel11Layout.createSequentialGroup()
                                .addGap(71, 71, 71)
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(pdfPreviewBtn)
                                    .addComponent(pdfExportBtn)
                                    .addComponent(PDFgenBtn))))
                        .addGap(0, 8, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(24, Short.MAX_VALUE)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14))
        );

        jTabbedPane1.addTab("REPORTS", jPanel4);

        jPanel3.setBackground(new java.awt.Color(51, 153, 255));

        jPanel2.setOpaque(false);

        addValueBtn.setText("Add Value");
        addValueBtn.addActionListener(this::addFixedValue);

        colAddSelect.addItemListener(this::valueShift);

        eraseFixedBtn.setText("Erase Value");
        eraseFixedBtn.addActionListener(this::eraseFixedBtnActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(valueBar, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addValueBtn)
                            .addComponent(fixedListSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(colAddSelect, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(eraseFixedBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(eraseFixedBtn)
                    .addComponent(fixedListSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setOpaque(false);

        transactionBtn.setText("Transaccion");
        transactionBtn.setEnabled(false);
        transactionBtn.addActionListener(this::transactionSQL);

        lowStockTrigger.setText("Low Stock Alert");
        lowStockTrigger.addItemListener(this::Activated);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(transactionBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 54, Short.MAX_VALUE)
                .addComponent(lowStockTrigger)
                .addGap(17, 17, 17))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(68, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lowStockTrigger)
                    .addComponent(transactionBtn))
                .addGap(65, 65, 65))
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
                .addContainerGap(448, Short.MAX_VALUE))
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
        Table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TableMouseClicked(evt);
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

    private void sfMenuBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sfMenuBtnActionPerformed
        SFopen();
    }//GEN-LAST:event_sfMenuBtnActionPerformed

    private void createBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createBtnActionPerformed
        create();
    }//GEN-LAST:event_createBtnActionPerformed

    private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
        update();
    }//GEN-LAST:event_updateBtnActionPerformed

    private void clearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBtnActionPerformed
        clearData();
    }//GEN-LAST:event_clearBtnActionPerformed

    private void TableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableMouseClicked
        int selected = Table.getSelectedRow();

        if(selected == -1){
            return;
        }

        int row = Table.convertRowIndexToModel(selected);
        getData(row);
    }//GEN-LAST:event_TableMouseClicked

    private void deleteBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteBtnActionPerformed
       delete();
    }//GEN-LAST:event_deleteBtnActionPerformed

    private void tableSelectItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_tableSelectItemStateChanged
        if (evt.getStateChange() != java.awt.event.ItemEvent.SELECTED) return;
        if (loaded) {
            loadTable();
        }
    }//GEN-LAST:event_tableSelectItemStateChanged

    private void Colshift(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Colshift
        colShiftAction(evt);
    }//GEN-LAST:event_Colshift

    private void sMODIFYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sMODIFYActionPerformed
        String colSort = this.colSortSelector.getSelectedItem().toString();
        String sortOp = this.sortOperatorSelect.getSelectedItem().toString();
        String sortSQL= " ORDER BY " + colSort + " " + sortOp;
        this.sortShowcasetxt.setText(sortSQL);
        if(currentSF !=null && !currentSF.isBlank()){
            this.listSF.get(this.selected_table).get(currentSF).setSorter(sortSQL);
        }
    }//GEN-LAST:event_sMODIFYActionPerformed

    private void fAddBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fAddBtnActionPerformed
        addSF();
    }//GEN-LAST:event_fAddBtnActionPerformed

    private void applyBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyBtnActionPerformed
        try{
            applySF();
        }catch(Exception e){
            JOptionPane.showMessageDialog(this, "Error de SFApply: " + e.getMessage());
        }
    }//GEN-LAST:event_applyBtnActionPerformed

    private void unapplyBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unapplyBtnActionPerformed
        undoSF();
    }//GEN-LAST:event_unapplyBtnActionPerformed

    private void resetBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetBtnActionPerformed
        this.search.setRowFilter(null);
        access.consult(model,selected_table,filterSQLSF);
    }//GEN-LAST:event_resetBtnActionPerformed

    private void searchBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchBtnActionPerformed
    search();
    }//GEN-LAST:event_searchBtnActionPerformed

    private void tableShiftSF(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_tableShiftSF
        if (UIloading) return;
        if (evt.getStateChange() != ItemEvent.SELECTED) return;

        currentSF = (String) evt.getItem(); 

        if (loaded && currentSF != null && !currentSF.isBlank()) {
            loadSFTable();
        }
    }//GEN-LAST:event_tableShiftSF

    private void sfCreateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sfCreateBtnActionPerformed
        createSFTable();
    }//GEN-LAST:event_sfCreateBtnActionPerformed

    private void sfDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sfDeleteActionPerformed
        deleteSFTable();
    }//GEN-LAST:event_sfDeleteActionPerformed

    private void fDeleteBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fDeleteBtnActionPerformed
        int sfRow = SFTable.convertRowIndexToModel(SFTable.getSelectedRow());
        try{
            deleteSF(sfRow);
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }//GEN-LAST:event_fDeleteBtnActionPerformed

    private void fUpdateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fUpdateBtnActionPerformed
        int sfRow = SFTable.convertRowIndexToModel(SFTable.getSelectedRow());
        try{
            updateSF(sfRow);
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }//GEN-LAST:event_fUpdateBtnActionPerformed

    private void sDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sDeleteActionPerformed
    this.sortShowcasetxt.setText("");
    if(currentSF !=null && !currentSF.isBlank()){
        this.listSF.get(this.selected_table).get(currentSF).setSorter("");
    }
    }//GEN-LAST:event_sDeleteActionPerformed

    private void interactSFTable(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_interactSFTable
        int sfRow = SFTable.convertRowIndexToModel(SFTable.getSelectedRow());
        try{
            getDataSFT(sfRow);
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(this, "Error in getData: " + e.getMessage());
        }
    }//GEN-LAST:event_interactSFTable

    private void addFixedValue(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFixedValue
        String nfValue = this.valueBar.getText();
        String col = this.colAddSelect.getSelectedItem().toString();
        if(nfValue.isBlank() || col.isBlank()){
            JOptionPane.showMessageDialog(this,"Error, ni el valor y la columna pueden estar vacios.");
            return;
        }
        nfValue = nfValue.replace("'", "''");
        String addSQL = String.format("SELECT add_fixed_value('%s','%s','%s')",selected_table,col,nfValue);
        try{
            access.actionSP(addSQL);
            tableObj.getCol(col).addValue(nfValue);
            loadTable();
        }
        catch (Exception e){
            System.out.println(e.getMessage() + "ERROR ERROR");
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error, desafortunadamente ha habido un error: " + e.getMessage());
            return;
        }
    }//GEN-LAST:event_addFixedValue

    private void transactionSQL(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transactionSQL
        if(rowData.isEmpty() && Table.getSelectedRow() == -1){
            JOptionPane.showMessageDialog(this,"Error, la informacion no puede estar vacia y ocupa selecionar una fila.");
            return;
        }
        String tID = "";
        for(Map.Entry<String,Backend.Col> entry : tableObj.getColList().entrySet()){
        Backend.Col c = entry.getValue();
            if(c.getPrKey()){
                Object value=rowData.get(c.getName());
                if(value!=null){
                    tID=value.toString();
                }
            }
        }
        if (tID == null || tID.isBlank()) {
            JOptionPane.showMessageDialog(this, "No se encontró la PK.");
            return;
        }
        String tSQL = "SELECT fin_transaction(" + tID + ")";
        try{
            access.actionSP(tSQL);
            access.consult(model,selected_table,filterSQLSF);
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(this, "Error, desafortunadamente ha habido un error: " + e.getMessage());
            return;
        }
    }//GEN-LAST:event_transactionSQL

    private void Activated(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Activated

    if (evt.getStateChange() == ItemEvent.SELECTED) {
        access.startListening(this::handleLowStockUI);
    } else if (evt.getStateChange() == ItemEvent.DESELECTED){
        access.stopListening();
    }
    else{
        return;
    }
    lowStockTrigger.setText(lowStockTrigger.isSelected()? "Low Stock: ON": "Low Stock: OFF");
    }//GEN-LAST:event_Activated

    private void colorChoice(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorChoice
        titleColor = JColorChooser.showDialog(this, "Pick Title Color",Color.BLACK);
    }//GEN-LAST:event_colorChoice

    private void PDFgenBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PDFgenBtnActionPerformed
        String fontTitle = this.titleFontSel.getSelectedItem().toString();
        String fontText = this.fontSel.getSelectedItem().toString();
        int titleSize = (Integer) this.titleSizeSpinnor.getValue();
        int textSize = (Integer) this.textSizeSpinnor.getValue();
        String titleText =this.titleTxtInput.getText();
        boolean textBold = this.boldOpt.isSelected();
        boolean textItalic = this.italicOpt.isSelected();
        boolean titleBold = this.boldTitleOpt.isSelected();
        boolean titleItalic = this.italicTitleOpt.isSelected();
        pdf.genStyle(titleText, titleSize, fontTitle, titleBold, titleItalic, titleColor, fontText, textSize, textBold, textItalic);
        pdf.genModel(Table);
    }//GEN-LAST:event_PDFgenBtnActionPerformed

    private void pdfPreviewBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pdfPreviewBtnActionPerformed
        pdf.previewPDF();
    }//GEN-LAST:event_pdfPreviewBtnActionPerformed

    private void pdfExportBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pdfExportBtnActionPerformed
        pdf.export();
    }//GEN-LAST:event_pdfExportBtnActionPerformed

    private void valueShift(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_valueShift
        if (evt.getStateChange() != java.awt.event.ItemEvent.SELECTED) return;
        this.fixedListSelect.removeAllItems();
        java.util.List<String> safe = new ArrayList<>(
            this.tableObj
                .getCol(this.colAddSelect.getSelectedItem().toString())
                .getValues()
        );

        this.fixedListSelect.setModel(
            new DefaultComboBoxModel<>(safe.toArray(new String[0]))
        );
    }//GEN-LAST:event_valueShift

    private void eraseFixedBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eraseFixedBtnActionPerformed

        if (this.fixedListSelect.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "No value selected.");
            return;
        }
        String nfValue = this.fixedListSelect.getSelectedItem().toString();
        String col = this.colAddSelect.getSelectedItem().toString();
        if(nfValue.isBlank() || col.isBlank()){
            JOptionPane.showMessageDialog(this,"Error, ni el valor y la columna pueden estar vacios.");
            return;
        }
        nfValue = nfValue.replace("'", "''");
        String removeSQL = String.format("SELECT remove_fixed_value('%s','%s','%s')",selected_table,col,nfValue);
        try{
            access.actionSP(removeSQL);
            tableObj.getCol(col).removeValue(nfValue);
            loadTable();
        }
        catch (Exception e){
            System.out.println(e.getMessage() + "ERROR ERROR");
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error, desafortunadamente ha habido un error: " + e.getMessage());
            return;
        }
    }//GEN-LAST:event_eraseFixedBtnActionPerformed

    private void sfClearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sfClearBtnActionPerformed
        clearSF();
    }//GEN-LAST:event_sfClearBtnActionPerformed

    private void filterBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterBarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_filterBarActionPerformed
private void handleLowStockUI(LowStockEvent event) {
    System.out.println("USED: ");
    System.out.println(event);
    SwingUtilities.invokeLater(() -> {
        JOptionPane.showMessageDialog(
            this,
            "Low stock detected:\n" +
            event.getName() +
            " (Stock: " + event.getStock() + ")",
            "Alert",
            JOptionPane.WARNING_MESSAGE
        );
    });
}
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
        UIManager.put(
        "FileChooser.useShellFolder",
        Boolean.FALSE
        );
        java.awt.EventQueue.invokeLater(() -> new UI().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton PDFgenBtn;
    private javax.swing.JTable SFTable;
    private javax.swing.JDialog SFWindow;
    private javax.swing.JPanel SFbarPanel;
    private javax.swing.JTable Table;
    private javax.swing.JButton addValueBtn;
    private javax.swing.JButton applyBtn;
    private javax.swing.JCheckBox boldOpt;
    private javax.swing.JCheckBox boldTitleOpt;
    private javax.swing.JPanel btnPanel;
    private javax.swing.JButton clearBtn;
    private javax.swing.JComboBox<String> colAddSelect;
    private javax.swing.JComboBox<String> colSFSelector;
    private javax.swing.JComboBox<String> colSelect;
    private javax.swing.JComboBox<String> colSortSelector;
    private javax.swing.JButton colorBtn;
    private javax.swing.JComboBox<String> connectSelect;
    private javax.swing.JButton createBtn;
    private javax.swing.JButton deleteBtn;
    private javax.swing.JButton eraseFixedBtn;
    private javax.swing.JButton fAddBtn;
    private javax.swing.JButton fDeleteBtn;
    private javax.swing.JButton fUpdateBtn;
    private javax.swing.JTextField filterBar;
    private javax.swing.JComboBox<String> fixedListSelect;
    private javax.swing.JComboBox<String> fontSel;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JCheckBox italicOpt;
    private javax.swing.JCheckBox italicTitleOpt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
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
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JRadioButton lowStockTrigger;
    private javax.swing.JComboBox<String> operatorSelect;
    private javax.swing.JButton pdfExportBtn;
    private javax.swing.JButton pdfPreviewBtn;
    private javax.swing.JButton resetBtn;
    private javax.swing.JButton sDelete;
    private javax.swing.JButton sMODIFY;
    private javax.swing.JTextField searchBar;
    private javax.swing.JButton searchBtn;
    private javax.swing.JTextField sfBar;
    private javax.swing.JButton sfClearBtn;
    private javax.swing.JButton sfCreateBtn;
    private javax.swing.JButton sfDelete;
    private javax.swing.JButton sfMenuBtn;
    private javax.swing.JComboBox<String> sfObjSelector;
    private javax.swing.JLabel sortLabel;
    private javax.swing.JComboBox<String> sortOperatorSelect;
    private javax.swing.JTextField sortShowcasetxt;
    private javax.swing.JPanel tableOpPanel;
    private javax.swing.JComboBox<String> tableSelect;
    private javax.swing.JSpinner textSizeSpinnor;
    private javax.swing.JComboBox<String> titleFontSel;
    private javax.swing.JSpinner titleSizeSpinnor;
    private javax.swing.JTextField titleTxtInput;
    private javax.swing.JButton transactionBtn;
    private javax.swing.JButton unapplyBtn;
    private javax.swing.JButton updateBtn;
    private javax.swing.JTextField valueBar;
    // End of variables declaration//GEN-END:variables
}
