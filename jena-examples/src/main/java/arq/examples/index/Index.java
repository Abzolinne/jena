package arq.examples.index;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter; 


public class Index {
	public static void main(String[] args) throws Exception {
		ARQ.init();
		String resourcePath = "src/main/resources/jena/examples/arq/testvec.ttl";
		Model model = ModelFactory.createDefaultModel();
		model.read(resourcePath);
		/**
		List<VecItem> items = new ArrayList<>();
		Property hasVector = model.createProperty("http://sim.dcc.uchile.cl/hasVector");
		StmtIterator it = model.listStatements(null, hasVector, (RDFNode) null);
		while (it.hasNext()) {
		    Statement st = it.next();
		    Resource subject = st.getSubject();
		    RDFNode obj = st.getObject();

		    String key = subject.getURI();
		    String literal = obj.asLiteral().getString();
		    float[] vec = parse(literal);

		    items.add(new VecItem(key, vec));
		}
		int dim = items.get(0).dimensions();

		HnswIndex<String, float[], VecItem, Float> hnswIndex =
		        HnswIndex
		            .newBuilder(dim, DistanceFunctions.FLOAT_INNER_PRODUCT, items.size())
		            .withM(16)
		            .withEf(200)
		            .withEfConstruction(200)
		            .build();
		hnswIndex.addAll(items);
		Dataset ds = DatasetFactory.wrap(model);
		Context c = ARQ.getContext().copy();
		c.set(SimKeys.SIM_INDEX, hnswIndex);
		ds.getContext().putAll(c);
				String queryStr = """
PREFIX : <http://example.org/>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>

SELECT ?p1 ?name1 ?p2 ?name2 ?similarity
WHERE {
  SIMILARITY JOIN ON (?name1) (?name2)
  TOP 10
  DISTANCE :levenshteinW
  AS ?similarity
  {
    ?p1 a foaf:Person ;
        foaf:name ?name1 .
    ?p2 a foaf:Person ;
        foaf:name ?name2 .
    FILTER(?p1 != ?p2)
  }
}
ORDER BY ?similarity	
						""";
		
		*/
		Dataset ds = DatasetFactory.wrap(model);
		String queryStr = """
					PREFIX ex: <http://example.org/>
					PREFIX sim: <http://sim.dcc.uchile.cl/> 
					 
SELECT ?x ?y ?d
WHERE {
    ?x sim:vec ?vx .
    ?x sim:age ?ax .

    similarity join on (?vx , ?ax) (?vy , ?ay)
        top 1
        distance <http://sj.dcc.uchile.cl/sim#euclidean> as ?d
    {
        ?y sim:vec ?vy .
        ?y sim:age ?ay .
    }
}

				""";
		Query q = QueryFactory.create(queryStr, Syntax.syntaxSPARQL_11_sim);
		try (QueryExecution qexec = QueryExecutionFactory.create(q,ds)) {
		    ResultSet rs = qexec.execSelect();
		    ResultSetFormatter.out(System.out,rs);
		}
	}
	
	public static float[] parse(String literal) {
		String[] tokens = literal.split(" ");
		float[] vector = new float[tokens.length - 1];
        for (int i = 1; i < tokens.length; i++) {
            vector[i - 1] = Float.parseFloat(tokens[i]);
        }
        return vector;
	}
}
