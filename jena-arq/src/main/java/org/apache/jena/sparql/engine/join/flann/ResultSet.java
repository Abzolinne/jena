package org.apache.jena.sparql.engine.join.flann;

public interface ResultSet {
	public boolean full();
	public void addPoint (double distance, int index);
	public double worstDistance();
}
