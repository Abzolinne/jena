package org.apache.jena.sparql.engine.cluster;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;

public class KMedoidsClusterResult {

	private List<List<Binding>> clusters;
	private List<Double> medoidCosts;
	private List<Binding> medoids;
	private boolean converged = false;
	VarExprList clusterVars;
	
	private final double EPSILON = .985;
	
	public KMedoidsClusterResult(List<Binding> medoids, VarExprList clusterVars) {
		this.clusters = new ArrayList<List<Binding>>();
		this.medoids = medoids;
		this.clusterVars = clusterVars;
		this.medoidCosts = new ArrayList<Double>(medoids.size());
		for(int i=0; i < medoids.size(); i++) {
			this.medoidCosts.add(0.0);
			this.clusters.add(new ArrayList<Binding>());
		}
	}

	public void addAssociation(int cluster, Binding result) {
		this.clusters.get(cluster).add(result);
	}

	public void updateMedoidCost(int cluster, double minDist) {
		this.medoidCosts.set(cluster, this.medoidCosts.get(cluster) + minDist);
	}

	public void updateMedoids() {
		this.converged = true;
		for (int i = 0; i < medoids.size(); i++) {
			double oldMedoidCost = medoidCosts.get(i);
			for(Binding b : clusters.get(i)) {
				double currentCost = 0;
				for(Binding b2 : clusters.get(i)) {
					currentCost += ClusterDistances.manhattan(b, b2, clusterVars);
				}
				if(currentCost < oldMedoidCost) {
					this.medoids.set(i, b);
					this.medoidCosts.set(i, currentCost);
					this.converged = false;
				}
			}
		}
	}

	public boolean converged() {
		return this.converged;
	}

	public Iterator<Binding> iterator() {
		return flatten(clusters).iterator();
	}

	private List<Binding> flatten(List<List<Binding>> clusters) {
		List<Binding> ret = new ArrayList<Binding>();
		for(List<Binding> cluster : clusters) {
			ret.addAll(cluster);
		}
		return ret;
	}
	
}
