import java.util.*;

public class STImportClass extends STEntry{
    List<List<String>> argTypes;

    public STImportClass(String id, String type, String attribute, int line, int column,  List<String> argTypes){
        super(id, type, attribute, line, column);

        this.argTypes = new ArrayList<>();
        this.argTypes.add(argTypes);
    }

    int getArity(){
        return argTypes.size();
    }

    List<String> getArgType(int index){
        return argTypes.get(index);
    }

    void addArgs(List<String> argTypes){
        this.argTypes.add(argTypes);
    }

    public void print(int nivel){
        for(int i=0; i<nivel; i++){
            System.out.print("\t");
        }
        System.out.print("Import Class:\n");

        for(int i=0; i<nivel; i++){
            System.out.print("\t");
        }
        System.out.print("\tID: " + this.id + "; Attributes: " + this.attribute + "; Line: " + this.line + "; Column: " + this.column + "\n");
    }
}