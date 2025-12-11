package org.apache.jena.sparql.algebra.op;

import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.lib.PairOfSameType;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public abstract class OpSimJoin extends Op2 {

	protected String distance;
	protected ExprList leftAttributes;
	protected ExprList rightAttributes;
	protected Map<Expr, List<PairOfSameType<Number>>> minMax;
	protected Var v;

	protected OpSimJoin(Op left, Op right) {
		super(left, right);
	}

	public OpSimJoin(Op left, Op right, String distance, ExprList leftAttrs, ExprList rightAttrs, Var v) {
		super(left, right);
		this.distance = distance.replace("'", "");
		this.leftAttributes = leftAttrs;
		this.rightAttributes = rightAttrs;
		this.v = v;
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

	public static Op create(Op left, Op right, ExprList leftAttrs, ExprList rightAttrs, int top, double within,
			String index, String distFunc, Var v) {
		if(top > 0 && within == -1 && index == null) {
			return new OpKNNSimJoin(left, right, top, distFunc, leftAttrs, rightAttrs, v);
		} else if (within > 0 && top == -1 && index == null) {
			return new OpRangeSimJoin(left, right, within, distFunc, leftAttrs, rightAttrs, v);
		} else if (index != null && (within == -1) && (top != -1)) {
			return new OpIndexSimJoin(left, right, top, index, distFunc, leftAttrs, rightAttrs, v);
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
	public abstract String getIndex();

	public abstract QueryIterator createIterator(QueryIterator left, QueryIterator right, ExecutionContext execCxt);

	public void setNormMap(Map<Expr, List<PairOfSameType<Number>>> condensedMinMax) {
		this.minMax = condensedMinMax;
	}
	

	public Map<Expr, List<PairOfSameType<Number>>> getMinMax() {
		return minMax;
	}

	public Var getAsVar() {
		return v;
	}

}
