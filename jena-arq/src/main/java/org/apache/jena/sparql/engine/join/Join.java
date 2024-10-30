/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.sparql.engine.join;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List ;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.PairOfSameType;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Table ;
import org.apache.jena.sparql.algebra.TableFactory ;
import org.apache.jena.sparql.algebra.op.OpSimJoin;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.BufferedQueryIteratorFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import org.apache.jena.sparql.engine.main.OpExecutor ;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList ;

/** API to various join algorithms */
public class Join {
    // See also package org.apache.jena.sparql.engine.index
    // The anti-join code for MINUS

    private final static boolean useNestedLoopJoin      = false ;
    private final static boolean useNestedLoopLeftJoin  = false ;

    /**
     * Standard entry point to a join of two streams.
     * This is not a substitution/index join.
     * (See {@link OpExecutor} for streamed execution using substitution).
     * @param left
     * @param right
     * @param execCxt
     * @return QueryIterator
     */
    public static QueryIterator join(QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        return join(null, left, right, execCxt);
    }

    /**
     * Standard entry point to a join of two streams.
     * This is not a substitution/index join.
     * (See {@link OpExecutor} for streamed execution using substitution).
     * @param joinKey
     * @param left
     * @param right
     * @param execCxt
     * @return QueryIterator
     */
    public static QueryIterator join(JoinKey joinKey, QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        if ( false )
            return debug(left, right, execCxt,
                         (_left, _right)->hashJoin(joinKey, _left, _right, execCxt)) ;
        if ( useNestedLoopJoin )
            return nestedLoopJoin(left, right, execCxt) ;
        return hashJoin(joinKey, left, right, execCxt) ;
    }

    /** Standard entry point to a left join of two streams.
     * This is not a substitution/index join.
     * (See {@link OpExecutor} for streamed execution using substitution).
     *
     * @param left
     * @param right
     * @param conditions
     * @param execCxt
     * @return QueryIterator
     */
    public static QueryIterator leftJoin(QueryIterator left, QueryIterator right, ExprList conditions, ExecutionContext execCxt) {
        if ( false )
            return debug(left, right, execCxt,
                         (_left, _right)->hashLeftJoin(_left, _right, conditions, execCxt)) ;
        if ( useNestedLoopLeftJoin )
            return nestedLoopLeftJoin(left, right, conditions, execCxt) ;
        return hashLeftJoin(left, right, conditions, execCxt) ;
    }

    interface JoinOp {
        public QueryIterator exec(QueryIterator left, QueryIterator right) ;
    }

    /** Inner loop join.
     *  Cancellable.
     * @param left      Left hand side
     * @param right     Right hand side
     * @param execCxt       ExecutionContext
     * @return          QueryIterator
     */
    public static QueryIterator nestedLoopJoin(QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        return new QueryIterNestedLoopJoin(left, right, execCxt) ;
    }

    /** Left loop join.
     *  Cancellable.
     * @param left      Left hand side
     * @param right     Right hand side
     * @param execCxt       ExecutionContext
     * @return          QueryIterator
     */
    public static QueryIterator nestedLoopLeftJoin(QueryIterator left, QueryIterator right, ExprList conditions, ExecutionContext execCxt) {
        return new QueryIterNestedLoopLeftJoin(left, right, conditions, execCxt) ;
    }

    /** Evaluate using a hash join.
     *
     * @param left      Left hand side
     * @param right     Right hand side
     * @param execCxt   ExecutionContext
     * @return          QueryIterator
     */
    public static QueryIterator hashJoin(QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        //return new QueryIterNestedLoopJoin(left, right, conditions, execCxt) ;
        return QueryIterHashJoin.create(left, right, execCxt) ;
    }

    /** Evaluate using a hash join.
     *
     * @param joinKey   The key for the probe table.
     * @param left      Left hand side
     * @param right     Right hand side
     * @param execCxt   ExecutionContext
     * @return          QueryIterator
     */
    public static QueryIterator hashJoin(JoinKey joinKey, QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        return QueryIterHashJoin.create(joinKey, left, right, execCxt) ;
    }

    /**
     * Left outer join by using hash join. Normally, this is
     * hashing the right hand side and streaming the left.  The reverse
     * implementation (hash left, stream right) is also available.
     * @param left
     * @param right
     * @param conditions
     * @param execCxt
     * @return QueryIterator
     */
    public static QueryIterator hashLeftJoin(QueryIterator left, QueryIterator right, ExprList conditions, ExecutionContext execCxt) {
        return QueryIterHashLeftJoin_Right.create(left, right, conditions, execCxt) ;
    }

    /**
     * Left outer join by using hash join. Normally, this is
     * hashing the right hand side and streaming the left.  The reverse
     * implementation (hash left, stream right) is also available.
     * @param joinKey
     * @param left
     * @param right
     * @param conditions
     * @param execCxt
     * @return QueryIterator
     */
    public static QueryIterator hashLeftJoin(JoinKey joinKey, QueryIterator left, QueryIterator right, ExprList conditions, ExecutionContext execCxt) {
        return QueryIterHashLeftJoin_Right.create(joinKey, left, right, conditions, execCxt) ;
    }

    /** Very simple, materializing version - useful for debugging.
     *  Builds output early. Materializes left, streams right.
     *  Does <b>not</b> scale.
     *  No cancellation, no stats.
     *
     * @see #nestedLoopJoin
     */
    public static QueryIterator nestedLoopJoinBasic(QueryIterator left, QueryIterator right, ExecutionContext execCxt) {
        List<Binding> leftRows = Iter.toList(left) ;
        List<Binding> output = new ArrayList<>() ;
        for ( ; right.hasNext() ; ) {
            Binding row2 = right.next() ;
            for ( Binding row1 : leftRows ) {
                Binding r = Algebra.merge(row1, row2) ;
                if ( r != null )
                    output.add(r) ;
            }
        }
        return QueryIterPlainWrapper.create(output.iterator(), execCxt) ;
    }

    /** Very simple, materializing version for leftjoin - useful for debugging.
     *  Builds output early. Materializes right, streams left.
     *  Does <b>not</b> scale.
     */
    public static QueryIterator nestedLoopLeftJoinBasic(QueryIterator left, QueryIterator right, ExprList conditions, ExecutionContext execCxt) {
        // Stream from left, materialize right.
        List<Binding> rightRows = Iter.toList(right) ;
        List<Binding> output = new ArrayList<>() ;
        long count = 0 ;
        for ( ; left.hasNext() ; ) {
            Binding row1 = left.next() ;
            boolean match = false ;
            for ( Binding row2 : rightRows ) {
                Binding r = Algebra.merge(row1, row2) ;
                if ( r != null && applyConditions(r, conditions, execCxt)) {
                    output.add(r) ;
                    match = true ;
                }
            }
            if ( ! match )
                output.add(row1) ;
        }
        return QueryIterPlainWrapper.create(output.iterator(), execCxt) ;
    }

    // apply conditions.
    private static boolean applyConditions(Binding row, ExprList conditions, ExecutionContext execCxt) {
        if ( conditions == null )
            return true ;
        return conditions.isSatisfied(row, execCxt) ;
    }

    /* Debug.
     * Print inputs and outputs.
     * This involves materializing the iterators.
     */
    private static QueryIterator debug(QueryIterator left, QueryIterator right, ExecutionContext execCxt, JoinOp action) {
            Table t1 = TableFactory.create(left) ;
            Table t2 = TableFactory.create(right) ;

            left = t1.iterator(execCxt) ;
            right = t2.iterator(execCxt) ;

            QueryIterator qIter = action.exec(left, right) ;
            Table t3 = TableFactory.create(qIter) ;
            System.out.println("** Left") ;
            System.out.println(t1) ;
            System.out.println("** Right") ;
            System.out.println(t2) ;
            System.out.println("** ") ;
            System.out.println(t3) ;
    //        // Could do again here, different algoithm for comparison.
    //        left = t1.iterator(execCxt) ;
    //        right = t2.iterator(execCxt) ;
    //        System.out.println("** nestedLoopJoin") ;
    //        Table t4 = TableFactory.create(?????) ;
    //        System.out.println(t4) ;
            return t3.iterator(execCxt) ;
        }
    
    public static QueryIterator simJoin(QueryIterator left, QueryIterator right, OpSimJoin opSimJoin,
			ExecutionContext execCxt) {
		BufferedQueryIteratorFactory leftFactory = new BufferedQueryIteratorFactory(left);
		BufferedQueryIteratorFactory rightFactory = new BufferedQueryIteratorFactory(right);
		PairOfSameType<Map<Expr, PairOfSameType<Number>>> minMax = getNormalisationMap(leftFactory.createBufferedQueryIterator(),
				rightFactory.createBufferedQueryIterator(), opSimJoin.getLeftAttributes(), opSimJoin.getRightAttributes());
		Map<Expr, PairOfSameType<Number>> condensedMinMax = condense(minMax);
		opSimJoin.setNormMap(condensedMinMax);
		return QueryIterSimJoin.create(leftFactory.createBufferedQueryIterator(), rightFactory.createBufferedQueryIterator(), opSimJoin, execCxt);
	}

	private static Map<Expr, PairOfSameType<Number>> condense(
			PairOfSameType<Map<Expr, PairOfSameType<Number>>> minMax) {
		Map<Expr, PairOfSameType<Number>> res = new HashMap<Expr, PairOfSameType<Number>>();
		Iterator<Entry<Expr, PairOfSameType<Number>>> leftIter = minMax.getLeft().entrySet().iterator();
		Iterator<Entry<Expr, PairOfSameType<Number>>> rightIter = minMax.getRight().entrySet().iterator();
		while (leftIter.hasNext() && rightIter.hasNext()) {
			Entry<Expr, PairOfSameType<Number>> left = leftIter.next();
			Entry<Expr, PairOfSameType<Number>> right = rightIter.next();
			PairOfSameType<Number> pair = new PairOfSameType<Number>(
					Math.min(left.getValue().getLeft().doubleValue(), right.getValue().getLeft().doubleValue()),
					Math.max(left.getValue().getRight().doubleValue(), right.getValue().getRight().doubleValue()));
			res.put(left.getKey(), pair);
		}
		return res;
	}

	private static PairOfSameType<Map<Expr, PairOfSameType<Number>>> getNormalisationMap(QueryIterator left, QueryIterator right,
			ExprList leftAttributes, ExprList rightAttributes) {
		Map<Expr, PairOfSameType<Number>> resultLeft = new HashMap<Expr, PairOfSameType<Number>>();
		Map<Expr, PairOfSameType<Number>> resultRight = new HashMap<Expr, PairOfSameType<Number>>();
		for(;left.hasNext();) {
			Binding current = left.nextBinding();
			for(Expr lexpr : leftAttributes.getList()) {
				probeToMap(resultLeft, current, lexpr);
			}
		}
		for(;right.hasNext();) {
			Binding current = right.nextBinding();
			for(Expr rexpr : rightAttributes.getList()) {
				probeToMap(resultRight, current, rexpr);
			}
		}
		return new PairOfSameType<Map<Expr,PairOfSameType<Number>>>(resultLeft, resultRight);
	}

	private static void probeToMap(Map<Expr, PairOfSameType<Number>> result, Binding current, Expr expr) {
		if(current.get(expr.asVar())==null)
			throw new IllegalArgumentException("Similarity Join variables should not be obtained from an OPTIONAL clause");
	    Object literalValue = current.get(expr.asVar()).getLiteralValue();
	    if (literalValue instanceof String) {
	    	return;
	    } 
		Number currentValue = (Number) literalValue;
		if (!result.containsKey(expr)) {
			result.put(expr, new PairOfSameType<Number>(currentValue, currentValue));
			return;
		}
		if (currentValue.doubleValue() < result.get(expr).getLeft().doubleValue()) {
			result.put(expr, new PairOfSameType<Number>(currentValue, result.get(expr).getRight()));
		}
		if (currentValue.doubleValue() > result.get(expr).getRight().doubleValue()) {
			result.put(expr, new PairOfSameType<Number>(result.get(expr).getLeft(), currentValue));
		}
	}
}
