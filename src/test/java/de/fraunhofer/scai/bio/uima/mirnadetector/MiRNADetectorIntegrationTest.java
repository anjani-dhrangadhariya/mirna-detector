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
package de.fraunhofer.scai.bio.uima.mirnadetector;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import de.fraunhofer.scai.bio.uima.core.util.UIMAFileUtils;
import de.fraunhofer.scai.bio.uima.core.analysisengine.AbstractAnalysisEngine;
import de.fraunhofer.scai.bio.uima.core.analysisengine.GenericAnnotatorIntegrationTest;
import de.fraunhofer.scai.bio.uima.mirnadetector.annotator.MiRNADetector;
import de.fraunhofer.scai.bio.uima.mirnadetector.deploy.MiRNADetectorDeployer;

public class MiRNADetectorIntegrationTest extends GenericAnnotatorIntegrationTest {

	private MiRNADetector annotator;
	
	@Test
	public void testPositive() throws Exception  {
		String basePath = UIMAFileUtils.getTestCaseBasePath(getClass());
		
		String[] args = {"-log", "ALL", 
				"--sysExit", "false",
	    		"FILE", 
	    		"-i", FilenameUtils.concat(basePath, "positiveIn"),
	    		"-o", FilenameUtils.concat(basePath, "positiveOut")
			};
		
		MiRNADetectorDeployer.main(args);
	}
	
	@Override
	public AbstractAnalysisEngine configureAEforTest() throws Exception {
		annotator = new MiRNADetector();
		return annotator;
	}
}
