/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Backend;

import java.util.*;
import Data.*;

/**
 *
 * @author emimo
 */
import Data.IndexMap;
public class Table {
    private String name;
    private IndexMap<String,Col> columns = new IndexMap<>();
    
    public Table(String name,IndexMap<String,Col> columns){
        this.name=name;
        this.columns=columns;
    }
    
    public String getTableName(){
        return this.name;
    }
    public Object getColData(String colName,String data){
        try{
            switch(data){
                case "NAME" ->{
                    return this.columns.get(colName).getName();
                }
                case "TYPE" ->{
                    return this.columns.get(colName).getType();
                }
                case "STATE" ->{
                    return this.columns.get(colName).getState();
                }
                case "NullType" ->{
                    return this.columns.get(colName).getNull();
                }
                case "PRIMARY" ->{
                    return this.columns.get(colName).getPrKey();
                }
                default ->{
                    return null;
                }
            }
        } catch(Exception e){
            return null;
        }
    }
    public Object getColData(int colNum,String data){
        try{
            switch(data){
                case "NAME" ->{
                    return this.columns.getByIndex(colNum).getName();
                }
                case "TYPE" ->{
                    return this.columns.getByIndex(colNum).getType();
                }
                case "STATE" ->{
                    return this.columns.getByIndex(colNum).getState();
                }
                case "NullType" ->{
                    return this.columns.getByIndex(colNum).getNull();
                }
                case "PRIMARY" ->{
                    return this.columns.getByIndex(colNum).getPrKey();
                }
                default ->{
                    return null;
                }
            }
        } catch(Exception e){
            return null;
        }
    }
    public IndexMap<String,Col> getColList(){
        return this.columns;
    }
}
