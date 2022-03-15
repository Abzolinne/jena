package org.apache.jena.sparql.engine.join;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.jena.sparql.algebra.op.OpKNNSimJoin;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.join.solver.KNNSimJoinFLANNSolver;
import org.apache.jena.sparql.engine.join.solver.KNNSimJoinNestedLoopSolver;
import org.apache.jena.sparql.engine.join.solver.KNNSimJoinVPTreeSolver;

public class QueryIterKNNSimJoin extends QueryIterSimJoin {
	
	private int k;
	protected Map<Binding, PriorityQueue<Neighbor<Binding>>> knn = new HashMap<>();
	
	protected QueryIterKNNSimJoin(QueryIterator left, QueryIterator right, OpKNNSimJoin opKNNSimJoin, ExecutionContext execCxt) {
		super(left, right, execCxt);
		this.k = opKNNSimJoin.getTop();
		this.leftAttributes = opKNNSimJoin.getLeftAttributes();
		this.rightAttributes = opKNNSimJoin.getRightAttributes();
		this.distFunc = Distances.getDistance(opKNNSimJoin.getDistance());
		this.minMax= opKNNSimJoin.getMinMax();
		this.solver = new KNNSimJoinNestedLoopSolver(this);
		this.distVar = opKNNSimJoin.getAsVar();
		solver.setUp();
	}

	public static QueryIterator createknn(QueryIterator left, QueryIterator right, OpKNNSimJoin opKNNSimJoin,
			ExecutionContext execCxt) {
		return new QueryIterKNNSimJoin(left, right, opKNNSimJoin, execCxt);
	}

	public int getK() {
		return k;
	}

	public Map<Binding, PriorityQueue<Neighbor<Binding>>> getKnn() {
		return knn;
	}
	
}
