import java.util.*;


public class SemanticAnalyzer implements jmmVisitor {

    public Object visit(ASTArguments node, SymbolTable data)throws SemanticException{
        return null;
    }

    public Object visit(ASTClassDeclaration node, SymbolTable data) throws SemanticException{
        String id = ((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString();
        STEntry entry = data.lookup(id);
        if(entry != null){
            int line = ((SimpleNode) node.jjtGetChild(0)).getLine();
            throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Class name " + id + " already in use (l." + entry.getLine() + " c." + entry.getColumn() + ").");
        }
        String attributes = "";

        //caso a class faça extends
        if(node.jjtGetNumChildren() > 1){
            if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(1)).getId()].equals("Extends")) {
                String extendId = ((SimpleNode) node.jjtGetChild(1).jjtGetChild(0)).jjtGetValue().toString();
                attributes = "extends " + extendId;
                CCEntry classEntry = new CCEntry(id, extendId, data);
                ClassChecker.add(classEntry);
            }
        }

        STClass newEntry = new STClass(id, attributes, ((SimpleNode) node.jjtGetChild(0)).getLine(), ((SimpleNode) node.jjtGetChild(0)).getColumn(), data);
        data.getSuperTable().insert(id, newEntry);
        return null;
    }

    public Object visit(ASTCondition node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTDot node, SymbolTable data) throws SemanticException{

        SimpleNode parentNode = (SimpleNode) node.jjtGetParent();
        if(parentNode instanceof ASTImport ||
            parentNode instanceof ASTEquals ||
            parentNode instanceof ASTClassDeclaration ||
            parentNode instanceof ASTLeftEqual ||
            parentNode instanceof ASTRightEqual)
            {
            return null;
        }

        int line = node.getLine();
        int column = node.getColumn();
        boolean isThis = false;

        if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(0)).getId()].equals("This")){
            isThis = true;
        }

        if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(1)).getId()].equals("Length")){
            if(isThis){
                throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Class does not have property 'length'.");
            }
            
            String leftId = ((SimpleNode)node.jjtGetChild(0)).jjtGetValue().toString();
            
            STEntry leftEntry = data.lookup(leftId);

            if(leftEntry == null){
                throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Variable " + leftId + " was not found.");
            }
        
            
        
        }
        else{
            String id = "";

            try{
                if(isThis){
                    id = "this." + ((SimpleNode)node.jjtGetChild(1)).jjtGetValue().toString();
                }
                else{
                    id = ((SimpleNode)node.jjtGetChild(0)).jjtGetValue().toString() + "." + ((SimpleNode)node.jjtGetChild(1)).jjtGetValue().toString();
                }
            }
            catch(NullPointerException e){
                id = jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(0)).getId()] + "." + jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(1)).getId()];
            }

            String[] splitId = id.split("\\.");

            STEntry leftIdEntry = data.lookup(splitId[0]);
            //a variavel representa uma class
            if(leftIdEntry != null){
                String className = leftIdEntry.getType();
                STEntry classEntry = data.lookup(className);

                if(classEntry == null){
                    String funcName = splitId[1];
                    List<String> argTypes = new ArrayList<>();
                    
                    if(((SimpleNode)node.jjtGetChild(2)).jjtGetNumChildren() == 0){
                        argTypes.add("void");
                    }
                    else{
                        for(int i = 0; i < ((SimpleNode)node.jjtGetChild(2)).jjtGetNumChildren(); i++){
                            if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(2).jjtGetChild(i)).getId()].equals("void")){
                                argTypes.add("void");
                            }
                            else if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(2).jjtGetChild(i)).getId()].equals("ArrayAccess")){
                                argTypes.add("int");
                            }
                            else{
                                String arg = ((SimpleNode)node.jjtGetChild(2).jjtGetChild(i)).jjtGetValue().toString();
                                STEntry argEntry = data.lookup(arg);
                                if(argEntry == null){
                                    throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Argument " + arg + " was not declared.");
                                }
                                argTypes.add(argEntry.getType());
                            }
                        }
                    }
                    FCFunc funcEntry = new FCFunc(funcName, argTypes, line, column, data);
                    FunctionChecker.add(funcEntry);
                    return null;
                }

                SymbolTable classTable = ((STClass) classEntry).getSymbolTable();
                STEntry rightIdEntry = classTable.lookup(splitId[1]);

                if(rightIdEntry == null){
                    String funcName = splitId[1];
                    List<String> argTypes = new ArrayList<>();
                    
                    if(((SimpleNode)node.jjtGetChild(2)).jjtGetNumChildren() == 0){
                        argTypes.add("void");
                    }
                    else{
                        for(int i = 0; i < ((SimpleNode)node.jjtGetChild(2)).jjtGetNumChildren(); i++){
                            if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(2).jjtGetChild(i)).getId()].equals("void")){
                                argTypes.add("void");
                            }
                            else if (jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(2).jjtGetChild(i)).getId()].equals("IntegerLiteral")){
                                argTypes.add("int");
                            }                            
                            else if (jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(2).jjtGetChild(i)).getId()].equals("Operator")){
                                String operator = ((SimpleNode)node.jjtGetChild(2).jjtGetChild(i)).jjtGetValue().toString();
                                switch (operator) {
                                    case "+":
                                    case "/":
                                    case "*":
                                    case "-":
                                        argTypes.add("int");                                        
                                        break;
                                    case "&&":
                                    case "<":
                                        argTypes.add("boolean");
                                        break;
                                    default:
                                        break;
                                }
                            }
                            else{
                                String arg = ((SimpleNode)node.jjtGetChild(2).jjtGetChild(i)).jjtGetValue().toString();
                                STEntry argEntry = data.lookup(arg);
                                if(argEntry == null){
                                    throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Argument " + arg + " was not declared.");
                                }
                                argTypes.add(argEntry.getType());
                            }
                        }
                    }
                    FCFunc funcEntry = new FCFunc(funcName, argTypes, line, column, classTable);
                    FunctionChecker.add(funcEntry);
                    return null;
                }
            }
            else{
                STEntry funcEntry = data.lookup(id);
                if(funcEntry == null){
                    String funcName = id;
                    List<String> argTypes = new ArrayList<>();

                    if(((SimpleNode)node.jjtGetChild(2)).jjtGetNumChildren() == 0){
                        argTypes.add("void");
                    }
                    else{
                        for(int i = 0; i < ((SimpleNode)node.jjtGetChild(2)).jjtGetNumChildren(); i++){
                            if (jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetChild(2).jjtGetChild(i)).getId()].equals("ArrayAccess")) {
                                String arg = ((SimpleNode)node.jjtGetChild(2).jjtGetChild(i).jjtGetChild(0)).jjtGetValue().toString();
                                STEntry argEntry = data.lookup(arg);
                                if(argEntry == null){
                                    throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Argument " + arg + " was not declared.");
                                }
                                if(argEntry.getType().equals("int[]")){
                                    SimpleNode arrAccessNode = (SimpleNode) node.jjtGetChild(2).jjtGetChild(i).jjtGetChild(1);

                                    while(jmmTreeConstants.jjtNodeName[arrAccessNode.getId()].equals("ArrayAccess")){
                                        String arrayArg = ((SimpleNode)arrAccessNode.jjtGetChild(0)).jjtGetValue().toString();
                                        STEntry arrayArgEntry = data.lookup(arrayArg);
                                        if(arrayArgEntry == null){
                                            throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Argument " + arg + " was not declared.");
                                        }
                                        if(arrayArgEntry.getType().equals("int[]")){
                                            arrAccessNode = (SimpleNode)arrAccessNode.jjtGetChild(1);
                                        }
                                        else{
                                            throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Argument " + arg + " is not an array to access.");
                                        }
                                    }

                                    if(jmmTreeConstants.jjtNodeName[arrAccessNode.getId()].equals("IntegerLiteral")){
                                        argTypes.add("int");
                                    }
                                    else if (jmmTreeConstants.jjtNodeName[arrAccessNode.getId()].equals("Operator")){
                                        String operator = arrAccessNode.jjtGetValue().toString();
                                        switch (operator) {
                                            case "+":
                                            case "/":
                                            case "*":
                                            case "-":
                                                argTypes.add("int");
                                                break;
                                            case "&&":
                                            case "<":
                                                throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Array access must be done with an int.");
                                            default:
                                                break;
                                        }
                                    }
                                    else{
                                        arg = arrAccessNode.jjtGetValue().toString();
                                        STEntry arrayAccessEntry = data.lookup(arg);
                                        if(arrayAccessEntry == null){
                                            throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Argument " + arg + " was not declared.");
                                        }
                                        if(!arrayAccessEntry.getType().equals("int")){
                                            throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Array access must be done with an int.");
                                        }
                                        argTypes.add("int");
                                    }
                                }
                                else{
                                    throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Argument " + arg + " is not an array to access.");
                                }
                            }
                            else if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(2).jjtGetChild(i)).getId()].equals("void")){
                                argTypes.add("void");
                            }
                            else if (jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(2).jjtGetChild(i)).getId()].equals("IntegerLiteral")){
                                argTypes.add("int");
                            }
                            else if (jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(2).jjtGetChild(i)).getId()].equals("Operator")){
                                String operator = ((SimpleNode)node.jjtGetChild(2).jjtGetChild(i)).jjtGetValue().toString();
                                switch (operator) {
                                    case "+":
                                    case "/":
                                    case "*":
                                    case "-":
                                        argTypes.add("int");
                                        break;
                                    case "&&":
                                    case "<":
                                        argTypes.add("boolean");
                                        break;
                                    default:
                                        break;
                                }
                            }
                            else{
                                String arg = ((SimpleNode)node.jjtGetChild(2).jjtGetChild(i)).jjtGetValue().toString();
                                STEntry argEntry = data.lookup(arg);
                                if(argEntry == null){
                                    throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Argument " + arg + " was not declared.");
                                }
                                argTypes.add(argEntry.getType());
                            }
                        }
                    }
                    FCFunc funcEntry2 = new FCFunc(funcName, argTypes, line, column, data);
                    FunctionChecker.add(funcEntry2);
                    return null;
                }
            }
        }
        return null;
    }

    public Object visit(ASTEquals node, SymbolTable data) throws SemanticException{
        boolean isArrayElement = false;
        int line = ((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).getLine();
        int column = ((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).getColumn();
        if(line == 0){
            line = ((SimpleNode) node.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0)).getLine();
            column = ((SimpleNode) node.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0)).getColumn();
        }
        String leftId;
        String leftType = "";

        //é elemento de array
        if(jmmTreeConstants.jjtNodeName[((SimpleNode)node.jjtGetChild(0).jjtGetChild(0)).getId()].equals("ArrayAccess")){
            isArrayElement = true;
            leftId = ((SimpleNode) node.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0)).jjtGetValue().toString();
            leftType = "int";
            try {
                int leftPosition = Integer.parseInt(((SimpleNode) node.jjtGetChild(0).jjtGetChild(0).jjtGetChild(1)).jjtGetValue().toString());
            }
            catch (java.lang.NumberFormatException e){
                String intId = ((SimpleNode) node.jjtGetChild(0).jjtGetChild(0).jjtGetChild(1)).jjtGetValue().toString();
                STEntry intEntry = data.lookup(intId);

                if(intEntry == null || !intEntry.getType().equals("int")){
                    throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n " + intId + " must be an integer to access array " + leftId + ".");
                }
            }
        }
        else { //nao é elemento de array
            leftId = ((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).jjtGetValue().toString();
        }

        STEntry entry = data.lookup(leftId);

        //checking if it was declared
        if(entry == null){
            throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n variable "+ leftId +" not declared before this line.");
        }

        if(!isArrayElement){
            leftType = entry.getType();
        }

        String rightId = "";
        String rightType = "";
        SimpleNode rightNode = (SimpleNode)node.jjtGetChild(1).jjtGetChild(0);
        int rightNodeId = rightNode.getId();

        //caso seja array
        if(leftType.equals("int[]")){
            if(!isArrayElement){
                if(jmmTreeConstants.jjtNodeName[rightNodeId].equals("New")) {
                    rightType = ((SimpleNode)rightNode.jjtGetChild(0)).jjtGetValue().toString();

                    //checks if size of new array is an int                
                    try {
                        int rightSize = Integer.parseInt(((SimpleNode) rightNode.jjtGetChild(0).jjtGetChild(0)).jjtGetValue().toString());
                    }catch (java.lang.NumberFormatException e){
                        String s = ((SimpleNode) rightNode.jjtGetChild(0).jjtGetChild(0)).jjtGetValue().toString();
                        throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n " + s + " must be an integer to define array size " + rightId + ".");
                    }
                    catch (java.lang.NullPointerException e){
                        //caso nao seja inteiro
                        if(jmmTreeConstants.jjtNodeName[((SimpleNode)rightNode.jjtGetChild(0).jjtGetChild(0)).getId()].equals("Operator")){
                            String operator = ((SimpleNode)rightNode.jjtGetChild(1)).jjtGetValue().toString();
                            switch(operator){
                                case "&&":
                                case "<":
                                    throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Operator " + operator + " must return an integer to define array size " + rightId + ".");
                            }
                        }
                        else if(jmmTreeConstants.jjtNodeName[((SimpleNode)rightNode.jjtGetChild(0).jjtGetChild(0)).getId()].equals("True") ||
                                jmmTreeConstants.jjtNodeName[((SimpleNode)rightNode.jjtGetChild(0).jjtGetChild(0)).getId()].equals("False") ||
                                jmmTreeConstants.jjtNodeName[((SimpleNode)rightNode.jjtGetChild(0).jjtGetChild(0)).getId()].equals("Not") ||
                                jmmTreeConstants.jjtNodeName[((SimpleNode)rightNode.jjtGetChild(0).jjtGetChild(0)).getId()].equals("This")){
                                    throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Array size declaration must use an int.");
                                }
                        else if(jmmTreeConstants.jjtNodeName[((SimpleNode)rightNode.jjtGetChild(0).jjtGetChild(0)).getId()].equals("Dot")){
                            if(!jmmTreeConstants.jjtNodeName[((SimpleNode)rightNode.jjtGetChild(0).jjtGetChild(0).jjtGetChild(1)).getId()].equals("Length")){
                                if(jmmTreeConstants.jjtNodeName[((SimpleNode)rightNode.jjtGetChild(0)).getId()].equals("This")){
                                    rightId = "this." + ((SimpleNode)rightNode.jjtGetChild(0).jjtGetChild(0)).jjtGetValue().toString();
                                }
                                else{
                                    rightId = ((SimpleNode)rightNode.jjtGetChild(0).jjtGetChild(0)).jjtGetValue().toString();

                                }
                                STEntry rightEntry = data.lookup(rightId);

                                if(rightEntry == null){
                                    String returnType = leftType;
                                    String funcName = rightId;
                                    List<String> argTypes = new ArrayList<>();
                                    if(((SimpleNode)rightNode.jjtGetChild(2)).jjtGetNumChildren() == 0){
                                        argTypes.add("void");
                                    }
                                    else {
                                        for (int i = 0; i < ((SimpleNode) rightNode.jjtGetChild(2)).jjtGetNumChildren(); i++) {
                                            String arg = ((SimpleNode) rightNode.jjtGetChild(2).jjtGetChild(i)).jjtGetValue().toString();
                                            STEntry argEntry = data.lookup(arg);
                                            if (argEntry == null) {
                                                throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n" + arg + " not declared.");
                                            }
                                            argTypes.add(argEntry.getType());
                                        }
                                    }
                                    FCEntry newEntry = new FCEntry(funcName, argTypes, returnType, line, column, data);
                                    FunctionChecker.add(newEntry);
                                    return null;
                                }
                                rightType = rightEntry.getType();
                            }
                        }
                    }
                    ((STVarDeclaration)entry).setInitialized(true);
                }
                else if(jmmTreeConstants.jjtNodeName[rightNodeId].equals("Dot")){ //aceder a função da classe principal
                    //System.out.println(jmmTreeConstants.jjtNodeName[((SimpleNode)rightNode.jjtGetChild(0)).getId()]);
                    if(jmmTreeConstants.jjtNodeName[((SimpleNode)rightNode.jjtGetChild(0)).getId()].equals("This")){
                        rightId = "this." + ((SimpleNode)rightNode.jjtGetChild(1)).jjtGetValue().toString();
                    }
                    else{
                        rightId = ((SimpleNode)rightNode.jjtGetChild(0)).jjtGetValue().toString() + "." +((SimpleNode)rightNode.jjtGetChild(1)).jjtGetValue().toString();

                    }
                    STEntry rightEntry = data.lookup(rightId);

                    if(rightEntry == null){
                        String returnType = leftType;
                        String funcName = rightId;
                        List<String> argTypes = new ArrayList<>();
                        if(((SimpleNode)rightNode.jjtGetChild(2)).jjtGetNumChildren() == 0){
                            argTypes.add("void");
                        }
                        else {
                            for (int i = 0; i < ((SimpleNode) rightNode.jjtGetChild(2)).jjtGetNumChildren(); i++) {
                                String arg = ((SimpleNode) rightNode.jjtGetChild(2).jjtGetChild(i)).jjtGetValue().toString();
                                STEntry argEntry = data.lookup(arg);
                                if (argEntry == null) {
                                    throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n" + arg + " not declared.");
                                }
                                argTypes.add(argEntry.getType());
                            }
                        }
                        FCEntry newEntry = new FCEntry(funcName, argTypes, returnType, line, column, data);
                        FunctionChecker.add(newEntry);
                        return null;
                    }
                    rightType = rightEntry.getType();
                }
                else{
                    if(((SimpleNode)rightNode).jjtGetNumChildren()>0){
                        rightType = ((SimpleNode)rightNode.jjtGetChild(0)).jjtGetValue().toString();
                    }
                    else{
                        rightId = rightNode.jjtGetValue().toString();
                        STEntry rightEntry = data.lookup(rightId);

                        if(rightEntry == null){
                            throw new SemanticException("SEMANTIC ERROR on line " + line + ".\nVariable " + rightId + " not declared.");
                        }
                        rightType = rightEntry.getType();
                    }
                }
            }
            else{
                leftType = "int";
            }
        }

        //caso seja array do lado direito
        if(jmmTreeConstants.jjtNodeName[rightNodeId].equals("ArrayAccess")){
            rightId = ((SimpleNode)node.jjtGetChild(1).jjtGetChild(0).jjtGetChild(0)).jjtGetValue().toString();
            STEntry rightEntry = data.lookup(rightId);

            if(rightEntry == null){
                throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n variable "+ rightId +" not declared before this line.");
            }

            String arrayAccess = ((SimpleNode)node.jjtGetChild(1).jjtGetChild(0).jjtGetChild(1)).jjtGetValue().toString();
            SimpleNode arrayAccessNode = (SimpleNode)node.jjtGetChild(1).jjtGetChild(0).jjtGetChild(1);
            if(jmmTreeConstants.jjtNodeName[arrayAccessNode.getId()].equals("Operator")){
                String operator = arrayAccessNode.jjtGetValue().toString();
                switch(operator){
                    case "&&":
                    case "<":
                        throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Must access array with an int.");
                    default:
                        if(rightEntry.getType().equals("int[]")){
                            rightType = "int";
                        }
                        break;
                }
            }
            else if(jmmTreeConstants.jjtNodeName[arrayAccessNode.getId()].equals("True") ||
                    jmmTreeConstants.jjtNodeName[arrayAccessNode.getId()].equals("False") ||
                    jmmTreeConstants.jjtNodeName[arrayAccessNode.getId()].equals("Not") ||
                    jmmTreeConstants.jjtNodeName[arrayAccessNode.getId()].equals("This") ||
                    jmmTreeConstants.jjtNodeName[arrayAccessNode.getId()].equals("New")){
                        throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Must access array with an int.");
            }
            else if(jmmTreeConstants.jjtNodeName[arrayAccessNode.getId()].equals("IntegerLiteral")){
                if(rightEntry.getType().equals("int[]")){
                    rightType = "int";
                }
            }
            else if(jmmTreeConstants.jjtNodeName[arrayAccessNode.getId()].equals("Identifier")){
                String arrayAccessId = arrayAccessNode.jjtGetValue().toString();
                STEntry arrayAccessEntry = data.lookup(arrayAccessId);
                
                if(arrayAccessEntry == null){
                    throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Variable " + arrayAccessId + " not found.");
                }
            
                if(!arrayAccessEntry.getType().equals("int")){
                    throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Must access array with an int.");
                }

                if(rightEntry.getType().equals("int[]")){
                    rightType = "int";
                }
        
            }
            //MEGA DOT =====================================================================================================
            else if(jmmTreeConstants.jjtNodeName[arrayAccessNode.getId()].equals("Dot")){        
                //new + function call
                if(jmmTreeConstants.jjtNodeName[((SimpleNode)arrayAccessNode.jjtGetChild(0)).getId()].equals("New")){
                    throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Must access array with an int.");  
                }              
                //length
                else if(jmmTreeConstants.jjtNodeName[((SimpleNode)arrayAccessNode.jjtGetChild(1)).getId()].equals("Length")){
                    if(rightEntry.getType().equals("int[]")){
                        rightType = "int";
                    }
                }
                else{//function call
                    try {
                        if(jmmTreeConstants.jjtNodeName[((SimpleNode)arrayAccessNode.jjtGetChild(0)).getId()].equals("This")){
                            rightId = "this." + ((SimpleNode)arrayAccessNode.jjtGetChild(1)).jjtGetValue().toString();
                        }
                        else{
                            rightId = ((SimpleNode) arrayAccessNode.jjtGetChild(0)).jjtGetValue().toString() + "." + ((SimpleNode) arrayAccessNode.jjtGetChild(1)).jjtGetValue().toString();
                        }
                    }
                    catch (java.lang.NullPointerException e){
                        rightId = jmmTreeConstants.jjtNodeName[((SimpleNode) arrayAccessNode.jjtGetChild(0)).getId()] + "." + ((SimpleNode) arrayAccessNode.jjtGetChild(1)).jjtGetValue().toString();
                    }
    
                    String[] splitRightId = rightId.split("\\.");               //Ex. Lazysort q; (...) d = q.printL(L)
                    STEntry leftOfDotEntry = data.lookup(splitRightId[0]);      // data.lookup("q");
    
                    if(leftOfDotEntry != null){         //"q" é uma variavel que representa uma classe, neste caso Lazysort
                        String className = leftOfDotEntry.getType();
                        STEntry classEntry = data.lookup(className);
    
                        if(classEntry == null){
                            String returnType = "int";
                            String funcName = splitRightId[1];
                            List<String> argTypes = new ArrayList<>();
                            if((arrayAccessNode.jjtGetChild(2)).jjtGetNumChildren() == 0){
                                System.out.println(((SimpleNode)arrayAccessNode.jjtGetChild(1)).jjtGetValue().toString());
                                argTypes.add("void");
                            }
                            else {
                                for (int i = 0; i < ((SimpleNode) arrayAccessNode.jjtGetChild(2)).jjtGetNumChildren(); i++) {
                                    if(jmmTreeConstants.jjtNodeName[((SimpleNode) arrayAccessNode.jjtGetChild(2).jjtGetChild(i)).getId()].equals("void")){
                                        argTypes.add("void");
                                    }
                                    else {
                                        String arg = ((SimpleNode) arrayAccessNode.jjtGetChild(2).jjtGetChild(i)).jjtGetValue().toString();
                                        STEntry argEntry = data.lookup(arg);
                                        if (argEntry == null) {
                                            throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n" + arg + " not declared.");
                                        }
                                        argTypes.add(argEntry.getType());
                                    }
                                }
                            }
                            FCEntry newEntry = new FCEntry(funcName, argTypes, returnType, line, column, data);
                            FunctionChecker.add(newEntry);
                            return null;
                        }
                        SymbolTable classTable = ((STClass)classEntry).getSymbolTable();
                        STEntry rightOfDotEntry = classTable.lookup(splitRightId[1]);
                        if(rightOfDotEntry == null){
                            String returnType = "int";
                            String funcName = splitRightId[1];
                            List<String> argTypes = new ArrayList<>();
                            if(((SimpleNode)arrayAccessNode.jjtGetChild(1)).jjtGetNumChildren() == 0){
                                argTypes.add("void");
                            }
                            else {
                                for (int i = 0; i < ((SimpleNode) arrayAccessNode.jjtGetChild(1)).jjtGetNumChildren(); i++) {
                                    String arg = ((SimpleNode) arrayAccessNode.jjtGetChild(1).jjtGetChild(i)).jjtGetValue().toString();
                                    STEntry argEntry = data.lookup(arg);
                                    if (argEntry == null) {
                                        throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n" + arg + " not declared.");
                                    }
                                    argTypes.add(argEntry.getType());
                                }
                            }
                            FCEntry newEntry = new FCEntry(funcName, argTypes, returnType, line, column, data);
                            FunctionChecker.add(newEntry);
                            return null;
                        }
    
                        if(!rightOfDotEntry.getType().equals("int")){
                            throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Must access array with an int.");
                        }
                        if(rightEntry.getType().equals("int[]")){ ///////////////////////////////////////////////////////////////////////////////talvez seja mudado
                            rightType = "int";
                        }
                    }
                    else {                              //"q" nao é uma variável mas sim a propria classe
                        STEntry rightArrayEntry = data.lookup(rightId);
                        if (rightArrayEntry == null) {
                            //se a função ainda nao existir adicionar ao FunctionChecker
                            String returnType = "int";
                            String funcName = rightId;
    
                            List<String> argTypes = new ArrayList<>();
                            if(((SimpleNode)arrayAccessNode.jjtGetChild(2)).jjtGetNumChildren() == 0){
                                argTypes.add("void");
                            }
                            else {
                                for (int i = 0; i < ((SimpleNode) arrayAccessNode.jjtGetChild(2)).jjtGetNumChildren(); i++) {
                                    if (jmmTreeConstants.jjtNodeName[((SimpleNode) arrayAccessNode.jjtGetChild(2).jjtGetChild(i)).getId()].equals("IntegerLiteral")) {
                                        argTypes.add("int");
                                        continue;
                                    }
                                    SimpleNode argNode = (SimpleNode) arrayAccessNode.jjtGetChild(2).jjtGetChild(i);
                                    if (jmmTreeConstants.jjtNodeName[argNode.getId()].equals("Operator")) {
                                        String operator = argNode.jjtGetValue().toString();
                                        switch (operator) {
                                            case "/":
                                            case "*":
                                            case "+":
                                            case "-":
                                                argTypes.add("int");
                                                break;
                                            case "&&":
                                            case "<":
                                                argTypes.add("boolean");
                                                break;
                                        }
                                    } else {
                                        String arg = argNode.jjtGetValue().toString();
    
                                        STEntry argEntry = data.lookup(arg);
                                        if (argEntry == null) {
                                            throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n" + arg + " not declared.");
                                        }
                                        argTypes.add(argEntry.getType());
                                    }
                                }
                            }
                            FCEntry newEntry = new FCEntry(funcName, argTypes, returnType, line, column, data);
                            FunctionChecker.add(newEntry);
                            return null;
                        }
                        if(rightEntry.getType().equals("int[]")){  ///////////////////////////////////////////////////////////////////////////////talvez seja mudado
                            rightType = "int";
                        }
                    }
                }
            }
        }//MEGA DOT - END =================================================================================================

        //caso segundo filho -> operator
        if(jmmTreeConstants.jjtNodeName[rightNodeId].equals("Operator")){
            String operator = ((SimpleNode)node.jjtGetChild(1).jjtGetChild(0)).jjtGetValue().toString();
            switch(operator){
                case "/":
                case "*":
                case "+":
                case "-":
                    rightType = "int";
                    //rightId = operator + "(int)";
                    break;
                case "&&":
                case "<":
                    rightType = "boolean";
                    //rightId = operator + "(boolean)";
                    break;
            }
        }

        //caso seja bollean do lado direito do igual
        if(jmmTreeConstants.jjtNodeName[rightNodeId].equals("True") ||
         jmmTreeConstants.jjtNodeName[rightNodeId].equals("False") ||
         jmmTreeConstants.jjtNodeName[rightNodeId].equals("Not")){
            rightType = "boolean";
        }

        //caso seja this do lado direito do igual
        if(jmmTreeConstants.jjtNodeName[rightNodeId].equals("This")){
            SimpleNode classNode = data.getNode();
            while(!jmmTreeConstants.jjtNodeName[classNode.getId()].equals("ClassDeclaration")){
                classNode = data.getSuperTable().getNode();
            }
            rightType = ((SimpleNode) classNode.jjtGetChild(0)).jjtGetValue().toString();
        }

        //caso seja integerLiteral do lado direito do igual
        if(jmmTreeConstants.jjtNodeName[rightNodeId].equals("IntegerLiteral")){
            rightType = "int";
        }

        //caso seja dot do lado direito do igual
        if(jmmTreeConstants.jjtNodeName[rightNodeId].equals("Dot")){        
            //new + function call
            if(jmmTreeConstants.jjtNodeName[((SimpleNode)rightNode.jjtGetChild(0)).getId()].equals("New")){

                //criar FCEntry com os dados da funcção e no fim testar (no main) se existem todas as funções que
                //forem adicionadas ao FunctionChecker
                String className = ((SimpleNode)rightNode.jjtGetChild(0).jjtGetChild(0)).jjtGetValue().toString();
                STEntry classEntry = data.lookup(className);
                if(classEntry == null){
                    //criar entrada no FunctionChecker
                    //FCEntry(String name, List<String> argTypes, String returnType, int line, int column, SymbolTable symbolTable)
                    String returnType = leftType;
                    String funcName;
                    if(jmmTreeConstants.jjtNodeName[((SimpleNode)rightNode.jjtGetChild(0)).getId()].equals("This")){
                        funcName = "this." + ((SimpleNode)rightNode.jjtGetChild(1)).jjtGetValue().toString();
                    }
                    else{
                        funcName = ((SimpleNode)rightNode.jjtGetChild(1)).jjtGetValue().toString();
                    }

                    //String funcName = ((SimpleNode)rightNode.jjtGetChild(1)).jjtGetValue().toString();
                    List<String> argTypes = new ArrayList<>();
                    if(((SimpleNode)rightNode.jjtGetChild(2)).jjtGetNumChildren() == 0){
                        argTypes.add("void");
                    }
                    else {
                        for (int i = 0; i < ((SimpleNode) rightNode.jjtGetChild(2)).jjtGetNumChildren(); i++) {
                            String arg = ((SimpleNode) rightNode.jjtGetChild(2).jjtGetChild(i)).jjtGetValue().toString();
                            STEntry argEntry = data.lookup(arg);
                            if (argEntry == null) {
                                throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n" + arg + " not declared.");
                            }
                            argTypes.add(argEntry.getType());
                        }
                    }
                    FCEntry newEntry = new FCEntry(funcName, argTypes, returnType, line, column, data);
                    FunctionChecker.add(newEntry);
                    return null;
                }

                SymbolTable classTable = ((STClass)classEntry).getSymbolTable();
                String funcName = ((SimpleNode)rightNode.jjtGetChild(0)).jjtGetValue().toString();
                STEntry funcEntry = classTable.lookup(funcName);
                if(funcEntry == null){
                    throw new SemanticException("SEMANTIC ERROR on line " + line + ".\nFunction " + funcName + " not declared inside " + className + ".");
                }
                rightType = funcEntry.getType();
            }
            //length
            else if(jmmTreeConstants.jjtNodeName[((SimpleNode)rightNode.jjtGetChild(1)).getId()].equals("Length")){
                String leftToDotId = ((SimpleNode)rightNode.jjtGetChild(0)).jjtGetValue().toString();
                STEntry leftToDotEntry = data.lookup(leftToDotId);
                if(leftToDotEntry == null){
                    throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n" + leftToDotId + " not declared.");
                }
                rightType = "int";
            }
            else{//function call
                try {
                    if(jmmTreeConstants.jjtNodeName[((SimpleNode)rightNode.jjtGetChild(0)).getId()].equals("This")){
                        rightId = "this." + ((SimpleNode)rightNode.jjtGetChild(1)).jjtGetValue().toString();
                    }
                    else{
                        rightId = ((SimpleNode) rightNode.jjtGetChild(0)).jjtGetValue().toString() + "." + ((SimpleNode) rightNode.jjtGetChild(1)).jjtGetValue().toString();
                    }
                }
                catch (java.lang.NullPointerException e){
                    rightId = jmmTreeConstants.jjtNodeName[((SimpleNode) rightNode.jjtGetChild(0)).getId()] + "." + ((SimpleNode) rightNode.jjtGetChild(1)).jjtGetValue().toString();
                }
                String[] splitRightId = rightId.split("\\.");               //Ex. Lazysort q; (...) d = q.printL(L)
                STEntry leftOfDotEntry = data.lookup(splitRightId[0]);      // data.lookup("q");
                if(leftOfDotEntry != null){         //"q" é uma variavel que representa uma classe, neste caso Lazysort
                    String className = leftOfDotEntry.getType();
                    STEntry classEntry = data.lookup(className);

                    if(classEntry == null){
                        String returnType = leftType;
                        String funcName = splitRightId[1];
                        List<String> argTypes = new ArrayList<>();
                        if((rightNode.jjtGetChild(2)).jjtGetNumChildren() == 0){
                            System.out.println(((SimpleNode)rightNode.jjtGetChild(1)).jjtGetValue().toString());
                            argTypes.add("void");
                        }
                        else {
                            for (int i = 0; i < ((SimpleNode) rightNode.jjtGetChild(2)).jjtGetNumChildren(); i++) {
                                if(jmmTreeConstants.jjtNodeName[((SimpleNode) rightNode.jjtGetChild(2).jjtGetChild(i)).getId()].equals("void")){
                                    argTypes.add("void");
                                }
                                else {
                                    String arg = ((SimpleNode) rightNode.jjtGetChild(2).jjtGetChild(i)).jjtGetValue().toString();
                                    STEntry argEntry = data.lookup(arg);
                                    if (argEntry == null) {
                                        throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n" + arg + " not declared.");
                                    }
                                    argTypes.add(argEntry.getType());
                                }
                            }
                        }
                        FCEntry newEntry = new FCEntry(funcName, argTypes, returnType, line, column, data);
                        FunctionChecker.add(newEntry);
                        return null;
                    }
                    SymbolTable classTable = ((STClass)classEntry).getSymbolTable();
                    STEntry rightOfDotEntry = classTable.lookup(splitRightId[1]);
                    if(rightOfDotEntry == null){
                        String returnType = leftType;
                        String funcName = splitRightId[1];

                        List<String> argTypes = new ArrayList<>();
                        if(((SimpleNode)rightNode.jjtGetChild(1)).jjtGetNumChildren() == 0){
                            argTypes.add("void");
                        }
                        else {
                            for (int i = 0; i < ((SimpleNode) rightNode.jjtGetChild(1)).jjtGetNumChildren(); i++) {
                                String arg = ((SimpleNode) rightNode.jjtGetChild(1).jjtGetChild(i)).jjtGetValue().toString();
                                STEntry argEntry = data.lookup(arg);
                                if (argEntry == null) {
                                    throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n" + arg + " not declared.");
                                }
                                argTypes.add(argEntry.getType());
                            }
                        }
                        FCEntry newEntry = new FCEntry(funcName, argTypes, returnType, line, column, data);
                        FunctionChecker.add(newEntry);
                        return null;
                    }
                    rightType = rightOfDotEntry.getType();
                }
                else {                              //"q" nao é uma variável mas sim a propria classe
                    STEntry rightEntry = data.lookup(rightId);
                    if (rightEntry == null) {
                        //se a função ainda nao existir adicionar ao FunctionChecker
                        String returnType = leftType;
                        String funcName = rightId;
                        List<String> argTypes = new ArrayList<>();
                        if(((SimpleNode)rightNode.jjtGetChild(2)).jjtGetNumChildren() == 0){
                            argTypes.add("void");
                        }
                        else {
                            for (int i = 0; i < ((SimpleNode) rightNode.jjtGetChild(2)).jjtGetNumChildren(); i++) {
                                if (jmmTreeConstants.jjtNodeName[((SimpleNode) rightNode.jjtGetChild(2).jjtGetChild(i)).getId()].equals("IntegerLiteral")) {
                                    argTypes.add("int");
                                    continue;
                                }
                                SimpleNode argNode = (SimpleNode) rightNode.jjtGetChild(2).jjtGetChild(i);
                                if (jmmTreeConstants.jjtNodeName[argNode.getId()].equals("Operator")) {
                                    String operator = argNode.jjtGetValue().toString();
                                    switch (operator) {
                                        case "/":
                                        case "*":
                                        case "+":
                                        case "-":
                                            argTypes.add("int");
                                            break;
                                        case "&&":
                                        case "<":
                                            argTypes.add("boolean");
                                            break;
                                    }
                                } else {
                                    String arg = argNode.jjtGetValue().toString();

                                    STEntry argEntry = data.lookup(arg);
                                    if (argEntry == null) {
                                        throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n" + arg + " not declared.");
                                    }
                                    argTypes.add(argEntry.getType());
                                }
                            }
                        }
                        FCEntry newEntry = new FCEntry(funcName, argTypes, returnType, line, column, data);
                        FunctionChecker.add(newEntry);
                        return null;
                    }
                    rightType = rightEntry.getType();
                }
            }
        }

        if(jmmTreeConstants.jjtNodeName[rightNodeId].equals("New")){
            rightType = ((SimpleNode)rightNode.jjtGetChild(0)).jjtGetValue().toString();
        }

        if(jmmTreeConstants.jjtNodeName[rightNodeId].equals("Identifier") && (node.jjtGetChild(1)).jjtGetNumChildren() == 1){
            rightId = rightNode.jjtGetValue().toString();

            STEntry rightEntry = data.lookup(rightId);
            if(rightEntry == null){
                throw new SemanticException("SEMANTIC ERROR on line " + line + ".\nVariable " + rightId + " not declared.");
            }
            rightType = rightEntry.getType();
        }

        if(!rightType.equals(leftType)){
            //System.out.println("SEMANTIC ERROR on line " + line + ".\n Incompatible variable types: " + leftType + " and " + rightType + ".");
            throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Incompatible variable types: " + leftType + " and " + rightType + ".");
        }
        else {
            ((STVarDeclaration)entry).setInitialized(true);
        }
        return null;
    }

    public Object visit(ASTExpression node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTExtends node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTFalse node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTIdentifier node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTIf node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTImport node, SymbolTable data) throws SemanticException{
        String id = "";
        int line;
        int column;
        String type = "";
        List<String> argTypes = new ArrayList<>();
        String attribute = "";
        STEntry entry;

        if(node.isStatic()){
            attribute = "Static";
        }

        line = ((SimpleNode) node.jjtGetChild(0)).getLine();
        column = ((SimpleNode) node.jjtGetChild(0)).getColumn();

        //se so tiver um filho estamos a falar de um construtor sem argumentos.
        if(node.jjtGetNumChildren() == 1){
            id = ((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString();
            type = "null";
            if((entry = data.lookup(id)) == null){
                argTypes.add("void");
                STImportClass newEntry = new STImportClass(id, type, attribute, line, column, argTypes);
                data.insert(id, newEntry);
                //return null;
            }
            else{
                //throw new SemanticException("ERROR on line " + line + ".\n Import class name " + id + " already in use (l." + entry.getLine() + " c." + entry.getColumn() + ").");
                //podemos ter imports iguais !!!!! Ex: TicTacToe.jmm
            }
            return null;
        }

        //se node tiver mais do que 1 filho, entao trata-se de uma função a ser importada
        if(node.jjtGetChild(0).jjtGetNumChildren() > 0){

            //nome da função
            id = ((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).jjtGetValue().toString();
            id += ".";
            id += ((SimpleNode) node.jjtGetChild(0).jjtGetChild(1)).jjtGetValue().toString();

            line = ((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).getLine();
            column = ((SimpleNode) node.jjtGetChild(0).jjtGetChild(0)).getColumn();

            //ver o return type
            if(((SimpleNode) node.jjtGetChild(2)).jjtGetNumChildren() == 0){
                type = "void";
            }
            else{
                type = ((SimpleNode) node.jjtGetChild(2).jjtGetChild(0)).jjtGetValue().toString();
            }
        }
        else{ //caso contrario é um construtor com varios args
            id = ((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString();
            type = "null";
        }

        if(node.jjtGetChild(1).jjtGetNumChildren() == 0){
            argTypes.add("void");
        }
        else {
            for (int i = 0; i < node.jjtGetChild(1).jjtGetNumChildren(); i++) {
                argTypes.add(((SimpleNode) node.jjtGetChild(1).jjtGetChild(i)).jjtGetValue().toString());
            }
        }

        if((entry = data.lookup(id)) == null){
            STImportFunc newEntry = new STImportFunc(id, type, attribute, line, column, argTypes);
            data.insert(id, newEntry);
            return null;
        }

        if(entry instanceof STImportFunc){
            STImportFunc funcEntry = (STImportFunc) entry;

            if(!funcEntry.getType().equals(type)){
                throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Import " + id + " return type diferent from import with same id (l." + entry.getLine() + " c." + entry.getColumn() + ").");
            }

            for(int i = 0; i < funcEntry.getArity(); i++){
                if(funcEntry.getArgType(i).equals(argTypes)){
                    //throw new SemanticException("ALREADY DECLARED" + line);
                    return null; //podemos ter imports iguais !!!!! Ex: TicTacToe.jmm
                }
            }
            funcEntry.addArgs(argTypes);
        }
        else if(entry instanceof STImportClass){
            STImportClass funcEntry = (STImportClass) entry;
            for(int i = 0; i < funcEntry.getArity(); i++){
                if(funcEntry.getArgType(i).equals(argTypes)){
                    //throw new SemanticException("ALREADY DECLARED" + line);
                    return null; //podemos ter imports iguais !!!!! Ex: TicTacToe.jmm
                }
            }
            funcEntry.addArgs(argTypes);
        }
        return null;
    }

    public Object visit(ASTIntegerLiteral node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTLeftEqual node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTLength node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTMainDeclaration node, SymbolTable data) throws SemanticException{
        String id = "main";
        int line = ((SimpleNode) node.jjtGetChild(0)).getLine();
        int column = ((SimpleNode) node.jjtGetChild(0)).getColumn();
        STEntry entry = data.lookup(id);
        if(entry != null){
            throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Main already declared (l." + entry.getLine() + " c." + entry.getColumn() + ").");
        }
        String type = "void";
        String attribute = "static";
        List<List<String>> argTypes = new ArrayList<>();
        List<String> arg = new ArrayList<>();
        arg.add(((SimpleNode)node.jjtGetChild(0)).jjtGetValue().toString());
        arg.add("String[]");
        argTypes.add(arg);
        STFunc newEntry = new STFunc(id, type, attribute, ((SimpleNode) node.jjtGetChild(0)).getLine(), ((SimpleNode) node.jjtGetChild(0)).getColumn(), argTypes, data);
        data.getSuperTable().insert(id, newEntry);
        return null;
    }

    public Object visit(ASTMethodDeclaration node, SymbolTable data) throws SemanticException{
        String id = ((SimpleNode) node.jjtGetChild(1)).jjtGetValue().toString();
        String returnType = ((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString();

        int line = ((SimpleNode) node.jjtGetChild(0)).getLine();
        int column = ((SimpleNode) node.jjtGetChild(0)).getColumn();

        List<List<String>> argTypes = new ArrayList<>();
        List<String> arg = new ArrayList<>();
        STEntry entry = data.getSuperTable().lookup(id);

        if(jmmTreeConstants.jjtNodeName[((SimpleNode) node.jjtGetChild(2)).getId()].equals("Arguments")){
            for(int i=0; i<((SimpleNode) node.jjtGetChild(2)).jjtGetNumChildren(); i+=2){
                arg.add(((SimpleNode)node.jjtGetChild(2).jjtGetChild(i)).jjtGetValue().toString());
                arg.add(((SimpleNode)node.jjtGetChild(2).jjtGetChild(i+1)).jjtGetValue().toString());
                argTypes.add(arg);
                arg = new ArrayList<>();
            }
        }
        else{
            arg.add("void");
            arg.add("null");
            argTypes.add(arg);
        }

        if(entry != null){      //id ja existe
            if(!(entry instanceof STFunc)){
                id = "this." + id;
            }
            else {
                //comparar return type
                if (!entry.getType().equals(returnType)) {
                    throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n method " + id + " already declared with different return type (l." + entry.getLine() + " c." + entry.getColumn() + ").");
                }

                //comparar com o que ja existe
                for (int i = 0; i < ((STFunc) entry).getArity(); i++) {
                    List<List<String>> entryArgTypes = ((STFunc) entry).getArgType(i);
                    boolean sameArgs = true;
                    if(entryArgTypes.size() != argTypes.size()){
                        sameArgs = false;
                    }
                    else {
                        for (int j = 0; j < argTypes.size(); j++) {
                            System.out.println(line);
                            if (!entryArgTypes.get(j).get(0).equals(argTypes.get(j).get(0)))
                                sameArgs = false;
                        }
                    }
                    if (sameArgs && argTypes.size() == entryArgTypes.size()) {
                        throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n method " + id + " already declared with same arguments (l." + entry.getLine() + " c." + entry.getColumn() + ").");
                    }
                }

                ((STFunc) entry).addArgs(argTypes, data);
                return null;
            }
        }

        //caso o id nao exista
        String attribute = "";
        STFunc newEntry = new STFunc(id, returnType, attribute, ((SimpleNode) node.jjtGetChild(0)).getLine(), ((SimpleNode) node.jjtGetChild(0)).getColumn(), argTypes, data);
        data.getSuperTable().insert(id, newEntry);
        return null;
    }

    public Object visit(ASTNew node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTNot node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTOperator node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTReturn node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTRightEqual node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTStart_parser node, SymbolTable data) throws SemanticException{
        //auto import io.println
        List<String> arg = new ArrayList<>();
        arg.add("void");
        STImportFunc entry = new STImportFunc("io.println", "void", "", 0, 0, arg);
        data.insert("io.println", entry);
        return null;
    }

    public Object visit(ASTStatement node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTThis node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTTrue node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTType node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTVarDeclaration node, SymbolTable data) throws SemanticException{
        String id = ((SimpleNode) node.jjtGetChild(1)).jjtGetValue().toString();
        int line = ((SimpleNode) node.jjtGetChild(0)).getLine();
        int column = ((SimpleNode) node.jjtGetChild(0)).getColumn();
        STEntry entry = data.lookupCurrentTable(id);
        if(entry != null){
            throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n" + id + " already declared (l." + entry.getLine() + " c." + entry.getColumn() + ").");
        }
        if(data.lookup(id)!=null){ //caso exista mas numa tabela de nível superior
            id = "this." + id;
        }
        String type = ((SimpleNode) node.jjtGetChild(0)).jjtGetValue().toString();
        if(!(type.equals("int") || type.equals("int[]") || type.equals("boolean"))){
            STEntry typeEntry = data.lookup(type);

            if(typeEntry == null){
                VCEntry varEntry = new VCEntry(id, type, data, line, column);
                VarChecker.add(varEntry);
                //return null;
            }
        }
        String attribute = "";
        STVarDeclaration newEntry = new STVarDeclaration(id, type, attribute, line, column);
        data.insert(id, newEntry);
        return null;
    }

    public Object visit(ASTVoid node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTWhile node, SymbolTable data) throws SemanticException{
        return null;
    }

    public Object visit(ASTArrayAccess node, SymbolTable data) throws SemanticException{
        int line = ((SimpleNode) node.jjtGetChild(0)).getLine();
        int column = ((SimpleNode) node.jjtGetChild(0)).getColumn();
        SimpleNode accessChild;
        if(node.jjtGetNumChildren() == 1){
            accessChild = ((SimpleNode) node.jjtGetChild(0));
        }
        else{
            accessChild = ((SimpleNode) node.jjtGetChild(1));
        }
        String rightId = "";

        try {
            int accessNumber = Integer.parseInt(accessChild.jjtGetValue().toString());
        }
        catch (java.lang.NullPointerException | java.lang.NumberFormatException e){
            //caso nao seja inteiro
            if(jmmTreeConstants.jjtNodeName[accessChild.getId()].equals("Operator")){
                String operator = accessChild.jjtGetValue().toString();
                switch(operator){
                    case "&&":
                    case "<":
                        throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Array access must use an int.");
                }
            }
            else if(jmmTreeConstants.jjtNodeName[accessChild.getId()].equals("True") ||
                    jmmTreeConstants.jjtNodeName[accessChild.getId()].equals("False") ||
                    jmmTreeConstants.jjtNodeName[accessChild.getId()].equals("Not") ||
                    jmmTreeConstants.jjtNodeName[accessChild.getId()].equals("This")){
                throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Array access must use an int.");
            }
            else if(jmmTreeConstants.jjtNodeName[accessChild.getId()].equals("Dot")){
                if(!jmmTreeConstants.jjtNodeName[((SimpleNode)accessChild.jjtGetChild(1)).getId()].equals("Length")){
                    if(jmmTreeConstants.jjtNodeName[((SimpleNode)accessChild.jjtGetChild(0)).getId()].equals("This")){
                        rightId = "this." + ((SimpleNode)accessChild.jjtGetChild(1)).jjtGetValue().toString();
                    }
                    else{
                        rightId = ((SimpleNode)accessChild.jjtGetChild(0)).jjtGetValue().toString() + "." + ((SimpleNode)accessChild.jjtGetChild(1)).jjtGetValue().toString();
                    }
                    STEntry entry = data.lookup(rightId);

                    if(entry == null){
                        String returnType = "int";
                        String funcName = rightId;
                        List<String> argTypes = new ArrayList<>();
                        if(((SimpleNode)accessChild.jjtGetChild(2)).jjtGetNumChildren() == 0){
                            argTypes.add("void");
                        }
                        else {
                            for (int i = 0; i < ((SimpleNode) accessChild.jjtGetChild(2)).jjtGetNumChildren(); i++) {
                                String arg = ((SimpleNode) accessChild.jjtGetChild(2).jjtGetChild(i)).jjtGetValue().toString();
                                STEntry argEntry = data.lookup(arg);
                                if (argEntry == null) {
                                    throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n" + arg + " not declared.");
                                }
                                argTypes.add(argEntry.getType());
                            }
                        }
                        FCEntry newEntry = new FCEntry(funcName, argTypes, returnType, line, column, data);
                        FunctionChecker.add(newEntry);
                        return null;
                    }
                    if(!entry.getType().equals("int")){
                        throw new SemanticException("SEMANTIC ERROR on line " + line + ".\n Array access must use an int.");
                    }
                }
            }
        }
        return null;
    }
}