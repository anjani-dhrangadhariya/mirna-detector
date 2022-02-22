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
package de.fraunhofer.scai.bio.uima.mirnadetector.annotator;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.impl.UimaContext_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import de.fraunhofer.scai.bio.extraction.types.meta.provenance.ActivityProvenance;
import de.fraunhofer.scai.bio.extraction.types.text.NamedEntity;
import de.fraunhofer.scai.bio.extraction.types.text.NormalizedNamedEntity;
import de.fraunhofer.scai.bio.extraction.types.text.RegEx;
import de.fraunhofer.scai.bio.uima.core.UIMATestUtils;
import de.fraunhofer.scai.bio.uima.core.deploy.AbstractDeployer;
import de.fraunhofer.scai.bio.uima.core.util.UIMAViewUtils;
import de.fraunhofer.scai.bio.uima.entitycomparator.NamedEntityComparator;
import de.fraunhofer.scai.bio.uima.entitycomparator.NormalizedNamedEntityComparator;


public class MiRNADetectorAnnotatorTest {

	private MiRNADetector annotator; // Could also have been written as 
	private JCas docViewGoldS; //Document View of the gold standard
	private NamedEntityComparator neComparator;
	private NormalizedNamedEntityComparator nneComparator;
	private int numberOfNEs; //Number of NEs in gold standard
	private int numberOfNNEs;
	private int numberOfRegEXs;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	
	@Before
	public void setUp() throws Exception {
		annotator = new MiRNADetector(); // Annotator from class MiRNADetector
		JCas aJCasGoldS = UIMATestUtils.createCas(getClass().getResource("/positiveOutGS/").getPath()).getJCas();
		docViewGoldS = UIMAViewUtils.getOrCreatePreferredView(aJCasGoldS, AbstractDeployer.VIEW_DOCUMENT);
		neComparator = new NamedEntityComparator();
		nneComparator = new NormalizedNamedEntityComparator();
		numberOfNEs = docViewGoldS.getAnnotationIndex(NamedEntity.type).size();
		numberOfNNEs = docViewGoldS.getAnnotationIndex(NormalizedNamedEntity.type).size();	
		numberOfRegEXs = docViewGoldS.getAnnotationIndex(RegEx.type).size();
	}

	
	@Test
	public void testRun() throws Exception {
	
		JCas aJCasInput = buildJCas();
		
		try {
			annotator.run(aJCasInput);
		} catch (Exception e) {
			fail("No exception should have been thrown.");
		}
	}

	
	/**Tests, whether matches are found 
	 * and correct named entities are created **/
	@Test
	public void testCreateNEs() throws Exception{
		
		JCas docViewInput = buildJCas();
		FSIterator<Annotation> goldIterator = docViewGoldS.getAnnotationIndex(NamedEntity.type).iterator();
		int count = 0;

		Assert.assertEquals(docViewInput.getAnnotationIndex(NamedEntity.type).size(), docViewGoldS.getAnnotationIndex(NamedEntity.type).size());
		//Iterate over all NEs in the gold standard
		while(goldIterator.hasNext()){
			
			FSIterator<Annotation> inputIterator = docViewInput.getAnnotationIndex(NamedEntity.type).iterator();
			
			NamedEntity neGold = (NamedEntity) goldIterator.next();
			
			//Check, whether the NE from gold standard is contained in the JCas built from the input file
			//Iterate over all NEs in input JCas
			while(inputIterator.hasNext()){
				
				NamedEntity neInput = (NamedEntity) inputIterator.next();
				
				if(neComparator.equals(neGold, neInput)){
					
					count++;
					
				}
				
			}
			
		}
		
		/* If count = number of found NEs, then all expected NEs were found
		 * If not there are errors */
		Assert.assertEquals("The expected entities are not contained", numberOfNEs, count);
	}
	

	@Test
	public void testCreateNNEs() throws Exception{
		
		JCas docViewInput = buildJCas();
		FSIterator<Annotation> goldIterator = docViewGoldS.getAnnotationIndex(NormalizedNamedEntity.type).iterator();
		int count = 0;

		Assert.assertTrue(docViewInput.getAnnotationIndex(NormalizedNamedEntity.type).size() == docViewGoldS.getAnnotationIndex(NormalizedNamedEntity.type).size());
		
		//Iterate over all NEs in the gold standard
		while(goldIterator.hasNext()){
			
			FSIterator<Annotation> inputIterator = docViewInput.getAnnotationIndex(NormalizedNamedEntity.type).iterator();
			
			NormalizedNamedEntity nneGold = (NormalizedNamedEntity) goldIterator.next();
			
			//Check, whether the NE from gold standard is contained in the JCas built from the input file
			//Iterate over all NEs in input JCas
			while(inputIterator.hasNext()){
				
				NormalizedNamedEntity nneInput = (NormalizedNamedEntity) inputIterator.next();
				
				if(nneComparator.equals(nneGold, nneInput)){
					
					count++;
					
				}
				
			}
			
		}
		
		/* If count = number of found NNEs, then all expected NNEs were found
		 * If not there are errors */
		
		Assert.assertEquals("The expected entities are not contained", numberOfNEs, count);
	}
	
	
	@Test
	public void testCreateRegEXs() throws Exception{
		
		JCas docViewInput = buildJCas();
		FSIterator<Annotation> goldIterator = docViewGoldS.getAnnotationIndex(RegEx.type).iterator();
		int count = 0;

		Assert.assertTrue(docViewInput.getAnnotationIndex(RegEx.type).size() == docViewGoldS.getAnnotationIndex(RegEx.type).size());

		//Iterate over all NEs in the gold standard
		while(goldIterator.hasNext()){
			
			FSIterator<Annotation> inputIterator = docViewInput.getAnnotationIndex(RegEx.type).iterator();
			
			RegEx reGold = (RegEx) goldIterator.next();
			
			//Check, whether the NE from gold standard is contained in the JCas built from the input file
			//Iterate over all NEs in input JCas
			while(inputIterator.hasNext()){
				
				RegEx reInput = (RegEx) inputIterator.next();
				
				if((reInput.getBegin() == reGold.getBegin()) && (reInput.getEnd() == reGold.getEnd())){
					
					count++;
					
				}
				
			}
			
		}
		
		/* If count = number of found NNEs, then all expected NNEs were found
		 * If not there are errors */
		Assert.assertEquals("The expected entities are not contained", numberOfRegEXs, count);
	}

	/**Create a JCas of object of the input file**/
	private JCas buildJCas() throws Exception{
		JCas aJCasInput = UIMATestUtils.createCasFromInputFolder(getClass().getResource("/positiveIn/").getPath()).getJCas();

		ActivityProvenance ac = new ActivityProvenance(aJCasInput);
		ac.setComponentId("");
		
		//Use Mockito, because activity provenance and context is needed
		annotator = spy(annotator);
		UimaContext context = Mockito.mock(UimaContext_ImplBase.class);
        doReturn(context).when(annotator).getUIMAContext();
        doReturn(ac).when(annotator).getActivityProvenance();
		
        annotator.init();
        
        annotator.run(aJCasInput);
		
        //return the document view of the JCas object
		return UIMAViewUtils.getOrCreatePreferredView(aJCasInput,AbstractDeployer.VIEW_DOCUMENT); 
	}
}
