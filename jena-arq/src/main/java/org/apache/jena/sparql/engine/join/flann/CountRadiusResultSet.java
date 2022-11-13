package org.apache.jena.sparql.engine.join.flann;



/**
 * This result-set only counts the neighbors within a specified radius.
 */
public class CountRadiusResultSet implements ResultSet {
	double radius;
	int count;

	public CountRadiusResultSet (double radius) {
		this.radius = radius;
		clear ();
	}

	public void clear () {
		count = 0;
	}

	public int size () {
		return count;
	}

	@Override
	public boolean full () {
		return true;
	}

	@Override
	public void addPoint (double distance, int index) {
		if (distance < worstDistance())
			count++;
	}

	@Override
	public double worstDistance () {
		return radius;
	}
}