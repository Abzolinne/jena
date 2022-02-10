package org.apache.jena.sparql.engine.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.BufferedQueryIteratorFactory;

public class KMeansSolver implements ClusteringSolver {

	protected int K;
	protected int maxIter;
	protected List<Binding> results;
	protected double EPSILON = 1e-4;
	
	public KMeansSolver(int K, int maxIter) {
		assert K > 0;
		this.K = K;
		this.maxIter = maxIter;
		results = new LinkedList<Binding>();
	}

	@Override
	public void solve(QueryIterator iter, VarExprList clusterVars, Var clusterVar) {
		BufferedQueryIteratorFactory factory = new BufferedQueryIteratorFactory(iter); 
        QueryIterator rewindable = factory.createBufferedQueryIterator();
        List<Binding> centroids = new ArrayList<>();
        for (int i = 0; i < K; i++) {
			if(rewindable.hasNext())
				centroids.add(rewindable.nextBinding());
			else
				throw new QueryException("Query Set has less than K objects");
		}
        List<List<Binding>> clusters = null;
        for (int i=0; i < maxIter; i++) {
        	QueryIterator elements = factory.createBufferedQueryIterator();
        	clusters = new ArrayList<List<Binding>>(K);
    		for (int t=0; t < K; t++) {
    			clusters.add(new LinkedList<Binding>());
    		}
        	while(elements.hasNext()) {
        		Binding current = elements.next();
        		double minDist = Double.MAX_VALUE;
        		int cluster = -1;
        		for (int j=0; j < K; j++) {
        			Binding c = centroids.get(j);
        			double dist = ClusterDistances.manhattan(current, c, clusterVars);
        			if (dist < minDist) {
        				minDist = dist;
        				cluster = j;
        			}
        		}
        		clusters.get(cluster).add(current);
        	}
        	List<Binding> oldCentroids = centroids;
    		centroids = updateCentroids(clusterVars, clusters);
    		if (converged(oldCentroids, centroids, clusterVars)) {
    			break;
    		}
        }
        int c = 1;
        for (List<Binding> cluster : clusters) {
        	for (Binding b : cluster) {
        		BindingBuilder result = BindingFactory.builder();
            	result.addAll(b);
            	result.add(clusterVar, NodeFactory.createLiteralByValue(c, XSDDatatype.XSDinteger));
            	results.add(result.build());
        	}
        	c++;
        }
	}

	private boolean converged(List<Binding> oldCentroids, List<Binding> centroids, VarExprList clusterVars) {
		for (int i=0; i < K; i++) {
			double d = ClusterDistances.manhattan(oldCentroids.get(i), centroids.get(i), clusterVars);
			if (d < EPSILON) {
				return true;
			}
		}
		return false;
	}

	private List<Binding> updateCentroids(VarExprList clusterVars, List<List<Binding>> clusters) {
		List<Binding> newCentroids = new ArrayList<Binding>(K);
		for (int i=0; i<K; i++) {
			Map<Var, Double> sums = new HashMap<>();
			for (Binding b : clusters.get(i)) {
				for (Var v : clusterVars.getVars()) {
					if (!sums.containsKey(v)) {
						sums.put(v, ((Number) b.get(v).getLiteral().getValue()).doubleValue());
					} else {
						sums.put(v, sums.get(v) + ((Number) b.get(v).getLiteral().getValue()).doubleValue());
					}
				}
			}
			BindingBuilder newCentroid = BindingFactory.builder();
			for (Var v : clusterVars.getVars()) {
				double val = sums.get(v) / clusters.get(i).size();
				newCentroid.add(v, NodeFactory.createLiteralByValue(val, XSDDatatype.XSDdouble));
			}
			newCentroids.add(newCentroid.build());
		}
		return newCentroids;
	}

	@Override
	public Iterator<Binding> iterator() {
		return results.iterator();
	}

}
