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
import java.util.*;
public class Col {
    private String name;
    private Tag type;
    private boolean is_fixed;
    private List<String> values = new ArrayList<>();
    private boolean is_nullable;
    private String format;
    private boolean is_increment;
    private boolean prKey;
    
    public Col(String name,Tag type, boolean is_fixed,List<String> values ,boolean is_nullable, String format,boolean is_increment,boolean prKey){
        this.name=name;
        this.type=type;
        this.is_fixed=is_fixed;
        if(is_fixed){
            this.values=values;
        }
        else{
            this.values=null;
        }
        this.is_nullable=is_nullable;
        this.format=format;
        this.is_increment=is_increment;
        this.prKey=prKey;
    }
    public String getName(){
        return this.name;
    }
    public Tag getType(){
        return this.type;
    }
    public boolean getState(){
        return this.is_fixed;
    }
    public void setName(String name){
        this.name=name;
    }
    public void setType(Tag type){
        this.type=type;
    }
    public void setState(boolean is_fixed){
        this.is_fixed=is_fixed;
    }
    public void setNull(boolean is_nullable){
        this.is_nullable=is_nullable;
    }
    public boolean getNull(){
        return this.is_nullable;
    }
    public void setFormat(String format){
        this.format=format;
    }
    public String getFormat(){
        return this.format;
    }
    public void setValues(ArrayList<String> NewValues){
        this.values=NewValues;
    }
    public void addValues(String value){
        this.values.add(value);
    }
    public List<String> getValues(){
        return this.values;
    }
    public String getValue(int index){
        return this.values.get(index);
    }
    public void setIncrementState(boolean is_increment){
        this.is_increment=is_increment;
    }
    public boolean getIncrementState(){
        return this.is_increment;
    }
    public boolean getPrKey(){
        return this.prKey;
    }
    public void setPrKey(boolean prKey){
        this.prKey=prKey;
    }
}
