/* Generated By:JJTree: Do not edit this line. ASTNew.java Version 6.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTNew extends SimpleNode {
  public ASTNew(int id) {
    super(id);
  }

  public ASTNew(jmm p, int id) {
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
/* JavaCC - OriginalChecksum=8ff661061638722092696d91ec5a1a1a (do not edit this line) */
