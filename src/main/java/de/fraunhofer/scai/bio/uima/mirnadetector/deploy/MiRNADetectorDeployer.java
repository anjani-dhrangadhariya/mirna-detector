/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package de.fraunhofer.scai.bio.uima.mirnadetector.deploy;

import de.fraunhofer.scai.bio.uima.core.deploy.AbstractMessageDeployer;
import de.fraunhofer.scai.bio.uima.core.util.DescriptionFactory;
import de.fraunhofer.scai.bio.uima.mirnadetector.annotator.MiRNADetector;

/**
 * Main Class that runs a Collection Processing Engine (CPE). This class reads a
 * CPE Descriptor as a command-line argument and instantiates the CPE. It also
 * registers a callback listener with the CPE, which will print progress and
 * statistics to logger.
 */
public class MiRNADetectorDeployer extends AbstractMessageDeployer {

	/**
	 * Main
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		MiRNADetectorDeployer deployer = new MiRNADetectorDeployer();

		deployer.parseArguments(args, null);

		// --> tagger
		deployer.setAnalysisEngineDescription(
        		DescriptionFactory.createAnalysisEngineDescription(MiRNADetector.class)
				);

		deployer.deploy();

	}

	/* (non-Javadoc)
	 * @see de.fraunhofer.scai.bio.msa.MicroService#getServiceDescription()
	 */
	@Override
	public String getServiceDescription() {
		return "tags and normalizes mentionings of miRNA";
	}

	/* (non-Javadoc)
	 * @see de.fraunhofer.scai.bio.msa.MicroService#getServiceConfiguration()
	 */
	@Override
	public String getServiceConfiguration() {
		return "no specific paramaters configured";
	}
}
