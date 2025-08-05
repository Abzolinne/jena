/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.sparql.expr;

import java.util.List;

import org.apache.jena.sparql.expr.nodevalue.NodeFunctions ;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.sse.Tags ;
import org.apache.jena.graph.Node;

public class E_Size extends ExprFunction1 {
	
	private static final String symbol = Tags.tagSize ;

    public E_Size(Expr expr)
    {
        super(expr, symbol) ;
    }
    
	@Override
	public NodeValue eval(NodeValue v) {
		Node node = v.asNode();
		Object value = node.getLiteralValue();
		if (value instanceof List) {
            int size = ((List<?>) value).size();
            return NodeValue.makeInteger(size);
        } else {
            throw new IllegalArgumentException("Literal no contiene un vector válido. Valor: " + value);
        }
	} 
    
    @Override
    public Expr copy(Expr expr) { return new E_Size(expr) ; }

}