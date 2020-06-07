import java.util.*;

public class VarChecker {

    private static List<VCEntry> vars = new ArrayList<>();


    public static List<VCEntry> getVars(){
        return vars;
    }

    public static void add(VCEntry entry){
        vars.add(entry);
    }

    public static void check() throws SemanticException {
        for(VCEntry entry : vars){
            entry.check();
        }
    }

    public static void clean() {
        vars = new ArrayList<>();
    }
}