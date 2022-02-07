package org.apache.jena.sparql.engine.join.solver;

import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.join.QueryIterSimJoin;
import org.apache.jena.sparql.engine.join.QueryIterSimJoin.Neighbor;

public abstract class KNNSimJoinSolver extends SimJoinSolver {

	protected Queue<Neighbor<Binding>> cache = new PriorityQueue<Neighbor<Binding>>(Neighbor.comparator);
	protected Binding currentSource = null;

	public KNNSimJoinSolver(QueryIterSimJoin simjoin) {
		super(simjoin);
	}
	
	protected abstract void getNextBatch(Binding l);
	
	@Override
	public Binding nextBinding() {
		if (cache.isEmpty() && simjoin.getLeft().hasNext()) {
			currentSource = simjoin.getLeft().nextBinding();
			getNextBatch(currentSource);
		}
		Binding r = consolidateKNN(currentSource, cache.poll(), simjoin.getVar());
		return r;
	}

	@Override
	public boolean hasNextBinding() {
		boolean b = (!cache.isEmpty()) || simjoin.getLeft().hasNext();
		return b;
	}

}
