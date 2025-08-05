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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.jena.datatypes;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.jena.datatypes.BaseDatatype ;

public class VectorDatatype extends BaseDatatype {
	
	public static final String URI = "http://sim.dcc.uchile.cl/vector";
	
	public static final VectorDatatype vectorDatatype = new VectorDatatype();
	
	public VectorDatatype() {
		super(URI);
	}
	
	@Override 
	public Object parse(String lexicalForm) {
		return Arrays.stream(lexicalForm.replaceAll("[\\[\\]]", "").trim().split("\\s+"))
				.map(Double::parseDouble)
				.collect(Collectors.toList());
	}
	
	@Override
	public boolean isValid(String lexicalForm) {
		try {
			parse(lexicalForm);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}