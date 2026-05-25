/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Backend;

/**
 *
 * @author emimo
 */
import Data.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.*;
import javax.swing.table.*;
import javax.swing.*;
import java.sql.*;
import org.postgresql.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.function.*;
public class DAO {
    private final String dbURL;
    private final Map<String,Table> tables = new HashMap<>();
    private PGConnection pgcon;
    private Connection con;
    private volatile boolean listen;
    public DAO(String dbURL){
        this.dbURL=dbURL;
    }
    
    public void start(JComboBox<String> tableSelect){
        try{
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection(dbURL,"admin","admin123");
            con.setAutoCommit(false);
            pgcon = con.unwrap(PGConnection.class);
            Statement st=con.createStatement();
            ResultSet rs=st.executeQuery("SELECT dataSift()");
            
            String json = null;
            
            if(rs.next()){
                json=rs.getString(1);
            }
            
            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(json);
            System.out.println(
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root)
            );
            
            for(JsonNode tableNode : root){
                IndexMap<String,Col> colList = new IndexMap<>();
                String tableName=tableNode.get("table").asText();
                
                JsonNode columns = tableNode.get("columns");
                
                for(JsonNode col : columns){
                    String format="NONE";
                    Tag type=Tag.DEFAULT;
                    boolean fixed=false;
                    List<String> values = new ArrayList<>();
                    String name = col.get("name").asText();
                    int typeOid = col.get("type_oid").intValue();
                        type = switch (typeOid) {
                        case 21, 23, 20 -> Tag.NUMERICAL;                    
                        case 700, 701, 1700 -> Tag.NUMERICAL_PRECISION;      
                        case 25, 1042, 1043 -> Tag.STRING;                   
                        case 1114 -> Tag.DATETIME;                           
                        case 1082, 1083 -> Tag.DATE;                         
                        case 16 -> Tag.BOOLEAN;                              
                        default -> Tag.DEFAULT;
                    };
                    Boolean nullable = col.get("nullable").asBoolean();
                    String data = col.path("comment").isNull() ? null : col.path("comment").asText();
                    if(data == null || data.isBlank()){
                        fixed=false;
                        format="NONE";
                    }
                    else{
                        String[] dataSplit = data.split("\\|");
                        
                        for (String part : dataSplit) {
                            
                            if (part.startsWith("FIXED:")) {

                                String fixedPart = part.substring(6).trim();

                                if(fixedPart.equalsIgnoreCase("NONE")){
                                    fixed=false;
                                }
                                else{
                                    fixed=true;
                                    values = Arrays.asList(
                                        fixedPart.split("\\s*,\\s*")
                                    );
                                }
                            }
                            if (part.startsWith("FORMAT:")) {
                                format = part.substring(7);
                            }
                        }

                    }
                    Boolean autoIncrement = col.get("auto_increment").asBoolean();
                    Boolean prKey = col.get("primary_key").asBoolean();
                    colList.put(name,new Col(name,type,fixed,values,nullable,format,autoIncrement,prKey));
                }
                tables.put(tableName, new Table(tableName,colList));
            }
            
            for(String table : tables.keySet()){
                tableSelect.addItem(table);
            }
            rs.close();
            st.close();
        }
        catch(Exception e){
            System.out.println("DAO start error");
            e.printStackTrace();
        }
    }
    
    
    
    
    public Table fetch(String name){
        return tables.get(name);
    }
    
    public void consult(DefaultTableModel model,String name,String filter){
        PreparedStatement query;
        try{
            if(filter.isBlank()){
                query = con.prepareStatement("SELECT * FROM " + name);
            }
            else{
                query = con.prepareStatement("SELECT * FROM " + name + " WHERE " + filter);
            }
            ResultSet rs= query.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            while(rs.next()){
                
                Object[] row = new Object[colCount];

                for (int i = 1; i <= colCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }

                model.addRow(row);
            }
            rs.close();
            query.close();
        } catch (Exception e){
            System.out.println("Error at consult");
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    
    
 
    public void create(String name,LinkedHashMap<String,ValueTag> values) throws Exception{
        PreparedStatement query;
        try{
            StringBuilder cols = new StringBuilder();
            StringBuilder vals = new StringBuilder();
            int i = 1;

            for (String col : values.keySet()) {
                cols.append(col).append(",");
                vals.append("?,");
            }
            cols.deleteCharAt(cols.length() - 1);
            vals.deleteCharAt(vals.length() - 1);

            String sql = "INSERT INTO " + name + "(" + cols + ")" + " VALUES (" + vals + ")";
            query = con.prepareStatement(sql);
            for (ValueTag valInfo : values.values()) {
                System.out.println("Value: " + valInfo.getCast() + " Type: " + valInfo.getTag());
                query.setObject(i, valInfo.getCast());
                i++;
            }
            query.executeUpdate();
            con.commit();
            query.close();
        }
        catch (SQLException e){
            try{
                con.rollback();
            } catch (SQLException rollbackError){
                rollbackError.printStackTrace();
            }
            throw e;
        }
    }
    public void delete(String name,LinkedHashMap<String,ValueTag>position) throws Exception{
        PreparedStatement query;
        try{
            StringBuilder sql = new StringBuilder("DELETE FROM " + name + " WHERE ");
            int i=1;
            for(String col: position.keySet()){
                sql.append(col).append(" = ? AND ");
            }
            sql.delete(sql.length()-5, sql.length());

            query = con.prepareStatement(sql.toString());

            for (ValueTag posInfo : position.values()) {
                System.out.println("Position: " + posInfo.getCast() + " Type: " + posInfo.getTag());
                query.setObject(i, posInfo.getCast());
                i++;
            }
            query.executeUpdate();
            con.commit();
            query.close();
        } 
        catch (SQLException e){
            try{
                con.rollback();
            } catch (SQLException rollbackError){
                rollbackError.printStackTrace();
            }
            throw e;
        }
    }
    public void update(String name,LinkedHashMap<String,ValueTag> values, LinkedHashMap<String,ValueTag> position)throws Exception{
        PreparedStatement query;
        try{
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
            String sql="UPDATE " + name + " SET " + cols + " WHERE " + idList;
            query = con.prepareStatement(sql);
            for(ValueTag valInfo: values.values()){
                System.out.println("Value: " + valInfo.getCast() + " Type: " + valInfo.getTag());
                query.setObject(i,valInfo.getCast());
                i++;
            }
            for(ValueTag posInfo: position.values()){
                System.out.println("Position: " + posInfo.getCast() + " Type: " + posInfo.getTag());
                query.setObject(i,posInfo.getCast());
                i++;
            }
            query.executeUpdate();
            con.commit();
            query.close();
        }
        catch(SQLException e){
            try{
                con.rollback();
            } catch (SQLException rollbackError){
                rollbackError.printStackTrace();
            }
            throw e;
        }
    }
    
    
    public void actionSP(String sql) throws Exception{
        PreparedStatement query;
        try{
            query=con.prepareStatement(sql);
            query.executeUpdate();
            con.commit();
            query.close();
        }
        catch(SQLException e){

            try{
                con.rollback();
            }
            catch(SQLException ex){
                ex.printStackTrace();
            }

            throw e;
        }
    }
    public void startListening(Consumer<LowStockEvent> receiver){
        if(this.listen){
            return;
        }
        Thread listenThread = new Thread(()->{
                try(Statement st = con.createStatement()){
                    st.execute("LISTEN low_stock_channel");
                    ObjectMapper mapper = new ObjectMapper();
                    while(listen){
                        PGNotification[] notifications = pgcon.getNotifications();

                        if(notifications != null){
                            for(PGNotification n: notifications){
                                LowStockEvent event =
                                mapper.readValue(n.getParameter(), LowStockEvent.class);

                                receiver.accept(event);
                            }
                        }
                        Thread.sleep(1000);
                    }
                }
                catch(InterruptedException | SQLException | JsonProcessingException e){
                    System.out.println("Error: " + e.getMessage());
                }
                finally{
                    listen=false;
                }
        });
        this.listen=true;
        listenThread.setDaemon(true);
        listenThread.start();
    }
    
    
    public void stopListening(){
        this.listen=false;
    }
    
    
    
public void closeAll(){
    listen=false;

    try{
        if(con != null && !con.isClosed()){
            con.close();
        }
    }
    catch(SQLException e){
        e.printStackTrace();
    }
}
    
}
