/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data;

/**
 *
 * @author emimo
 */
import java.util.List;
public enum Tag {
    NUMERICAL(List.of("EQUAL","GREATER","LESSER","GRTEQ","LESEQ","NEQUAL")),
    NUMERICAL_PRECISION(List.of("EQUAL","GREATER","LESSER","GRTEQ","LESEQ","NEQUAL")),
    STRING(List.of("EQUAL","NEQUAL","LIKE","ILIKE","NLIKE","NILIKE")),
    DATETIME(List.of("EQUAL","GREATER","LESSER","GRTEQ","LESEQ","NEQUAL","BETWEEN")),
    DATE(List.of("EQUAL","GREATER","LESSER","GRTEQ","LESEQ","NEQUAL","BETWEEN")),
    BOOLEAN(List.of("DEFAULT","NOT")),
    DEFAULT(List.of());
    private final List<String> operators;

    Tag(List<String> operators) {
        this.operators = operators;
    }

    public List<String> getOperators() {
        return operators;
    }

}

