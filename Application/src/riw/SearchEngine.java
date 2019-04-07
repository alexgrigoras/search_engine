/**
 * @title Search engine application
 * @author Alexandru Grigoras
 * @version 4.0 
 */
	
package riw;

import static com.mongodb.client.model.Filters.eq;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

import static com.mongodb.client.model.Filters.eq;

import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import com.mongodb.BasicDBObject;

public class SearchEngine {
	/**
	 * Variables
	 */
	private SpecialWords stopwordsObj;						// hash for stopwords
	private SpecialWords exceptionsObj;						// hash for exceptions
	private HashMap<String, LinksList> wordLinks;			// global inverse indexing
	private HashMap<String, Double> idf;					// global idf
	private HashMap<String, WordTf> tf;						// global tf
	DatabaseModule dm;
	
	/**
	 * Class constructor
	 */
	public SearchEngine() {
		stopwordsObj = new SpecialWords("./files/special_words/stop_words.txt");
		exceptionsObj = new SpecialWords("./files/special_words/exception_words.txt");
		wordLinks = new HashMap<String, LinksList>();
		idf = new HashMap<String, Double>();
		tf = new HashMap<String, WordTf>();
		dm = new DatabaseModule();
	}
	
	/**
	 * Log data to console for a string
	 * @param msg
	 * @param newLine
	 */
	private static void log(String msg, boolean newLine) {
		if(newLine) {
			System.out.println(msg);
        }
		else {
			System.out.print(msg);
		}
	}
	
	/**
	 * Log data to console for an integer number
	 * @param number
	 * @param newLine
	 */
	private static void log(int number, boolean newLine) {
		StringBuilder sb = new StringBuilder();
		sb.append(number);
		if(newLine) {
			System.out.println(sb.toString());
        }
		else {
			System.out.print(sb.toString());
		}
	}
	
	/**
	 * Log data to console for a double number
	 * @param number
	 * @param newLine
	 */
	private static void log(double number, boolean newLine) {
		StringBuilder sb = new StringBuilder();
		sb.append(number);
		if(newLine) {
			System.out.println(sb.toString());
        }
		else {
			System.out.print(sb.toString());
		}
	}
	
	/**
	 * Porter algorithm
	 * @param text: a word to be processed
	 * @return the canonical form of the word
	 */
	private String getCanonicalForm(String text) {
		PorterStemmer porterStemmer = new PorterStemmer();
		String stem = porterStemmer.stemWord(text);
		
		return stem;
	}
	 
	/**
	 * Parse the keywords from console to a format with words and associated operation
	 * @param keywords: the keywords from console
	 * @return an arraylist with the words and operations
	 */
	private ArrayList<WordOperation> parseKeywords(String keywords) {
		// variables for extracting words
		int i = 0;
		boolean setFlag = true;
		StringBuilder tempStr = new StringBuilder("");
		String text = "";
		ArrayList<WordOperation> keywords_list = new ArrayList<WordOperation>();
		WordOperation word = null;
		
		// parse text
		for(i=0; i<keywords.length();i++) {
			if(i == keywords.length()-1)
			{
				tempStr.append(keywords.charAt(i));
			}
			if((keywords.charAt(i) == ' ' || keywords.charAt(i) == '+' || keywords.charAt(i) == '-' ||
					i == keywords.length()-1) && setFlag) {
				text = tempStr.toString().toLowerCase();
				word = new WordOperation(getCanonicalForm(text));
				if(exceptionsObj.hashContains(tempStr.toString().toLowerCase())) {
					// Exception
					keywords_list.add(word);
					switch (keywords.charAt(i)) {
						case ' ':
							word.setOperation(OpType.OR);
							break;
						case '+':
							word.setOperation(OpType.AND);
							break;
						case '-':
							word.setOperation(OpType.NOT);
							break;
					}		
				} else if(!stopwordsObj.hashContains(tempStr.toString().toLowerCase())) {
					// Dictionary
					keywords_list.add(word);
					switch (keywords.charAt(i)) {
						case ' ':
							word.setOperation(OpType.OR);
							break;
						case '+':
							word.setOperation(OpType.AND);
							break;
						case '-':
							word.setOperation(OpType.NOT);
							break;
					}		
				}
				tempStr = new StringBuilder("");  
				setFlag = false;
			}
			else if(Character.isLetter(keywords.charAt(i))) {
				tempStr.append(keywords.charAt(i));
				setFlag = true;
			}
		}
		
		return keywords_list;
	}

	/**
	 * Read keywords from console
	 * @return a string with the keywords
	 */
	private String readKeywords() {
		Scanner scanner = new Scanner(System.in);
		String keywords = scanner.nextLine();	
		
		return keywords;
	}
	
	/**
	 * Get the documents for a specified word
	 * @param _word: word to be searched
	 * @return a list with documents
	 */
	private LinksList getWordLocations(String _word) {
		LinksList list = null;
		if(wordLinks.containsKey(_word)) {
			list = wordLinks.get(_word);
			return list;
		}
		else {
			return null;
		}		
	}
	
	/**
	 * Print to console sorted results
	 * @param hm
	 * @return
	 */
	private static HashMap<String, Double> sortByValue(HashMap<String,Double> hm) 
    { 
        // Create a list from elements of HashMap 
        List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double> >(hm.entrySet()); 
  
        // Sort the list 
        Collections.sort(list, new Comparator<Map.Entry<String, Double> >() { 
            public int compare(Map.Entry<String, Double> o1,  
                               Map.Entry<String, Double> o2) 
            { 
                return -(o1.getValue()).compareTo(o2.getValue()); 
            } 
        }); 
          
        // put data from sorted list to hashmap  
        HashMap<String, Double> temp = new LinkedHashMap<String, Double>(); 
        for (Map.Entry<String, Double> aa : list) { 
            temp.put(aa.getKey(), aa.getValue()); 
        } 
        return temp; 
    } 
	
	/**
	 * Show the hash map containing the search results
	 * @param hash_map
	 */
	private void showResults(HashMap<String, Double> hash_map) {
		Map<String, Double> hm = sortByValue(hash_map); 
		  
        // print the sorted hashmap 
        for (Map.Entry<String, Double> en : hm.entrySet()) { 
            log(" - Document: " + en.getKey() + " [" + en.getValue() + "]", true); 
        } 
	}
	
	/**
	 * Add word to hash
	 * @param _text
	 * @param _link
	 */
	private void addToHash(String _text, Link _link)
	{
		if(!hashLinkContains(_text)) {
			LinksList ll = new LinksList(_link);
			wordLinks.put(_text, ll);
		}
		else {
			LinksList ll = wordLinks.get(_text);
			ll.addLink(_link);
			wordLinks.replace(_text, ll);
		}
	}
	
	/**
	 * Check if word exists in hash
	 * @param _doc
	 * @return
	 */
	private boolean hashLinkContains(String _doc)
	{
		return wordLinks.containsKey(_doc);
	}
	
	/** 
	 * Display words from hash (inverse indexing)
	 */
	public void showInverseIndex() {
		int nr = 0;
		
		if(wordLinks.size() == 0) {
			log("> Index has not been constructed or merged", true);
			log("> Exitting", true);
			System.exit(1);
		}
		
		log("> Showing inverse index: ", false);
		log("< ", false);  
		for (String doc: wordLinks.keySet()) {
			nr++;
			String key = doc.toString();
			LinksList value = wordLinks.get(doc);
			log(key + ": ", false);
			value.show();
			if(wordLinks.size() > nr )
			{
				log(", ", false);
			}            
		}
		log(">", true);
	}
	
	/**
	 * Binary Search
	 */
	private void binarySearch() {
		boolean exit = false;
		while(exit == false) {
			log("> Search: ", false);
			
			String keywords = readKeywords();			
			ArrayList<WordOperation> kw_list = new ArrayList<WordOperation>();
			int list_dimension = 0;
			LinksList words_list = new LinksList();
			StringBuilder words_ops = new StringBuilder();
			
			if(keywords.equals("exit")) {
				exit = true;
				break;
			}
			
			log("> Searched keywords: ", false);
			
			kw_list = parseKeywords(keywords);
			
			list_dimension = kw_list.size();
			
			if(list_dimension == 0) {
				log("Nothing typed!", false);
			}
			else if(list_dimension == 1) {
				String word = kw_list.get(0).getWord();
				
				log(word + " -> ", false);
				
				LinksList list = getWordLocations(word);
				list.show();
			}
			else {
				for(int i = 0; i < list_dimension - 1; i++) {
					String word_1 = kw_list.get(i).getWord();
					String word_2 = kw_list.get(i+1).getWord();
					OpType operation = kw_list.get(i).getOperation();
					
					if(i == 0) {
						words_ops.append(word_1 + " " + operation + " " + word_2);
					}
					else {
						words_ops.append(" " + operation + " " + word_2);
					}
					
					LinksList list_1 = null;	
					LinksList list_2 = null;
					if(words_list.size() == 0) {
						try {
							list_1 = getWordLocations(word_1);
						}
						catch(NullPointerException e) {
							list_1 = new LinksList();
						}
					}
					else {
						list_1 = words_list;
					}			
					
					try {
						list_2 = getWordLocations(word_2);
					}
					catch(NullPointerException e) {
						list_2 = new LinksList();
					}
					
					words_list = new LinksList();
					
					if(operation == OpType.OR) {
						if(list_1 != null && list_2 != null && list_1.size() >= list_2.size()) {
							try {
								for(Link l: list_1.getLinks()) {
									if(!words_list.hasLink(l.getLink())) {
										words_list.addLink(l);
									}
									else {
										words_list.addFreqToLink(l.getLink(), l.getFrequency());
									}
								}
							}
							catch(NullPointerException e) {}
							
							try {
								for(Link l: list_2.getLinks()) {
									if(!words_list.hasLink(l.getLink())) {
										words_list.addLink(l);
									}
									else {
										words_list.addFreqToLink(l.getLink(), l.getFrequency());
									}
								}
							}
							catch(NullPointerException e) {}
						}
						else {
							try {
								for(Link l: list_2.getLinks()) {
									if(!words_list.hasLink(l.getLink())) {
										words_list.addLink(l);
									}
									else {
										words_list.addFreqToLink(l.getLink(), l.getFrequency());
									}
								}
							}
							catch(NullPointerException e) {}
							
							try {
								for(Link l: list_1.getLinks()) {
									if(!words_list.hasLink(l.getLink())) {
										words_list.addLink(l);
									}
									else {
										words_list.addFreqToLink(l.getLink(), l.getFrequency());
									}
								}
							}
							catch(NullPointerException e) {}
						}
					}
					else if(operation == OpType.AND) {						
						
						if(list_1 != null && list_2 != null && list_1.size() <= list_2.size()) {
							try {
								for(Link l: list_1.getLinks()) {
									if(list_2.hasLink(l.getLink())) {
										if(!words_list.hasLink(l.getLink())) {
											words_list.addLink(l);
										}
										else {
											words_list.addFreqToLink(l.getLink(), l.getFrequency());
										}
									}
								}
							}
							catch(NullPointerException e) {}
						}
						else {
							try {
								for(Link l: list_2.getLinks()) {
									if(list_1.hasLink(l.getLink())) {
										if(!words_list.hasLink(l.getLink())) {
											words_list.addLink(l);
										}
										else {
											words_list.addFreqToLink(l.getLink(), l.getFrequency());
										}
									}
								}
							}
							catch(NullPointerException e) {}
						}										
					}
					else if(operation == OpType.NOT) {
						
						for(Link l: list_1.getLinks()) {
							if(!list_2.hasLink(l.getLink())) {
								if(!words_list.hasLink(l.getLink())) {
									words_list.addLink(l);
								}
								else {
									words_list.addFreqToLink(l.getLink(), l.getFrequency());
								}
							}
						}
						
					}				
				}
				
				log(words_ops + " -> ", false);
				
				if(words_list.size() == 0) {
					log("No results!", false);
				}
				else {
					words_list.show();
				}
			}
			
			log("", true);
		}
	}
	
	/**
	 * Returns a vector with tf*idf for searched words
	 * @param words_list
	 * @return
	 */
	private ArrayList<Double> calculateQueryVector(ArrayList<WordOperation> words_list) {
		int nrQueryWords = words_list.size();
		ArrayList<Double> vector = new ArrayList<Double>();
		
		int nrDocuments = tf.size();
		
		for(WordOperation word: words_list) {
			double tf = 1.0 / (double)nrQueryWords;
			double idf;

			LinksList listDocs = getWordLocations(word.getWord());
			double nrWordDocs = 1;
			try {
				nrWordDocs += listDocs.size();
			}
			catch(NullPointerException ex) {}
			
			double res = nrDocuments / nrWordDocs;
			
			idf = Math.log(res);
			
			if(idf < 0) {
				idf = 1;
			}

			vector.add(tf * idf);
		}
		
		return vector;
	}
	
	/**
	 * Calculates the cosine similarity of two numbers
	 * @param A: double number
	 * @param B: double number
	 * @return double value of cosine similarity
	 */
	public double cosineSimilarity(double A, double B) {
    	double sumProduct = 0;
    	double sumASq = 0;
    	double sumBSq = 0;
    	
    	sumProduct += A * B;
    	sumASq += A * A;
    	sumBSq += B * B;
    	
    	if (sumASq == 0 && sumBSq == 0) {
    		return 0;
    	}
    	return sumProduct / (Math.sqrt(sumASq) * Math.sqrt(sumBSq));
    }

	/**
	 * Calculates the cosine similarity of two vectors
	 * @param A: arraylist A
	 * @param B: arrayList B
	 * @return double value of cosine similarity
	 */
	public double cosineSimilarity(ArrayList<Double> A, ArrayList<Double> B) {
    	if (A == null || B == null || A.size() == 0 || B.size() == 0 || A.size() != B.size()) {
    		return 0;
    	}

    	double sumProduct = 0;
    	double sumASq = 0;
    	double sumBSq = 0;
    	for (int i = 0; i < A.size(); i++) {
    		sumProduct += A.get(i) * B.get(i);
    		sumASq += A.get(i) * A.get(i);
    		sumBSq += B.get(i) * B.get(i);
    	}
    	if (sumASq == 0 && sumBSq == 0) {
    		return 0;
    	}
    	return sumProduct / (Math.sqrt(sumASq) * Math.sqrt(sumBSq));
    }
	
	/**
	 * Read the tf from file
	 */
	public void getTf() {
		FileExplorer fileExp = new FileExplorer("json");		// file explorer object
		String directory = "files/tf/";							// directory to search indexes
		Queue<String> files;									// queue with file names and paths
		
		fileExp.searchFiles(directory, 0, 0);
		files = fileExp.getFiles();

		log("> Get TF from file:", true);
		
		for(String fileName: files) {
	        Object obj = null;
			
	        try {
				obj = new JSONParser().parse(new FileReader(fileName));
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			} 
	          
	        // typecasting obj to JSONObject
	        JSONArray docs = (JSONArray) obj;
	        
	        log("\t - file [" + fileName + "] with ", false);
	        log(docs.size(), false);
	        log(" docs", true);
	        
	        // iterating phoneNumbers
	        Iterator itr1 = docs.iterator(); 
	        
	        while (itr1.hasNext()) {
	        	JSONObject doc = (JSONObject) itr1.next();
	        	
	        	String docName = (String)doc.get("doc");

	        	WordTf wtf = new WordTf();
	        	
	        	JSONArray terms = (JSONArray)doc.get("terms");
	        	
	        	Iterator itr2 = terms.iterator(); 
		        
		        while (itr2.hasNext()) {
		        	JSONObject term = (JSONObject) itr2.next();
		        	
		        	wtf.insertWord((String)term.get("k"), (double)term.get("tf"));
		        }
		        
		        tf.put(docName, wtf);
	        }

		}
	}
	
	/**
	 * Display the tf for docs and terms
	 */
	public void showTf() {
		int nr = 0;
		
		log("> Showing tf for documents: ", true);  
		for (String doc: tf.keySet()) {
			nr++;
            String key = doc.toString();
            WordTf value = tf.get(doc);
            log(" <" + key + " -> ", false);
            value.show();
            if(tf.size() > nr )
            {
            	log(">", true);
            }            
		}
	}
	
	/**
	 * 
	 * @param _doc
	 * @param _term
	 * @return
	 */
	public double getTfValHash(String _doc, String _term) {
		if(tf.containsKey(_doc)) {
			WordTf wtf = tf.get(_doc);
			return wtf.getTfForWord(_term);
		}
		else {
			return 0;
		}
	}
	
	/**
	 * 
	 * @param _doc
	 * @param _term
	 * @return
	 */
	public double getTfValDB(String _doc, String _term) {
		dm.setCollection("tf_values");
		
		BasicDBObject criteria = new BasicDBObject();
		criteria.append("doc", _doc);
		criteria.append("k", _term);
		
		try {
			Document myDoc = dm.collection.find(criteria).first();
			return (double) myDoc.get("tf");
		}
		catch (NullPointerException e) {
			return 0;
		}
	}
	
	/**
	 * To be done get tf
	 * @param _doc
	 * @param _term
	 * @return
	 */
	public double getTfVal(String _doc, String _term) {
		return getTfValHash(_doc, _term);
		//return getTfValDB(_doc, _term);
	}
	
	/**
	 * Returns idf for a specified word
	 * @param term: a word from a document
	 */
	public double getInverseDocumentFrequency(String term) {
		if (wordLinks.containsKey(term)) {
			double size = wordLinks.size();
			LinksList list = wordLinks.get(term);
			double documentFrequency = list.size();
			return Math.log(size / (1 + documentFrequency));
		} else {
			return 0;
		}
	}
	
	/**
	 * 	
	 * @param term
	 * @return
	 */
	public double getIdfValHash(String term) {
		if (idf.containsKey(term)) {
			return idf.get(term);
		} else {
			return 0;
		}
	}
	
	/**
	 * 
	 * @param term
	 * @return
	 */
	public double getIdfValDB(String term) {
		dm.setCollection("idf_values");
				
		try {
			Document myDoc = dm.collection.find(eq("k", term)).first();
			return (double) myDoc.get("i");
		}
		catch (NullPointerException e) {
			return 0;
		}
	}
	
	/**
	 * Gets the value of the calculated idf for the specified word
	 * @param term: input word
	 * @return the idf value
	 */
	public double getIdfVal(String term) {
		// return getIdfValHash(term);
		return getIdfValDB(term);
	}
	
	/**
	 * Calculates the idf for the words
	 */
	public void calculateIdf() {
		for (String doc: wordLinks.keySet()) {
            String key = doc.toString();
            double idf_val;
            try {
            	idf_val = getInverseDocumentFrequency(key);
            }
            catch(NullPointerException ex) {
            	idf_val = 0;
            }
            
            idf.put(key, idf_val);
		}
	}
	
	/**
	 * Show calculated IDF for terms
	 */
	public void showIdfForTerms() {
		int nr = 0;
		
		log("> Showing idf for documents: ", false);
		log("< ", false);  
		for (String doc: idf.keySet()) {
			nr++;
            String key = doc.toString();
            Double value = idf.get(doc);
            log(key + ": " + value, false);
            if(idf.size() > nr )
            {
            	log(", ", false);
            }            
		}
		log(">", true);
	}
	
	/**
	 * Write IDF values of words to file in JSON format
	 */
	public void writeIdfToFile() {
		log("> Writing idf to file", true);
		
		JSONArray termsArray = new JSONArray();
		
		for (String doc: idf.keySet()) {
            JSONObject termName = new JSONObject();
            termName.put("k", doc.toString());
            termName.put("i", idf.get(doc));
            
            termsArray.add(termName);
		}
		
		JSONObject termsJson = new JSONObject();
        termsJson.put("terms", termsArray);
		
		// Write JSON file
        try (FileWriter file = new FileWriter("files/idf/global_idf.json")) {
            file.write(termsJson.toJSONString());
            file.flush(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	/**
	 * Store IDF to mongoDB database
	 */
	public void storeIdfToDB() {
		log("> Writing idf to file", true);
		
		dm.setCollection("idf_values");
		
		List<Document> documents = new ArrayList<Document>();
		
		for (String doc: idf.keySet()) {          
            documents.add(new Document("k", doc.toString())
        			.append("i", idf.get(doc)));
		}
		
		dm.insertMultipleDocs(documents);
	}
	
	/**
	 * Read the idf from file
	 */
	public void getIdf() {
		FileExplorer fileExp = new FileExplorer("json");		// file explorer object
		String directory = "files/idf/";						// directory to search indexes
		Queue<String> files;									// queue with file names and paths
		
		fileExp.searchFiles(directory, 0, 0);
		files = fileExp.getFiles();

		log("> Get IDF from file:", true);
		
		for(String fileName: files) {
	        Object obj = null;
			
	        try {
				obj = new JSONParser().parse(new FileReader(fileName));
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			} 
	          
	        // typecasting obj to JSONObject
	        JSONObject terms = (JSONObject) obj;
	        
	        JSONArray termIdf = (JSONArray) terms.get("terms");
	        
	        log("\t - file [" + fileName + "] with ", false);
	        log(termIdf.size(), false);
	        log(" indexes", true);
	        
	        // iterating phoneNumbers
	        Iterator itr = termIdf.iterator(); 
	        
	        while (itr.hasNext())
	        {
	        	JSONObject term = (JSONObject) itr.next();
	        	
	        	idf.put((String)term.get("k"), (double)term.get("i"));
	        } 
		}
	}

	/**
	 * Vectorial Search
	 */
	private void vectorialSearch() {
		boolean exit = false;
		while(exit == false) {
			log("> Search: ", false);
			
			String keywords = readKeywords();
			
			ArrayList<WordOperation> kw_list = new ArrayList<WordOperation>();
			int list_dimension = 0;
			LinksList docs_list = new LinksList();
			StringBuilder words_ops = new StringBuilder();
			
			if(keywords.equals("exit")) {
				exit = true;
				break;
			}
			
			kw_list = parseKeywords(keywords);
			list_dimension = kw_list.size();
			
			ArrayList<Double> vector = new ArrayList<Double>();
			vector = calculateQueryVector(kw_list);
			
			log("> ", false);			
			
			if(list_dimension == 0) {
				log("Nothing typed!", false);
			}
			else if(list_dimension == 1) {
				String word = kw_list.get(0).getWord();
				
				log(word + " has ", false);
				LinksList list = getWordLocations(word);
				try {
					log(list.size(), false);
					
					log(" results: ", true);				
					
					HashMap<String, Double> cosSimVal = new HashMap<String, Double>(); 
					
					double idf = getIdfVal(word);
					for(Link l: list.getLinks()) {
						String link = l.getLink();
						double tf = getTfVal(link, word);
						cosSimVal.put(link, cosineSimilarity(vector.get(0), tf*idf));
					}				
					
					showResults(cosSimVal);
				}
				catch (NullPointerException ex) {
					log("0 results", true);
				}
				
			}
			else {
				for(int i = 0; i < list_dimension - 1; i++) {
					String word_1 = getCanonicalForm(kw_list.get(i).getWord());
					String word_2 = getCanonicalForm(kw_list.get(i+1).getWord());
					OpType operation = kw_list.get(i).getOperation();
					
					if(i == 0) {
						words_ops.append(word_1 + " " + operation + " " + word_2);
					}
					else {
						words_ops.append(" " + operation + " " + word_2);
					}
					
					LinksList list_1 = null;	
					LinksList list_2 = null;
					if(docs_list.size() == 0) {
						try {
							list_1 = getWordLocations(word_1);
						}
						catch(NullPointerException e) {
							list_1 = new LinksList();
						}
					}
					else {
						list_1 = docs_list;
					}			
					
					try {
						list_2 = getWordLocations(word_2);
					}
					catch(NullPointerException e) {
						list_2 = new LinksList();
					}
					
					docs_list = new LinksList();
					
					if(operation == OpType.OR) {						
						if(list_1 != null && list_2 != null && list_1.size() >= list_2.size()) {
							try {
								for(Link l: list_1.getLinks()) {
									if(!docs_list.hasLink(l.getLink())) {
										docs_list.addLink(l);
									}
									else {
										docs_list.addFreqToLink(l.getLink(), l.getFrequency());
									}
								}
							}
							catch(NullPointerException e) {}
							
							try {
								for(Link l: list_2.getLinks()) {
									if(!docs_list.hasLink(l.getLink())) {
										docs_list.addLink(l);
									}
									else {
										docs_list.addFreqToLink(l.getLink(), l.getFrequency());
									}
								}
							}
							catch(NullPointerException e) {}
						}
						else {
							try {
								for(Link l: list_2.getLinks()) {
									if(!docs_list.hasLink(l.getLink())) {
										docs_list.addLink(l);
									}
									else {
										docs_list.addFreqToLink(l.getLink(), l.getFrequency());
									}
								}
							}
							catch(NullPointerException e) {}
							
							try {
								for(Link l: list_1.getLinks()) {
									if(!docs_list.hasLink(l.getLink())) {
										docs_list.addLink(l);
									}
									else {
										docs_list.addFreqToLink(l.getLink(), l.getFrequency());
									}
								}
							}
							catch(NullPointerException e) {}
						}
					}
					else if(operation == OpType.NOT) {
						
						for(Link l: list_1.getLinks()) {
							if(!list_2.hasLink(l.getLink())) {
								if(!docs_list.hasLink(l.getLink())) {
									docs_list.addLink(l);
								}
								else {
									docs_list.addFreqToLink(l.getLink(), l.getFrequency());
								}
							}
						}
						
					}				
				}
				
				log(words_ops + " has ", false);
				log(docs_list.size(), false);
				log(" results: ", true);
				
				HashMap<String, Double> cosSimVal = new HashMap<String, Double>(); 
				
				ArrayList<Double> link_vectors = new ArrayList<Double>();
				
				for(WordOperation w: kw_list) {
					double idf = getIdfVal(w.getWord());
					for(Link l: docs_list.getLinks()) {
						String link = l.getLink();
						double tf = getTfVal(link, w.getWord());
						link_vectors.add(tf*idf);
					}				
				}
				
				for(int i=0;i<docs_list.size();i++)
				{
					ArrayList<Double> cosVector = new ArrayList<Double>();
					for(int j=0; j<kw_list.size(); j++)
					{
						cosVector.add(link_vectors.get(i+j*docs_list.size()));
					}
					
					cosSimVal.put(docs_list.getLinks().get(i).getLink(), cosineSimilarity(vector, cosVector));
				}
				
				showResults(cosSimVal);
			}
		}
	}
	
	/**
	 * Build the inverse index and tf, idf for terms for each thread
	 */
	public void buildIndex()
	{
		FileExplorer fileExp = new FileExplorer("txt");		// file explorer object
		String directory;									// directory name to be processed		
		Queue<String> files;								// queue with file names and paths
		int level = 0;										// how many levels to search recursively
		int links = 0;										// limit the number of links from the queue
		int nr_threads = 5;									// number of threads
		
		//parser.log("> Type the selected directory: ", false);
		//directory = parser.readKeywords();
		
		// directory = "D:\\Facultate\\Anul 4\\Semestrul I\\ALPD\\Tema de casa\\test_files";
		directory = "E:\\Facultate\\Anul IV - Facultate\\Semestrul I\\ALPD - Algoritmi paraleli si distribuiti\\Tema de casa\\test-files\\test-files";
		
		log("> Getting files from folder: " + directory, true);
				
		fileExp.searchFiles(directory, level, 0);
		
		if(links > 0) {
			files = fileExp.getFiles(links);
		}
		else {
			files = fileExp.getFiles();
		}
		
		int nr_files_in_queue = files.size();		
		int nr_files_per_thread;
		
		if(nr_files_in_queue >= nr_threads) { 
			nr_files_per_thread = nr_files_in_queue / nr_threads;
		}
		else {
			log("> Nr of threads is bigger than files in queue", true);
			return;
		}
		
		ArrayList<Worker> WorkerPool = new ArrayList<Worker>();
		
		for(int i=0; i < nr_threads; i++) {
			Queue<String> temp_files = new LinkedList<String>();
			for(int j = 0; j<nr_files_per_thread; j++) {
				String element = files.poll();
				temp_files.add(element);
			}
			
			Worker T = new Worker("worker_" + i, temp_files);
			WorkerPool.add(T);
		}
		
		if(files.size() > 0) {			
			Worker T = new Worker("worker_last", files);
			WorkerPool.add(T);
		}
		
		for(Worker w: WorkerPool) {
			w.start();
		}
		
		for(Worker w: WorkerPool) {
			try {
				w.getThread().join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		log("> Threads finished", true);
	}
	

	/**
	 * Merge the created indexes from each thread
	 */
	public void mergeIndexes() {
		FileExplorer fileExp = new FileExplorer("json");		// file explorer object
		String directory = "files/indexes/";					// directory to search indexes
		Queue<String> files;								// queue with file names and paths
		
		fileExp.searchFiles(directory, 0, 0);
		files = fileExp.getFiles();

        log("> Merging the inverse indexes from files:", true);
        
		for(String fileName: files) {
	        Object obj = null;
			
	        try {
				obj = new JSONParser().parse(new FileReader(fileName));
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			} 
	          
	        // typecasting obj to JSONArray
	        JSONArray terms = (JSONArray) obj;
	        
	        log("\t - file [" + fileName + "] with ", false);
	        log(terms.size(), false);
	        log(" indexes", true);
	        
	        
	        // iterating phoneNumbers 
	        Iterator itr1 = terms.iterator(); 
	          
	        while (itr1.hasNext())  
	        {
	        	JSONObject term = (JSONObject) itr1.next();
	        	
	        	String termName = (String)term.get("term");
	        	
	        	JSONArray docs = (JSONArray)term.get("docs"); 
	            
	            Iterator itr2 = docs.iterator();
	            
	            while (itr2.hasNext()) { 
	            	JSONObject doc = (JSONObject) itr2.next();
	            	
	            	Link l = new Link((String)doc.get("d"), (int)((long)doc.get("c")));
	            	
	            	addToHash(termName, l);
	            }
	        } 

		}
	}
	
	
	/**
	 * Main function
	 * @param args: arguments from command line
	 */
	public static void main(String[] args) {
		SearchEngine se = new SearchEngine();

		long startTime = System.nanoTime();
		
		//se.buildIndex();
		
		se.mergeIndexes();
		// se.showInverseIndex();
		
		// se.calculateIdf();
		// se.writeIdfToFile();
		// se.storeIdfToDB();
		
		se.getIdf();
		// se.showIdfForTerms();
		
		se.getTf();
		// se.showTf();
		
		long endTime = System.nanoTime();

		// get difference of two nanoTime values
		long timeElapsed = endTime - startTime;

		se.log("> Execution time in milliseconds : ", false);
		se.log(timeElapsed / 1000000, true);
		
		// se.binarySearch();
		se.vectorialSearch();
	}

}
