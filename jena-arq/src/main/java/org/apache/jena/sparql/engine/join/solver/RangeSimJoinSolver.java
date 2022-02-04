package org.apache.jena.sparql.engine.join.solver;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.join.QueryIterSimJoin;

public abstract class RangeSimJoinSolver extends SimJoinSolver {

	protected Queue<Pair<Pair<Binding, Binding>, Double>> cache = new LinkedList<Pair<Pair<Binding,Binding>,Double>>();

	public RangeSimJoinSolver(QueryIterSimJoin simjoin) {
		super(simjoin);
	}

	protected abstract void getNextBatch(Binding l);
	
	@Override
	public Binding nextBinding() {
		Pair<Pair<Binding, Binding>, Double> next = cache.poll();
		//System.out.println(next);
		return consolidateRange(next);
	}

	@Override
	public boolean hasNextBinding() {
		while (cache.isEmpty() && simjoin.getLeft().hasNext()) {
			getNextBatch(simjoin.getLeft().nextBinding());
		}
		return !cache.isEmpty();
	}

}
