import java.util.*;

public class CCEntry {

    private String className;
    private String extendName;
    private SymbolTable symbolTable;

    public CCEntry(String name, String extendName, SymbolTable symbolTable){
        this.className = name;
        this.extendName = extendName;
        this.symbolTable = symbolTable;
    }

    public void check() throws SemanticException {
        //to do
        //go to this.symbolTable and check if the functions exists with same args and return type;

        List<STEntry> entrys = symbolTable.getSuperTable().lookupClassChecker(extendName);
        for(STEntry entry : entrys){
            String[] id = entry.getId().split("\\.");
            symbolTable.insert(id[1], entry);

            /*
            String type = entry.getType();
            String attribute = entry.getAttribute();
            int line = entry.getLine();
            int column = entry.getColumn();
            List<List<String>> argTypes = ((STImportFunc) entry).getArgTypes();
            System.out.println("argTypes: " + argTypes);

            List<List<List<String>>> newArgTypes = new ArrayList<>();
            for(List<String> list : argTypes){
                List<List<String>> tmp = new ArrayList<>();
                for(String arg : list){
                    List<String> tmp2 = new ArrayList<>();
                    tmp2.add(arg);
                    tmp2.add("null");
                    tmp.add(tmp2);
                }
                newArgTypes.add(tmp);
            }


            STFunc funcEntry = new STFunc(id[1], type, attribute, line, column, newArgTypes, symbolTable, 0);

            symbolTable.insert(id[1], funcEntry);
*/
            
        }
    }

}