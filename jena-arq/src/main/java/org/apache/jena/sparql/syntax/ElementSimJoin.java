package org.apache.jena.sparql.syntax;

import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class ElementSimJoin extends Element {

	private Element simJoinPart;

	public ElementSimJoin(Element simJoinPart) {
		this.simJoinPart = simJoinPart;
	}

	@Override
	public void visit(ElementVisitor v) {
		v.visit(this);
	}

	public Element getSimJoinElement() {
		return simJoinPart;
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
