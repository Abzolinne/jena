package org.apache.jena.sparql.engine.join.solver;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.join.QueryIterKNNSimJoin;
import org.apache.jena.sparql.engine.join.QueryIterSimJoin;
import org.apache.jena.sparql.engine.join.QueryIterSimJoin.Neighbor;
import org.apache.jena.sparql.expr.Expr;

public class KNNSimJoinNestedLoopSolver extends KNNSimJoinSolver {

	public KNNSimJoinNestedLoopSolver(QueryIterSimJoin simjoin) {
		super(simjoin);
		this.bindingIterator = simjoin.getRightRows().iterator();
	}

	@Override
	public void setUp() {	}

	@Override
	protected void getNextBatch(Binding l) {
		QueryIterKNNSimJoin knnSimJoin = (QueryIterKNNSimJoin) simjoin;
		List<Node> lvals = new ArrayList<>();
		for (Expr v: knnSimJoin.getLeftAttributes().getListRaw()) {
		    lvals.add(l.get(v.asVar()));
		}
		while(bindingIterator.hasNext()){
		    Binding r = bindingIterator.next();
		    List<Node> rvals = new ArrayList<>();
		    for (Expr v: knnSimJoin.getRightAttributes().getListRaw()) {
		        rvals.add(r.get(v.asVar()));
		    }
		    double d = simjoin.getDistFunc().distance(lvals, rvals, simjoin.getMinMax(), simjoin.getLeftAttributes(), simjoin.getRightAttributes());
		    if (d == 0 && sameObject(lvals, rvals)) {
				continue;
			}
		    if (cache.size() < knnSimJoin.getK()){
		    	cache.add(new Neighbor<Binding>(r, d));
		    } else if(d< cache.peek().getDistance()){
		    	cache.poll();
		    	cache.add(new Neighbor<Binding>(r, d));
		    }
		}
		bindingIterator = knnSimJoin.getRightRows().iterator();
	}
	
}
