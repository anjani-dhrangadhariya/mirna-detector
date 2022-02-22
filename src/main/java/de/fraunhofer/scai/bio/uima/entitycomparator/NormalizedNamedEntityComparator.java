package de.fraunhofer.scai.bio.uima.entitycomparator;

import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fraunhofer.scai.bio.extraction.types.text.NormalizedNamedEntity;
/**
 * @author TODO: Mehdi Ali
 */
public class NormalizedNamedEntityComparator implements EntityComparator {

	private static final Logger logger = LoggerFactory.getLogger(NormalizedNamedEntityComparator.class);
	@Override
	public boolean equals(Annotation a1, Annotation a2) {
		
		NormalizedNamedEntity nne1 = (NormalizedNamedEntity) a1;
		NormalizedNamedEntity nne2 = (NormalizedNamedEntity) a2;
		String prefLabel1 = nne1.getConcept().getPrefLabel().getValue();
		String prefLabel2 = nne2.getConcept().getPrefLabel().getValue();
		String id1 = nne1.getConcept().getIdentifier();
		String id2 = nne2.getConcept().getIdentifier();
		String idSoruce1 = nne1.getConcept().getIdentifierSource();
		String idSoruce2 = nne2.getConcept().getIdentifierSource();
		
		
		
		//Check begin and end of the normalized named entity annotations
		if((nne1.getBegin() == nne2.getBegin()) && (nne1.getEnd() == nne2.getEnd())){
			logger.info("PrefLabel1: " + prefLabel1 + "  PrefLabel2: " + prefLabel2);
			logger.info("ID1:: " + id1 + "  ID2: " + id2);
			logger.info("IDSource1: " + idSoruce1 + "  IDScoure2: " + idSoruce2);
			//Check preferred Label
			if(prefLabel1.equalsIgnoreCase(prefLabel2)){
							
				//Check identifier
				if(id1.equals(id2)){
								
					//Check identifier source
					if(idSoruce1.equals(idSoruce2)){
									
						return true;
									
					}
				}
			}
		}
		
		return false;
	}
	
}
