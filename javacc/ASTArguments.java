/* Generated By:JJTree: Do not edit this line. ASTArguments.java Version 6.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTArguments extends SimpleNode {
  public ASTArguments(int id) {
    super(id);
  }

  public ASTArguments(jmm p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  @Override
  public Object accept(jmmVisitor visitor, SymbolTable data) throws SemanticException{
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        SimpleNode n = (SimpleNode) children[i];
        if (n != null) {
          n.accept(visitor, data);
        }
      }
    }
    
    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=3f25f1c51da3f7cdd0eb76761560339c (do not edit this line) */
