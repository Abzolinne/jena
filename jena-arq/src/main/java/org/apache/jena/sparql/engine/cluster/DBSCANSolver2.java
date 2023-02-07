package org.apache.jena.sparql.engine.cluster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.BufferedQueryIteratorFactory;

import com.eatthepath.jvptree.VPTree;

public class DBSCANSolver2 implements ClusteringSolver {

	protected double epsilon;
	protected int minElements;
	private List<Set<Binding>> clusters;
	private List<Binding> results = new ArrayList<>();

	public DBSCANSolver2(double epsilon, int minElements) {
		this.clusters = new ArrayList<>();
		this.epsilon = epsilon;
		this.minElements = minElements;
		this.clusters.add(new HashSet<>());
	}

	//implementation based on the one of Apache Commons Math
	@Override
	public void solve(QueryIterator iter, VarExprList clusterVars, Var clusterVar) {
		BufferedQueryIteratorFactory factory = new BufferedQueryIteratorFactory(iter); 
        VPTree<Binding, Binding> vptree = new VPTree<Binding, Binding>(ClusterDistances.generateManhattan(clusterVars), 
        		factory.createBufferedQueryIterator().consume());
        QueryIterator rewindable = factory.createBufferedQueryIterator();
        int currentCluster = 1;
        while(rewindable.hasNext()) {
        	Binding current = rewindable.next();
        	if (anycontains(clusters, current)) {
				continue;
			}
        	List<Binding> neighbors = vptree.getAllWithinDistance(current, epsilon);
        	if (neighbors.size() < minElements) {
        		clusters.get(0).add(current);
				continue;
				
			}
        	clusters.add(new HashSet<Binding>());
        	clusters.get(currentCluster).add(current);
        	List<Binding> seeds = new ArrayList<>(neighbors);
        	int index = 0;
        	while(index < seeds.size()) {
        		Binding n = seeds.get(index);
        		if(current.equals(n)) continue;
        		if(clusters.get(0).contains(n)) {
        			clusters.get(0).remove(n);
        			clusters.get(currentCluster).add(n);
        		}
        		if(anycontains(clusters, n)) continue;
        		List<Binding> moreNeighbors = vptree.getAllWithinDistance(n, epsilon);
        		clusters.get(currentCluster).add(n);
        		if(moreNeighbors.size() < minElements) continue;
        		seeds.addAll(moreNeighbors);
        		index++;
        	}
			currentCluster++;
        }	
        addClusterToResults(clusters.get(0), -1, clusterVar);
	}

	private boolean anycontains(List<Set<Binding>> clusters, Binding b) {
		return clusters.stream().anyMatch(x->x.contains(b));
	}
	
	private void addClusterToResults(Set<Binding> set, int c, Var clusterVar) {
		for (Binding b : set) {
    		BindingBuilder result = BindingFactory.builder();
        	result.addAll(b);
        	result.add(clusterVar, NodeFactory.createLiteralByValue(c, XSDDatatype.XSDinteger));
        	results.add(result.build());
    	}
	}

	@Override
	public Iterator<Binding> iterator() {
		return results.iterator();
	}

}
