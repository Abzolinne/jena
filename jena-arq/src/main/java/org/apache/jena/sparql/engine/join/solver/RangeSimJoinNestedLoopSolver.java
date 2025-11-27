package org.apache.jena.sparql.engine.join.solver;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.join.QueryIterRangeSimJoin;
import org.apache.jena.sparql.engine.join.QueryIterSimJoin;
import org.apache.jena.sparql.expr.Expr;

public class RangeSimJoinNestedLoopSolver extends RangeSimJoinSolver {

	public RangeSimJoinNestedLoopSolver(QueryIterSimJoin simjoin) {
		super(simjoin);
		this.bindingIterator = simjoin.getRightRows().iterator();
	}

	@Override
	public void setUp() {	}

	@Override
	protected void getNextBatch(Binding l) {
		List<Node> lvals = new ArrayList<>();
        for (Expr v: simjoin.getLeftAttributes().getListRaw()) {
            lvals.add(l.get(v.asVar()));
        }
		while(bindingIterator.hasNext()){
		    Binding r = bindingIterator.next();
		    List<Node> rvals = new ArrayList<>();
		    for (Expr v: simjoin.getRightAttributes().getListRaw()) {
		        rvals.add(r.get(v.asVar()));
		    }
		    double d = simjoin.getDistFunc().distance(lvals, rvals, simjoin.getMinMax(), simjoin.getLeftAttributes(), simjoin.getRightAttributes());
		    if (d==0 && sameObject(lvals, rvals)) {
					continue;
			}
		    if (d <= ((QueryIterRangeSimJoin)simjoin).getRadius()) {
		    	Pair<Binding, Binding> result = new Pair<Binding, Binding>(l, r);
		    	Pair<Pair<Binding, Binding>, Double> resultWithDistance = new Pair<Pair<Binding, Binding>, Double>(result, d);
		    	cache.add(resultWithDistance);
			}
		}
		bindingIterator = simjoin.getRightRows().iterator();
	}

}
