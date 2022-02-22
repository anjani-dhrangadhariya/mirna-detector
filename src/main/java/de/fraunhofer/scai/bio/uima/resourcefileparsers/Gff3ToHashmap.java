package de.fraunhofer.scai.bio.uima.resourcefileparsers;

import java.io.BufferedReader;	
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import de.fraunhofer.scai.bio.uima.mirnadetector.annotator.MiRNADetector;

/**
 * The class Gff3ToHashmap generates a Hashmap where the Keys are miRBase ID's and Values are PrefLabels
 * The resource file for this class is "hsa.gff3"
 * 
 * @author: Anjani K Dhrangadhariya
 */
public class Gff3ToHashmap  {
	
	public static HashMap<String, String> createHashmap() throws Exception{
		BufferedReader br = null;
		StringBuilder hsaString_Mat = new StringBuilder();
		StringBuilder hsaString = new StringBuilder();
		String line;
		
		try {
			InputStream hsaGff3FileStream = MiRNADetector.class.getResourceAsStream(File.separator + "hsa.gff3");
			br = new BufferedReader(new InputStreamReader(hsaGff3FileStream));
			
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("#") && !line.contains(";Derives_from=") == true) {
					hsaString.append(line);
					hsaString.append("\n");
				} else if (!line.startsWith("#") && line.contains(";Derives_from=") == true) {
					hsaString_Mat.append(line);
					hsaString_Mat.append("\n");
				}
			}
		}finally {
			if (br != null) {
				br.close();
			}
		}
		
		String hsaArray[] = hsaString.toString().split("\n");
		String hsaString_MatArray[] = hsaString_Mat.toString().split("\n");
		
		int beginIndex = 0;
		for (int i = 0; i < hsaArray.length; i++) {
			beginIndex = hsaArray[i].toString().indexOf("ID=");
			hsaArray[i] = hsaArray[i].substring(beginIndex);
		}

		for (int i = 0; i < hsaString_MatArray.length; i++) {
			beginIndex = hsaString_MatArray[i].toString().indexOf("ID=");
			hsaString_MatArray[i] = hsaString_MatArray[i].substring(beginIndex);
		}

		String hsaString_MatArray_Updated[] = null;
		String hsaArray_Updated[] = null;
		
		// Creating and populating hashmap
		HashMap<String, String> prefLabelMap = new HashMap<String, String>();
		
		for (int i = 0; i < hsaString_MatArray.length; i++) {
			hsaString_MatArray_Updated = hsaString_MatArray[i].split(";");
			prefLabelMap.put(hsaString_MatArray_Updated[0].replace("ID=", ""), hsaString_MatArray_Updated[2].replace("Name=", ""));
		}
		
		for (int i = 0; i < hsaArray.length; i++) {
			hsaArray_Updated = hsaArray[i].split(";");
			prefLabelMap.put(hsaArray_Updated[0].replace("ID=", ""), hsaArray_Updated[2].replace("Name=", ""));
		}
		
//		for (String name : prefLabelMap.keySet()) {
//			String key = name.toString();
//			String value = prefLabelMap.get(name).toString();
//			System.out.println(key + "--------- " + value);
//		}		
		return prefLabelMap;
	}
}