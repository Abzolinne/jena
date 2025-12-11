package org.apache.jena.sparql.engine.join;

import org.apache.jena.sparql.algebra.op.OpIndexSimJoin;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.join.solver.IndexSimJoinSolver;
import org.apache.jena.sparql.engine.join.solver.RangeSimJoinNestedLoopSolver;

public class QueryIterIndexSimJoin extends QueryIterSimJoin{
	private int k;
	private String index;
	
	public QueryIterIndexSimJoin(QueryIterator left, QueryIterator right, OpIndexSimJoin opIndexSimJoin, ExecutionContext execCxt) {
		super(left, right, execCxt);
		this.k = opIndexSimJoin.getTop();
		this.index = opIndexSimJoin.getIndex();
		this.leftAttributes = opIndexSimJoin.getLeftAttributes();
		this.rightAttributes = opIndexSimJoin.getRightAttributes();
		this.distFunc = Distances.getDistance(opIndexSimJoin.getDistance());
		this.minMax = opIndexSimJoin.getMinMax();
		this.solver = new IndexSimJoinSolver(this);
		this.distVar = opIndexSimJoin.getAsVar();
		this.solver.setUp();
	}
	
	public static QueryIterator createIndex(QueryIterator left, QueryIterator right, OpIndexSimJoin opIndexSimJoin,
			ExecutionContext execCxt) {
		return new QueryIterIndexSimJoin(left, right, opIndexSimJoin, execCxt);
	}
	
	public int getK() {
		return k;
	}
	
	public String getIndex() {
		return index;
	}
	
}
