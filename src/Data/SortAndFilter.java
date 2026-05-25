/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data;

/**
 *
 * @author emimo
 */
import java.util.*;
public class SortAndFilter {
    private String name;
    private ArrayList<String> arguments = new ArrayList<>();
    private String sorter;
    public SortAndFilter(String name,ArrayList<String> arguments,String sorter){
        this.name=name;
        this.arguments=arguments;
        this.sorter=sorter;
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
    public void setSorter(String sorter){
        this.sorter=sorter;
    }
    public void setName(String name){
        this.name=name;
    }
    public void setArguments(ArrayList<String> arguments){
        this.arguments=arguments;
    }
    public void addArgument(String arg){
        this.arguments.add(arg);
    }
    public void updateArgument(int index,String arg){
        this.arguments.set(index,arg);
    }
    public void removeArguments(int index){
        this.arguments.remove(index);
    }
    
}
