package org.apache.jena.sparql.engine.iterator;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorDelayedInitialization;
import org.apache.jena.query.cluster.ClusterConfiguration;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.cluster.ClusteringSolver;

public class QueryIterCluster extends QueryIterPlainWrapper {

	private QueryIterator innerQI;
	protected ClusterConfiguration conf;

	public QueryIterCluster(QueryIterator qIter, VarExprList clusterVars, 
			ClusterConfiguration configuration, Var clusterVar, ExecutionContext execCxt) {
		super(calc(qIter, clusterVars, configuration, clusterVar, execCxt));
		innerQI = qIter;
		conf = configuration;
	}
	
	@Override
    public void requestCancel() {
        this.innerQI.cancel();
        super.requestCancel();
    }

    @Override
    protected void closeIterator() {
        this.innerQI.close();
        super.closeIterator();
    }
    
    private static Iterator<Binding> calc(final QueryIterator iter, final VarExprList clusterVars, 
    		final ClusterConfiguration conf, Var clusterVar, final ExecutionContext execCxt) {
    	return new IteratorDelayedInitialization<Binding>() {

			@Override
			protected Iterator<Binding> initializeIterator() {
                boolean noInput = ! iter.hasNext();

                // Case: No input.
                if ( noInput ) {
                	return Iter.nullIterator() ;
                }
                ClusteringSolver solver = conf.getSolver();
                solver.solve(iter, clusterVars, clusterVar);
                return solver.iterator();
			}
    		
    	};
    }
	
}
