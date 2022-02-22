package de.fraunhofer.scai.bio.uima.entitycomparator;

import org.apache.uima.jcas.tcas.Annotation;

import de.fraunhofer.scai.bio.extraction.types.text.NamedEntity;
/**
 * @author TODO: Mehdi Ali
 */
public class NamedEntityComparator implements EntityComparator{

	
	public boolean equals(Annotation a1, Annotation a2) {
		
		NamedEntity ne1 = (NamedEntity) a1;
		NamedEntity ne2 = (NamedEntity) a2;
		
		//Check begin and end of the named entity annotations
		if((ne1.getBegin() == ne2.getBegin()) && (ne1.getEnd() == ne2.getEnd())){
					
			return true;
		}
				
		return false;
	}

}
