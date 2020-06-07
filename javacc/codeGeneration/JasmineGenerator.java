import java.util.*;

public class JasmineGenerator {

    SymbolTable symbolTable;

    public JasmineGenerator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void print() {
        System.out.println("Jasmine symbol table: " + ((SimpleNode) symbolTable.getNode().jjtGetChild(0)).toString());
        for (int i = 0; i < symbolTable.getNode().jjtGetChild(0).jjtGetNumChildren(); i++) {
            System.out.println("Jasmine symbol table: " + ((SimpleNode) symbolTable.getNode().jjtGetChild(0).jjtGetChild(i)).toString());
        }

        for (int i = 0; i < ((SimpleNode) symbolTable.getNode().jjtGetChild(0)).jjtGetNumChildren(); i++) {
            System.out.println("\nabrakadabra: " + symbolTable.getNode().jjtGetChild(0).getClass());
        }

        //esta linha esta a dar *****
        System.out.println(visit((ASTClassDeclaration) symbolTable.getNode().jjtGetChild(0)));
    }

    public String genIntegerInstruction(int value) {
        String out = "";

        if (value >= 0 && value <= 5) {
            out = "iconst_" + value;
        } else if (value <= 127) {
            out = "bipush " + value;
        } else if (value <= 32767) {
            out = "sipush " + value;
        } else {
            out = "lcd " + value;
        }

        return out;
    }

    public String visit(ASTClassDeclaration node) {
        System.out.println("CLASS DECLARATION\n");
        String classStr = "";
        int children = ((SimpleNode) node).jjtGetNumChildren();
        boolean extend = false;

        //classStr += ".source <source-file>\n";

        String id = ((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString();

        classStr += ".class " + id;

        if (children == 1) {
            classStr += "\n.super java/lang/Object\n";
        } else if (children > 1) {

            if ((((SimpleNode) node.jjtGetChild(1)).toString()) == "Extends") {
                classStr += "\n.super " + ((SimpleNode) node.jjtGetChild(1).jjtGetChild(0)).jjtGetValue().toString() + "\n";
                extend = true;
            } else {
                classStr += "\n.super java/lang/Object\n";
            }
        }

        classStr += classStr;
        classStr += "\n";

        /*if (extend) {
            for (int i = 2; i < children; i++){
                SimpleNode n = (SimpleNode) node.jjtGetChild(i);
                if (n != null) {
                    n.accept(this, data);
                }
            }
        }
        else {
            for (int i = 1; i < children; i++){
                SimpleNode n = (SimpleNode) node.jjtGetChild(i);
                if (n != null) {
                    n.accept(this, data);
                }
            }
        }*/

        return classStr;
    }
}
