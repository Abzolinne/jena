package org.apache.jena.query.cluster;

import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.engine.cluster.ClusteringSolver;

public interface ClusterConfiguration {

	String getClusterMethod();

	void setParameters(PathBlock bgp);

	ClusteringSolver getSolver();
	
}
