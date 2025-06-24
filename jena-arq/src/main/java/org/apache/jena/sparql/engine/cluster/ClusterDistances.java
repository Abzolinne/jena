package org.apache.jena.sparql.engine.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.lib.PairOfSameType;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;

import com.eatthepath.jvptree.DistanceFunction;

public class ClusterDistances {

	static public double manhattan(Binding b1, Binding b2, VarExprList vars) {
	    double d = 0.0;
	    for (Var v : vars.getVars()) {
	        Node n1 = b1.get(v);
	        Node n2 = b2.get(v);
	        if (n1 != null && n2 != null && n1.isLiteral() && n2.isLiteral()) {
	            Object val1 = n1.getLiteralValue();
	            Object val2 = n2.getLiteralValue();
	            if (val1 instanceof String && val2 instanceof String) {
	                // Both are vector strings
	                List<Double> vec1 = parseVectorString((String) val1);
	                List<Double> vec2 = parseVectorString((String) val2);
	                int size = Math.min(vec1.size(), vec2.size());
	                for (int i = 0; i < size; i++) {
	                    d += Math.abs(vec1.get(i) - vec2.get(i));
	                }
	            } else if (val1 instanceof Number && val2 instanceof Number) {
	                // Both are numbers
	                d += Math.abs(((Number) val1).doubleValue() - ((Number) val2).doubleValue());
	            }
	        }
	    }
	    return d;
	}

	
	static public List<Double> parseVectorString(String vectorString) {

	    vectorString = vectorString.replaceAll("\\[", "").replaceAll("\\]", "").trim();
	    String[] parts = vectorString.split(",");

	    List<Double> vector = new ArrayList<>();
	    for (String part : parts) {

	        part = part.trim();
	  
	        if (part.startsWith("\"")) {
	            part = part.substring(1);
	        }
	        if (part.endsWith("\"")) {
	            part = part.substring(0, part.length() - 1);
	        }

	        vector.add(Double.parseDouble(part));
	    }
	    return vector;
	}
	
	public static DistanceFunction<Binding> generateManhattan(VarExprList clusterVars) {
		return new DistanceFunction<Binding>() {

			@Override
			public double getDistance(Binding firstPoint, Binding secondPoint) {
				double res = 0;
				for(Var v : clusterVars.getVars()) {
					Node x = firstPoint.get(v);
					Node y = secondPoint.get(v);
					res += Math.abs(((Number)x.getLiteralValue()).doubleValue()-((Number)y.getLiteralValue()).doubleValue());
				}
				return res;
			}
		};
	}
	
	
	
	
}
