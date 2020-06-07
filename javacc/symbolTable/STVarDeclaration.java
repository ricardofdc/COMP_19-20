public class STVarDeclaration extends STEntry{
    protected boolean initialized;

    public STVarDeclaration(String id, String type, String attribute, int line, int column){
        super(id, type, attribute, line, column);
        this.initialized = false;
    }

    public void setInitialized(boolean value){
        this.initialized = value;
    }

    public boolean getInitialized(){
        return this.initialized;
    }

    public void print(int nivel){
        for(int i=0; i<nivel; i++){
            System.out.print("\t");
        }
        System.out.print("Variable:\n");

        for(int i=0; i<nivel; i++){
            System.out.print("\t");
        }
        System.out.print("\tID: " + this.id + "; Type: " + this.type + "; Attribute: " + this.attribute + "; Inicialized: " + this.initialized + "; Line: " + this.line + "; Column: " + this.column + "\n");
    }
}