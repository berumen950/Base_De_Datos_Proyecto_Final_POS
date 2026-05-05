/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Backend;

/**
 *
 * @author emimo
 */
public class Col {
    private String name;
    private Tag type;
    private boolean is_fixed;
    
    public Col(String name,Tag type, boolean is_fixed){
        this.name=name;
        this.type=type;
        this.is_fixed=is_fixed;
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
}
