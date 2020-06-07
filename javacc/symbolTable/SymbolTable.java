import java.util.*;

public class SymbolTable {
    
    private HashMap<String, STEntry> symboltable = new HashMap<>();
    private SimpleNode node;
    //original symbol table
    private SymbolTable superSymbolTable;


    //(?)
    public SymbolTable(SimpleNode node){
        this.node = node;
    }

    public SymbolTable(SimpleNode node, SymbolTable superTable){
        this.node = node;
        this.superSymbolTable = superTable;
    }

    public List<STEntry> lookupClassChecker(String extendName){
        List<STEntry> funcList = new ArrayList<>();

        symboltable.forEach((key, value) -> {
            if(key.contains(extendName) && !key.equals(extendName)){
                funcList.add(value);
            }
        });
        return funcList;
    }

    public STEntry lookup(String id){
        STEntry entry = this.symboltable.get(id);
        if(entry != null){
            return entry;
        }
        else {
            if(superSymbolTable != null){
                return superSymbolTable.lookup(id);
            }
            else{
                return null;
            }
        }
    }

    public STEntry lookupCurrentTable(String id){
        return this.symboltable.get(id);
    }

    public void insert(String id, STEntry entry){
        this.symboltable.put(id, entry);
    }

    public int size(){
        return this.symboltable.size();
    }

    public SymbolTable getSuperTable(){
        return superSymbolTable;
    }

    public void printTable(int nivel){
        System.out.println();
        for(int i=0; i<nivel; i++){
            System.out.print("========");
        }
        System.out.print("== SYMBOL TABLE LEVEL " + nivel + " ==\n");
        System.out.println();
        for(STEntry entry : symboltable.values()){
            entry.print(nivel);
        }
        System.out.println();
        for(int i=0; i<nivel; i++){
            System.out.print("========");
        }
        System.out.print("== END SYMBOL TABLE LEVEL " + nivel + " ==\n");
    }

    public SimpleNode getNode(){
        return node;
    }
}