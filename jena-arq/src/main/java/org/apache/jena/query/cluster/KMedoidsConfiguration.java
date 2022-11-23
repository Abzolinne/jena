package org.apache.jena.query.cluster;

import java.util.List;

import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.engine.cluster.ClusteringSolver;
import org.apache.jena.sparql.engine.cluster.FASTPAMSolver;
import org.apache.jena.sparql.engine.cluster.KMedoidsSolver;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.vocabulary.SIM;

public class KMedoidsConfiguration implements ClusterConfiguration {

	protected int nbOfClusters;
	
	public KMedoidsConfiguration(int nbOfClusters) {
		this.nbOfClusters = nbOfClusters;
	}
	
	public KMedoidsConfiguration() {
		this.nbOfClusters = 3;
	}

	@Override
	public String getClusterMethod() {
		return SIM.kmedoids.getURI();
	}

	public int getNbOfClusters() {
		return nbOfClusters;
	}

	@Override
	public void setParameters(List<Expr> args) {
		if(args.size()==0) {
			this.nbOfClusters = 3;
		} else if (args.size() == 1) {
			this.nbOfClusters = args.get(0).getConstant().getInteger().intValue();
		} else {
			throw new QueryBuildException("Too many arguments provided for k-medoids");
		}
	}

	@Override
	public ClusteringSolver getSolver() {
		//return new KMedoidsSolver(nbOfClusters);
		return new FASTPAMSolver(nbOfClusters);
	}

}
