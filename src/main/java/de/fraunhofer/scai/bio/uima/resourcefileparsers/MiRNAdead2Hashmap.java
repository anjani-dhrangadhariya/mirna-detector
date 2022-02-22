package de.fraunhofer.scai.bio.uima.resourcefileparsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import de.fraunhofer.scai.bio.uima.mirnadetector.annotator.MiRNADetector;

/**
 * The class MiRNAdead2Hashmap generates a Hashmap where the Keys are prefLabels (Which are dead in miRBase)
 * and Values are their new corresponding miRBase identifier.
 * The resource file for this class is "miRNA.dead"
 * 
 * @author: Anjani K Dhrangadhariya
 */
public class MiRNAdead2Hashmap {
	public static HashMap<String, String> createMirnaDeadMap() throws Exception {
		BufferedReader br = null;
		String line;
		StringBuffer sb = new StringBuffer();

		try {
			InputStream deadMiRNAFileStream = MiRNADetector.class.getResourceAsStream(File.separator + "miRNA.dead");
			br = new BufferedReader(new InputStreamReader(deadMiRNAFileStream));

			while ((line = br.readLine()) != null) {
				if (line.startsWith("ID   ")) {
					sb.append(line);
					sb.append(";");
				}
				if (line.startsWith("PI   ")) {
					sb.append(line);
					sb.append(";");
				}
				if (line.startsWith("FW   ")) {
					sb.append(line);
				}
				if (line.startsWith("//")) {
					sb.append(line);
				}
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}

		// Constructing a map for non- PI entries of miRNA.dead
		HashMap<String, String> deadMap = new HashMap<String, String>();

		String[] deadArray = sb.toString().split("//");
		String[] deadArrayUpdatedNoPI = null;
		
		for (int i = 0; i < deadArray.length; i++) {
			if (deadArray[i].contains("PI   ") == false) {
				deadArrayUpdatedNoPI = deadArray[i].split(";");
				deadMap.put(deadArrayUpdatedNoPI[0].substring(5,deadArrayUpdatedNoPI[0].length()),	deadArrayUpdatedNoPI[1].substring(5,deadArrayUpdatedNoPI[1].length()));
			} else if (deadArray[i].contains("PI   ") == true) {
				if (i == 4) {
					continue;
				} else {
					deadArrayUpdatedNoPI = deadArray[i].split(";");

					deadMap.put(deadArrayUpdatedNoPI[0].substring(5,deadArrayUpdatedNoPI[0].length()),deadArrayUpdatedNoPI[2].substring(5,deadArrayUpdatedNoPI[2].length()));
					deadMap.put(deadArrayUpdatedNoPI[1].substring(5,deadArrayUpdatedNoPI[1].length()),deadArrayUpdatedNoPI[2].substring(5,deadArrayUpdatedNoPI[2].length()));
				}
			}
			deadArrayUpdatedNoPI = null;
		}

		// Manual addition of one key!! XXX
		// This is not so good code snippet
		deadMap.put("hsa-mir-102-7.2", "MI0000105");
		deadMap.put("hsa-mir-102-3", "MI0000105");
		deadMap.put("hsa-mir-29b-3", "MI0000105");

		// Printing hashmap
//		for (String name : deadMap.keySet()) {
//			String key = name.toString();
//			String value = deadMap.get(name).toString();
//			System.out.println(key + "--------- " + value);
//		}
		return deadMap;
	}
}