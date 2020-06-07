import java.util.*;

public class ClassChecker {

    private static List<CCEntry> classes = new ArrayList<>();


    public static List<CCEntry> getClasses(){
        return classes;
    }

    public static void add(CCEntry entry){
        classes.add(entry);
    }

    public static void check() throws SemanticException {
        for(CCEntry entry : classes){
            entry.check();
        }
    }

    public static void clean() {
        classes = new ArrayList<>();
    }
}