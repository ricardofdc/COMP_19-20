import java.util.*;

public class STFunc extends STEntry{

    List<List<List<String>>> argTypes; //Ex.: int name; int line;
    List<SymbolTable> funcSymbolTables;

    public STFunc(String id, String type, String attribute, int line, int column,  List<List<String>> argTypes, SymbolTable funcSymbolTable){
        super(id, type, attribute, line, column);

        this.argTypes = new ArrayList<>();
        this.argTypes.add(argTypes);
        this.funcSymbolTables = new ArrayList<>();
        this.funcSymbolTables.add(funcSymbolTable);

    }

    public STFunc(String id, String type, String attribute, int line, int column,  List<List<List<String>>> argTypes, SymbolTable funcSymbolTable, int nada){
        super(id, type, attribute, line, column);

        this.argTypes = argTypes;
        this.funcSymbolTables = new ArrayList<>();
        this.funcSymbolTables.add(funcSymbolTable);
        
    }

    int getArity(){
        return argTypes.size();
    }

    List<List<String>> getArgType(int index){
        return argTypes.get(index);
    }

    List<List<List<String>>> getArgTypes(){
        return argTypes;
    }


    void addArgs(List<List<String>> argTypes, SymbolTable symbolTable){
        this.argTypes.add(argTypes);
        this.funcSymbolTables.add(symbolTable);

    }

    public void print(int nivel){

        for(int i=0; i<nivel; i++){
            System.out.print("\t");
        }
        System.out.print("Function: \n");

        for(int i=0; i<nivel; i++){
            System.out.print("\t");
        }
        System.out.print("\tID: " + this.id + "; Type: " + this.type + "; Attributes: " + this.attribute + "; Line: " + this.line + "; Column: " + this.column + "\n");
        for(int j=0; j<argTypes.size(); j++  /*List<List<String>> args : argTypes*/){
            for(int i=0; i<nivel; i++){
                System.out.print("\t");
            }
            System.out.print("\t\tArguments: ");
            for(int i=0; i<argTypes.get(j).size(); i++){
                System.out.print(argTypes.get(j).get(i).toString() + " ");
            }
            System.out.print("\n");
            funcSymbolTables.get(j).printTable(nivel+1);

        }
    }
}