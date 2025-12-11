package org.apache.jena.sparql.engine.join.solver;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;

import com.github.jelmerk.hnswlib.core.SearchResult;
import com.github.jelmerk.hnswlib.core.hnsw.HnswIndex;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.join.QueryIterIndexSimJoin;
import org.apache.jena.sparql.engine.join.QueryIterSimJoin;
import org.apache.jena.sparql.engine.join.VecItem;
import org.apache.jena.sparql.engine.join.VectorIndex;
import org.apache.jena.sparql.engine.join.VectorIndexFactory;
import org.apache.jena.sparql.engine.join.QueryIterSimJoin.Neighbor;
import org.apache.jena.sparql.expr.Expr;

public class IndexSimJoinSolver extends SimJoinSolver {

	private HnswIndex<String,float[],VecItem,Float> index;
	protected Queue<Neighbor<Binding>> cache = new PriorityQueue<Neighbor<Binding>>(Neighbor.comparator);
	protected Binding currentSource = null;
	private Var rightEntityVar;
	
	public IndexSimJoinSolver(QueryIterSimJoin simjoin) {
		super(simjoin);
		bindingIterator = simjoin.getRightRows().iterator();
	}

	@Override
    public void setUp() {
		//cargar indice
        QueryIterIndexSimJoin it = ((QueryIterIndexSimJoin) simjoin); 
        String indexPath = it.getIndex();
        try (FileInputStream fis = new FileInputStream(indexPath);
        		ObjectInputStream ois = new ObjectInputStream(fis)){
        	Object o = ois.readObject();
        	this.index = (HnswIndex<String, float[], VecItem, Float>) o;
        } catch (Exception e) {
			e.printStackTrace();
        }
        Var vectorVar = simjoin.getRightAttributes().get(0).asVar();
        this.rightEntityVar = findEntityVar(vectorVar);
    }
	
	@Override
	public Binding nextBinding() {
		if (cache.isEmpty() && simjoin.getLeft().hasNext()) {
			currentSource = simjoin.getLeft().nextBinding();
			getNextBatch(currentSource);
		}
		Binding r = consolidateKNN(currentSource, cache.poll(), simjoin.getVar());
		return r;
	}

	@Override
	public boolean hasNextBinding() {
		boolean b = (!cache.isEmpty()) || simjoin.getLeft().hasNext();
		return b;
	}
	
	public void getNextBatch(Binding l) {

	    QueryIterIndexSimJoin indexSimJoin = (QueryIterIndexSimJoin) simjoin;

	    float[] query = (float[]) l.get(simjoin.getLeftAttributes().get(0).asVar()).getLiteralValue();

	    int k1 = indexSimJoin.getK() + 1;
	    List<SearchResult<VecItem, Float>> res = index.findNearest(query, k1);
	    for (int i = 1; i < res.size(); i++) {
	        SearchResult<VecItem, Float> r = res.get(i);
	        String id = r.item().id();
	        Float d = r.distance();
	        cache.add(buildNeighborBinding(id, d));
	    }
	}
	private Neighbor<Binding> buildNeighborBinding(String id, Float distance) {
        // Crear nodo desde el ID
        Node entityNode = NodeFactory.createURI(id);
        
        // Crear binding mínimo con solo la entidad
        Binding binding = BindingFactory.binding(rightEntityVar, entityNode);
        
        return new Neighbor<>(binding, distance);
    }
	private Var findEntityVar(Var vectorVar) {
        Iterator<Binding> rightRows = simjoin.getRightRows().iterator();
        if (rightRows.hasNext()) {
            Binding sample = rightRows.next();
            Iterator<Var> vars = sample.vars();
            while (vars.hasNext()) {
                Var v = vars.next();
                if (!v.equals(vectorVar)) {
                    return v;
                }
            }
        }
        throw new RuntimeException("No se pudo identificar la variable de entidad");
    }
}
