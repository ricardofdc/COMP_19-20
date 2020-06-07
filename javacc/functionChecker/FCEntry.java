import java.util.*;

public class FCEntry extends FCEntryChecker{

    private String funcName;
    private List<String> argTypes;
    private String returnType;
    private int line;
    private int column;
    private SymbolTable symbolTable; //used to chek in the end if the function exists

    public FCEntry(String name, List<String> argTypes, String returnType, int line, int column, SymbolTable symbolTable){
        this.funcName = name;
        this.argTypes = argTypes;
        this.returnType = returnType;
        this.line = line;
        this.column = column;
        this.symbolTable = symbolTable;
    }

    public void check() throws SemanticException {
        //to do
        //go to this.symbolTable and check if the functions exists with same args and return type;

        STEntry entry = symbolTable.lookup(funcName);

        if(entry == null){
            if(funcName.contains("this.")){
                String[] s = funcName.split("\\.");
                this.funcName = s[1];
                this.check();
                return;
            }
            else throw new SemanticException("SEMANTIC ERROR on line " + line + ".\nFunction " + funcName + " not declared.");
        }

        if(!entry.getType().equals(returnType)){
            throw new SemanticException("SEMANTIC ERROR on line " + line + ".\nFunction " + funcName + " return type is " + entry.getType() + " and it was expected " + returnType + ".");
        }

        boolean acept = false;

        if(entry instanceof STFunc) {

            for (int i = 0; i < ((STFunc) entry).getArity(); i++) {
                List<List<String>> argsList = ((STFunc) entry).getArgType(i);
                boolean temp = true;

                if(argsList.size() != argTypes.size()){
                    continue;
                }

                for (int j = 0; j < argsList.size(); j++) {
                    if (!argsList.get(j).get(0).equals(argTypes.get(j))) {
                        temp = false;
                    }
                }
                if (temp == true) {
                    acept = true;
                    break;
                }
            }

        }
        else {
            for (int i = 0; i < ((STImportFunc) entry).getArity(); i++) {
                List<String> argsList = ((STImportFunc) entry).getArgType(i);
                boolean temp = true;

                if(argsList.size() != argTypes.size()){
                    continue;
                }
                
                for (int j = 0; j < argsList.size(); j++) {
                    if (!argsList.get(j).equals(argTypes.get(j))) {
                        temp = false;
                    }
                }
                if (temp == true) {
                    acept = true;
                    break;
                }
            }
        }

        if(!acept){
            throw new SemanticException("SEMANTIC ERROR on line " + line + ".\nFunction " + funcName + " arguments don't match any function declaration.");
        }


    }

}