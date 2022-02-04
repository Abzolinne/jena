package org.apache.jena.sparql.algebra.op;

import java.util.Map;

import org.apache.jena.atlas.lib.PairOfSameType;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public abstract class OpSimJoin extends Op2 {

	protected String distance;
	protected ExprList leftAttributes;
	protected ExprList rightAttributes;
	private Map<Expr, PairOfSameType<Number>> minMax;

	protected OpSimJoin(Op left, Op right) {
		super(left, right);
	}

	public OpSimJoin(Op left, Op right, String distance, ExprList leftAttrs, ExprList rightAttrs) {
		super(left, right);
		this.distance = distance.replace("'", "");
		this.leftAttributes = leftAttrs;
		this.rightAttributes = rightAttrs;
	}

	@Override
	public void visit(OpVisitor opVisitor) {
		opVisitor.visit(this);
	}

	@Override
	public abstract String getName();

	@Override
	public Op apply(Transform transform, Op left, Op right) {
		return transform.transform(this, left, right);
	}

	@Override
	public abstract Op2 copy(Op left, Op right);

	@Override
	public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
		return false;
	}

	public static Op create(Op left, Op right, Query q) {
		if (q==null || !q.isSimilarityJoin()) {
			return null;
		}
		String distance = q.getDistance();
		ExprList leftAttrs = q.getLeftAttrs();
		ExprList rightAttrs = q.getRightAttrs();
		if(q.getTop()>0 && q.getWithin()==-1) {
			return new OpKNNSimJoin(left, right, q.getTop(), distance, leftAttrs, rightAttrs);
		} else if (q.getWithin()>0 && q.getTop()==-1) {
			return new OpRangeSimJoin(left, right, q.getWithin(), distance, leftAttrs, rightAttrs);
		} 
		return null;
	}

	public void setLeft(Op left) {
		this.left = left;
	}

	public void setRight(Op right) {
		this.right = right;
	}
	
	public String getDistance() {
		return distance;
	}

	public ExprList getLeftAttributes() {
		return leftAttributes;
	}

	public ExprList getRightAttributes() {
		return rightAttributes;
	}

	public abstract int getTop();
	public abstract double getWithin();

	public abstract QueryIterator createIterator(QueryIterator left, QueryIterator right, ExecutionContext execCxt);

	public void setNormMap(Map<Expr, PairOfSameType<Number>> condensedMinMax) {
		this.minMax = condensedMinMax;
	}
	

	public Map<Expr, PairOfSameType<Number>> getMinMax() {
		return minMax;
	}

}
