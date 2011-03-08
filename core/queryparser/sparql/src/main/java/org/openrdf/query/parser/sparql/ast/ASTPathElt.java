/* Generated By:JJTree: Do not edit this line. ASTPathElt.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.openrdf.query.parser.sparql.ast;

public
class ASTPathElt extends SimpleNode {
  private boolean inverse;

public ASTPathElt(int id) {
    super(id);
  }

  public ASTPathElt(SyntaxTreeBuilder p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data) throws VisitorException {
    return visitor.visit(this, data);
  }
  
  public void setInverse(boolean inverse) {
	  this.inverse = inverse;
  }
  
  public boolean isInverse() {
	  return this.inverse;
  }
  
  public ASTPathMod getPathMod() {
	  return jjtGetChild(ASTPathMod.class);
  }
}
/* JavaCC - OriginalChecksum=775f2a21c17018fa87a23bafe7b048dd (do not edit this line) */
