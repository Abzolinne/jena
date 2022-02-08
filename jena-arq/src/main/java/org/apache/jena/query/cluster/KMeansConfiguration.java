package org.apache.jena.query.cluster;

import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.engine.cluster.ClusteringSolver;
import org.apache.jena.sparql.engine.cluster.KMeansSolver;
import org.apache.jena.vocabulary.SIM;

public class KMeansConfiguration implements ClusterConfiguration {

	protected int nbOfClusters;
	protected int maxIterations;
	
	public KMeansConfiguration(int nbOfClusters, int minIterations) {
		this.nbOfClusters = nbOfClusters;
		this.maxIterations = minIterations;
	}
	
	public KMeansConfiguration() {
		nbOfClusters = -1;
		maxIterations = 10;
	}

	@Override
	public String getClusterMethod() {
		return SIM.kmeans.getURI();
	}

	public int getNbOfClusters() {
		return nbOfClusters;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	@Override
	public void setParameters(PathBlock bgp) {
		for (final TriplePath triple : bgp.getList()) {
			if (triple.getPredicate().hasURI(SIM.nbOfClusters.getURI())) {
				this.nbOfClusters = (int) triple.getObject().getLiteral().getValue();
			} else if (triple.getPredicate().hasURI(SIM.maxIterations.getURI())) {
				this.maxIterations = (int) triple.getObject().getLiteral().getValue();
			}
		}
		if (nbOfClusters == -1) {
			throw new QueryBuildException("Number of clusters was not provided for kmeans method");
		}
	}

	@Override
	public ClusteringSolver getSolver() {
		return new KMeansSolver(nbOfClusters, maxIterations);
	}

}
