package org.apache.jena.sparql.engine.cluster;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

public class KMedoidsSolver implements ClusteringSolver {

	protected int K;
	protected KMedoidsClusterResult result =  null;
	
	public KMedoidsSolver(int K) {
		assert K > 0;
		this.K = K;
	}
	
	@Override
	public void solve(QueryIterator iter, VarExprList clusterVars, Var clusterVar) {
		BufferedQueryIteratorFactory factory = new BufferedQueryIteratorFactory(iter); 
        QueryIterator rewindable = factory.createBufferedQueryIterator();
        List<Binding> medoids = new ArrayList<>(K);
        for (int i = 0; i < K; i++) {
			if(rewindable.hasNext())
				medoids.add(rewindable.nextBinding());
			else
				throw new QueryException("Query Set has less than K objects");
		}
        while(true) {
        	result = kmedoids(medoids, factory.createBufferedQueryIterator(), clusterVars, clusterVar);
        	result.updateMedoids();
        	if(result.converged()) 
        		break;
        }
	}
	
	@Override
	public Iterator<Binding> iterator() {
		return result.iterator();
	}
	
	protected KMedoidsClusterResult kmedoids(List<Binding> medoids, QueryIterator queryIterator, 
			VarExprList clusterVars, Var clusterVar) {
		KMedoidsClusterResult results = new KMedoidsClusterResult(medoids, clusterVars);
        while(queryIterator.hasNext()) {
        	Binding current = queryIterator.nextBinding();
        	double minDist = Double.POSITIVE_INFINITY;
        	int cluster = -1;
        	for(int i=0; i<K; i++) {
        		Binding medoid = medoids.get(i);
        		double distance = ClusterDistances.manhattan(current, medoid, clusterVars);
        		if(distance < minDist) {
        			minDist = distance;
        			cluster = i;
        		}
        	}
        	if(cluster==-1) {
        		throw new QueryException("Binding could not be assigned to a cluster");
        	}
        	BindingBuilder result = BindingFactory.builder();
        	result.addAll(current);
        	result.add(clusterVar, NodeFactory.createLiteralByValue(cluster, XSDDatatype.XSDinteger));
        	results.addAssociation(cluster, result.build());
        	results.updateMedoidCost(cluster, minDist);
        }
        return results;
	}

}
