package org.apache.jena.sparql.engine.cluster;

import java.util.Iterator;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;

public interface ClusteringSolver {

	void solve(QueryIterator iter, VarExprList clusterVars, Var clusterVar);
	Iterator<Binding> iterator();
	
}
