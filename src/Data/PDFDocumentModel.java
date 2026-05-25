/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data;

/**
 *
 * @author emimo
 */
import java.util.List;

public class PDFDocumentModel {

    private String title;
    private String[] headers;
    private List<String[]> tableData;


    public PDFDocumentModel() {
    }

    public PDFDocumentModel(String title, String[] headers, List<String[]> tableData) {
        this.title = title;
        this.headers = headers;
        this.tableData = tableData;
    }

    public String getTitle() {
        return title;
    }

    public String[] getHeaders() {
        return headers;
    }

    public List<String[]> getTableData() {
        return tableData;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    public void setTableData(List<String[]> tableData) {
        this.tableData = tableData;
    }
}
