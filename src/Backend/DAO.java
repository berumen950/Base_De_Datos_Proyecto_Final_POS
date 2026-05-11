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
import javax.swing.table.*;
import javax.swing.*;
import java.sql.*;
import org.postgresql.util.*;
import org.postgresql.PGConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;
public class DAO {
    private String dbURL;
    private Map<String,Table> tables = new HashMap<>();
    private PGConnection pgcon;
    private Connection con;
    private PreparedStatement query;
    private Statement st;
    private ResultSet rs;
    
    public DAO(String dbURL){
        this.dbURL=dbURL;
    }
    
    public void start(JComboBox<String> tableSelect){
        String tableName=null;
        String format=null;
        Tag type=null;
        boolean fixed=false;
        List<String> values = new ArrayList<>();
        ArrayList<Col> colList = new ArrayList<>();
        try{
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection(dbURL,"admin","admin123");
            pgcon = con.unwrap(PGConnection.class);
            st=con.createStatement();
            rs=st.executeQuery("SELECT dataSift()");
            
            String json = null;
            
            if(rs.next()){
                json=rs.getString(1);
            }
            
            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(json);
            
            for(JsonNode tableNode : root){
                colList.clear();
                tableName=tableNode.get("table").asText();
                
                JsonNode columns = tableNode.get("columns");
                
                for(JsonNode col : columns){
                    String name = col.get("name").asText();
                    String typeText = col.get("Type").asText();
                    if(typeText.equals("INT") || typeText.equals("SERIAL") || typeText.equals("NUMERIC(10,2)")){
                        type=Tag.NUMERICAL;
                    }
                    if(typeText.equals("TEXT") || typeText.equals("VARCHAR")){
                        type=Tag.STRING;
                    }
                    if(typeText.equals("DATE")){
                        type=Tag.DATE;
                    }
                    if(typeText.equals("TIMESTAMP")){
                        type=Tag.DATETIME;
                    }
                    Boolean nullable = Boolean.parseBoolean(col.get("nullable").asText());
                    String data = col.path("comment").isNull() ? null : col.path("comment").asText();
                    if(data.isBlank()){
                        fixed=false;
                        format="NONE";
                    }
                    else{
                        String[] dataSplit = data.split("\\|");
                        
                        for (String part : dataSplit) {

                            if (part.startsWith("FIXED:")) {

                                String fixedPart = part.substring(6); 

                                if (fixedPart.startsWith("TRUE")) {
                                    fixed = true;

                                    int start = fixedPart.indexOf('(');
                                    int end = fixedPart.indexOf(')');

                                    if (start != -1 && end != -1) {
                                        String inside = fixedPart.substring(start + 1, end);
                                        values = Arrays.asList(inside.split(","));
                                    }

                                } else {
                                    fixed = false;
                                    values=null;
                                }
                            }

                            if (part.startsWith("FORMAT:")) {
                                format = part.substring(7);
                            }
                        }

                    }
                    Boolean autoIncrement = Boolean.parseBoolean(col.get("auto_increment").asText());
                    colList.add(new Col(name,type,fixed,values,nullable,format,autoIncrement));
                }
                tables.put(tableName, new Table(tableName,colList));
            }
            
            for(String table : tables.keySet()){
                tableSelect.addItem(table);
            }
            
        }
        catch(Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    public Table fetch(String name){
        return tables.get(name);
    }
    
    public void consult(DefaultTableModel model,String name){
        try{
            query = con.prepareStatement("SELECT * FROM ?");
            query.setString(1,name);
            rs= query.executeQuery();
            
            while(rs.next()){
                
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                Object[] row = new Object[colCount];

                for (int i = 1; i <= colCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }

                model.addRow(row);
            }
        } catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    public void create(){
        
    }
    public void delete(){
        
    }
    public void update(){
        
    }
    public void actionSF(){
        
    }
}
