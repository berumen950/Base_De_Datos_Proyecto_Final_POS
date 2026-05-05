/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Backend;

import java.util.*;

/**
 *
 * @author emimo
 */
public class Table {
    private String name;
    private ArrayList<Col> columns = new ArrayList<>();
    
    public Table(String name){
        this.name=name;
    }
    
    public String getTableName(){
        return this.name;
    }
    public Object getColData(int colNum,String data){
        try{
            switch(data){
                case "NAME" ->{
                    return this.columns.get(colNum).getName();
                }
                case "TYPE" ->{
                    return this.columns.get(colNum).getType();
                }
                case "STATE" ->{
                    return this.columns.get(colNum).getState();
                }
                default ->{
                    return null;
                }
            }
        } catch(Exception e){
            return null;
        }
    }
}
