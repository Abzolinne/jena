package org.apache.jena.query.cluster;

import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.engine.cluster.ClusteringSolver;
import org.apache.jena.sparql.engine.cluster.KMedoidsSolver;
import org.apache.jena.vocabulary.SIM;

public class KMedoidsConfiguration implements ClusterConfiguration {

protected int nbOfClusters;
	
	public KMedoidsConfiguration(int nbOfClusters) {
		this.nbOfClusters = nbOfClusters;
	}
	
	public KMedoidsConfiguration() {
		this.nbOfClusters = -1;
	}

	@Override
	public String getClusterMethod() {
		return SIM.kmedoids.getURI();
	}

	public int getNbOfClusters() {
		return nbOfClusters;
	}

	@Override
	public void setParameters(PathBlock bgp) {
		for (final TriplePath triple : bgp.getList()) {
			if (triple.getPredicate().hasURI(SIM.nbOfClusters.getURI())) {
				this.nbOfClusters = (int) triple.getObject().getLiteral().getValue();
			}
		}
		if (nbOfClusters == -1) {
			throw new QueryBuildException("Number of clusters was not provided for kmedoids method");
		}
	}

	@Override
	public ClusteringSolver getSolver() {
		return new KMedoidsSolver(nbOfClusters);
	}

}
