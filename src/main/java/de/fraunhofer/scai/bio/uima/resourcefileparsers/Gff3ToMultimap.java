package de.fraunhofer.scai.bio.uima.resourcefileparsers;

import java.io.BufferedReader;	
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.fraunhofer.scai.bio.uima.mirnadetector.annotator.MiRNADetector;

/**
 * The class Gff3ToMultimap generates a MultiMap where the Keys are preferred labels  and Values are miRBase ID's
 * There are multiple Values for a single key in MultiMap
 * The resource file for this class is "hsa.gff3"
 * 
 * @author: Anjani K Dhrangadhariya
 */
public class Gff3ToMultimap {
		// Parser to convert gff3 file to multimap to be used for normalization
		public static Multimap<String, String> createGff3ToMultimap() throws Exception {

			BufferedReader br = null;
			String line;
			StringBuilder hsaString_Mat = new StringBuilder();
			StringBuilder hsaString = new StringBuilder();
			
			try {
				InputStream hsaGff3_FileStream = MiRNADetector.class.getResourceAsStream(File.separator + "hsa.gff3");
				br = new BufferedReader(new InputStreamReader(hsaGff3_FileStream));
				
				while ((line = br.readLine()) != null) {
					if (!line.startsWith("#") && !line.contains(";Derives_from=") == true) {
						hsaString.append(line);
						hsaString.append("\n");
					} else if (!line.startsWith("#") && line.contains(";Derives_from=") == true) {
						hsaString_Mat.append(line);
						hsaString_Mat.append("\n");
					}
				}
				
			} finally {
				if (br != null) {
					br.close();
				}
			}

			String hsaArray[] = hsaString.toString().split("\n");
			String hsaString_MatArray[] = hsaString_Mat.toString().split("\n");

			int beginIndex = 0;

			for (int i = 0; i < hsaString_MatArray.length; i++) {
				beginIndex = hsaString_MatArray[i].toString().indexOf("ID=");
				hsaString_MatArray[i] = hsaString_MatArray[i].substring(beginIndex);
			}

			// List from String[] array
			List<String> hsaList_Mat_ID = new ArrayList<String>(); // DB ID's
			List<String> hsaList_Mat_Name = new ArrayList<String>(); // DB names
			List<String> hsaList_Mat_Derives_from = new ArrayList<String>(); // derives from ID's

			int firstIndex = 0, lastIndex = 0;
			for (int i = 0; i < hsaString_MatArray.length; i++) {
				firstIndex = hsaString_MatArray[i].toString().indexOf("MIMAT");
				lastIndex = hsaString_MatArray[i].toString().indexOf(";");
				hsaList_Mat_ID.add(hsaString_MatArray[i].toString().substring(firstIndex, lastIndex));
			}

			List<String> hsaList_Mat_ID_2 = new ArrayList<String>(); // DB ID's
			for (int i = 0; i < hsaList_Mat_ID.size(); i++) {
				if (hsaList_Mat_ID.get(i).contains("_")) {
					hsaList_Mat_ID_2.add(hsaList_Mat_ID.get(i).substring(0,	hsaList_Mat_ID.get(i).length() - 2));
				} else if (!hsaList_Mat_ID.get(i).contains("_")) {
					hsaList_Mat_ID_2.add(hsaList_Mat_ID.get(i));
				}
			}

			firstIndex = 0;
			lastIndex = 0;
			for (int i = 0; i < hsaString_MatArray.length; i++) {
				firstIndex = hsaString_MatArray[i].toString().indexOf("hsa");
				lastIndex = hsaString_MatArray[i].toString().indexOf(";D");
				hsaList_Mat_Name.add(hsaString_MatArray[i].toString().substring(firstIndex, lastIndex));
			}

			firstIndex = 0;
			lastIndex = 0;
			for (int i = 0; i < hsaString_MatArray.length; i++) {
				firstIndex = hsaString_MatArray[i].toString().indexOf("MI0");
				hsaList_Mat_Derives_from.add(hsaString_MatArray[i].toString().substring(firstIndex));
			}

			Multimap<String, String> hsa_Mat_multiMap = ArrayListMultimap.create();
			for (int i = 0; i < hsaString_MatArray.length; i++) {
				if (!hsaList_Mat_ID.get(i).contains("_")) {
					hsa_Mat_multiMap.put(hsaList_Mat_Name.get(i), hsaList_Mat_ID.get(i));
				}
				if (!hsaList_Mat_Derives_from.get(i).contains("_")) {
					hsa_Mat_multiMap.put(hsaList_Mat_Name.get(i), hsaList_Mat_Derives_from.get(i));
				}
			}

			// Perform the same for pre-mature miRNA ID's
			beginIndex = 0;
			for (int i = 0; i < hsaArray.length; i++) {
				beginIndex = hsaArray[i].toString().indexOf("ID=");
				hsaArray[i] = hsaArray[i].substring(beginIndex);
			}

			List<String> hsaList_ID = new ArrayList<String>(); // Contains main Database ID
			List<String> hsaList_Name = new ArrayList<String>(); 
			firstIndex = 0;
			lastIndex = 0;
			for (int i = 0; i < hsaArray.length; i++) {
				firstIndex = hsaArray[i].toString().indexOf("MI0");
				lastIndex = hsaArray[i].toString().indexOf(";");
				hsaList_ID.add(hsaArray[i].toString().substring(firstIndex,	lastIndex));
			}

			firstIndex = 0;
			lastIndex = 0;
			for (int i = 0; i < hsaArray.length; i++) {
				firstIndex = hsaArray[i].toString().indexOf("hsa");
				hsaList_Name.add(hsaArray[i].toString().substring(firstIndex));
			}

			HashMap<String, String> hm = new HashMap<String, String>();
			for (int i = 0; i < hsaArray.length; i++) {
				hm.put(hsaList_ID.get(i), hsaList_Name.get(i));
			}

			Multimap<String, String> hsa_ParserMap = ArrayListMultimap.create();
			for (int i = 0; i < hsaString_MatArray.length; i++) {
				hsa_ParserMap.put(hsaList_Mat_Derives_from.get(i),hsaList_Mat_ID_2.get(i));
			}

			// Combining the maps

			Multimap<String, String> comboMap = ArrayListMultimap.create();

			Set<String> keys_4 = hm.keySet();
			for (String key : keys_4) {
				comboMap.put(key, key);
			}

			Set<String> keys_5 = hsa_ParserMap.keySet();
			for (String key : keys_5) {
				comboMap.putAll(key, hsa_ParserMap.get(key));
			}

			// convert a multimap to hashmap
			// SWAP the key and value in the combomap to get desired KEYS and VALUES
			Multimap<String, String> comboMapUpdate = ArrayListMultimap.create();
			Set<String> keys_7 = comboMap.keySet();
			for (String key : keys_7) {
				comboMapUpdate.putAll(hm.get(key), comboMap.get(key));
			}

			// Combine MATURE miRNA map
			Set<String> keys_6 = hsa_Mat_multiMap.keySet();

			// for (String key : keys_6) {
			// comboMapUpdate.putAll(key, hsa_Mat_multiMap.get(key));
			// }

			for (String key : keys_6) {
				if (key.contains("miR") == true) {
					comboMapUpdate.putAll(key.replace("miR", "mir"),
							hsa_Mat_multiMap.get(key));
				} else {
					comboMapUpdate.putAll(key, hsa_Mat_multiMap.get(key));
				}

			}
			return comboMapUpdate;
		}
	}