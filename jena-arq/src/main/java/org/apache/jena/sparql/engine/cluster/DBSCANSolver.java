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

public class DBSCANSolver implements ClusteringSolver {

	protected double epsilon;
	protected int minElements;
	private List<List<Binding>> clusters;
	private List<Binding> results = new ArrayList<>();

	public DBSCANSolver(double epsilon, int minElements) {
		this.clusters = new ArrayList<>();
		this.epsilon = epsilon;
		this.minElements = minElements;
		this.clusters.add(new ArrayList<>());
	}

	//implementation based on the one of Apache Commons Math
	@Override
	public void solve(QueryIterator iter, VarExprList clusterVars, Var clusterVar) {
		BufferedQueryIteratorFactory factory = new BufferedQueryIteratorFactory(iter); 
        VPTree<Binding, Binding> vptree = new VPTree<Binding, Binding>(ClusterDistances.generateManhattan(clusterVars), 
        		factory.createBufferedQueryIterator().consume());
        QueryIterator rewindable = factory.createBufferedQueryIterator();
        Set<Binding> visited = new HashSet<>(vptree.size());
        int currentCluster = 1;
        while(rewindable.hasNext()) {
        	Binding current = rewindable.next();
        	if (visited.contains(current)) {
				continue;
			}
        	List<Binding> neighbors = vptree.getAllWithinDistance(current, epsilon);
        	if (neighbors.size() >= minElements) {
				clusters.add(new ArrayList<Binding>());
				expandCluster(currentCluster, current, neighbors, vptree, visited, clusterVar);
				currentCluster++;
			} else {
				clusters.get(0).add(current);
			}
        }	
	}

	private void expandCluster(int currentCluster, Binding e, List<Binding> neighbors,
			VPTree<Binding, Binding> vptree, Set<Binding> visited, Var clusterVar) {
		
		clusters.get(currentCluster).add(e);
		visited.add(e);
		
		List<Binding> seeds = new ArrayList<Binding>(neighbors);
		int index = 0;
		while (index < seeds.size()) {
			Binding current = seeds.get(index);
			if (visited.contains(current)) {
				index ++;
				continue;
			}
			List<Binding> currentNeighbors = vptree.getAllWithinDistance(current, epsilon);
			if (currentNeighbors.size() >= minElements) {
				for (Binding b : currentNeighbors) {
					if (! seeds.contains(b) ) {
						seeds.add(b);
					}
				}
			}
			if (! visited.contains(current) ) {
				visited.add(current);
				clusters.get(currentCluster).add(current);
			}
			index++;
		}
		addClusterToResults(clusters.get(currentCluster), currentCluster, clusterVar);
	}
	
	private void addClusterToResults(List<Binding> cluster, int c, Var clusterVar) {
		for (Binding b : cluster) {
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
