package org.apache.jena.example.basic;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.apache.jena.datatypes.VectorDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.SIM;

import com.github.jelmerk.hnswlib.core.DistanceFunctions;
import com.github.jelmerk.hnswlib.core.Index;
import com.github.jelmerk.hnswlib.core.SearchResult;
import com.github.jelmerk.hnswlib.core.hnsw.HnswIndex;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.jena.example.basic.VectorUtils.normalize;

public class FastText {

    private static final String WORDS_FILE_URL = "https://dl.fbaipublicfiles.com/fasttext/vectors-crawl/cc.en.300.vec.gz";
    private static final Path TMP_PATH = Paths.get(System.getProperty("java.io.tmpdir"));

    // -------------------------------------------------------------------------
    // VectorStore: persistencia binaria simple
    // -------------------------------------------------------------------------

    static final class VectorStore {

        private final Path path;
        private final int dim;

        VectorStore(Path path, int dim) {
            this.path = path;
            this.dim = dim;
        }

        void save(List<Word> words) throws IOException {
            try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(path))) {
                out.writeInt(dim);
                out.writeInt(words.size());

                for (Word w : words) {
                    out.writeUTF(w.id());
                    float[] v = w.vector();
                    for (float f : v) out.writeFloat(f);
                }
            }
        }
        public List<Word> loadBinary() throws IOException {
            try (DataInputStream in = new DataInputStream(Files.newInputStream(this.path))) {
                int dim = in.readInt();
                int count = in.readInt();

                List<Word> words = new ArrayList<>(count);

                for (int i = 0; i < count; i++) {
                    String id = in.readUTF();
                    float[] v = new float[dim];
                    for (int j = 0; j < dim; j++) {
                        v[j] = in.readFloat();
                    }
                    words.add(new Word(id, v));
                }

                return words;
            }
        }
    }
    // -------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {

        Path file = TMP_PATH.resolve("cc.en.300.vec.gz");
        Model model;
        int limit = 100;
       if (!Files.exists(file)) {
            downloadFile(WORDS_FILE_URL, file);
        } else {
            System.out.printf("Input file already downloaded. Using %s%n", file);
       }
       model = loadModelVectors(file, limit);
       model.setNsPrefix("sim", "http://sim.dcc.uchile.cl/");
       model.setNsPrefix("ex", "http://example.org/");
       //model.setNsPrefix("sj",  "http://sj.dcc.uchile.cl/sim#");
       try (OutputStream out = Files.newOutputStream(Paths.get("output100.ttl"))){
               System.out.printf("Saving file");
               model.write(out, "TURTLE");
       }
    }

    private static void downloadFile(String url, Path path) throws IOException {
        System.out.printf("Downloading %s to %s. This may take a while.%n", url, path);
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, path);
        }
    }
    private static Model loadModelVectors(Path path, int limit) throws IOException {
       System.out.printf("Loading words from %s%n", path);

       try (BufferedReader reader = new BufferedReader( new InputStreamReader(
                       new GZIPInputStream(Files.newInputStream(path)),
                       StandardCharsets.UTF_8))) {
               var stream = reader.lines().skip(1);
               Model model = ModelFactory.createDefaultModel();
               Property hasVector = model.createProperty("http://sim.dcc.uchile.cl/hasVector");
               if (limit > 0) {
                       stream = stream.limit(limit);
               }
               stream
                       .forEach(line -> {
                                       String[] tokens = line.split(" ");
                                       String word = tokens[0];
                                       String[] vectorString = Arrays.copyOfRange(tokens, 1, tokens.length);
                                       Resource r = model.createResource("http://example.org/" + word);
                    String lexical = String.join(" ", vectorString);
                    Literal lit = model.createTypedLiteral(lexical, VectorDatatype.vectorDatatype);
                    model.add(r, hasVector, lit);
                               });
               return model;
       }
    }
    private static List<Word> loadWordVectors(Path path, int limit) throws IOException {
        System.out.printf("Loading words from %s%n", path);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
        System.out.printf("Loading words from %s%n", path);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(Files.newInputStream(path)),
                StandardCharsets.UTF_8))) {
               var stream = reader.lines().skip(1);
               if (limit > 0) {
                       stream = stream.limit(limit);
               }
            return stream
                    .map(line -> {
                        String[] tokens = line.split(" ");
                        String word = tokens[0];
                        float[] vector = new float[tokens.length - 1];
                        for (int i = 1; i < tokens.length; i++) {
                            vector[i - 1] = Float.parseFloat(tokens[i]);
                        }

                        return new Word(word, normalize(vector));
                    })
                    .collect(Collectors.toList());
        }
    }
}