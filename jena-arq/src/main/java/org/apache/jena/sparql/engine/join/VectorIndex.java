package org.apache.jena.sparql.engine.join;

public interface VectorIndex {

    int[] knnSearch(float[] query, int k);

    int[] radiusSearch(float[] query, double radius);

    float distance(float[] a, float[] b);

    int dims();

    void close();
}
