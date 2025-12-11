package arq.examples.index;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.io.FileOutputStream;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.sys.JenaSystem;

import com.github.jelmerk.hnswlib.core.DistanceFunctions;
import com.github.jelmerk.hnswlib.core.hnsw.HnswIndex;

import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.engine.join.VecItem;

public class Index {
	
	static { JenaSystem.init(); }
	
	public static List<VecItem> getVector(Model model) {
	    Property has_vector = model.createProperty("http://sim.dcc.uchile.cl/hasVector");
	    List<VecItem> results = new ArrayList<>();

	    StmtIterator it = model.listStatements(null, has_vector, (RDFNode) null);
	    while (it.hasNext()) {
	        Statement stmt = it.nextStatement();
	        Resource r = stmt.getSubject();
	        if (r.isAnon()) continue;
	        
	        float[] vec = (float[]) stmt.getObject().asLiteral().getValue();
	        results.add(new VecItem(r.getURI(), vec));
	    }
	    return results;
	}



	public static void main(String[] args) throws Exception {
		ARQ.init();
		String resourcePath = "src/main/resources/jena/examples/arq/testvec.ttl";
		Model model = ModelFactory.createDefaultModel();
		model.read(resourcePath);
		File f = new File("index.bin");
		if (f.exists()) {
			List<VecItem> vector = getVector(model);
			int dim = vector.get(0).dimensions();
			HnswIndex<String, float[], VecItem, Float> hnswIndex =
			        HnswIndex.newBuilder(dim, DistanceFunctions.FLOAT_EUCLIDEAN_DISTANCE, vector.size())
			            .withM(16)
			            .withEf(200)
			            .withEfConstruction(200)
			            .build();
			hnswIndex.addAll(vector);
			try (FileOutputStream fos = new FileOutputStream("index.bin")) {hnswIndex.save(fos);}
		}

		String indexPath = f.getAbsolutePath().replace("\\","\\\\");
		//Dataset ds = DatasetFactory.wrap(model);
		//Context c = ARQ.getContext().copy();
		//c.set(SimKeys.SIM_INDEX, hnswIndex);
		//ds.getContext().putAll(c);
		Dataset ds = DatasetFactory.wrap(model);
		String queryStr = """
					PREFIX ex: <http://example.org/>
					PREFIX sim: <http://sim.dcc.uchile.cl/> 
					 
					SELECT ?x ?y ?d
					WHERE {
					    ?x sim:hasVector ?vx .
					
					    similarity join on (?vx) (?vy)
					        top 1
					        index '%s'
					        distance <http://sj.dcc.uchile.cl/sim#euclidean> as ?d
					    {
					        ?y sim:hasVector ?vy .
					    }
					}

				""".formatted(indexPath);
		Query q = QueryFactory.create(queryStr, Syntax.syntaxSPARQL_11_sim);
		try (QueryExecution qexec = QueryExecutionFactory.create(q,ds)) {
		    ResultSet rs = qexec.execSelect();
		    ResultSetFormatter.out(System.out,rs);
		}
	}

}
