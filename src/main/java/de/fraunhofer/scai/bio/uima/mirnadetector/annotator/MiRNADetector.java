package de.fraunhofer.scai.bio.uima.mirnadetector.annotator;

import java.util.ArrayList;							
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.fraunhofer.scai.bio.extraction.types.meta.Concept;
import de.fraunhofer.scai.bio.extraction.types.meta.Confidence;
import de.fraunhofer.scai.bio.extraction.types.meta.Label;
import de.fraunhofer.scai.bio.extraction.types.text.NamedEntity;
import de.fraunhofer.scai.bio.extraction.types.text.NormalizedNamedEntity;
import de.fraunhofer.scai.bio.extraction.types.text.RegEx;
import de.fraunhofer.scai.bio.uima.core.analysisengine.AbstractAnalysisEngine;
import de.fraunhofer.scai.bio.uima.core.deploy.AbstractDeployer;
import de.fraunhofer.scai.bio.uima.core.util.UIMAViewUtils;
import de.fraunhofer.scai.bio.uima.resourcefileparsers.*;

/**
 * Annotator class for miRNA detection
 * 
 * Description: The MiRNADetector uses Regular Expressions to search microRNA
 * mentions found in text.Authors use microRNA names as continuous pattern for
 * instance miR-9, miR-137, miR-29a/b-1, MicroRNA-21, miR-17/92, etc. The main
 * divisions of REGEX used here are General class and Specific Regex. In general
 * class, all the mentions relating to microRNA are captured and such mentions
 * do not point out to a single specific microRNA entity. The specific class of
 * REGEX aims to identify miRNA mentions which point to single entity, either
 * miRNA itself or miRNA cluster, group, paralogs, family. Currently
 * MiRNADetector, to some extent identifies single miRNA from miRNA cluster,
 * group, paralogs, family. This is in the cases where the miRNA mentions are
 * flanked on either sides by mentions of "cluster, group, paralogs, family".
 * Preferred label from a Named Entity is generated by manipulating heuristics.
 * These are further normalized to miRBase ID's. 
 * 
 * The Regular Expressions here are built for human miRNAs and miRNAs for other
 * organism may not be captured by it.
 * 
 * @author: Anjani K Dhrangadhariya
 */
public class MiRNADetector extends AbstractAnalysisEngine {

	private String mirBaseURL = "mirbase.org";
	private String mirBaseFamURL = "mirbase.org";
//	private String mirBaseURL = "http://www.mirbase.org/cgi-bin/mirna_entry.pl?acc=";
//	private String mirBaseFamURL = "http://www.mirbase.org/cgi-bin/mirna_summary.pl?fam=";

	private HashMap<String, String> aliasMiRNAmap = null;
	private HashMap<String, String> deadMiRNAmap = null;
	private HashMap<String, String> prefLabelMap = null;
	private HashMap<String, String> miFamMap = null;
	
	@Override
	protected void init() throws Exception {
		
		aliasMiRNAmap = MirnaAliasToHashmap.createAliasMap();
		
		deadMiRNAmap = MiRNAdead2Hashmap.createMirnaDeadMap();
		
		prefLabelMap = Gff3ToHashmap.createHashmap();
		
		miFamMap = MiFam2Hashmap.createMiFamMap();		
	}

	@Override
	protected void run(JCas aJCas) throws Exception {

		/*-----------------------Instead of initial view we use now document view-------------------------*/
		JCas docView = UIMAViewUtils.getOrCreatePreferredView(aJCas,AbstractDeployer.VIEW_DOCUMENT);

		// Retrieving CAS documentText from view (default)
		String docText = docView.getDocumentText();

		if (docText == null) {

			logger.error("documentText is null");
			throw new AnalysisEngineProcessException(AnalysisEngineProcessException.REQUIRED_FEATURE_STRUCTURE_MISSING_FROM_CAS,new Object[] { "documentText" });

		} else if (docText.isEmpty()) {

			logger.error("documentText is empty");

		} else {

			/*------Detect general miRNA-------*/
			findGeneralMiRNA(docView);

			/*---------Detect specific miRNAs---------*/
			findSpecificMiRNA(docView);

			/*------Normalize the found Specific miRNAs----*/
			normalizeMiRNA(docView);
		}
	}

	@Override
	protected void close() {
		// Close the file readers, scanners, etc
	}

	private void findGeneralMiRNA(JCas docView) {

		String docText = docView.getDocumentText();

		// Named entity - General miRNA terms
		find_General_miRNA_terms(docView, docText);

	}

	private void findSpecificMiRNA(JCas docView) {

		String docText = docView.getDocumentText();

		// Named entity generation for Specific miRNA terms
		find_Specific_miRNA_terms_erste(docView, docText);
		find_Specific_miRNA_terms_zweite(docView, docText);
		find_Specific_miRNA_terms_dritte(docView, docText);
	}

	private void normalizeMiRNA(JCas docView) throws Exception {

		FSIterator<Annotation> it = docView.getAnnotationIndex(NamedEntity.type).iterator();
		List<String> linMiRNA = new ArrayList<String>();

		while (it.hasNext()) {
			NamedEntity ne = (NamedEntity) it.next();
			String nneStorage = ne.getCoveredText();
			String nneStoragePrefLabel = null;
			
			Pattern group = Pattern.compile("(.*|)(([cC][lL][uU][sS][tT][eE][rR]|[gG][rR][oO][uU][pP]|[fF][aA][mM][iI][lL][yY]|approximately|[pP][aA][rR][aA][lL][oO][gG][sS]|[aA][nN][tT][aA][gG][oO][mM][iI][rR])).*");
			Matcher groupMatch = group.matcher(nneStorage);
	        boolean matches = groupMatch.matches();

			// Conditionals for primary screening of ReGeX terms
			if (nneStorage != null	&& nneStorage.matches("(([pP][rR]([iI]|[eE])([ \\-][ \\-]*))|([sS][yY][nN][tT][hH][eE][tT][iI][cC] )|([hH][sS][aA]([ \\-][ \\-]*)|[cC][eE][lL]([ \\-][ \\-]*)|[mM][aA][tT][uU][rR][eE]\\s))?(([mM][iI]([cC][rR][oO])?)([ \\-][ \\-]*)?([rR][nN][aA][sS]?)|[oO][nN][cC][oO][mM][iI][rR][sS]?|[aA][nN][gG][iI][oO][mM][iI][rR][sS]?)") == false) {
				
				// The miRNAs which are NOT EXPLICITLY cluster, family, paralogs, antagomirs or groups are processed here!
				if(matches == false) {
					
					if (nneStorage.matches(".*([lL][eE][tT]).*")) {
						if (nneStorage.contains("-")) {
							nneStoragePrefLabel = "hsa-" + nneStorage;
						} else {
							nneStoragePrefLabel = "hsa-" + nneStorage.replaceAll("[lL]et","let-");
						}
					}
					else if (nneStorage.matches(".*([lL]in).*")) {
						// Organism specificity required to normalize miRNA
						// Do not normalize "lin" to hsa- because lin is not a human miRNA
						linMiRNA.add(nneStorage); // Lin terms are stored in this list
					}
					
					else if (nneStorage.matches("[cC][eE][lL].*")) {
						// C. elegans microRNA (Would produce NNE without an ID for C. elegans)
						nneStoragePrefLabel = nneStorage.replaceAll("\\-[mM][iI]R\\-", "\\-mir\\-");
					}
					else if(nneStorage.matches(".*(\\d+)([a-zA-Z])(\\d+)")) {
						// Normalize miR-26a2 kind of entities
						int k = nneStorage.indexOf("-");
						String temp = nneStorage.substring(k);
						Pattern pat = Pattern.compile(".*(\\d+[a-zA-Z])(\\d+)");
						Matcher patMatch = pat.matcher(temp);
						while(patMatch.find()){
							String x = patMatch.group(2);
							int z = patMatch.group(2).length();
							String temp2 = "hsa-mir"+temp;
							nneStoragePrefLabel = temp2.substring(0,temp2.length() - z) + "-" + x;
						}
					}
					else if(nneStorage.matches(".*(~|〜|～|〜|∼).*")) {
						nneStoragePrefLabel = nneStorage;
					}
					else {
						Pattern numPosPattern = Pattern.compile("\\d+");
					    Matcher numPosMatcher = numPosPattern.matcher(nneStorage);
					    
					    if(nneStorage.contains("/") == false){
					    	if(numPosMatcher.find()){
					    		nneStoragePrefLabel = "hsa-mir-"+ nneStorage.substring(numPosMatcher.start());
					    	}
					    }
					    else if(nneStorage.contains("/") == true){					    	
					    	nneStoragePrefLabel = nneStorage; // These would be assumed as clusters with lower conf score
					    }					
					}
				}
		        else if (matches == true){
		        	// process the family group and cluster here!!
					if(nneStorage.matches(".*[fF][aA][mM][iI][lL][yY].*")){
						if(nneStorage.matches(".*([lL][iI][nN]|[lL][eE][tT]).*")){
							nneStoragePrefLabel = nneStorage.replaceAll("[fF][aA][mM][iI][lL][yY]", "").trim();
						}
						else {
							String temp;
							Pattern numPosPattern = Pattern.compile("\\d+");
						    Matcher numPosMatcher = numPosPattern.matcher(nneStorage);
						    temp = nneStorage.replaceAll("[fF][aA][mM][iI][lL][yY]", "").trim();
						    if(numPosMatcher.find()){
						    	nneStoragePrefLabel = "mir-"+temp.substring(numPosMatcher.start());
						    }
						}						
					}
					else {
						// Clusters, groups, paralogs and antagomirs are normalized alike to general miRNA mentions
						nneStoragePrefLabel = nneStorage;
					}
		        }
				
				/*----------------------------- Normalized Named Entity Annotation -------------------*/
				
				if (nneStoragePrefLabel != null && matches == false) {
					if (aliasMiRNAmap.containsKey(nneStoragePrefLabel)) {

						String prefLabelString = nneStoragePrefLabel;
						Label label = new Label(docView);
						label.setLanguageTag("EN");
						label.setValue(prefLabelString);
						label.addToIndexes();

						Concept microRNA = new Concept(docView);
						String identifier = aliasMiRNAmap.get(nneStoragePrefLabel);
						microRNA.setIdentifier(identifier);
						microRNA.setIdentifierSource(getURL());

						microRNA.setPrefLabel(label);
						microRNA.addToIndexes(); // store in CAS

						NormalizedNamedEntity nne = new NormalizedNamedEntity(docView);
						nne.setBegin(ne.getBegin());
						nne.setEnd(ne.getEnd());
						nne.setConcept(microRNA);
						nne.setWasGeneratedBy(this.getActivityProvenance());
						Confidence conf = new Confidence(docView);
						conf.setValue(1.00); // Pseudo value
						nne.setConfidence(conf);
						nne.addToIndexes(); // store in CAS

					} else if (deadMiRNAmap.containsKey(nneStoragePrefLabel)) {
						
						String fetchID = deadMiRNAmap.get(nneStoragePrefLabel);
						String prefLabelString = prefLabelMap.get(fetchID);
						Label label = new Label(docView);
						label.setLanguageTag("EN");
						label.setValue(prefLabelString);
						label.addToIndexes();
						
						Concept microRNA = new Concept(docView);
						String identifier = deadMiRNAmap.get(fetchID);
						microRNA.setIdentifier(identifier);
						microRNA.setIdentifierSource(getURL());

						microRNA.setPrefLabel(label);
						microRNA.addToIndexes(); // store in CAS

						NormalizedNamedEntity nne = new NormalizedNamedEntity(docView);
						nne.setBegin(ne.getBegin());
						nne.setEnd(ne.getEnd());
						nne.setConcept(microRNA);
						nne.setWasGeneratedBy(this.getActivityProvenance());
						Confidence conf = new Confidence(docView);
						conf.setValue(1.00); // Pseudo value
						nne.setConfidence(conf);
						nne.addToIndexes(); // store in CAS
					}
					else if(miFamMap.containsKey(nneStoragePrefLabel.replace("hsa-", ""))){
						// Assume the remaining miRNA mentions as family names and normalize to family ID's
						// in this case remove hsa- and search in Family map
						// Give a lower confidence value to it
						String prefLabelString = nneStoragePrefLabel.replace("hsa-", "");
						Label label = new Label(docView);
						label.setLanguageTag("EN");
						label.setValue(prefLabelString);
						label.addToIndexes();

						Concept microRNAFamily = new Concept(docView);
						String identifier = miFamMap.get(prefLabelString);
						microRNAFamily.setIdentifier(identifier);
						microRNAFamily.setIdentifierSource(getFamURL());

						microRNAFamily.setPrefLabel(label);
						microRNAFamily.addToIndexes(); // store in CAS

						NormalizedNamedEntity nne = new NormalizedNamedEntity(docView);
						nne.setBegin(ne.getBegin());
						nne.setEnd(ne.getEnd());
						nne.setConcept(microRNAFamily);
						nne.setWasGeneratedBy(this.getActivityProvenance());
						Confidence conf = new Confidence(docView);
						conf.setValue(0.50); // NOTE: There are less chances that it is a family because we are just assuming here
						nne.setConfidence(conf);
						nne.addToIndexes(); // store in CAS
					}
					else if(nneStoragePrefLabel.contains("/") || nneStoragePrefLabel.matches(".*[~〜～∼].*") == true){
						String prefLabelString = nneStoragePrefLabel;
						Label label = new Label(docView);
						label.setLanguageTag("EN");
						label.setValue(prefLabelString);
						label.addToIndexes();
						
						String identifier = "mirna_cluster";
						Concept microRNACluster = new Concept(docView);
						
						microRNACluster.setIdentifier(identifier);

						microRNACluster.setPrefLabel(label);
						microRNACluster.addToIndexes(); // store in CAS

						NormalizedNamedEntity nne = new NormalizedNamedEntity(docView);
						nne.setBegin(ne.getBegin());
						nne.setEnd(ne.getEnd());
						nne.setConcept(microRNACluster);
						nne.setWasGeneratedBy(this.getActivityProvenance());
						Confidence conf = new Confidence(docView);
						conf.setValue(0.5); // Pseudo value
						nne.setConfidence(conf);
						nne.addToIndexes(); // store in CAS						
					}
				}
				else if (nneStoragePrefLabel != null && matches == true){
					// This chain of conditions is for clusters, families, groups and paralogs
					if (miFamMap.containsKey(nneStoragePrefLabel)) {
						String prefLabelString = nneStoragePrefLabel;
						Label label = new Label(docView);
						label.setLanguageTag("EN");
						label.setValue(prefLabelString);
						label.addToIndexes();

						Concept microRNAFamily = new Concept(docView);
						String identifier = miFamMap.get(nneStoragePrefLabel);
						microRNAFamily.setIdentifier(identifier);
						microRNAFamily.setIdentifierSource(getFamURL());

						microRNAFamily.setPrefLabel(label);
						microRNAFamily.addToIndexes(); // store in CAS

						NormalizedNamedEntity nne = new NormalizedNamedEntity(docView);
						nne.setBegin(ne.getBegin());
						nne.setEnd(ne.getEnd());
						nne.setConcept(microRNAFamily);
						nne.setWasGeneratedBy(this.getActivityProvenance());
						Confidence conf = new Confidence(docView);
						conf.setValue(1.00); // Pseudo value
						nne.setConfidence(conf);
						nne.addToIndexes(); // store in CAS
					}
					else{
						/*
						 * There is no definite database normalization for clusters yet
						 * It is difficult to normlize clusters because there are is no standard defined for defined miRNA's in a cluster
						 * Look into metamirclust for ambiguity in miRNA cluster defintion
						 */
						
						String prefLabelString = nneStoragePrefLabel;
						Label label = new Label(docView);
						label.setLanguageTag("EN");
						label.setValue(prefLabelString);
						label.addToIndexes();

						String identifier = null;
						Concept microRNACluster = new Concept(docView);
						if(nneStoragePrefLabel.matches(".*[cC][lL][uU][sS][tT][eE][rR].*")) {
							identifier = ("mirna_cluster");
						}
						else if(nneStoragePrefLabel.matches(".*[gG][rR][oO][uU][pP].*")) {
							identifier = ("mirna_group");
						}
						else if(nneStoragePrefLabel.matches(".*[pP][aA][rR][aA][lL][oO][gG].*")) {
							identifier = ("mirna_paralog");
						}
						else if(nneStoragePrefLabel.matches(".*[aA][nN][tT][aA][gG][oO].*")) {
							identifier = ("mirna_antagomir");
						}
						else {
							identifier = "general_mirna"; // TODO: create a better identifier
						}

						microRNACluster.setIdentifier(identifier);
						microRNACluster.setIdentifierSource(getURL()); // TODO: create a better url

						microRNACluster.setPrefLabel(label);
						microRNACluster.addToIndexes(); // store in CAS

						NormalizedNamedEntity nne = new NormalizedNamedEntity(docView);
						nne.setBegin(ne.getBegin());
						nne.setEnd(ne.getEnd());
						nne.setConcept(microRNACluster);
						nne.setWasGeneratedBy(this.getActivityProvenance());
						Confidence conf = new Confidence(docView);
						conf.setValue(1.00); // Pseudo value
						nne.setConfidence(conf);
						nne.addToIndexes(); // store in CAS
					}
				}
			}
		}
	}		

	/* -------------------------------Pattern 1------------------------------ */
	// lin and let terms
	private void find_Specific_miRNA_terms_erste(JCas docView, String S) {
		Matcher specificMiRterms_1 = Pattern.compile("([hH][sS][aA]\\\\-)?(([lL][iI][nN]([ \\\\-])?[4]+[a-z]?)|([lL][eE][tT]([ \\\\-])?[7]+[a-z]?))(\\s[fF][aA][mM][iI][lL][yY])?").matcher(S);

		while (specificMiRterms_1.find()) {
			Confidence conf = new Confidence(docView);
			conf.setValue(1.0); // pseudo value

			NamedEntity ne = new NamedEntity(docView);
			ne.setBegin(specificMiRterms_1.start());
			ne.setEnd(specificMiRterms_1.end());
			ne.setConfidence(conf);
			ne.setWasGeneratedBy(this.getActivityProvenance());
			ne.addToIndexes();

			RegEx annotation = new RegEx(docView);
			annotation.setBegin(specificMiRterms_1.start());
			annotation.setEnd(specificMiRterms_1.end());
			String regExpression = specificMiRterms_1.pattern().pattern();
			String xml10pattern = "[^" + "\u0009\r\n" + "\u0020-\uD7FF"	+ "\uE000-\uFFFD" + "\ud800\udc00-\udbff\udfff" + "]";
			annotation.setPattern(regExpression.replaceAll(xml10pattern, ""));
			annotation.setWasGeneratedBy(this.getActivityProvenance());

			annotation.addToIndexes();
		}
	}

	/* -------------------------------Pattern 2------------------------------ */
	// Clusters, Paralogs, Groups, Antagomirs
	private void find_Specific_miRNA_terms_zweite(JCas docView, String S) {
		
		Matcher specificMiRterms_2 = Pattern.compile("(([cC][lL][uU][sS][tT][eE][rR][sS]?|[gG][rR][oO][uU][pP]|[fF][aA][mM][iI][lL][yY]|[pP][aA][rR][aA][lL][oO][gG][sS])\\s)?([hH][sS][aA]\\-|[cC][eE][lL]\\-|[pP][rR][eEiI]\\-)?\\b(([aA][nN][tT][aA][gG][oO])?(\\()?[mM][iI]([cC][rR][oO])?[rR]([nN][aA]?)?(\\))?)(\\-|)(\\d+[a-zA-Z]*((\\-|)(5p|3p|a\\s|as\\s|\\d+))?)((/|(\\s)?[~〜～∼](\\s)?|\\-|\\s[aA][pP][pP][rR][oO][xX][iI][mM][aA][tT][eE][lL][yY]\\\\s)(\\d+[a-zA-Z]?((\\-|)(5p|3p|as\\s|a\\s|\\d+))?|\\w\\b((\\-|)(5p|3p|as\\s|a\\s|\\d+))?))*(\\s([cC][lL][uU][sS][tT][eE][rR]|[gG][rR][oO][uU][pP]|[fF][aA][mM][iI][lL][yY]|[pP][aA][rR][aA][lL][oO][gG][sS]|[aA][nN][tT][aA][gG][oO][mM][iI][rR]))?").matcher(S);

		while (specificMiRterms_2.find()) {
			Confidence conf = new Confidence(docView);
			conf.setValue(1.0); // pseudo value

			NamedEntity ne = new NamedEntity(docView);
			ne.setBegin(specificMiRterms_2.start());
			ne.setEnd(specificMiRterms_2.end());
			ne.setConfidence(conf);
			ne.setWasGeneratedBy(this.getActivityProvenance());
			ne.addToIndexes();

			RegEx annotation = new RegEx(docView);
			annotation.setBegin(specificMiRterms_2.start());
			annotation.setEnd(specificMiRterms_2.end());
			String regExpression = specificMiRterms_2.pattern().pattern();
			String xml10pattern = "[^" + "\u0009\r\n" + "\u0020-\uD7FF"	+ "\uE000-\uFFFD" + "\ud800\udc00-\udbff\udfff" + "]";
			annotation.setPattern(regExpression.replaceAll(xml10pattern, ""));
			annotation.setWasGeneratedBy(this.getActivityProvenance());

			annotation.addToIndexes();
		}
	}

	/* -------------------------------Pattern 3------------------------------ */
	// Example: miR-107, -130a, -223, -292-5p, -433-3p, -451, -541, and -711,
	private void find_Specific_miRNA_terms_dritte(JCas docView, String S) {
		Matcher specificMiRterms_3 = Pattern.compile("([hH][sS][aA]\\-|[cC][eE][lL]\\-|[pP][rR][eEiI]\\-)?\\b((\\()?[mM][iI]([cC][rR][oO])?[rR]([nN][aA])?[sS]?(\\)?))([ \\-]|)(\\d+[a-zA-Z]*(\\*)?((\\-)(5p|3p|as|a|\\d))?)((, )([ \\-]|)?(\\d+[a-zA-Z]?(\\*)?((\\-)(5p|3p|as|a|\\d))?|\\w\\b((\\-)(5p|3p|a |as |\\d))?))*((,)?\\s?(and\\s?|&)([ \\-]|)(\\d+[a-zA-Z]?((\\-)(5p|3p|as|a|\\d))?|\\\\w((\\\\-)(5p|3p|a\\\\s|as\\\\s|\\\\d))?))*").matcher(S);
		Matcher modifiedMatcher = null;

		while (specificMiRterms_3.find()) {
			String S3 = specificMiRterms_3.group();

			if (S3.contains(",") || S3.contains("and")) {
				
				modifiedMatcher = Pattern.compile("((miR)?\\-?)\\d+\\w*(\\-\\d*\\w*)*").matcher(S).region(specificMiRterms_3.start(),specificMiRterms_3.end());

				while (modifiedMatcher.find()) {
					if (!modifiedMatcher.group().contains("miR") && !modifiedMatcher.group().contains("mir")) {
						Confidence conf = new Confidence(docView);
						conf.setValue(1.0); // pseudo value

						NamedEntity ne = new NamedEntity(docView);
						ne.setBegin(modifiedMatcher.start());
						ne.setEnd(modifiedMatcher.end());
						ne.setConfidence(conf);
						ne.setWasGeneratedBy(this.getActivityProvenance());
						ne.addToIndexes();

						RegEx annotation = new RegEx(docView);
						annotation.setBegin(modifiedMatcher.start());
						annotation.setEnd(modifiedMatcher.end());
						String regExpression = modifiedMatcher.pattern().pattern();
						String xml10pattern = "[^" + "\u0009\r\n"+ "\u0020-\uD7FF" + "\uE000-\uFFFD"+ "\ud800\udc00-\udbff\udfff" + "]";
						annotation.setPattern(regExpression.replaceAll(xml10pattern, ""));
						annotation.setWasGeneratedBy(this.getActivityProvenance());

						annotation.addToIndexes();
					}
				}
			}
		}
	}

	// Regular Expression to match General microRNA terms. Also NE and NNE are created here.
	private void find_General_miRNA_terms(JCas docView, String S) {
		// Examples for general microRNA: angiomir, oncomir, microRNA, mirna, micro rna, synthetic microRNA
		Matcher generalMatcher = Pattern.compile("(([pP][rR]([iI]|[eE])([ \\-][ \\-]*))|([sS][yY][nN][tT][hH][eE][tT][iI][cC] )|([hH][sS][aA]([ \\-][ \\-]*)|[cC][eE][lL]([ \\-][ \\-]*)|[mM][aA][tT][uU][rR][eE]\\s))?(([mM][iI]([cC][rR][oO])?)([ \\-][ \\-]*)?([rR][nN][aA][sS]?)(\\s[gG][rR][oO][uU][pP][sS]?|\\s[cC][lL][uU][sS][tT][eE][rR][sS]?|\\s[fF][aA][mM][iI][lL][yY]|\\s[pP][aA][rR][aA][lL][oO][gG][sS])?|[oO][nN][cC][oO][mM][iI][rR][sS]?|[aA][nN][gG][iI][oO][mM][iI][rR][sS]?)").matcher(S);
		while (generalMatcher.find()) {

			Confidence conf = new Confidence(docView);
			conf.setValue(1.0); // pseudo value

			NamedEntity ne = new NamedEntity(docView);
			ne.setBegin(generalMatcher.start());
			ne.setEnd(generalMatcher.end());
			ne.setConfidence(conf);
			ne.setWasGeneratedBy(this.getActivityProvenance());

			ne.addToIndexes();

			RegEx annotation = new RegEx(docView);
			annotation.setBegin(generalMatcher.start());
			annotation.setEnd(generalMatcher.end());
			String regExpression = generalMatcher.pattern().pattern();
			String xml10pattern = "[^" + "\u0009\r\n" + "\u0020-\uD7FF"	+ "\uE000-\uFFFD" + "\ud800\udc00-\udbff\udfff" + "]";
			annotation.setPattern(regExpression.replaceAll(xml10pattern, ""));
			annotation.setWasGeneratedBy(this.getActivityProvenance());

			annotation.addToIndexes();

			Label label = new Label(docView);
			label.setValue("microRNA");
			label.setLanguageTag("EN");
			label.addToIndexes();

			Concept miRNA = new Concept(docView);
			miRNA.setPrefLabel(label);
			miRNA.setIdentifier("general_mirna");
			miRNA.setIdentifierSource(getURL());
			miRNA.addToIndexes();

			Confidence confidence = new Confidence(docView);
			confidence.setValue(1.0);

			NormalizedNamedEntity nnEntity = new NormalizedNamedEntity(docView);
			nnEntity.setBegin(ne.getBegin());
			nnEntity.setEnd(ne.getEnd());
			nnEntity.setConcept(miRNA);
			nnEntity.setWasGeneratedBy(this.getActivityProvenance());
			nnEntity.setConfidence(conf);
			nnEntity.addToIndexes();
		}
	}

	// Create URLs for the identifiers
	private String getURL() {
		return mirBaseURL;
	}
	private String getFamURL() {
		return mirBaseFamURL;
	}
}