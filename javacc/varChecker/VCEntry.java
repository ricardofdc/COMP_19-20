import java.util.*;

public class VCEntry {

    private String varName;
    private String typeName;
    private SymbolTable symbolTable;
    private int line;
    private int column;

    public VCEntry(String varName, String typeName, SymbolTable symbolTable, int line, int column){
        this.varName = varName;
        this.typeName = typeName;
        this.symbolTable = symbolTable;
        this.line = line;
        this.column = column;
    }

    public void check() throws SemanticException {
        //to do
        //go to this.symbolTable and check if the functions exists with same args and return type;

        STEntry typeEntry = symbolTable.lookup(typeName);
        if(typeEntry == null){
            throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n" + typeName + " not declared.");
        }

    }

}