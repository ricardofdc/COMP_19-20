import java.util.*;

public class STImportFunc extends STEntry{
    List<List<String>> argTypes;

    public STImportFunc(String id, String type, String attribute, int line, int column,  List<String> argTypes){
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

    List<List<String>> getArgTypes(){
        return argTypes;
    }

    void addArgs(List<String> argTypes){
        this.argTypes.add(argTypes);
    }

    public void print(int nivel){
        for(int i=0; i<nivel; i++){
            System.out.print("\t");
        }
        System.out.print("Import Function:\n");

        for(int i=0; i<nivel; i++){
            System.out.print("\t");
        }
        System.out.print("\tID: " + this.id + "; Return Type: " + this.type + "; Attributes: " + this.attribute + "; Line: " + this.line + "; Column: " + this.column + "\n");
        for(List<String> args : argTypes){
            for(int i=0; i<nivel; i++){
                System.out.print("\t");
            }
            System.out.print("\t\tArguments: " + Arrays.toString(args.toArray()) + "\n");
        }

    }
}