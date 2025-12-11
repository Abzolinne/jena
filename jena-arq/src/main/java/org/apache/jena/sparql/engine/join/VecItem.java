package org.apache.jena.sparql.engine.join;

import com.github.jelmerk.hnswlib.core.Item;

public class VecItem implements Item<String, float[]> {
	private static final long serialVersionUID = 1L;
	private final String key;
    private final float[] vector;
    private final int dimensions;

    public VecItem(String key, float[] vector) {
        this.key = key;
        this.vector = vector;
        this.dimensions = vector.length;
    }

    public float[] vector() {
        return vector;
    }

    public int dimensions() {
        return dimensions;
    }
	@Override
	public String id() {
		return key;
	}
}