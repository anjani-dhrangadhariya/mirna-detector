package de.fraunhofer.scai.bio.uima.resourcefileparsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import de.fraunhofer.scai.bio.uima.mirnadetector.annotator.MiRNADetector;

/**
 * The class MirnaAliasToHashmap generates a Hashmap where the Keys are preferred labels and Values are miRBase ID's
 * The resource file for this class is "aliases.txt"
 * 
 * @author: Anjani K Dhrangadhariya 
 */
public class MirnaAliasToHashmap {
	public static HashMap<String, String> createAliasMap() throws Exception{

		String line;
		HashMap<String, String> aliasMirnaMap = new HashMap<String, String>();

		BufferedReader br = null;
		try {
			InputStream aliasFileStream = MiRNADetector.class.getResourceAsStream(File.separator + "aliases.txt");
			br = new BufferedReader(new InputStreamReader(aliasFileStream));
			
			while ((line = br.readLine()) != null) {
				int k = 0;
				k = line.indexOf("\t");
				
				String id = line.substring(0, k);
				String prefLabels = line.substring(k + 1);
				String[] prefLabelArray = prefLabels.split(";");
				
				for (int j = 0; j < prefLabelArray.length; j++) {
					if(prefLabelArray[j].matches(".*-[mM][iI]R-.*")== true) {
						aliasMirnaMap.put(prefLabelArray[j].replaceAll("-[mM][iI]R-", "-mir-"), id);
					}
					else if(prefLabelArray[j].matches(".*-[mM][iI]R-.*") == false){
						aliasMirnaMap.put(prefLabelArray[j], id);
					}
				}
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		return aliasMirnaMap;
	}
}