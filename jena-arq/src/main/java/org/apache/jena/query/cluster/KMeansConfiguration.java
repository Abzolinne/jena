package org.apache.jena.query.cluster;

import java.util.List;

import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.engine.cluster.ClusteringSolver;
import org.apache.jena.sparql.engine.cluster.KMeansSolver;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.vocabulary.SIM;

public class KMeansConfiguration implements ClusterConfiguration {

	protected int nbOfClusters = 3;
	protected int maxIterations = 10;
	
	public KMeansConfiguration(int nbOfClusters, int minIterations) {
		this.nbOfClusters = nbOfClusters;
		this.maxIterations = minIterations;
	}
	
	public KMeansConfiguration() {}

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
	public void setParameters(List<Expr> args) {
		if(args.size() == 0) {
			return;
		} else if (args.size() > 0) {
			this.nbOfClusters = args.get(0).getConstant().getInteger().intValue();
		} if (args.size()==2) { 
			this.maxIterations = args.get(1).getConstant().getInteger().intValue();
		} else if (args.size() > 2) {
			throw new QueryBuildException("Too many arguments provided for k-means");
		}
	}

	@Override
	public ClusteringSolver getSolver() {
		return new KMeansSolver(nbOfClusters, maxIterations);
	}

}
