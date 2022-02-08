package org.apache.jena.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class SIM {

	/** <p>The RDF model that holds the vocabulary terms</p> */
    private static final Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string ({@value})</p> */
    public static final String NS = "http://sj.dcc.uchile.cl/sim#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>The k-medoids clustering algorithm.</p> */
    public static final Resource kmedoids = m_model.createResource( "http://sj.dcc.uchile.cl/sim#kmedoids" );
    
    /** <p>The k-medoids clustering algorithm.</p> */
    public static final Resource kmeans = m_model.createResource( "http://sj.dcc.uchile.cl/sim#kmeans" );
    
    /** <p>The k-medoids clustering algorithm.</p> */
    public static final Resource dbscan = m_model.createResource( "http://sj.dcc.uchile.cl/sim#dbscan" );
    
    /** <p>The number of clusters parameter.</p> */
    public static final Property nbOfClusters = m_model.createProperty( "http://sj.dcc.uchile.cl/sim#numberOfClusters" );
    
    /** <p>The maximum number of kmeans iterations.</p> */
    public static final Property maxIterations = m_model.createProperty( "http://sj.dcc.uchile.cl/sim#maxIterations" );
    
    /** <p>The maximum number of kmeans iterations.</p> */
    public static final Property minDistance = m_model.createProperty( "http://sj.dcc.uchile.cl/sim#minDistance" );
    
    /** <p>The maximum number of kmeans iterations.</p> */
    public static final Property minPoints = m_model.createProperty( "http://sj.dcc.uchile.cl/sim#minPoints" );
	
}
