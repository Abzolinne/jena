package org.apache.jena.sparql.engine.join.solver;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.join.QueryIterSimJoin;
import org.apache.jena.sparql.engine.join.QueryIterSimJoin.Neighbor;

public abstract class SimJoinSolver {
	
	protected QueryIterSimJoin simjoin;
	protected Iterator<Binding> bindingIterator;
	
	public SimJoinSolver(QueryIterSimJoin simjoin) {
		this.simjoin = simjoin;
	}
	
	public abstract Binding nextBinding();
	
	public abstract boolean hasNextBinding();

	protected boolean sameObject(List<Node> lvals, List<Node> rvals) {
		for (int i = 0; i < lvals.size(); i++) {
			if (! lvals.get(i).equals(rvals.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	
	protected Binding consolidateRange(Pair<Pair<Binding,Binding>,Double> pair) {
		return Algebra.joinRange(pair, Var.alloc("d"));
	}
	
	protected Binding consolidateKNN(Binding l, Neighbor<Binding> n) {
		if (n==null)
			System.out.println(simjoin.getLeftRows());
		return Algebra.joinKNN(l, n, Var.alloc("d"));
	}

	public abstract void setUp();
}
