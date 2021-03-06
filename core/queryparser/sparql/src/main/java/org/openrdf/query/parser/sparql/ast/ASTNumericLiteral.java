/* Generated By:JJTree: Do not edit this line. ASTNumericLiteral.java */

package org.openrdf.query.parser.sparql.ast;

import org.openrdf.model.IRI;

public class ASTNumericLiteral extends ASTRDFValue {

	private String value;

	private IRI datatype;

	public ASTNumericLiteral(int id) {
		super(id);
	}

	public ASTNumericLiteral(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public IRI getDatatype() {
		return datatype;
	}

	public void setDatatype(IRI datatype) {
		this.datatype = datatype;
	}

	@Override
	public String toString()
	{
		return super.toString() + " (value=" + value + ", datatype=" + datatype + ")";
	}
}
