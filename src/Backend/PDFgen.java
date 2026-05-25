/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Backend;

/**
 *
 * @author emimo
 */
import java.util.*;
import java.awt.color.*;
import java.awt.Color;
import java.awt.Font;
import Data.PDFStyle;
import Data.PDFDocumentModel;
import javax.swing.JTable;
import javax.swing.table.*;
import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import org.openpdf.text.Document;



public class PDFgen {
    private PDFStyle style;
    private PDFDocumentModel pdfM = new PDFDocumentModel();
    
    public void genStyle(String title, int titleTextSize, Font titleFont, boolean boldTitle, boolean italicTitle,Color titleColor,Font textFont, int textSize, boolean boldText, boolean italicText){
        this.style=new PDFStyle(title,titleTextSize,titleFont,boldTitle,italicTitle,titleColor,textFont,textSize,boldText,italicText);
    }
    
    public void genModel(JTable table){
        int cols = table.getColumnCount();
        int rows = table.getRowCount();

        String[] headers = new String[cols];

        for (int i = 0; i < cols; i++) {
            headers[i] = table.getColumnName(i);
        }
        pdfM.setHeaders(headers);
        List<String[]> tableData = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            String[] row = new String[cols];

            for (int c = 0; c < cols; c++) {
                Object v = table.getValueAt(r, c);
                row[c] = v == null ? "" : v.toString();
            }

            tableData.add(row);
        }
        pdfM.setTableData(tableData);
    }
    
}