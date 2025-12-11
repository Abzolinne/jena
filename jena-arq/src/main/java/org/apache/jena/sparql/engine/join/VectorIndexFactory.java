package org.apache.jena.sparql.engine.join;

import org.apache.jena.sparql.ARQException;

public class VectorIndexFactory {
	public static VectorIndex load(String path) {
		if (path.endsWith(".hnsw")) {
			return new HnswIndex();
		}
		throw new ARQException("Unsopported index type: "+ path);
	}
}
