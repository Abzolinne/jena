package org.apache.jena.sparql.engine.cluster;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;

public class ClusterDistances {

	static public double manhattan(Binding b, Binding b2, VarExprList vars) {
		double res = 0;
		for(Var v : vars.getVars()) {
			Node x = b.get(v);
			Node y = b2.get(v);
			res += Math.abs(((Number)x.getLiteralValue()).doubleValue()-((Number)y.getLiteralValue()).doubleValue());
		}
		return res;
	}
	
}
