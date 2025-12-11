package org.apache.jena.sparql.algebra.op;

import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.PairOfSameType;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.join.QueryIterIndexSimJoin;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.Tags;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class OpIndexSimJoin extends OpSimJoin implements Op {
	private int top;
	private String index;
	
	public OpIndexSimJoin(Op left, Op right, int top, String index, String distance, ExprList leftAttrs, ExprList rightAttrs,
			Var v) {
		super(left, right, distance, leftAttrs, rightAttrs, v);
		this.top = top;
		this.index = index;
	}

	public int getTop() {
		return top;
	}
	
	@Override
	public String getIndex() {
		return index;
	}
	
	@Override
	public Op2 copy(Op left, Op right) {
		return new OpIndexSimJoin(left, right, top, index, distance, leftAttributes, rightAttributes, v);
	}
	
	@Override
	public QueryIterator createIterator(QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
		return QueryIterIndexSimJoin.createIndex(left, right, this, execCxt);
	}

	@Override
	public String getName() {
		return (Tags.tagIndexSimJoin) +"(index = "+index+", top = "+top+")";
	}

	@Override
	public double getWithin() {
		return -1;
	}
	
}
