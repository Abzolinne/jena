package org.apache.jena.sparql.algebra.op;

import java.util.Objects;

import org.apache.jena.query.cluster.ClusterConfiguration;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.sse.Tags;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class OpCluster extends Op1 {

	private VarExprList clusterVars;
	protected ClusterConfiguration conf;
	protected Var clusterVar;

	public OpCluster(Op subOp) {
		super(subOp);
	}

	public OpCluster(Op op, VarExprList clusterBy, ClusterConfiguration conf, Var clusterVar) {
		super(op);
		this.clusterVars = clusterBy;
		this.conf = conf;
		this.clusterVar = clusterVar;
	}

	@Override
	public void visit(OpVisitor opVisitor) {
		opVisitor.visit(this);
	}

	@Override
	public String getName() {
		return Tags.tagClusterBy;
	}

	@Override
	public Op apply(Transform transform, Op subOp) {
		return transform.transform(this, subOp);
	}

	@Override
	public Op1 copy(Op subOp) {
		return new OpCluster(subOp, clusterVars, conf, clusterVar);
	}

	public VarExprList getClusterVars() {
		return clusterVars;
	}
	
	public Var getClusterVar() {
		return clusterVar;
	}

	@Override
	public int hashCode() {
		int x = getSubOp().hashCode() ;
        if ( clusterVars != null ) 
            x ^= clusterVars.hashCode() ; 
        if ( conf != null ) 
            x ^= conf.hashCode() ; 
        return x ;
	}

	@Override
	public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
		if ( ! (other instanceof OpCluster) ) return false ;
		OpCluster opCluster = (OpCluster)other ;
        if ( ! Objects.equals(clusterVars, opCluster.clusterVars) ) 
            return false ;
        if ( ! Objects.equals(conf, opCluster.conf) )
            return false ;
            
        return getSubOp().equalTo(opCluster.getSubOp(), labelMap) ;
	}

	public static OpCluster create(Op op, VarExprList clusterBy, ClusterConfiguration conf, Var clusterVar) {
		return new OpCluster(op, clusterBy, conf, clusterVar);
	}

	public ClusterConfiguration getClusterConf() {
		return conf;
	}

}
