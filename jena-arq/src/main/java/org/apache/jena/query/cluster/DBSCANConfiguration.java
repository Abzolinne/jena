package org.apache.jena.query.cluster;

import java.util.List;

import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.engine.cluster.ClusteringSolver;
import org.apache.jena.sparql.engine.cluster.DBSCANSolver;
import org.apache.jena.sparql.engine.cluster.DBSCANSolver2;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.vocabulary.SIM;

public class DBSCANConfiguration implements ClusterConfiguration {

	protected double epsilon = 0.0;
	protected int minElements = 1;
	
	public DBSCANConfiguration() {}
	
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
	public void setParameters(List<Expr> args) {
		if (args.size() == 0) {
			return;
		} else if(args.size() >= 1) {
			this.epsilon = args.get(0).getConstant().getDecimal().doubleValue();
		 	if(args.size() == 2) {
				this.minElements = args.get(1).getConstant().getInteger().intValue();
		 	}
		} else if (args.size() > 2) {
			throw new QueryBuildException("Too many arguments provided for DBSCAN");
		}
	}

	@Override
	public ClusteringSolver getSolver() {
		return new DBSCANSolver2(epsilon, minElements);
	}

}
