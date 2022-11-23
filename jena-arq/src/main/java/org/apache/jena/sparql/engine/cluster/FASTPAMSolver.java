package org.apache.jena.sparql.engine.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jena.atlas.lib.PairOfSameType;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.BufferedQueryIteratorFactory;

public class FASTPAMSolver implements ClusteringSolver{
	
	private int K;
	private List<Binding> nearestMedoidResult;

	public FASTPAMSolver(int K) {
		assert K > 0;
		this.K = K;
	}

	@Override
	public void solve(QueryIterator iter, VarExprList clusterVars, Var clusterVar) {
		final BufferedQueryIteratorFactory factory = new BufferedQueryIteratorFactory(iter);
        final Map<PairOfSameType<Binding>, Double> distances = computeDistances(factory, clusterVars);
        final QueryIterator outer = factory.createBufferedQueryIterator();
        Binding initialMedoid = null;
        double TD = Double.MAX_VALUE;
        while(outer.hasNext()) {
            final Binding b1 = outer.next();
            double TDj = 0;
            final QueryIterator inner = factory.createBufferedQueryIterator();
            while(inner.hasNext()) {
                final Binding b2 = inner.next();
                if(b1.equals(b2)) continue;
                TDj += distances.getOrDefault(new PairOfSameType<>(b1, b2), distances.getOrDefault(new PairOfSameType<>(b2, b1), 0.0));	
            }
            if (TDj < TD) {
                initialMedoid = Binding.builder().addAll(b1).build();
                TD = TDj;
            }
        }
        final Map<Binding, Double> distanceToNearestMedoid = new HashMap<>();
        final Map<Binding, Double> distanceToSecondMedoid = new HashMap<>();
        final Map<Binding, Integer> nearestMedoid = new HashMap<>();
        final QueryIterator it = factory.createBufferedQueryIterator();
        while (it.hasNext()) {
            final Binding b = it.next();
            if(b.equals(initialMedoid)) continue;
            distanceToNearestMedoid.put(b, distances.getOrDefault(new PairOfSameType<>(b, initialMedoid), distances.getOrDefault(new PairOfSameType<>(initialMedoid, b), 0.0)));
        }
        List<Binding> medoids = new ArrayList<>(K);
        medoids.add(initialMedoid);
        for (int i=1; i < K; i++) {
            Binding candidate = null;
            double deltaTDBest = Double.MAX_VALUE;
            final QueryIterator outer2 = factory.createBufferedQueryIterator();
            while (outer2.hasNext()) {
                final Binding b1 = outer2.next();
                if (medoids.contains(b1)) continue;
                double deltaTD = 0;
                final QueryIterator inner2 = factory.createBufferedQueryIterator();
                while (inner2.hasNext()) {
                    final Binding b2 = inner2.next();
                    if (medoids.contains(b2) || b1.equals(b2)) continue;
                    double delta = distances.getOrDefault(new PairOfSameType<>(b1, b2), distances.getOrDefault(new PairOfSameType<>(b2, b1), 0.0)) 
                    		- distanceToNearestMedoid.get(b2);
                    if (delta < 0) deltaTD += delta;
                }
                if (deltaTD < deltaTDBest) {
                    candidate = Binding.builder().addAll(b1).build();
                    deltaTDBest = deltaTD;
                }
            }
            TD += deltaTDBest;
            medoids.add(candidate);
            updateCaches(factory, distances, distanceToNearestMedoid, distanceToSecondMedoid, nearestMedoid, medoids);
        }
        while(true) {
            double deltaTDBest = Double.MAX_VALUE;
            int swapMedoid = 0;
            Binding swapObject = null;
            List<Double> deltaTDmi = new ArrayList<>(K);
            for (int i=0; i<K; i++) {
            	final int j = i;
            	deltaTDmi.add(nearestMedoid.entrySet().stream().filter(e->e.getValue()==j)
            		.map(e->(distanceToSecondMedoid.get(e.getKey())-distanceToNearestMedoid.get(e.getKey())))
            		.reduce(Double::sum).get());
            }
            QueryIterator outer3 = factory.createBufferedQueryIterator();
            while (outer3.hasNext()) {
                Binding b1 = outer3.next();
                if (medoids.contains(b1)) continue;
                List<Double> deltaTD = new ArrayList<>(deltaTDmi);
                double deltaTDb1 = 0;
                QueryIterator inner3 = factory.createBufferedQueryIterator();
                while(inner3.hasNext()) {
                    Binding b2 = inner3.next();
                    double distb1b2 = distances.getOrDefault(new PairOfSameType<>(b1, b2), distances.getOrDefault(new PairOfSameType<>(b2, b1), 0.0));
                    if (distb1b2 < distanceToNearestMedoid.get(b2)) {
                        deltaTDb1 += distb1b2 - distanceToNearestMedoid.get(b2);
                        deltaTD.set(nearestMedoid.get(b2), deltaTD.get(nearestMedoid.get(b2))+distanceToNearestMedoid.get(b2)-distanceToSecondMedoid.get(b2));
                    } else if (distb1b2 < distanceToSecondMedoid.get(b2)) {
                    	deltaTD.set(nearestMedoid.get(b2), deltaTD.get(nearestMedoid.get(b2))+distb1b2-distanceToSecondMedoid.get(b2));
                    }
                }
                int i = deltaTD.indexOf(deltaTD.stream().min(Double::compareTo).get());
                deltaTD.set(i, deltaTD.get(i)+deltaTDb1);
                if (deltaTD.get(i) < deltaTDBest) {
                    deltaTDBest = deltaTD.get(i);
                    swapMedoid = i;
                    swapObject = Binding.builder().addAll(b1).build();
                }
            }
	        if (deltaTDBest >= 0) break;
	        medoids.set(swapMedoid, swapObject);
	        updateCaches(factory, distances, distanceToNearestMedoid, distanceToSecondMedoid, nearestMedoid, medoids);
	        TD += deltaTDBest;
        }
        nearestMedoidResult = nearestMedoid.entrySet().stream().map(e -> makeBinding(e, clusterVar)).collect(Collectors.toList());
	}
	
	private Binding makeBinding(Map.Entry<Binding, Integer> e, Var clusterVar) {
		return Binding.builder().addAll(e.getKey()).add(clusterVar, NodeFactory.createLiteral(e.getValue().toString(), XSDDatatype.XSDinteger)).build();
	}

	private Map<PairOfSameType<Binding>, Double> computeDistances(BufferedQueryIteratorFactory factory, VarExprList clusterVars) {
		final QueryIterator outer = factory.createBufferedQueryIterator();
		final Map<PairOfSameType<Binding>, Double> distances = new HashMap<>();
		while(outer.hasNext()) {
			final Binding b1 = outer.next();
			final QueryIterator inner = factory.createBufferedQueryIterator();
			while(inner.hasNext()) {
				final Binding b2 = inner.next();
				if (b1.equals(b2)) continue;
				final PairOfSameType<Binding> b1b2 = new PairOfSameType<>(b1, b2);
				final PairOfSameType<Binding> b2b1 = new PairOfSameType<>(b2, b1);
				if (distances.containsKey(b1b2) || distances.containsKey(b2b1))
					continue;
				final double distance = ClusterDistances.manhattan(b1, b2, clusterVars);
				distances.put(b1b2, distance);
				//distances.put(b2b1, distance);
			}
		}
		return distances;
	}
	
	private static void updateCaches(BufferedQueryIteratorFactory factory,
							            Map<PairOfSameType<Binding>, Double> distances,
							            Map<Binding, Double> distanceToNearestMedoid,
							            Map<Binding, Double> distanceToSecondMedoid,
							            Map<Binding, Integer> nearestMedoid,
							            List<Binding> medoids) {
		final QueryIterator it2 = factory.createBufferedQueryIterator();
		while(it2.hasNext()) {
			final Binding b = it2.next();
			//if (medoids.contains(b)) continue;
			double minDist = Double.MAX_VALUE;
			int minIdx = 0;
			double secondMinDist = Double.MAX_VALUE;
			int mi = 0;
			for(final Binding m : medoids) {
				double dist = distances.getOrDefault(new PairOfSameType<>(b, m), distances.getOrDefault(new PairOfSameType<>(m, b),0.0));
				if (dist < minDist) {
					secondMinDist = minDist;
					minDist = dist;
					minIdx = mi;
				} else if (dist < secondMinDist) {
					secondMinDist = dist;
				}
				mi++;
			}
			distanceToNearestMedoid.put(b, minDist);
			nearestMedoid.put(b, minIdx);
			distanceToSecondMedoid.put(b, secondMinDist);
		}
	}

	@Override
	public Iterator<Binding> iterator() {
		return nearestMedoidResult.iterator();
	}

}
