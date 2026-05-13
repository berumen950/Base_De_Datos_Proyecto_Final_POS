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
    private ArrayList<String> arguments = new ArrayList<>();
    private String sorter;
    public SortAndFilter(String name){
        this.name=name;
    }
    public String getName(){
        return this.name;
    }
    public ArrayList<String> getArguments(){
        return this.arguments;
    }
    public String getSorter(){
        return this.sorter;
    }
    public void setName(String name){
        this.name=name;
    }
    public void setArguments(ArrayList<String> arguments){
        this.arguments=arguments;
    }
    
}
