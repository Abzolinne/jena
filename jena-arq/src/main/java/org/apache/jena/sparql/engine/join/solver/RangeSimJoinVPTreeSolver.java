package org.apache.jena.sparql.engine.join.solver;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.join.Distances;
import org.apache.jena.sparql.engine.join.QueryIterRangeSimJoin;
import org.apache.jena.sparql.engine.join.QueryIterSimJoin;
import org.apache.jena.sparql.engine.join.solver.KNNSimJoinVPTreeSolver.VPVector;
import org.apache.jena.sparql.expr.Expr;

import com.eatthepath.jvptree.DistanceFunction;
import com.eatthepath.jvptree.VPTree;

public class RangeSimJoinVPTreeSolver extends RangeSimJoinSolver {
	private VPTree<List<Double>, VPVector<Binding>> index;
	private DistanceFunction<List<Double>> fun;

	public RangeSimJoinVPTreeSolver(QueryIterSimJoin simjoin) {
		super(simjoin);
		bindingIterator = simjoin.getRightRows().iterator();
	}

	@Override
	protected void getNextBatch(Binding l) {
		QueryIterRangeSimJoin rangeSimJoin = (QueryIterRangeSimJoin) simjoin;
		List<Double> lvals = new LinkedList<>();
		for (Expr v : simjoin.getLeftAttributes()) {
			lvals.add(((Number) l.get(v.asVar()).getLiteralValue()).doubleValue());
		}
		VPVector<Binding> query = new VPVector<>(l, lvals);
		List<VPVector<Binding>> res = index.getAllWithinDistance(query, rangeSimJoin.getRadius());
		for (VPVector<Binding> r : res) {
			Binding b = r.getKey();
			double d = fun.getDistance(query, r);
			cache.add(new Pair<Pair<Binding, Binding>, Double>(new Pair<Binding, Binding>(l, b), d));
		}
	}

	private List<VPVector<Binding>> materialize() {
		List<VPVector<Binding>> res = new LinkedList<>();
		for (Binding b = bindingIterator.next(); bindingIterator.hasNext(); b = bindingIterator.next()) {
			List<Double> row = new LinkedList<>();
			for (Expr v : simjoin.getRightAttributes()) {
				row.add(((Number) b.get(v.asVar()).getLiteralValue()).doubleValue());
			}
			res.add(new VPVector<>(b, row));
		}
		return res;
	}

	private boolean sameObject(Binding l, Binding r) {
		Iterator<Var> lvars = l.vars();
		Iterator<Var> rvars = r.vars();
		while (lvars.hasNext()) {
			Var lv = lvars.next();
			Var rv = rvars.next();
			if (l.get(lv).equals(r.get(rv))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setUp() {
		List<VPVector<Binding>> data = materialize();
		fun = Distances.asVPFunction(simjoin.getDistFunc(), simjoin.getMinMax(), simjoin.getLeftAttributes(), simjoin.getRightAttributes());
		index = new VPTree<List<Double>, VPVector<Binding>>(fun, data);
	}


}
