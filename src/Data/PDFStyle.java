/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data;

import java.util.*;
import java.awt.color.*;
import java.awt.Color;
import java.awt.Font;

public class PDFStyle {
    private String title;
    private int titleTextSize;
    private Font titleFont;
    private boolean boldTitle;
    private boolean italicTitle;
    private Color titleColor;
    private Font textFont;
    private int textSize;
    private boolean boldText;
    private boolean italicText;


    public PDFStyle(String title, int titleTextSize, Font titleFont, boolean boldTitle, boolean italicTitle,Color titleColor,
                    Font textFont, int textSize, boolean boldText, boolean italicText) {
        this.title = title;
        this.titleTextSize = titleTextSize;
        this.titleFont = titleFont;
        this.boldTitle = boldTitle;
        this.italicTitle = italicTitle;
        this.titleColor=titleColor;
        this.textFont = textFont;
        this.textSize = textSize;
        this.boldText = boldText;
        this.italicText = italicText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTitleTextSize() {
        return titleTextSize;
    }

    public void setTitleTextSize(int titleTextSize) {
        this.titleTextSize = titleTextSize;
    }

    public Font getTitleFont() {
        return titleFont;
    }

    public void setTitleFont(Font titleFont) {
        this.titleFont = titleFont;
    }

    public boolean isBoldTitle() {
        return boldTitle;
    }

    public void setBoldTitle(boolean boldTitle) {
        this.boldTitle = boldTitle;
    }

    public boolean isItalicTitle() {
        return italicTitle;
    }

    public void setItalicTitle(boolean italicTitle) {
        this.italicTitle = italicTitle;
    }

    public Color getTitleColor(){
        return this.titleColor;
    }
    
    public void setTitleColor(Color newColor){
        this.titleColor=newColor;
    }
    
    public Font getTextFont(){
        return textFont;
    }

    public void setTextFont(Font textFont) {
        this.textFont = textFont;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public boolean isBoldText() {
        return boldText;
    }

    public void setBoldText(boolean boldText) {
        this.boldText = boldText;
    }

    public boolean isItalicText() {
        return italicText;
    }

    public void setItalicText(boolean italicText) {
        this.italicText = italicText;
    }
}

