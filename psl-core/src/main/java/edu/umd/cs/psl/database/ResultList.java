/*
 * This file is part of the PSL software.
 * Copyright 2011-2015 University of Maryland
 * Copyright 2013-2015 The Regents of the University of California
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.umd.cs.psl.database;

import edu.umd.cs.psl.model.formula.Formula;
import edu.umd.cs.psl.model.term.Constant;
import edu.umd.cs.psl.model.term.Variable;

/**
 * List of substitutions for {@link Variable Variables} in a {@link Formula}.
 */
public interface ResultList {

	/**
	 * @return the number of sets of substitutions in the list
	 */
	public int size();
	
	/**
	 * @return the number of distinct {@link Variable Variables} replaced in
	 *             each substitution
	 */
	public int getArity();
	
	/**
	 * Returns a substitution for a single {@link Variable}
	 * 
	 * @param resultNo  the index of the substitution (from 0 to size-1)
	 * @param var  the Variable that is replaced
	 * @return  the substituted GroundTerm
	 * @throws IllegalArgumentException  if resultNo is out of range or var is invalid
	 */
	public Constant get(int resultNo, Variable var);
	
	/**
	 * Returns a substitution for all {@link Variable Variables}.
	 * <p>
	 * GroundTerms are ordered according to the Variables' first appearances in
	 * a depth-first, left-to-right traversal of the Formula in the DatabaseQuery
	 * that generated this ResultList. 
	 * 
	 * @param resultNo  the index of the substitution (from 0 to size-1)
	 * @return  the substituted GroundTerms
	 * @throws IllegalArgumentException  if resultNo is out of range
	 */
	public Constant[] get(int resultNo);
	
}
