package de.fraunhofer.scai.bio.uima.resourcefileparsers;

import java.io.BufferedReader;	
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fraunhofer.scai.bio.uima.mirnadetector.annotator.MiRNADetector;

/**
 * The class MiFam2Hashmap generates a Hashmap where the Keys are preferred labels for miRNA family names
 * and Values are miRBase ID's for family names
 * The resource file for this class is "miFam.dat"
 * 
 * @author: Anjani K Dhrangadhariya
 */
public class MiFam2Hashmap {
	public static HashMap<String, String> createMiFamMap() throws Exception{
		BufferedReader br = null;
		String line;
		HashMap<String, String> miFamMap = new HashMap<String, String>();
		List<String> miFamMIPF = new ArrayList<String>();
		List<String> miFamLabel = new ArrayList<String>();

		try {
			InputStream miFamFileStream = MiRNADetector.class.getResourceAsStream(File.separator + "miFam.dat");
			br = new BufferedReader(new InputStreamReader(miFamFileStream));
			while ((line = br.readLine()) != null) {
				if (line.startsWith("AC")) {
					miFamMIPF.add(line.substring(5));
				} else if (line.startsWith("ID")) {
					miFamLabel.add(line.substring(5));
				}
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}

		for (int i = 0; i < miFamMIPF.size(); i++) {
			miFamMap.put(miFamLabel.get(i), miFamMIPF.get(i));
		}

//		for (String name : miFamMap.keySet()) {
//			String key = name.toString();
//			String value = miFamMap.get(name).toString();
//			System.err.println(key + "--------- " + value);
//		}
		return miFamMap;
	}
}