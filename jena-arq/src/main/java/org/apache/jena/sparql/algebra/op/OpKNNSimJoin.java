package org.apache.jena.sparql.algebra.op;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.join.QueryIterKNNSimJoin;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.sse.Tags;

public class OpKNNSimJoin extends OpSimJoin {

	private int top;

	private OpKNNSimJoin(Op left, Op right) {
		super(left, right);
	}

	public OpKNNSimJoin(Op left, Op right, int top, String distance, ExprList leftAttrs, ExprList rightAttrs, Var v) {
		super(left, right, distance, leftAttrs, rightAttrs, v);
		this.top = top;
	}

	@Override
	public int getTop() {
		return top;
	}

	@Override
	public double getWithin() {
		return -1;
	}

	@Override
	public String getName() {
		return Tags.tagKNNSimJoin + " ("+top+")";
	}

	@Override
	public Op2 copy(Op left, Op right) {
		return new OpKNNSimJoin(left, right, top, distance, leftAttributes, rightAttributes, v);
	}

	@Override
	public QueryIterator createIterator(QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
		return QueryIterKNNSimJoin.createknn(left, right, this, execCxt);
	}

	@Override
	public String getIndex() {
		return null;
	}

}
