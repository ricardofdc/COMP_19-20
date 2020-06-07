public class Main {

    public static void main(String[] args) throws RuntimeException{
        boolean debug = false;

        if(args.length < 1 || args.length > 2){
            System.out.println("Error: java jmm <filename> [options]");
            return;
        }

        if(args.length == 2){
            if(args[1].equals("-D")){
                debug = true;
            }
            else{
                System.out.println("Error: option not available (-D for debug).");
                System.out.println("Usage: java jmm <filename> [options]");
                return;
            }
        }

        jmm parser;

        try{
            parser = new jmm(new java.io.FileInputStream(args[0]));
        }
        catch (java.io.FileNotFoundException e){
            System.out.println("Error: file " + args[0] + " not found.");
            return;
        }

        SimpleNode root;
        parser.resetErrors();

        try {
            root = parser.Start_parser();
            root.dump("");

            SemanticAnalyzer visitor = new SemanticAnalyzer();

            SymbolTable symbolTable = new SymbolTable(root);
            Object result = root.accept(visitor, symbolTable);

            JasminGenerator jg = new JasminGenerator();

            Object jasmin = root.accept(jg, symbolTable);

            ClassChecker.check();
            VarChecker.check();

            FunctionChecker.check();

            jg.saveToFile();
            
            if(debug){
                symbolTable.printTable(0);
                //js.printCode();
            }

        }
        catch (ParseException | SemanticException e){
            System.out.println("Main exception: " + e.toString());
            throw new RuntimeException("Error during parsing", e);
        }
        finally{
            ClassChecker.clean();
            FunctionChecker.clean();
            VarChecker.clean();
        }

        if(parser.getErrors()>0) {
            throw new RuntimeException("Error during parsing");
        }


    }

}