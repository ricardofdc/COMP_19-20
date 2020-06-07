//based on https://www.tutorialspoint.com/compiler_design/compiler_design_symbol_table.htm

public abstract class STEntry {
    
    protected String id;
    protected String type;
    protected String attribute;
    protected int line, column;

    public STEntry(String name, String type, String attribute, int line, int column) {
        this.id = name;
        this.type = type;
        this.attribute = attribute;
        this.line = line;
        this.column = column;
    }

    public String getId(){
        return id;
    }

    public String getType(){
        return this.type;
    }

    public int getLine(){
        return line;
    }

    public int getColumn(){
        return column;
    }

    public String getAttribute(){
        return attribute;
    }

    public abstract void print(int nivel);
}


