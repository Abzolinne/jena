package org.apache.jena.sparql.engine.join;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.sparql.algebra.op.OpRangeSimJoin;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.join.solver.RangeSimJoinNestedLoopSolver;

public class QueryIterRangeSimJoin extends QueryIterSimJoin {

	private double radius;
	private List<Pair<Pair<Binding, Binding>, Double>> pairs = new LinkedList<Pair<Pair<Binding,Binding>,Double>>();

	private QueryIterRangeSimJoin(QueryIterator left, QueryIterator right, OpRangeSimJoin op, ExecutionContext execCxt) {
		super(left, right, execCxt);
		this.radius = op.getWithin();
		this.leftAttributes = op.getLeftAttributes();
		this.rightAttributes = op.getRightAttributes();
		this.distFunc = Distances.getDistance(op.getDistance());
		this.minMax = op.getMinMax();
		this.solver = new RangeSimJoinNestedLoopSolver(this);
		this.distVar = op.getAsVar();
		this.solver.setUp();
	}

	public static QueryIterator createRange(QueryIterator left, QueryIterator right, OpRangeSimJoin op,
			ExecutionContext execCxt) {
		return new QueryIterRangeSimJoin(left, right, op, execCxt);
	}

	public double getRadius() {
		return radius;
	}

	public List<Pair<Pair<Binding, Binding>, Double>> getPairs() {
		return pairs;
	}
	
}
