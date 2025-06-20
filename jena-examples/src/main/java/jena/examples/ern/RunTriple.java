package jena.examples.ern;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.sim.VectorDatatype;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;

import java.io.File;
import java.io.InputStream;

public class RunTriple {
    public static void main(String[] args) {

        Model model = ModelFactory.createDefaultModel();
        String resourcePath = "src/main/resources/jena/examples/ern/data.ttl";
        System.out.println("📁 Ruta real del TTL: " + new File(resourcePath).getAbsolutePath());
        InputStream in = FileManager.getInternal().open(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("Archivo TTL no encontrado: " + resourcePath);
        }

        model.read(in, null, "TTL");

        System.out.println("📦 Modelo cargado:");
        model.write(System.out, "TTL");

        System.out.println("\n🔍 Resultado de la consulta SPARQL con SIZE():");

        String queryStr = """
                PREFIX ex: <http://example.org/>
PREFIX sim: <http://sim.dcc.uchile.cl/>

SELECT ?person ?vector ?size
WHERE {
  ?person sim:hasVector ?vector .
  BIND(SIZE(?vector) AS ?size)
  FILTER(?size > 3)
}
ORDER BY DESC(?size)
            """;

        Query query = QueryFactory.create(queryStr, Syntax.syntaxSPARQL_12);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            ResultSetFormatter.out(System.out, results, query);
        } catch (Exception e) {
            System.err.println("❌ Error ejecutando la consulta: " + e.getMessage());
            e.printStackTrace();
        }
    }
}