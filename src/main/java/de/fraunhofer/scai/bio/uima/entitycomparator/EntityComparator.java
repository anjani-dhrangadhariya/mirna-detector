package de.fraunhofer.scai.bio.uima.entitycomparator;

import org.apache.uima.jcas.tcas.Annotation;
/**
 * @author TODO: Mehdi Ali
 */
public interface EntityComparator {

	public boolean equals(Annotation a1, Annotation a2);
	
}
