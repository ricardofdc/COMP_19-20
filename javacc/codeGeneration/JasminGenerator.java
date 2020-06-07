import java.util.*;
import java.io.*;

public class JasminGenerator implements jmmVisitor {

    private String code = "";
    private String classDeclaration = "";
    private String vars = "";
    private List<String> methodsList = new ArrayList<>();
    private List<String> variables = new ArrayList<>();
    private List<String> methodsCode = new ArrayList<>();
    private List<String> fieldVars = new ArrayList<>();
    private List<String> loopVars = new ArrayList<>();
    private List<List<String>> imports = new ArrayList<>();
    private List<String> loop = new ArrayList<>();
    private List<String> objectVars = new ArrayList<>();
    private List<String> ifLabels = new ArrayList<>();
    private int numWhiles = 0;
    private List<String> variabelsArrays = new ArrayList<>();
    private String dotInfo = "";


    private String arrayInfo = "";


    // TEST VARIABLES
    private String methods = "";

    public String genIntegerInstruction(String value) {
        String out = "";

        int foo;
        try {
            foo = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            foo = 0;
        }

        if (foo >= 0 && foo <= 5) {
            out = "iconst_" + value;
        } else if (foo <= 127) {
            out = "bipush " + foo;
        } else if (foo <= 32767) {
            out = "sipush " + foo;
        } else {
            out = "ldc " + foo;
        }

        return out;
    }

    public Object visit(ASTStart_parser node, SymbolTable data) throws SemanticException {

        // STANDARD INITIALIZER
        this.methodsList.add(0,
                ".method public <init>()V\n\taload_0\n\n\tinvokenonvirtual java/lang/Object/<init>()V\n\n\treturn\n.end method\n");
        return null;
    }

    public Object visit(ASTDot node, SymbolTable data) throws SemanticException {

        String tmp = "";

        // IMPORT STATEMENT
        if (jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetParent()).getId()].equals("Import")) {

        }
        // GETTING LENGHT PROPERTY
        else if (jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetChild(1)).getId()].equals("Length")) {
            // DO FOR ARRAYS
            String info = "";
            for (int i = 0; i < this.variabelsArrays.size(); i++) {

                if (((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString()
                        .equals(this.variabelsArrays.get(i))) {
                    if ((i + 1 + this.variables.size()) <= 3) {
                        info += "aload_" + (i + 1 + this.variables.size()) + "\n\t";
                    } else {
                        info += "aload " + (i + 1 + this.variables.size()) + "\n\t";
                    }
                    break;
                }
            }

            info += "arraylength\n\t";

            if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetParent()).getId()].equals("Operator")){
                this.dotInfo = info;
            } else {
                tmp += info;
            }

        } else { // METHOD CALLS

            // LOAD VARIABLES
            for (int i = 0; i < ((SimpleNode) node.jjtGetChild(2)).jjtGetNumChildren(); i++) {
                // LOCAL VARIABLE
                boolean isLocal = false;
                for (int j = 0; j < this.variables.size(); j++) {
                    if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(2).jjtGetChild(i)).getId()].equals("void")
                    || jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(2).jjtGetChild(i)).getId()].equals("ArrayAccess")){
                        break;
                    }

                    if (((SimpleNode) node.jjtGetChild(2).jjtGetChild(i)).jjtGetValue().toString()
                            .equals(this.variables.get(j))) {
                        isLocal = true;
                        if (j <= 3) {
                            tmp += "iload_" + (j + 1) + "\n\t";
                            /*this.methodsCode.add(tmp);
                            tmp = "";*/
                        } else {
                            tmp += "iload " + (j + 1) + "\n\t";
                            /*this.methodsCode.add(tmp);
                            tmp = "";*/
                        }
                        break;
                    }
                }

                // FIELD VARIABLE
                if (!isLocal) {
                    for (int j = 0; j < this.fieldVars.size(); j++) {

                        String[] splitStr = this.fieldVars.get(j).split("\\s+");

                        if (((SimpleNode) node.jjtGetChild(2).jjtGetChild(i)).jjtGetValue().toString()
                                .equals(splitStr[0])) {
                            tmp += "getfield " + this.fieldVars.get(j) + "\n\t";
                            /*this.methodsCode.add(tmp);
                            tmp = "";*/
                        }
                    }
                }
            }        

            boolean isDefined = false;
            //CHECK IF CALLER IS AN OBJECT THAT HAS BEEN DEFINED
            if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(0)).getId()].equals("This")){
                isDefined = true;
            }else{
                String caller = ((SimpleNode)node.jjtGetChild(0)).jjtGetValue().toString();
                for (int i = 0; i < this.variables.size(); i++) {
                    if (caller.equals(this.variables.get(i))) {
                        isDefined = true;
                        //boolean isObject = false;
                        if(objectVars.contains(caller)){
                            if((i + 1) <= 3){
                                tmp = "aload_" + (i + 1) + "\n\t" + tmp;
                                //tmp += "aload_" + (i + 1);
                            }
                            else{
                                tmp = "aload " + (i + 1) + "\n\t" + tmp;
                                //tmp += "aload " + (i + 1);
                            }
                        }
                        break;
                    }
                }
                this.methodsCode.add(tmp);
                tmp = "";
                if (!isDefined) {
                    for (int i = 0; i < this.fieldVars.size(); i++) {

                        String[] splitStr = this.fieldVars.get(i).split("\\s+");

                        if (caller.equals(splitStr[0])) {
                            isDefined = true;
                            break;
                        }
                    }
                }
            }

            // VARIABLE IS DEFINED - INVOKEVIRTUAL
            String methodName = ((SimpleNode) node.jjtGetChild(1)).jjtGetValue().toString();

            if (isDefined) {
                tmp = "invokevirtual" + " " + methodName;

                int argsNr = ((SimpleNode) node.jjtGetChild(2)).jjtGetNumChildren();
                List<String> argTypes = new ArrayList<>();
                String returnType = "";

                // FINDING METHOD ARGS AND RETURN TYPE
                boolean done = false;
                for (String methodCode : methodsList) {
                    if (done) {
                        break;
                    }
                    String lines[] = methodCode.split("\\r?\\n");

                    for (int i = 0; i < lines.length; i++) {
                        if (done) {
                            break;
                        }
                        String words[] = lines[i].split(" ");
                        String firstWord = words[0];

                        if (firstWord.equals(".method")) {
                            String[] nameAndArgs = words[1].split("[(]");
                            nameAndArgs[1] = nameAndArgs[1].substring(0, nameAndArgs[1].length() - 1);

                            String name = nameAndArgs[0];
                            String[] args = nameAndArgs[1].split("[;]");

                            if (!name.equals(methodName)) {
                                break;
                            }

                            done = true;
                            tmp += " (";
                            for (int j = 0; j < args.length; j++) {
                                if (j == (args.length - 1)) {
                                    tmp += args[j] + ") " + words[2];
                                    break;
                                }
                                tmp += args[j] + ";";
                            }

                        }

                    }

                }

                if (!done) {

                }

            } else { // METHOD CALL FROM IMPORT - INVOKESTATIC
                String object = ((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString();
                tmp = "invokestatic" + " " + object + "/" + methodName + "(";

                // FINDING IMPORT ARGS AND RETURN TYPE
                int argsNr = ((SimpleNode) node.jjtGetChild(2)).jjtGetNumChildren();
                List<String> argTypes = new ArrayList<>();
                String returnType = "";

                boolean done = false;
                for (int i = 0; i < this.imports.size(); i++) {
                    if (done) {
                        break;
                    }

                    String name = this.imports.get(i).get(0).split(" ")[1];
                    if (name.equals(object + methodName)) {

                        List<String> importInfo = this.imports.get(i);
                        for (String info : importInfo) {
                            String[] splitInfo = info.split(" ");

                            if (splitInfo[0].equals("name")) {
                                continue;
                            }

                            if (splitInfo[0].equals("arg")) {
                                argTypes.add(getJasminTypeFromString(splitInfo[1]));
                                continue;
                            }

                            if (splitInfo[0].equals("ret")) {
                                if (argTypes.size() != argsNr) {// method with same name but different nr of args
                                    argTypes = new ArrayList<>();
                                    break;
                                }
                                returnType = getJasminTypeFromString(splitInfo[1]);
                                done = true;
                                break;
                            }
                        }
                    }
                }

                for (int i = 0; i < argTypes.size(); i++) {

                    if(argTypes.get(i).equals("V")){
                        tmp += ")" + returnType;
                        break;
                    }

                    if (i == (argTypes.size() - 1)) {
                        

                        tmp += argTypes.get(i) + ")" + returnType;
                        break;
                    }

                    tmp += argTypes.get(i) + ";";
                }
            }

        }

        if (isDescendentOf("While", node)) {
            this.loop.add(tmp);
        } else {
            this.methodsCode.add(tmp);
        }

        return null;
    }

    public Object visit(ASTReturn node, SymbolTable data) throws SemanticException {

        if(((SimpleNode)node).jjtGetNumChildren() > 0){

            if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(0)).getId()].equals("IntegerLiteral")){
                this.methodsCode.add(genIntegerInstruction(((SimpleNode)node.jjtGetChild(0)).jjtGetValue().toString()));
            } else if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(0)).getId()].equals("Identifier")){
                boolean isLocal = false;
                boolean isLoop = false;
                String tmp = "";
                for (int i = 0; i < this.variables.size(); i++) {

                    if (((SimpleNode)node.jjtGetChild(0)).jjtGetValue().toString()
                            .equals(this.variables.get(i))) {
                        isLocal = true;
                        if (i <= 3) {
                            tmp += "iload_" + (i + 1) + "\n\t";
                        } else {
                            tmp += "iload " + (i + 1) + "\n\t";
                        }
                        break;
                    }
                }
                
                if (!isLocal) {
                    for (int i = 0; i < this.loopVars.size(); i++) {

                        if (((SimpleNode)node.jjtGetChild(0)).jjtGetValue().toString()
                                .equals(this.loopVars.get(i))) {
                            isLoop = true;
                            if (i <= 3) {
                                tmp += "iload_" + (i + 1 + this.variables.size()) + "\n\t";
                            } else {
                                tmp += "iload " + (i + 1 + this.variables.size()) + "\n\t";
                            }
                            break;
                        }
                    }
                }

                if (!isLoop && !isLocal) {
                    for (int i = 0; i < this.fieldVars.size(); i++) {

                        String[] splitStr = this.fieldVars.get(i).split("\\s+");

                        if (((SimpleNode)node.jjtGetChild(0)).jjtGetValue().toString()
                                .equals(splitStr[0])) {
                            tmp += "getfield " + this.fieldVars.get(i) + "\n\t";
                        }
                    }
                }

                this.methodsCode.add(tmp);
            }
        }

        return null;
    }

    public Object visit(ASTImport node, SymbolTable data) throws SemanticException {

        // STORE IMPORT INFORMATION
        if (((SimpleNode) node).jjtGetNumChildren() != 1) {
            List<String> importInfo = new ArrayList<>();

            if (((SimpleNode) node.jjtGetChild(0)).jjtGetNumChildren() > 0) {// import has dot
                importInfo.add("name " + ((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).jjtGetValue().toString()
                        + ((SimpleNode) node.jjtGetChild(0).jjtGetChild(1)).jjtGetValue().toString());
            } else {
                importInfo.add("name " + ((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString());
            }

            for (int i = 0; i < ((SimpleNode) node.jjtGetChild(1)).jjtGetNumChildren(); i++) {
                importInfo.add("arg " + ((SimpleNode) node.jjtGetChild(1).jjtGetChild(i)).jjtGetValue().toString());
            }

            if (((SimpleNode) node.jjtGetChild(2)).jjtGetNumChildren() > 0) {
                String returnType = ((SimpleNode) node.jjtGetChild(2).jjtGetChild(0)).jjtGetValue().toString();
                importInfo.add("ret " + returnType);
            } else {
                importInfo.add("ret void");
            }

            imports.add(importInfo);
        }

        return null;
    }

    public Object visit(ASTArguments node, SymbolTable data) throws SemanticException {

        for (int i = 1; i < ((SimpleNode) node).jjtGetNumChildren(); i += 2) {
            if(((SimpleNode)node.jjtGetChild(i - 1)).jjtGetValue().toString().equals("int[]")){
                this.variabelsArrays.add(((SimpleNode) node.jjtGetChild(i)).jjtGetValue().toString());
                continue;
            }
            this.variables.add(((SimpleNode) node.jjtGetChild(i)).jjtGetValue().toString());
        }

        return null;
    }

    public Object visit(ASTClassDeclaration node, SymbolTable data) throws SemanticException {
        String classStr = "";
        int children = ((SimpleNode) node).jjtGetNumChildren();
        boolean extend = false;

        String id = ((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString();

        STEntry entry = data.lookup(id);

        classStr += ".class " + id;

        if (children == 1) {
            classStr += "\n.super java/lang/Object\n";
        } else if (children > 1) {

            if ((((SimpleNode) node.jjtGetChild(1)).toString()) == "Extends") {
                classStr += "\n.super " + ((SimpleNode) node.jjtGetChild(1).jjtGetChild(0)).jjtGetValue().toString()
                        + "\n";
                extend = true;
            } else {
                classStr += "\n.super java/lang/Object\n";
            }
        }

        classDeclaration = classStr + "\n";

        // ADICIONAR NOME DA CLASS ONDE E NECESSARIO
        int cnt = 0;
        for (String methodCode : methodsList) {
            String lines[] = methodCode.split("\\r?\\n");

            for (int i = 0; i < lines.length; i++) {
                String words[] = lines[i].split(" ");
                if(words.length > 0){
                    String firstWord = words[0];

                    // ADICIONAR FIELD APOS CADA GETFIELD E PUTFIELD
                    if (firstWord.equals("\tputfield") || firstWord.equals("\tgetfield")) {
                        words[1] = id + "/" + words[1];
                    }

                    // ADICIONAR NOME DA CLASS EM CADA INVOKEVIRTUAL
                    if (firstWord.equals("\tinvokevirtual")) {
                        if(words.length == 4){            
                            words[1] = id + "/" + words[1] + words[2];                
                            //words[1] += "/" + id + words[2];
                            words[2] = words[3];
                            words[3] = "";
                        }
                    }

                    lines[i] = concatenate_array_elements(words, ' ');
                }

            }

            this.methodsList.set(cnt, concatenate_array_elements(lines, '\n'));
            cnt++;
        }
        cnt = 0;

        //REMOVER ESPACOS ENTRE ARGUMENTOS E RETURN TYPE NOS METODOS
        for (String methodCode : methodsList) {
            String lines[] = methodCode.split("\\r?\\n");

            for (int i = 0; i < lines.length; i++) {
                String words[] = lines[i].split(" ");
                if(words.length > 0){
                    String firstWord = words[0];

                    if (firstWord.equals("\tinvokevirtual")) {
                        if(words.length == 3){                            
                            words[1] += words[2];
                            words[2] = "";
                        }
                    } else if(firstWord.equals(".method")){
                        if(words.length == 3){                            
                            words[1] += words[2];
                            words[2] = "";
                        }
                    }

                    lines[i] = concatenate_array_elements(words, ' ');
                }

            }

            this.methodsList.set(cnt, concatenate_array_elements(lines, '\n'));
            cnt++;
        }

        return null;
    }

    public Object visit(ASTExtends node, SymbolTable data) throws SemanticException {
        return null;
    }

    public Object visit(ASTVarDeclaration node, SymbolTable data) throws SemanticException {

        if (node.jjtGetParent() instanceof ASTClassDeclaration) {
            String fields = ".field " + ((SimpleNode) node.jjtGetChild(1)).jjtGetValue().toString() + " ";
            if (getJasminTypeFromString(((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString()).equals("?")) {
                fields += ((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString() + "\n";
            } else {
                fields += getJasminTypeFromString(((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString()) + "\n";
            }

            String varId = ((SimpleNode) node.jjtGetChild(1)).jjtGetValue().toString();
            String varType = getJasminTypeFromString(((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString());
            this.fieldVars.add(varId + " " + varType);

            return null;

        } else if (node.jjtGetParent() instanceof ASTWhile) {
            this.loopVars.add(((SimpleNode) node.jjtGetChild(1)).jjtGetValue().toString());
        } else if (node.jjtGetChild(0) != null && ((SimpleNode)node.jjtGetChild(0)).jjtGetValue().toString().equals("int[]")){
            this.variabelsArrays.add(((SimpleNode) node.jjtGetChild(1)).jjtGetValue().toString());
        } 
        else {
            this.variables.add(((SimpleNode) node.jjtGetChild(1)).jjtGetValue().toString());
        }

        return null;
    }

    public Object visit(ASTMainDeclaration node, SymbolTable data) throws SemanticException {

        String maincode = "\n.method public static main([Ljava/lang/String;)V\n" + "\t.limit stack 99\n"
                + "\t.limit locals " + (this.variables.size() + this.loopVars.size() + 2) + "\n\n";

        for (int i = 0; i < this.methodsCode.size(); i++) {
            maincode += "\t" + this.methodsCode.get(i) + "\n";
        }

        maincode += "\n\treturn\n.end method\n";

        methodsList.add(maincode);

        // CLEAR LISTS AND VARS FOR NEXT METHOD
        this.variables.clear();
        this.methodsCode.clear();

        this.methods = "";

        this.vars = "";

        return null;
    }

    public Object visit(ASTMethodDeclaration node, SymbolTable data) throws SemanticException {

        // PARSE CURRENT METHOD

        String header = "\n.method ";

        String identifier = ((SimpleNode) node.jjtGetChild(1)).jjtGetValue().toString();

        String argumentTypes = "(";
        List<String> typesInsert = new ArrayList<>();
        if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(2)).getId()].equals("Arguments")){
            for (int i = 0; i < ((SimpleNode) node.jjtGetChild(2)).jjtGetNumChildren();) {

                if (getJasminTypeFromString(((SimpleNode) node.jjtGetChild(2).jjtGetChild(i)).jjtGetValue().toString())
                        .equals("?")) {
                    argumentTypes += ((SimpleNode) node.jjtGetChild(2).jjtGetChild(i)).jjtGetValue().toString();
                } else {argumentTypes += getJasminTypeFromString(
                            ((SimpleNode) node.jjtGetChild(2).jjtGetChild(i)).jjtGetValue().toString());
                            typesInsert.add(getJasminTypeFromString(((SimpleNode) node.jjtGetChild(2).jjtGetChild(i)).jjtGetValue().toString()));
                }

                i = i + 2;

            }
        }
        argumentTypes += ") ";

        String type = translateType(((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString());

        // ADICIONAR INFO ONDE E NECESSARIO
        int cnt = 0;
        for (String methodCode : methodsList) {
            String lines[] = methodCode.split("\\r?\\n");

            for (int i = 0; i < lines.length; i++) {
                String words[] = lines[i].split(" ");
                String firstWord = words[0];

                // ADICIONAR NOME DA CLASS EM CADA INVOKEVIRTUAL
                if (firstWord.equals("\tinvokevirtual") && words[1].equals(identifier)) {
                    words[1] += " " + argumentTypes + type;       
                }

                lines[i] = concatenate_array_elements(words, ' ');

            }

            this.methodsList.set(cnt, concatenate_array_elements(lines, '\n'));
            cnt++;
        }




        String toCode = header + identifier + argumentTypes + type + "\n";
        toCode += "\t.limit stack 99\n\t.limit locals " + this.variables.size() + 1 + "\n\n";

        // SAVE METHOD

        this.methods += toCode;

        this.methods += classDeclaration;

        for (int i = 0; i < this.methodsCode.size(); i++) {
            this.methods += "\t" + this.methodsCode.get(i) + "\n";
        }

        this.methods += getReturn(((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString());

        this.methodsList.add(this.methods);

        // CLEAR LISTS AND VARS FOR NEXT METHOD

        this.variables.clear();
        this.methodsCode.clear();
        this.loop.clear();
        this.loopVars.clear();

        this.methods = "";

        this.vars = "";

        return null;
    }

    public Object visit(ASTType node, SymbolTable data) throws SemanticException {
        return null;
    }

    public Object visit(ASTStatement node, SymbolTable data) throws SemanticException {

        String tmp = "";
        String label = "";

        if (jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetParent()).getId()].equals("If")) {
            String lastLabel = this.ifLabels.get(this.ifLabels.size() - 1);
            if (this.ifLabels.size() == 1) {
                label = "else_0";
                tmp = "goto endif_0";
                this.ifLabels.add(label);
                this.methodsCode.add(tmp);
            } else {

                String labelArgs[] = lastLabel.split("[_]");
                int index = Integer.parseInt(labelArgs[1]);
                if (labelArgs[0].equals("else")) {
                    if (this.ifLabels.get(this.ifLabels.size() - 2).split("_")[0].equals("endif")) {
                        // FIRST STATEMENT
                        label = "else_" + index;
                        tmp = "goto endif_" + index;
                        this.ifLabels.add(label);
                        this.methodsCode.add(tmp);
                    } else {
                        // SECOND STATEMENT
                        label = "endif_" + index;
                        this.ifLabels.add(label);
                    }
                }

            }

            this.methodsCode.add(label + ":");

        }

        return null;
    }

    public Object visit(ASTIf node, SymbolTable data) throws SemanticException {
        return null;
    }

    public Object visit(ASTCondition node, SymbolTable data) throws SemanticException {
        String tmp = "";
        String label = "";

        // CHECK IF ITS WORKING WITH INT OR BOOLEAN VALUES
        if (jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetChild(0)).getId()].equals("Not")) {
            tmp = "ifne ";
        } else if (((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString().equals("&&")
                || jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetChild(0)).getId()].equals("Identifier")) {
            tmp = "ifeq ";
        } else {
            tmp = "if_icmpge ";
        }

        // ASSIGN LABEL
        if (this.ifLabels.size() == 0) {
            label = "else_0";
        } else {
            String lastLabel = this.ifLabels.get(this.ifLabels.size() - 1);
            String[] labelArgs = lastLabel.split("[_]");
            int index = Integer.parseInt(labelArgs[1]);
            int newIndex = index + 1;
            label += "else_" + newIndex;
        }

        tmp += label;
        this.ifLabels.add(label);
        this.methodsCode.add(tmp);

        return null;
    }

    public Object visit(ASTWhile node, SymbolTable data) throws SemanticException {

        String loopCode = "\n\twhile_" + numWhiles + ":\n";

        for (int i = 0; i < loop.size(); i++) {
            loopCode += "\t" + loop.get(i) + "\n";
        }

        loopCode += "\tgoto while_" + numWhiles + "\n";
        loopCode += "\twhile_" + numWhiles + "_end:\n";

        methodsCode.add(loopCode);

        this.loop.clear();

        numWhiles++;

        return null;
    }

    public Object visit(ASTExpression node, SymbolTable data) throws SemanticException {
        return null;
    }

    public Object visit(ASTEquals node, SymbolTable data) throws SemanticException {

        SimpleNode tmpNode = node;

        String tmp = "";

        boolean isLocal = false;
        boolean isLoop = false;
        boolean isArray = false;

        if (node.jjtGetChild(0).jjtGetChild(0) != null 
        && jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).getId()].equals("ArrayAccess")){

            tmp += "iastore\n\t";

            if (isDescendentOf("While", node)) {
                this.loop.add(tmp);
            } else {
                this.methodsCode.add(tmp);
            }
            return null;
        }


        // RIGHT SIDE IS AN INTEGER LITERAL
        if (jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetChild(1).jjtGetChild(0)).getId()]
                .equals("IntegerLiteral")) {
            tmp += genIntegerInstruction(((SimpleNode) node.jjtGetChild(1).jjtGetChild(0)).jjtGetValue().toString())
                    + "\n\t";

        } else if (jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetChild(1).jjtGetChild(0)).getId()]
                .equals("True")
                || jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetChild(1).jjtGetChild(0)).getId()]
                        .equals("False")) {
            // RIGHT SIDE IS BOOLEAN
            String right_value = jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetChild(1).jjtGetChild(0))
                    .getId()];
            if (right_value.equals("False")) {
                right_value = "0";
            } // TRUE = 1; FALSE = 0
            else {
                right_value = "1";
            }

            tmp += genIntegerInstruction(right_value) + "\n\t";

        } else if (jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetChild(1).jjtGetChild(0)).getId()].equals("Dot")
                || jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetChild(1).jjtGetChild(0)).getId()].equals("New")
                || jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetChild(1).jjtGetChild(0)).getId()].equals("ArrayAccess")) {
            // SKIP 
        }
        // RIGHT SIDE IS A VARIABLE
        else {
            // RIGHT SIDE IS A LOCAL VARIABLE

            for (int i = 0; i < this.variables.size(); i++) {

                if (((SimpleNode) node.jjtGetChild(1).jjtGetChild(0)).jjtGetValue().toString()
                        .equals(this.variables.get(i))) {
                    isLocal = true;
                    if (i <= 3) {
                        tmp += "iload_" + (i + 1) + "\n\t";
                    } else {
                        tmp += "iload " + (i + 1) + "\n\t";
                    }
                    break;
                }
            }
            // RIGHT SIDE IS LOOP VARIABLE
            if (!isLocal) {
                for (int i = 0; i < this.loopVars.size(); i++) {

                    if (((SimpleNode) node.jjtGetChild(1).jjtGetChild(0)).jjtGetValue().toString()
                            .equals(this.loopVars.get(i))) {
                        isLoop = true;
                        if (i <= 3) {
                            tmp += "iload_" + (i + 1 + this.variables.size()) + "\n\t";
                        } else {
                            tmp += "iload " + (i + 1 + this.variables.size()) + "\n\t";
                        }
                        break;
                    }
                }
            }

            //RIGHT SIDE IS AN ARRAY
            if(!isLoop && !isLocal){
                for (int i = 0; i < this.variabelsArrays.size(); i++) {

                    if (((SimpleNode) node.jjtGetChild(1).jjtGetChild(0)).jjtGetValue().toString()
                            .equals(this.variabelsArrays.get(i))) {
                        isArray = true;
                        if ((i + 1 + this.variables.size()) <= 3) {
                            tmp += "aload_" + (i + 1 + this.variables.size()) + "\n\t";
                        } else {
                            tmp += "aload " + (i + 1 + this.variables.size()) + "\n\t";
                        }
                        break;
                    }
                }
            }


            // RIGHT SIDE IS A FIELD VARIABLE
            if (!isLoop && !isLocal && !isArray) {
                for (int i = 0; i < this.fieldVars.size(); i++) {

                    String[] splitStr = this.fieldVars.get(i).split("\\s+");

                    if (((SimpleNode) node.jjtGetChild(1).jjtGetChild(0)).jjtGetValue().toString()
                            .equals(splitStr[0])) {
                        tmp += "getfield " + this.fieldVars.get(i) + "\n\t";
                    }
                }
            }

            
        }



        for (int i = 0; i < this.variables.size(); i++) {
            if (((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).jjtGetValue() != null
            && ((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).jjtGetValue().toString().equals(this.variables.get(i))) {

                if(jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetChild(1).jjtGetChild(0)).getId()].equals("New")
                && jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetChild(1).jjtGetChild(0).jjtGetChild(0)).getId()].equals("Identifier")){
                    if ((i + 1) <= 3) {
                        tmp += "astore_" + (i + 1);
                    } else {
                        tmp += "astore " + (i + 1);
                    }
                    if(jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).getId()].equals("Identifier")){
                        this.objectVars.add(((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).jjtGetValue().toString());
                    }
                    
                    break;
                }


                if ((i + 1) <= 3) {
                    tmp += "istore_" + (i + 1);
                } else {
                    tmp += "istore " + (i + 1);
                }
            }
        }

        for (int i = 0; i < this.loopVars.size(); i++) {
            if (((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).jjtGetValue().toString()
                    .equals(this.loopVars.get(i))) {
                if (i <= 3) {
                    tmp += "istore_" + (i + 1 + this.variables.size());
                } else {
                    tmp += "istore " + (i + 1 + this.variables.size());
                }
            }
        }

        for (int i = 0; i < this.variabelsArrays.size(); i++) {

            if (((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).jjtGetValue().toString()
                    .equals(this.variabelsArrays.get(i))) {
                if ((i + 1 + this.variables.size()) <= 3) {
                    tmp += "astore_" + (i + 1 + this.variables.size()) + "\n\t";
                } else {
                    tmp += "astore " + (i + 1 + this.variables.size()) + "\n\t";
                }
                break;
            }
        }

        for (int i = 0; i < this.fieldVars.size(); i++) {

            String[] splitStr = this.fieldVars.get(i).split("\\s+");

            if (((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).jjtGetValue() != null
            && ((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).jjtGetValue().toString().equals(splitStr[0])) {
                tmp += "putfield " + this.fieldVars.get(i);
            }
        }

        if (isDescendentOf("While", node)) {
            this.loop.add(tmp);
        } else {
            this.methodsCode.add(tmp);
        }

        return null;
    }

    public Object visit(ASTLeftEqual node, SymbolTable data) throws SemanticException {
        return null;
    }

    public Object visit(ASTRightEqual node, SymbolTable data) throws SemanticException {       

        return null;
    }

    public Object visit(ASTOperator node, SymbolTable data) throws SemanticException {

        String tmp = "";

        // OBTER TIPO DE OPERACAO
        String operation = ((SimpleNode) node).jjtGetValue().toString();


        if(!jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(0)).getId()].equals("Not")
        && !jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(0)).getId()].equals("ArrayAccess")
        && !jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(0)).getId()].equals("Dot")){

        

            //SE O PRIMEIRO OPERANDO FOR INTEGER LITERAL
            boolean isInteger = false;
            if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(0)).getId()].equals("IntegerLiteral")){
                isInteger = true;
                tmp += genIntegerInstruction(((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString()) + "\n\t";
            } else {
                // SE O PRIMEIRO OPERANDO FOR UMA LOCAL VARIABLE
                boolean isLocal = false;
                for (int i = 0; i < this.variables.size(); i++) {
                    if (((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString().equals(this.variables.get(i))) {
                        isLocal = true;
                        if (i <= 3) {
                            tmp += "iload_" + (i + 1) + "\n\t";
                        } else {
                        tmp += "iload " + (i + 1) + "\n\t";
                        }
                        break;
                    }
                }

                // SE O PRIMEIRO OPERANDO FOR UMA FIELD VARIABLE
                if (!isLocal) {
                    for (int i = 0; i < this.fieldVars.size(); i++) {

                        String[] splitStr = this.fieldVars.get(i).split("\\s+");

                        if (((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString().equals(splitStr[0])) {
                            tmp += "getfield " + this.fieldVars.get(i) + "\n\t";
                        }
                    }
                }
            } 

            if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(1)).getId()].equals("Dot")){
                tmp += this.dotInfo + "\n\t";
                this.dotInfo = "";
            }
        }

        if(!jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(1)).getId()].equals("Not")
        && !jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(1)).getId()].equals("ArrayAccess")
        && !jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(1)).getId()].equals("Dot")){

        // SE O SEGUNDO OPERANDO FOR INTEGER LITERAL
        if (jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetChild(1)).getId()].equals("IntegerLiteral")) {
            tmp += genIntegerInstruction(((SimpleNode) node.jjtGetChild(1)).jjtGetValue().toString()) + "\n\t";
        } else {
            // SE O SEGUNDO OPERANDO FOR UMA LOCAL VARIABLE
            boolean isLocal = false;
            for (int i = 0; i < this.variables.size(); i++) {
                if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().toString().equals(this.variables.get(i))) {
                    isLocal = true;
                    if (i <= 3) {
                        tmp += "iload_" + (i + 1) + "\n\t";
                    } else {
                        tmp += "iload " + (i + 1) + "\n\t";
                    }
                    break;
                }
            }

            // SE O SEGUNDO OPERANDO FOR UMA FIELD VARIABLE
            if (!isLocal) {
                for (int i = 0; i < this.fieldVars.size(); i++) {

                    String[] splitStr = this.fieldVars.get(i).split("\\s+");

                    if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().toString().equals(splitStr[0])) {
                        tmp += "getfield " + this.fieldVars.get(i) + "\n\t";
                    }
                }
            }
        }

    }
        
        switch (operation) {
            case "+":
                tmp += "iadd";
                break;
            case "-":
                tmp += "isub";
                break;
            case "*":
                tmp += "imul";
                break;
            case "/":
                tmp += "idiv";
                break;
            default:
                break;
        }

        if (((SimpleNode) node.jjtGetParent().jjtGetParent()).toString().equals("While")) {
            tmp += while_instruction((SimpleNode) node.jjtGetParent().jjtGetParent());
        }

        if (isDescendentOf("While", node)) {
            this.loop.add(tmp);
        } else {
            this.methodsCode.add(tmp);
        }

        return null;
    }

    public Object visit(ASTNot node, SymbolTable data) throws SemanticException {
        return null;
    }

    public Object visit(ASTIntegerLiteral node, SymbolTable data) throws SemanticException {
        return null;
    }

    public Object visit(ASTTrue node, SymbolTable data) throws SemanticException {
        return null;
    }

    public Object visit(ASTFalse node, SymbolTable data) throws SemanticException {
        return null;
    }

    public Object visit(ASTThis node, SymbolTable data) throws SemanticException {
        return null;
    }

    public Object visit(ASTNew node, SymbolTable data) throws SemanticException {

        String className = ((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString();

        String type = getJasminTypeFromString(className);

        if(type.equals("?")){
            this.methodsCode.add("new " + className);
            this.methodsCode.add("dup");
            this.methodsCode.add("invokespecial " + className + "/<init>()V");
        }
        else if(type.charAt(0) == '['){

            //GET ARRAY SIZE
            if(jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).getId()].equals("ArrayAccess")){
                this.methodsCode.add(genIntegerInstruction(((SimpleNode) node.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0)).jjtGetValue().toString()));
            }
            
            this.methodsCode.add("newarray int");
        }
        else{
            this.methodsCode.add("new " + type);
        }


        return null;
    }

    public Object visit(ASTLength node, SymbolTable data) throws SemanticException {
        return null;
    }

    public Object visit(ASTIdentifier node, SymbolTable data) throws SemanticException {
        return null;
    }

    public Object visit(ASTVoid node, SymbolTable data) throws SemanticException {
        return null;
    }

    private String getJasminType(SimpleNode node) {
        String type = ((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString();
        switch (type) {
            case "void":
                return "V";
            case "int":
                return "I";
            case "int[]":
                return "[I";
            case "boolean":
                return "Z";
            case "String":
                return "Ljava/lang/String;";
            case "String[]":
                return "[Ljava/lang/String;";
            default:
                break;
        }

        return ((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString();
    }

    private String getJasminTypeFromString(String type) {
        switch (type) {
            case "void":
                return "V";
            case "int":
                return "I";
            case "int[]":
                return "[I";
            case "boolean":
                return "Z";
            case "String":
                return "Ljava/lang/String;";
            case "String[]":
                return "[Ljava/lang/String;";
            default:
                break;
        }

        return "?";
    }

    private void addEndMethods() {

        System.out.println("\n============ CODE GENERATION ============\n");

        // return e .end method do ultimo method

        this.code += classDeclaration;

        for (int i = 0; i < this.fieldVars.size(); i++) {
            this.code += ".field " + this.fieldVars.get(i) + "\n";
        }

        this.code += "\n";

        for (int i = 0; i < methodsList.size(); i++) {
            this.code += methodsList.get(i);
        }

        System.out.println(this.code);

        System.out.println("============ END CODE GENERATION ============\n");

    }

    public void saveToFile() {
        addEndMethods();
        try {
            File jasmin = new File(".\\jasmin.j");
            jasmin.createNewFile();
            FileOutputStream fos = new FileOutputStream(jasmin);
            fos.write(this.code.getBytes());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String translateType(String type) {
        String ret = getJasminTypeFromString(type);

        if (type.equals("?")) {
            return type;
        }

        return ret;
    }

    private String getReturn(String type) {
        switch (type) {
            case "int":
                return "\n\tireturn\n.end method\n";
            default:
                return "\n\treturn\n.end method\n";
        }
    }

    public Object visit(ASTArrayAccess node, SymbolTable data) throws SemanticException {

        String tmp = "";

        //CASO SEJA UM NEW ARRAY, DEVE IGNORAR PORQUE O CASO JA ESTA TRATADO
        if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetParent().jjtGetParent()).getId()].equals("New")){
            return null;
        }

        boolean isLocal = false;

        for (int i = 0; i < this.variabelsArrays.size(); i++) {

            if(((SimpleNode)node.jjtGetChild(0)).jjtGetValue().toString().equals(this.variabelsArrays.get(i))) {
                isLocal = true;

                if ((i + 1 + this.variables.size()) <= 3) {
                    tmp += "aload_" + (i + 1 + this.variables.size()) + "\n\t";
                } else {
                    tmp += "aload " + (i + 1 + this.variables.size()) + "\n\t";
                }

                break;
            }
            
        }

        if(!isLocal){
            for (int i = 0; i < this.fieldVars.size(); i++) {

                String[] splitStr = this.fieldVars.get(i).split("\\s+");

                if (((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString().equals(splitStr[0])) {
                    tmp += "getfield " + this.fieldVars.get(i) + "\n\t";
                }
            }
        }

        if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(1)).getId()].equals("IntegerLiteral")){
            tmp += genIntegerInstruction(((SimpleNode)node.jjtGetChild(1)).jjtGetValue().toString()) + "\n\t";
        } else if (jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(1)).getId()].equals("Identifier")){
            isLocal = false;
            boolean isLoop = false;

            //ACCESS VIA LOCAL VARIABLE
            for (int i = 0; i < this.variables.size(); i++) {

                if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().toString()
                        .equals(this.variables.get(i))) {
                    isLocal = true;
                    if ((i + 1) <= 3) {
                        tmp += "iload_" + (i + 1) + "\n\t";
                    } else {
                        tmp += "iload " + (i + 1) + "\n\t";
                    }
                    break;
                }
            }
            //ACCESS VIA LOOP VARIABLE
            if (!isLocal) {
                for (int i = 0; i < this.loopVars.size(); i++) {

                    if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().toString()
                            .equals(this.loopVars.get(i))) {
                        isLoop = true;
                        if ((i + 1 + this.variables.size()) <= 3) {
                            tmp += "iload_" + (i + 1 + this.variables.size()) + "\n\t";
                        } else {
                            tmp += "iload " + (i + 1 + this.variables.size()) + "\n\t";
                        }
                        break;
                    }
                }
            }

            //ACCESS VIA FIELD VARIABLE
            if (!isLoop && !isLocal) {
                for (int i = 0; i < this.fieldVars.size(); i++) {

                    String[] splitStr = this.fieldVars.get(i).split("\\s+");

                    if (((SimpleNode) node.jjtGetChild(1)).jjtGetValue().toString()
                            .equals(splitStr[0])) {
                        tmp += "getfield " + this.fieldVars.get(i) + "\n\t";
                    }
                }
            }
        }

        if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetParent()).getId()].equals("LeftEqual")){
            if (isDescendentOf("While", node)) {
                this.loop.add(tmp);
            } else {
                this.methodsCode.add(tmp);
            }
            return null;
        }

        tmp += "iaload\n\t";

        if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetParent()).getId()].equals("RightEqual")
        && jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetParent().jjtGetParent().jjtGetChild(0).jjtGetChild(0)).getId()].equals("ArrayAccess")){
            this.arrayInfo = tmp + "\n" + this.arrayInfo;
            return null;
        }

        if (isDescendentOf("While", node)) {
            this.loop.add(tmp);
        } else {
            this.methodsCode.add(tmp);
        }

        return null;
    }

    private String concatenate_array_elements(String[] array, char token) {
        String ret = "";

        for (int i = 0; i < array.length; i++) {
            ret = ret + array[i] + token;
        }

        return ret;
    }

    private String while_instruction(Node node) {

        String op = ((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).jjtGetValue().toString();

        switch (op) {
            case "<":
                op = "if_icmpge while_" + numWhiles + "_end";
                break;
            case ">":
                op = "if_icmple while_" + numWhiles + "_end";
                break;
            case "<=":
                op = "if_icmpgt while_" + numWhiles + "_end";
                break;
            case ">=":
                op = "if_icmplt while_" + numWhiles + "_end";
                break;
            case "=":
                op = "if_icmpne while_" + numWhiles + "_end";
                break;
            case "!=":
                op = "if_icmpeq while_" + numWhiles + "_end";
                break;
        }

        return op;
    }

    public boolean isDescendentOf(String target, SimpleNode node) {

        while (!(node.toString().equals("Start_parser"))) {

            if (((SimpleNode) node.jjtGetParent()).toString().equals(target)) {
                return true;
            } else {
                node = (SimpleNode) node.jjtGetParent();
            }
        }
        return false;
    }
}