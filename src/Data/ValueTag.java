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
import java.sql.*;
public record ValueTag(Object value, Tag type) {

public Object getCast(){
    return switch(this.type){
        case Tag.NUMERICAL -> toInt(this.value);
        case Tag.NUMERICAL_PRECISION -> toDouble(this.value);
        case Tag.DATE -> toDate(this.value);
        case Tag.DATETIME -> toTimestamp(this.value);
        case Tag.BOOLEAN -> toBoolean(this.value);
        case Tag.STRING -> toStringSafe(this.value);
        case Tag.DEFAULT -> this.value;
        default -> this.value;
    };
}
public Tag getTag(){
    return this.type;
}

private Integer toInt(Object v) {
    if (v == null) return null;
    if (v instanceof Integer i) return i;
    return Integer.parseInt(v.toString().trim());
}
private Double toDouble(Object v) {
    if (v == null) return null;
    if (v instanceof Double d) return d;
    if (v instanceof Integer i) return i.doubleValue();
    return Double.parseDouble(v.toString().trim());
}
private java.sql.Date toDate(Object v) {
    if (v == null) return null;
    if (v instanceof java.sql.Date d) return d;
    return java.sql.Date.valueOf(v.toString().trim());
}
private java.sql.Timestamp toTimestamp(Object v) {
    if (v == null) return null;
    if (v instanceof java.sql.Timestamp t) return t;
    return java.sql.Timestamp.valueOf(v.toString().trim());
}
private Boolean toBoolean(Object v) {
    if (v == null) return null;
    if (v instanceof Boolean b) return b;
    return Boolean.parseBoolean(v.toString().trim());
}
private String toStringSafe(Object v) {
    if (v == null) return null;
    return v.toString();
}

}  
