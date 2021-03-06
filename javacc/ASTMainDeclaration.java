/* Generated By:JJTree: Do not edit this line. ASTMainDeclaration.java Version 6.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTMainDeclaration extends SimpleNode {
  public ASTMainDeclaration(int id) {
    super(id);
  }

  public ASTMainDeclaration(jmm p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  @Override
  public Object accept(jmmVisitor visitor, SymbolTable data) throws SemanticException {

    SymbolTable mainSymbolTable = new SymbolTable(this, data);


    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        SimpleNode n = (SimpleNode) children[i];
        if (n != null) {
          n.accept(visitor, mainSymbolTable);
        }
      }
    }
    return
    visitor.visit(this, mainSymbolTable);
  }
}
/* JavaCC - OriginalChecksum=95775a78fac4dbb74974ca109b33160d (do not edit this line) */
