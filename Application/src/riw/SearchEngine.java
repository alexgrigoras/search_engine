/**
 * @title Search engine application
 * @author Alexandru Grigoras
 * @version 3.0 
 */
	
package riw;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Map;
import java.util.List;
import java.util.Comparator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 */
public class SearchEngine {
	/**
	 * Arguments
	 */
	private SpecialWords stopwordsObj;						// create hash for exceptions and stopwords
	private SpecialWords exceptionsObj;
	private IndexWords indexObj;
	private HashMap<String, IndexWords> docKeys;			// indexare directa + tf
	private HashMap<String, LinksList> wordLinks;			// indexare inversa	
	
	private HashMap<String, Double> idf;					// indexare inversa	
	
	/**
	 * Methods
	 */
	// Class constructor

	public SearchEngine()
	{
		stopwordsObj = new SpecialWords("./files/special_words/stop_words.txt");
		exceptionsObj = new SpecialWords("./files/special_words/exception_words.txt");
		docKeys = new HashMap<String, IndexWords>();
		wordLinks = new HashMap<String, LinksList>();
		indexObj = new IndexWords();
		idf = new HashMap<String, Double>();
	}
	
	// Log data to console
	private void log(String msg, boolean newLine) {
		if(newLine) {
			System.out.println(msg);
        }
		else {
			System.out.print(msg);
		}
	}
	private void log(int number, boolean newLine) {
		StringBuilder sb = new StringBuilder();
		sb.append(number);
		if(newLine) {
			System.out.println(sb.toString());
        }
		else {
			System.out.print(sb.toString());
		}
	}
	private void log(double number, boolean newLine) {
		StringBuilder sb = new StringBuilder();
		sb.append(number);
		if(newLine) {
			System.out.println(sb.toString());
        }
		else {
			System.out.print(sb.toString());
		}
	}
	
	/*
	 * File process
	 */
	// Write data to a specified file
	private void writeToFile(String str, BufferedWriter writer) 
		throws IOException {
	    	writer.append(str);
	    	writer.append("\n");
	}
	
	// Erase content from a file
	private void eraseFile(String str) {
	  	File f = new File(str);
	    if(f.exists()){
	    	f.delete();
	    	try {
	    		f.createNewFile();
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	}
	
	// Open the file from disk
	private BufferedWriter openFile(String fileName)
	{
		BufferedWriter writer = null;
		
		eraseFile(fileName);
		
		try {
			writer = new BufferedWriter(new FileWriter(fileName, true));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return writer;
	}
	
	// Close an opened file
	private void closeFile(BufferedWriter writer)
	{
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static void closeFile(BufferedReader reader)
	{
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Words process
	 */	
	// Get words from text
	private void parseText(String str, IndexWords indexWords) {
		// variables for extracting words
		int i = 0;
		boolean setFlag = true;
		StringBuilder tempStr = new StringBuilder("");
		String text = "";
		
		// parse text
		for(i=0; i<str.length();i++) {
			if(str.charAt(i) == ' ' && setFlag) {
				text = tempStr.toString().toLowerCase();
				if(exceptionsObj.hashContains(tempStr.toString().toLowerCase())) {
					// Exception
					indexWords.addToHash(getCanonicalForm(text));
				} else if(!stopwordsObj.hashContains(tempStr.toString().toLowerCase())) {
					// Dictionary
					indexWords.addToHash(getCanonicalForm(text));
				}
				tempStr = new StringBuilder("");  
				setFlag = false;
			}
			else if(Character.isLetter(str.charAt(i))) {
				tempStr.append(str.charAt(i));
				setFlag = true;
			}
		}
	}
	
	// Parse the keywords from console to a format with words and associated operation
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
	
	// Parse the links and remove the fragment
	private String parseLink(String str) {
		int i = 0;
		boolean setFlag = true;
		StringBuilder tempStr = new StringBuilder("");
		
		for(i=0; i<str.length() && setFlag;i++){
			if(str.charAt(i) != '#'){
				tempStr.append(str.charAt(i));
			}
			else 
			{
				setFlag = false;
			}
		}
		
		return tempStr.toString();
	}

	// Read keywords from console
	private String readKeywords() {
		Scanner scanner = new Scanner(System.in);
		String keywords = scanner.nextLine();		
		return keywords;
	}
		
	/*
	 * Porter algorithm
	 */
	private String getCanonicalForm(String text) {
		PorterStemmer porterStemmer = new PorterStemmer();
		String stem = porterStemmer.stemWord(text);
		
		return stem;
	}
	
	/*
	 * Direct Index
	 */
	// Add word to hash
	private void addToHash(String _doc, IndexWords _words)
	{
		if(!hashContains(_doc)) {
			docKeys.put(_doc, _words);
		}
	}
	
	// Check if word exists in hash
	private boolean hashContains(String _doc)
	{
		return docKeys.containsKey(_doc);
	}
	
	// Display words from hash (direct indexing)
	private void showDirectIndex() {
		log("> Showing direct index: ", false);
		for (String doc: docKeys.keySet()) {
            String key = doc.toString();
            IndexWords value = docKeys.get(doc);  
            log("<" + key + ", ", false);  
            value.showHash();
            log(">", true);
		} 
	}
	
	/*
	 * Inverse Index
	 */
	// Add word to hash
	private void addToHash(String _text, Link _link)
	{
		if(!hashLinkContains(_text)) {
			LinksList ll = new LinksList(_link);
			wordLinks.put(_text, ll);
		}
		else {
			LinksList ll = (LinksList)wordLinks.get(_text);
			ll.addLink(_link);
			wordLinks.replace(_text, ll);
		}
	}
	
	// Check if word exists in hash
	private boolean hashLinkContains(String _doc)
	{
		return wordLinks.containsKey(_doc);
	}

	// Display words from hash (inverse indexing)
	private void showInverseIndex() {
		int nr = 0;
		
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
		log(">", false);
	}
	
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
	
	/*
	 * Process files
	 */	
	// Process the HTML file
	private void processHTML(String _link, String _path) {
		String dataFile = "./files/output/data_file.txt";
		String linksFile = "./files/output/links_file.txt";
		File input = new File(_path);
		BufferedWriter writerData = openFile(dataFile);
		BufferedWriter writerLink = openFile(linksFile);
		Document doc;

		try {
			/*
			 * Extract data
			 */
			doc = Jsoup.parse(input, null);					// get document from local file	
			// doc = Jsoup.connect(_link).get();			// get document from web
			writeToFile(doc.title(), writerData);
			
			// meta elements
			Elements metaElements = doc.select("meta");
			for (Element meta : metaElements) {
				if(meta.attr("name") == "keywords" || meta.attr("name") == "description") {
					writeToFile(meta.attr("content"), writerData);
				}
				if(meta.attr("name") == "robots") {
					writeToFile(meta.attr("content"), writerLink);
				}
			}
			
			// a attributes
			Elements aElements = doc.select("a");
			for (Element a : aElements) {
				if(a.absUrl("href") != "") {
					writeToFile(parseLink(a.absUrl("href")), writerLink);
				}	
			}
			
			// text from body
			writeToFile(doc.body().text(), writerData);
			
			/*
			 * Process data
			 */
			// parse text
			parseText(doc.body().text(), indexObj);
			// add to hash
			addToHash(_link, indexObj);
			
			//showHash();
			
		} catch (IOException e) {
			log(e.getMessage(), true);
		}
		
		// close files
		closeFile(writerData);
		closeFile(writerLink);
	}
	
	// Process a text file (.txt extension)
	private void processTextFile(String _path) {
		StringBuffer stringBuffer = new StringBuffer();
		String line = null;
		BufferedReader reader = null;	
		indexObj = new IndexWords();

		/*
		 * Extract data
		 */
		try {
			reader = new BufferedReader(new FileReader(_path));
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		try {
			while((line = reader.readLine()) != null) {
				stringBuffer.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		 * Process data
		 */
		// parse text
		parseText(stringBuffer.toString(), indexObj);
		// add to hash
		addToHash(_path, indexObj);
		
		closeFile(reader);
	}

	/*
	 * Indexing
	 */
	// Indexes the files in the queue
	private void indexFiles(Queue<String> files, int _limitLinks) {
		int limit = 0;
		int i;

		log("> Building direct index", true);
		
		while(!files.isEmpty() && (limit < _limitLinks || _limitLinks == 0)) {
		  String element = files.poll();
      	
		  processTextFile(element);
		  
		  limit++;
		}
	}
	
	// Create the inverse index from the direct indexing
	private void inverseIndex() {
		log("> Building inverse index", true);
		
		for (String doc: docKeys.keySet()) {
            String key = doc.toString();
            IndexWords value = docKeys.get(doc);
            
            HashMap<String, Integer> indWords = value.getHashMap();
            
            for (String docW: indWords.keySet()) {
                String keyW = docW.toString();
                int valueW = indWords.get(docW);
                
                Link link = new Link(key, valueW);
                
                addToHash(keyW, link);
            }
		}
	}
	
	/*
	 * Search
	 */
	// Binary Search
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
	
	// Returns a vector with tf*idf for searched words
	private ArrayList<Double> calculateQueryVector(ArrayList<WordOperation> words_list) {
		int nrQueryWords = words_list.size();
		ArrayList<Double> vector = new ArrayList<Double>();
		
		int nrDocuments = docKeys.size();
		
		for(WordOperation word: words_list) {
			double tf = 1.0 / (double)nrQueryWords;
			double idf;
			LinksList listDocs = getWordLocations(word.getWord());
			double nrWordDocs = 1;
			try {
				nrWordDocs += listDocs.size();
			}
			catch(NullPointerException ex){}
			
			double res = nrDocuments / nrWordDocs;
			
			idf = Math.log(res);
			
			vector.add(tf*idf);
		}
		
		return vector;
	}
	
	// Vectorial Search
	private void vectorialSearch() {
		boolean exit = false;
		while(exit == false) {
			log("> Search: ", false);
			
			//String keywords = readKeywords();			
		
			String keywords = "anna  has apples and fears";
			log(keywords, true);
			exit = true;
			
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
				log(list.size(), false);
				log(" results: ", true);				
				
				HashMap<String, Double> cosSimVal = new HashMap<String, Double>(); 
				
				double idf = getInverseDocumentFrequency(word);
				for(Link l: list.getLinks()) {
					String link = l.getLink();
					if(hashContains(link)) {
						double tf = docKeys.get(link).getTfOfWord(word);
						cosSimVal.put(link, cosineSimilarity(vector.get(0), tf*idf));
					}
				}				
				
				showResults(cosSimVal);
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
					double idf = getInverseDocumentFrequency(w.getWord());
					for(Link l: docs_list.getLinks()) {
						String link = l.getLink();
						if(hashContains(link)) {
							double tf = docKeys.get(link).getTfOfWord(w.getWord());
							link_vectors.add(tf*idf);
						}
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

	/*
	 * Functions for vectorial Search
	 */
	
	// calculate tf for documents
	private void calculateTfForDocs() {
		for (String doc: docKeys.keySet()) {
            String key = doc.toString();
            IndexWords value = docKeys.get(doc);  
            value.calculateTf();
		} 
	}
	
	// show tf for documents
	private void showTfForDocs() {
		log("> Showing tf for documents: ", false);
		for (String doc: docKeys.keySet()) {
            String key = doc.toString();
            IndexWords value = docKeys.get(doc);  
            log("< " + key + ", ", false);
            value.showTf();
            log(" >", true);
		} 
	}	
	
	// show the hash map containing the search results
	private void showResults(HashMap<String, Double> hash_map) {
		Map<String, Double> hm = sortByValue(hash_map); 
		  
        // print the sorted hashmap 
        for (Map.Entry<String, Double> en : hm.entrySet()) { 
            log(" - Document: " + en.getKey() + " [" + en.getValue() + "]", true); 
        } 
	}
	
	// print to console sorted results
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
	
	/*
	 * metoda returneaza idf pentru un anumit termen.
	 * @param term : un termen dintr-un document
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
	
	public double cosineSimilarity(double A, double B) {
    	double sumProduct = 0;
    	double sumASq = 0;
    	double sumBSq = 0;
    	
    	sumProduct += A * B;
    	sumASq += A * A;
    	sumBSq += B * B;
    	
    	if (sumASq == 0 && sumBSq == 0) {
    		return 2.0;
    	}
    	return sumProduct / (Math.sqrt(sumASq) * Math.sqrt(sumBSq));
    }
	
	public double cosineSimilarity(ArrayList<Double> A, ArrayList<Double> B) {
    	if (A == null || B == null || A.size() == 0 || B.size() == 0 || A.size() != B.size()) {
    		return 2;
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
    		return 2.0;
    	}
    	return sumProduct / (Math.sqrt(sumASq) * Math.sqrt(sumBSq));
    }
	
	// main function
	public static void main(String[] args) {
		SearchEngine parser = new SearchEngine();
		FileExplorer fileExp = new FileExplorer();
		Queue<String> files;
		int level = 0;									// how many levels to search recursively
		int links = 0;									// limit the number of links from the queue
		String directory;
		
		//parser.processHTML(link, path);
		
		//parser.log("> Type the selected directory: ", false);
		//directory = parser.readKeywords();
		
		//directory = "D:\\Facultate\\Anul 4\\Semestrul I\\ALPD\\Tema de casa\\test_files";
		directory = "E:\\Facultate\\Anul IV - Facultate\\Semestrul I\\ALPD - Algoritmi paraleli si distribuiti\\Tema de casa\\test-files\\test-files";
		
		parser.log("> Getting files from folder: " + directory, true);
				
		fileExp.searchFiles(directory, level, 0);
		files = fileExp.getFiles();
		
		parser.indexFiles(files, links);
		//parser.showDirectIndex();
		
		parser.calculateTfForDocs();
		//parser.showTfForDocs();
		
		parser.inverseIndex();
		//parser.showInverseIndex();
		
		parser.calculateIdf();
		//parser.showIdfForTerms();
		
		//parser.binarySearch();
		
		parser.vectorialSearch();
	}

}
