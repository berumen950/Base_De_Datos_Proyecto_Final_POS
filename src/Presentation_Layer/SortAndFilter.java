/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Presentation_Layer;

/**
 *
 * @author emimo
 */
import java.util.*;
public class SortAndFilter {
    private String name;
    private ArrayList<String> filters = new ArrayList<>();
    private String sorter;
    public SortAndFilter(String name){
        this.name=name;
    }
    public String getName(){
        return this.name;
    }
    public ArrayList<String> getFilters(){
        return this.filters;
    }
    public String getSorter(){
        return this.sorter;
    }
    public void setName(String name){
        this.name=name;
    }
    public void setFilter(ArrayList<String> filters){
        this.filters=filters;
    }
    
}
