package org.apache.jena.sparql.engine.join.flann;

public interface Metric {
	public double distance(double[] a, double[] b);

	public double distance(double a, double b);

	public int distance(int[] a, int[] b);

	public int distance(int a, int b);
}
