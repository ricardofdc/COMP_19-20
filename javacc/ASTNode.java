
public interface ASTNode {

    public Object accept(jmmVisitor visitor, SymbolTable data) throws SemanticException;


}