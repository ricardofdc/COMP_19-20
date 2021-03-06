/* Generated By:JJTree: Do not edit this line. ASTCondition.java Version 6.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTCondition extends SimpleNode {
  public ASTCondition(int id) {
    super(id);
  }

  public ASTCondition(jmm p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  @Override
  public Object accept(jmmVisitor visitor, SymbolTable data)  throws SemanticException{

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
/* JavaCC - OriginalChecksum=82e6bb5194f504804c64ebb6555dda43 (do not edit this line) */
