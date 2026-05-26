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
import java.awt.Color;
import Data.PDFStyle;
import Data.PDFDocumentModel;
import java.awt.Desktop;
import javax.swing.JTable;
import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.PdfPTable;

public class PDFgen {
    private PDFStyle style;
    private PDFDocumentModel pdfM = new PDFDocumentModel();
    
    /**
     * Registers system fonts for use in PDF generation
     */
    public PDFgen(){
        
    }
    public void genFonts() {
        org.openpdf.text.FontFactory.registerDirectories();
    }
    
    /**
     * Configure PDF styling
     */
    public void genStyle(String title, int titleTextSize, String titleFont, 
                        boolean boldTitle, boolean italicTitle, Color titleColor,
                        String textFont, int textSize, boolean boldText, boolean italicText) {
        this.style = new PDFStyle(title, titleTextSize, titleFont, boldTitle, 
                                  italicTitle, titleColor, textFont, textSize, 
                                  boldText, italicText);
    }
    
    /**
     * Extract data from JTable into the document model
     */
    public void genModel(JTable table) {
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
        pdfM = new PDFDocumentModel();
        int cols = table.getColumnCount();
        int rows = table.getRowCount();

        // Extract headers
        String[] headers = new String[cols];
        for (int i = 0; i < cols; i++) {
            Object headerValue = table.getColumnName(i);
            headers[i] = headerValue == null ? "" : headerValue.toString();
        }
        pdfM.setHeaders(headers);

        // Extract table data
        List<String[]> tableData = new ArrayList<>(rows);
        for (int r = 0; r < rows; r++) {
            String[] row = new String[cols];
            for (int c = 0; c < cols; c++) {
                Object v = table.getValueAt(r, c);
                if (v == null) {
                    row[c] = "";
                } else {
                    String cell = v.toString();
                    row[c] = cell.isBlank() ? "" : cell;
                }
            }
            tableData.add(row);
        }
        pdfM.setTableData(tableData);
    }
    
    /**
     * Export PDF with file chooser dialog
     */
    public void export() {
        try {
            if(style==null){
                JOptionPane.showMessageDialog(
                    null,
                    "Configure PDF style first."
                );
                return;
            }

            if(pdfM.getHeaders()==null){
                JOptionPane.showMessageDialog(
                    null,
                    "Load table data first."
                );
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            int result = chooser.showSaveDialog(null);
            if (result != JFileChooser.APPROVE_OPTION) return;

            File file = chooser.getSelectedFile();
            if (!file.getName().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }

            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();
            
            buildPDF(doc);
            
            doc.close();

            JOptionPane.showMessageDialog(null, "PDF exported successfully!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error exporting PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generate a temporary PDF and open it for preview
     */
    public void previewPDF() {
    try {
        if (style == null) {
            JOptionPane.showMessageDialog(null, "Configure PDF style first.");
            return;
        }

        if (pdfM.getHeaders() == null || pdfM.getHeaders().length == 0) {
            JOptionPane.showMessageDialog(null, "Load table data first.");
            return;
        }

        File temp = File.createTempFile("preview_", ".pdf");

        try (FileOutputStream out = new FileOutputStream(temp)) {

            Document doc = new Document();
            PdfWriter writer = PdfWriter.getInstance(doc, out);

            doc.open();
            buildPDF(doc);
            doc.close();

            writer.flush();
        }

        // IMPORTANT: force file write completion
        if (!temp.exists() || temp.length() < 100) {
            throw new RuntimeException("PDF generation failed (file too small or missing)");
        }

        // Try opening safely
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();

            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(temp);
            } else {
                JOptionPane.showMessageDialog(null,
                    "PDF created at:\n" + temp.getAbsolutePath());
            }
        } else {
            JOptionPane.showMessageDialog(null,
                "Desktop not supported.\nFile: " + temp.getAbsolutePath());
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null,
            "Preview error: " + e.getMessage());

        e.printStackTrace();
    }
}
    /**
     * Build the PDF content with proper styling
     */
    private void buildPDF(Document doc) throws Exception {
        if (style == null) {
            throw new IllegalStateException("Style not configured. Call genStyle() first.");
        }
        if (pdfM.getHeaders() == null) {
            throw new IllegalStateException("Model not configured. Call genModel() first.");
        }
        if(pdfM.getHeaders().length==0){
            throw new IllegalStateException("No columns available.");
        }
        // ===== TITLE =====
        org.openpdf.text.Font titleFont = createOpenPDFFont(
            style.getTitleFont(),
            style.getTitleTextSize(),
            style.isBoldTitle(),
            style.isItalicTitle(),
            style.getTitleColor()
        );
        String titleText =style.getTitle()==null || style.getTitle().isBlank() ? "Report": style.getTitle();
        Paragraph title = new Paragraph(titleText, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        doc.add(new Paragraph(" ")); // spacing

        // ===== TABLE =====
        PdfPTable table = new PdfPTable(pdfM.getHeaders().length);
        table.setWidthPercentage(100);

        // ===== HEADER FONT (always bold) =====
        org.openpdf.text.Font headerFont = createOpenPDFFont(
            style.getTextFont(),
            style.getTextSize(),
            true, // headers are always bold
            false,
            Color.BLACK
        );

        // Add headers
        for (String h : pdfM.getHeaders()) {
            table.addCell(new Phrase(h, headerFont));
        }

        // ===== BODY FONT =====
        org.openpdf.text.Font bodyFont = createOpenPDFFont(
            style.getTextFont(),
            style.getTextSize(),
            style.isBoldText(),
            style.isItalicText(),
            Color.BLACK
        );

        // Add table data
        for (String[] row : pdfM.getTableData()) {
            for (String cell : row) {
                table.addCell(new Phrase(cell != null ? cell : "", bodyFont));
            }
        }

        doc.add(table);
    }
    
    /**
     * Helper method to create OpenPDF fonts from AWT font specifications
     */
    private org.openpdf.text.Font createOpenPDFFont(String fontName, int size, boolean bold, boolean italic, Color color) {
        int style = org.openpdf.text.Font.NORMAL;
        if (bold && italic) {
            style = org.openpdf.text.Font.BOLDITALIC;
        } else if (bold) {
            style = org.openpdf.text.Font.BOLD;
        } else if (italic) {
            style = org.openpdf.text.Font.ITALIC;
        }

        org.openpdf.text.Font font = org.openpdf.text.FontFactory.getFont(
            fontName, 
            size, 
            style
        );
        
        if (color != null) {
            font.setColor(color);
        }
        
        return font;
    }
    
    /**
     * Get current style configuration
     */
    public PDFStyle getStyle() {
        return style;
    }
    
    /**
     * Get current document model
     */
    public PDFDocumentModel getPdfModel() {
        return pdfM;
    }
}