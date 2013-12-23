/*
 * This file is part of the PSL software.
 * Copyright 2011-2013 University of Maryland
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
package edu.umd.cs.psl.application.learning.weight.em;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.psl.application.learning.weight.TrainingMap;
import edu.umd.cs.psl.application.learning.weight.maxlikelihood.VotedPerceptron;
import edu.umd.cs.psl.application.util.Grounding;
import edu.umd.cs.psl.config.ConfigBundle;
import edu.umd.cs.psl.config.ConfigManager;
import edu.umd.cs.psl.database.Database;
import edu.umd.cs.psl.model.Model;
import edu.umd.cs.psl.model.atom.ObservedAtom;
import edu.umd.cs.psl.model.atom.RandomVariableAtom;
import edu.umd.cs.psl.model.kernel.linearconstraint.GroundValueConstraint;
import edu.umd.cs.psl.reasoner.ReasonerFactory;

/**
 * Abstract superclass for implementations of the expectation-maximization
 * algorithm for learning with latent variables.
 * <p>
 * This class extends {@link VotedPerceptron}, which is used during the M-step
 * to update the weights.
 * 
 * @author Stephen Bach <bach@cs.umd.edu>
 */
abstract public class ExpectationMaximization extends VotedPerceptron {

	private static final Logger log = LoggerFactory.getLogger(ExpectationMaximization.class);
	
	/**
	 * Prefix of property keys used by this class.
	 * 
	 * @see ConfigManager
	 */
	public static final String CONFIG_PREFIX = "em";
	
	/**
	 * Key for positive int property for the number of iterations of expectation
	 * maximization to perform
	 */
	public static final String ITER_KEY = CONFIG_PREFIX + ".iterations";
	/** Default value for ITER_KEY property */
	public static final int ITER_DEFAULT = 10;
	
	protected final int iterations;
	
	protected List<GroundValueConstraint> labelConstraints;
	
	public ExpectationMaximization(Model model, Database rvDB,
			Database observedDB, ConfigBundle config) {
		super(model, rvDB, observedDB, config);
		
		iterations = config.getInt(ITER_KEY, ITER_DEFAULT);
	}

	@Override
	protected void doLearn() {
		for (int round = 0; round < iterations; round++) {
			log.debug("Beginning EM round {} of {}", round+1, iterations);
			/* E-step */
			minimizeKLDivergence();
			/* M-step */
			super.doLearn();
		}
	}

	abstract protected void minimizeKLDivergence();

	@Override
	protected void initGroundModel()
			throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		trainingMap = new TrainingMap(rvDB, observedDB);
		reasoner = ((ReasonerFactory) config.getFactory(REASONER_KEY, REASONER_DEFAULT)).getReasoner(config);
		Grounding.groundAll(model, trainingMap, reasoner);
		
		/* Creates constraints to fix labeled random variables to their true values */
		labelConstraints = new ArrayList<GroundValueConstraint>();
		for (Map.Entry<RandomVariableAtom, ObservedAtom> e : trainingMap.getTrainingMap().entrySet())
			labelConstraints.add(new GroundValueConstraint(e.getKey(), e.getValue().getValue()));
	}
	
	public void inferLatentVariables() {
		/* Adds constraints to fix values of labeled random variables */
		for (GroundValueConstraint con : labelConstraints)
			reasoner.addGroundKernel(con);
		
		/* Infers most probable assignment latent variables */
		reasoner.optimize();
		
		/* Removes constraints */
		for (GroundValueConstraint con : labelConstraints)
			reasoner.removeGroundKernel(con);
	}

}