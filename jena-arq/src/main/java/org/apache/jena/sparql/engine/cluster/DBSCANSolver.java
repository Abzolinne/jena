package org.apache.jena.sparql.engine.cluster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.BufferedQueryIteratorFactory;
import org.apache.jena.sparql.engine.iterator.BufferedQueryIteratorFactory.BufferedQueryIterator;

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
        QueryIterator rewindable = factory.createBufferedQueryIterator();
        Set<Binding> visited = new HashSet<>();
        int currentCluster = 1;
        while(rewindable.hasNext()) {
        	Binding current = rewindable.next();
        	if (visited.contains(current)) {
				continue;
			}
        	List<Binding> neighbors = getNeighbors(current, factory.createBufferedQueryIterator(), clusterVars);
        	if (neighbors.size() >= minElements) {
				clusters.add(new ArrayList<Binding>());
				expandCluster(currentCluster, current, neighbors, factory, visited, clusterVars);
				currentCluster++;
			} else {
				clusters.get(0).add(current);
			}
        }	
	}

	private void expandCluster(int currentCluster, Binding e, List<Binding> neighbors,
			BufferedQueryIteratorFactory factory, Set<Binding> visited, VarExprList clusterVars) {
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
			List<Binding> currentNeighbors = getNeighbors(current, factory.createBufferedQueryIterator(), clusterVars);
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
	}

	private List<Binding> getNeighbors(Binding e, BufferedQueryIterator elements,
			VarExprList clusterVars) {
		List<Binding> results = new ArrayList<Binding>();
		while (elements.hasNext()) {
			Binding b = elements.next();
			if (!e.equals(b) && ClusterDistances.manhattan(e, b, clusterVars) < epsilon) {
				results.add(b);
			}
		}
		return results;
	}

	@Override
	public Iterator<Binding> iterator() {
		return results.iterator();
	}

}
