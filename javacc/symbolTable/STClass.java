public class STClass extends STEntry{

    SymbolTable classSymbolTable;

    public STClass(String id, String attributes, int line, int column, SymbolTable classSymbolTable){
        super(id, "Class", attributes, line, column);

        this.classSymbolTable = classSymbolTable;
    }

    public SymbolTable getSymbolTable(){
        return classSymbolTable;
    }


    public void print(int nivel){
        for(int i=0; i<nivel; i++){
            System.out.print("\t");
        }
        System.out.print("Class:\n");
        for(int i=0; i<nivel; i++){
            System.out.print("\t");
        }
        System.out.print("\tID: " + this.id + "; Attributes: " + this.attribute + "; Line: " + this.line + "; Column: " + this.column + "\n");
        classSymbolTable.printTable(++nivel);
    }

}