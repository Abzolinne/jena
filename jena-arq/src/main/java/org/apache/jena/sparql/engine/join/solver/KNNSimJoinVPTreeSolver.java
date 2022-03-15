package org.apache.jena.sparql.engine.join.solver;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.join.Distances;
import org.apache.jena.sparql.engine.join.QueryIterKNNSimJoin;
import org.apache.jena.sparql.engine.join.QueryIterSimJoin;
import org.apache.jena.sparql.engine.join.QueryIterSimJoin.Neighbor;
import org.apache.jena.sparql.expr.Expr;

import com.eatthepath.jvptree.DistanceFunction;
import com.eatthepath.jvptree.VPTree;

public class KNNSimJoinVPTreeSolver extends KNNSimJoinSolver {

	private VPTree<List<Double>, VPVector<Binding>> index;
	private DistanceFunction<List<Double>> fun;

	/**
	 * Uses a VP-tree to solve a knn-similarity join Materializes the right iterator
	 * to build the tree and probes the left bindings to find the result.
	 * 
	 * @param simjoin
	 */
	public KNNSimJoinVPTreeSolver(QueryIterSimJoin simjoin) {
		super(simjoin);
		this.bindingIterator = simjoin.getRightRows().iterator();
	}

	@Override
	public void setUp() {
		List<VPVector<Binding>> data = materialize();
		fun = Distances.asVPFunction(simjoin.getDistFunc(), simjoin.getMinMax(), simjoin.getLeftAttributes(), simjoin.getRightAttributes());
		index = new VPTree<List<Double>, VPVector<Binding>>(fun, data);
	}

	
	@Override
	protected void getNextBatch(Binding l) {
		QueryIterKNNSimJoin knnSimJoin = (QueryIterKNNSimJoin) simjoin;
		List<Double> lvals = new LinkedList<>();
		for (Expr v : simjoin.getLeftAttributes()) {
			lvals.add(((Number) l.get(v.asVar()).getLiteralValue()).doubleValue());
		}
		VPVector<Binding> query = new VPVector<>(l, lvals);
		List<VPVector<Binding>> res = index.getNearestNeighbors(query, knnSimJoin.getK()+1);
		for (VPVector<Binding> r : res) {
			double d = fun.getDistance(query, r);
			//if(d==0 && sameObject(l, r.key)) continue;
			cache.add(new Neighbor<Binding>(r.key, d));
		}
	}

	private boolean sameObject(Binding l, Binding r) {
		Iterator<Var> lvars = l.vars();
		Iterator<Var> rvars = r.vars();
		while (lvars.hasNext() && rvars.hasNext()) {
			Var lv = lvars.next();
			Var rv = rvars.next();
			if (!l.get(lv).equals(r.get(rv))) {
				return false;
			}
		}
		return true;
	}

	private List<VPVector<Binding>> materialize() {
		List<VPVector<Binding>> res = new LinkedList<>();
		while(bindingIterator.hasNext()) {
			Binding b = bindingIterator.next();
			List<Double> row = new LinkedList<>();
			for (Expr v : simjoin.getRightAttributes()) {
				row.add(((Number) b.get(v.asVar()).getLiteralValue()).doubleValue());
			}
			res.add(new VPVector<>(b, row));
		}
		return res;
	}

	static public class VPVector<K> extends LinkedList<Double> {

		private static final long serialVersionUID = 1L;
		private K key;
		private List<Double> content;

		public VPVector(K key, List<Double> content) {
			this.key = key;
			this.content = content;
		}

		public K getKey() {
			return key;
		}

		public List<Double> getContent() {
			return content;
		}

		public Iterator<Double> iterator() {
			return content.iterator();
		}

		public int size() {
			return content.size();
		}

		public Double get(int index) {
			return content.get(index);
		}
	}

}
