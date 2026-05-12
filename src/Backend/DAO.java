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
import org.postgresql.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.function.*;
import java.util.concurrent.*;

import java.util.*;
public class DAO {
    private String dbURL;
    private Map<String,Table> tables = new HashMap<>();
    private PGConnection pgcon;
    private Connection con;
    private PreparedStatement query;
    private Statement st;
    private ResultSet rs;
    private boolean listen;
    
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
                    Boolean prKey = Boolean.parseBoolean(col.get("primary_key").asText());
                    colList.add(new Col(name,type,fixed,values,nullable,format,autoIncrement,prKey));
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
    
    public void create(String name,LinkedHashMap<String,Object> values) throws Exception{
        StringBuilder cols = new StringBuilder();
        StringBuilder vals = new StringBuilder();
        int i = 1;
        
        for (String col : values.keySet()) {
            cols.append(col).append(",");
            vals.append("?,");
        }
        cols.deleteCharAt(cols.length() - 1);
        vals.deleteCharAt(vals.length() - 1);
        
        String sql = "INSERT INTO" + name + "(" + cols + ")" + " VALUES (" + vals + ")";
        query = con.prepareStatement(sql);
        for (Object value : values.values()) {
            query.setObject(i, value);
            i++;
        }
        query.executeUpdate();
    }
    public void delete(String name,LinkedHashMap<String,Object>position) throws Exception{
        StringBuilder sql = new StringBuilder("DELETE FROM " + name + " WHERE ");
        int i=1;
        for(String col: position.keySet()){
            sql.append(col).append(" = ? AND ");
        }
        sql.delete(sql.length()-5, sql.length());
        
        query = con.prepareStatement(sql.toString());
        
        for(Object id: position.values()){
            query.setObject(i,id);
            i++;
        }
        query.executeUpdate();
    }
    public void update(String name,LinkedHashMap<String,Object> values, LinkedHashMap<String,Object> position)throws Exception{
        StringBuilder cols = new StringBuilder();
        StringBuilder idList = new StringBuilder();
        int i=1;
        for(String col:values.keySet()){
            cols.append(col).append("=?,");
        }
        cols.deleteCharAt(cols.length()-1);
        for(String id: position.keySet()){
            idList.append(id).append("=? AND ");
        }
        idList.delete(idList.length()-5,idList.length());
        String sql="UPDATE " + name + "SET " + cols + " WHERE " + idList;
        query = con.prepareStatement(sql);
        for(Object value: values.values()){
            query.setObject(i,value);
            i++;
        }
        for(Object id: position.values()){
            query.setObject(i,id);
            i++;
        }
        query.executeUpdate();
    }
    public void actionSF(String name,String sql) throws Exception{
        query = con.prepareStatement(sql);
        query.executeUpdate();
    }
    public void StartListening(Consumer<String> receiver){
        if(this.listen){
            return;
        }
        this.listen=true;
        
        Thread listenThread = new Thread(()->{
                try{
                    st = con.createStatement();
                    st.execute("LISTEN low_stock_channel");
                   
                    while(listen){
                        PGNotification[] notifications = pgcon.getNotifications();

                        if(notifications != null){
                            for(PGNotification n: notifications){
                                receiver.accept(n.getParameter());
                            }
                        }
                        Thread.sleep(1000);
                    }
                }
                catch(InterruptedException | SQLException e){
                    System.out.println("Error: " + e.getMessage());
                }
        });
    }
    
    
    public void stopListening(){
        this.listen=false;
    }
    
    
    
    
    
}
