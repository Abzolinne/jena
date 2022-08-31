package org.apache.jena.query.cluster;

import java.util.List;

import org.apache.jena.sparql.engine.cluster.ClusteringSolver;
import org.apache.jena.sparql.expr.Expr;

public interface ClusterConfiguration {

	String getClusterMethod();

	void setParameters(List<Expr> args);

	ClusteringSolver getSolver();
	
}
