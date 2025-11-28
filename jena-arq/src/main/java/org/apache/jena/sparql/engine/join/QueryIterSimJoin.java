package org.apache.jena.sparql.engine.join;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.PairOfSameType;
import org.apache.jena.sparql.algebra.op.OpSimJoin;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter2;
import org.apache.jena.sparql.engine.join.Distances.DistFunc;
import org.apache.jena.sparql.engine.join.solver.SimJoinSolver;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;

public class QueryIterSimJoin extends QueryIter2 {
	
	protected ExprList leftAttributes;
	protected ExprList rightAttributes;
	
	protected List<Binding> results = new LinkedList<>();
	
	protected List<Binding> leftRows = null; 
	protected List<Binding> rightRows = null; 
	
	protected DistFunc distFunc;
	protected Var distVar;
	
    protected long s_countLHS = 0;
    protected long s_countRHS = 0;
    protected long s_countResults = 0;
    
    protected Binding slot;
	protected SimJoinSolver solver;
	protected Map<Expr, List<PairOfSameType<Number>>> minMax;

	public QueryIterSimJoin(QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
		super(left, right, execCxt);
	}

	public static QueryIterator create(QueryIterator left, QueryIterator right, OpSimJoin opSimJoin, ExecutionContext execCxt) {
		return opSimJoin.createIterator(left, right, execCxt);
	}
	
	public static class Neighbor<K>{
        private K key;
        private double distance;

        static public Comparator<Neighbor<?>> comparator= (n1, n2) -> n1.distance>n2.distance? -1:1;

        public Neighbor(K key, double distance) {
            this.key = key;
            this.distance = distance;
        }

        public K getKey() {
            return key;
        }

        public double getDistance() {
            return distance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Neighbor<?> neighbor = (Neighbor<?>) o;
            return key.equals(neighbor.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, distance);
        }
    }

	@Override
	protected boolean hasNextBinding() {
		if (slot != null) return true;
		if (solver.hasNextBinding()) {
			slot = solver.nextBinding();
			return true;
		}
		return false;
	}

	@Override
	protected Binding moveToNextBinding() {
		Binding x = slot;
		slot = null;
		return x;
	}

	@Override
	protected void requestSubCancel() {
		getLeft().cancel();
		getRight().cancel();
	}

	@Override
	protected void closeSubIterator() {
		getLeft().close();
		getRight().close();
	}

	public ExprList getRightAttributes() {
		return rightAttributes;
	}

	public List<Binding> getLeftRows() {
		if (leftRows == null)
			leftRows = Iter.toList(getLeft());
		return leftRows;
		
	}
	
	public List<Binding> getRightRows() {
		if (rightRows == null) {
			rightRows = Iter.toList(getRight());
		}
		return rightRows;
	}

	public ExprList getLeftAttributes() {
		return leftAttributes;
	}

	public DistFunc getDistFunc() {
		return distFunc;
	}

	public Map<Expr, List<PairOfSameType<Number>>> getMinMax() {
		return minMax;
	}

	public Var getVar() {
		return distVar;
	}
}
