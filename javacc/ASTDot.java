/* Generated By:JJTree: Do not edit this line. ASTDot.java Version 6.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTDot extends SimpleNode {
  public ASTDot(int id) {
    super(id);
  }

  public ASTDot(jmm p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  @Override
  public Object accept(jmmVisitor visitor, SymbolTable data) throws SemanticException {
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
/* JavaCC - OriginalChecksum=d0cf96a2713ee8b02d5759caadbee7a1 (do not edit this line) */
