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
public class DAO {
    private String dbURL;
    private ArrayList<Table> tables = new ArrayList<>();
    
    public DAO(String dbURL){
        this.dbURL=dbURL;
    }
    
    public ArrayList<String> start(){
        // temporal
        ArrayList<String> table_names = new ArrayList<>();
        return table_names;
    }
    
    public void IConsult(){
        
    }
    
    public void Rconsult(){
        
    }
    
    public void create(){
        
    }
    public void delete(){
        
    }
    public void update(){
        
    }
    public void filter(){
        
    }
}
