package org.apache.jena.sparql.syntax;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class ElementSimJoin extends Element {

	private Element simJoinPart;
	private ExprList leftAttrs;
	private ExprList rightAttrs;
	private int top;
	private double within;
	private String index;
	private String distFunc;
	private Var v;

	public ElementSimJoin(Element simJoinPart, ExprList expr1, ExprList expr2, int t, double w, String i, String dist, Var v) {
		this.simJoinPart = simJoinPart;
		this.leftAttrs = expr1;
		this.rightAttrs = expr2;
		this.top = t;
		this.within = w;
		this.index = i;
		this.distFunc = dist;
		this.v = v;
	}

	public ElementSimJoin(Element elt1, ElementSimJoin el) {
		this(elt1, el.leftAttrs, el.rightAttrs, el.top, el.within, el.index, el.distFunc, el.v);
	}

	@Override
	public void visit(ElementVisitor v) {
		v.visit(this);
	}

	public Element getSimJoinElement() {
		return simJoinPart;
	}

	public ExprList getLeftAttrs() {
		return leftAttrs;
	}

	public ExprList getRightAttrs() {
		return rightAttrs;
	}

	public int getTop() {
		return top;
	}

	public double getWithin() {
		return within;
	}
	
	public String getIndex() {
		return index;
	}
	public String getDistFunc() {
		return distFunc;
	}

	public Var getV() {
		return v;
	}

	@Override
	public int hashCode() {
		int hash = HashSJ;
		hash ^= getSimJoinElement().hashCode();
		return hash;
	}

	@Override
	public boolean equalTo(Element el2, NodeIsomorphismMap isoMap) {
		return false;
	}

}
