package org.apache.jena.query.cluster;

import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.engine.cluster.ClusteringSolver;
import org.apache.jena.sparql.engine.cluster.DBSCANSolver;
import org.apache.jena.vocabulary.SIM;

public class DBSCANConfiguration implements ClusterConfiguration {

	protected double epsilon;

	protected int minElements;
	
	public DBSCANConfiguration() {
		epsilon = 0.0;
		minElements = -1;
	}
	
	public DBSCANConfiguration(double epsilon, int minElements) {
		this.epsilon = epsilon;
		this.minElements = minElements;
	}

	@Override
	public String getClusterMethod() {
		return SIM.dbscan.getURI();
	}
	
	public double getEpsilon() {
		return epsilon;
	}

	public int getMinElements() {
		return minElements;
	}

	@Override
	public void setParameters(PathBlock bgp) {
		for (final TriplePath triple : bgp.getList()) {
			if (triple.getPredicate().hasURI(SIM.minDistance.getURI())) {
				this.epsilon = ((Number) triple.getObject().getLiteral().getValue()).doubleValue();
			} else if (triple.getPredicate().hasURI(SIM.minPoints.getURI())) {
				this.minElements = ((Number) triple.getObject().getLiteral().getValue()).intValue();
			}
		}
		if (epsilon == 0.0) {
			throw new QueryBuildException("Minimum distance not provided for DBSCAN");
		}
		if (minElements == -1) {
			throw new QueryBuildException("Minimum density not provided for DBSCAN");
		}
	}

	@Override
	public ClusteringSolver getSolver() {
		return new DBSCANSolver(epsilon, minElements);
	}

}
