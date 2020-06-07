import java.util.*;

public class FunctionChecker {

    private static List<FCEntryChecker> functions = new ArrayList<>();


    public static List<FCEntryChecker> getFunctions(){
        return functions;
    }

    public static void add(FCEntryChecker entry){
        functions.add(entry);
    }

    public static void check() throws SemanticException {
        for(FCEntryChecker entry : functions){
            entry.check();
        }
    }

    public static void clean() {
        functions = new ArrayList<>();
    }
}