package riw;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Worker extends Thread{
	/**
	 * Arguments
	 */
	private Thread t;
	private String threadName;
	Queue<String> files;
	int links = 5;
	   
	private SpecialWords stopwordsObj;						// create hash for exceptions and stopwords
	private SpecialWords exceptionsObj;
	private IndexWords indexObj;
	private HashMap<String, IndexWords> docKeys;			// indexare directa + tf
	private HashMap<String, LinksList> wordLinks;			// indexare inversa	
	private HashMap<String, Double> idf;					// idf
	

	
	/**
	 * Methods
	 */
	// Class constructor

	public Worker(String _threadName, Queue<String> _files, int _links)
	{
		stopwordsObj = new SpecialWords("./files/special_words/stop_words.txt");
		exceptionsObj = new SpecialWords("./files/special_words/exception_words.txt");
		docKeys = new HashMap<String, IndexWords>();
		wordLinks = new HashMap<String, LinksList>();
		indexObj = new IndexWords();
		idf = new HashMap<String, Double>();
		
		threadName = _threadName;
		files = _files;
		links = _links;
	}
	
	// Log data to console
	private static void log(String msg, boolean newLine) {
		if(newLine) {
			System.out.println(msg);
        }
		else {
			System.out.print(msg);
		}
	}
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
			LinksList ll = wordLinks.get(_text);
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
	public void showInverseIndex() {
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
		log(">", true);
	}
	
	public void writeInverseIndexToFile() {
		log("> Writing iverse index to file", true);
		
		JSONArray wordsJson = new JSONArray();
		
		for (String doc: wordLinks.keySet()) {
			
            String key = doc.toString();
            LinksList value = wordLinks.get(doc);
            ArrayList<Link> links = value.getLinks(); 
            
            JSONArray docs = new JSONArray();
            
            for(Link l: links) {
            	JSONObject document = new JSONObject();
            	document.put("d", l.getLink());
            	document.put("c", l.getFrequency());
            	
            	docs.add(document);
            }
            
            JSONObject term = new JSONObject();
            term.put("term", key);
            term.put("docs", docs);
            
            wordsJson.add(term);
		}
		
		//Write JSON file
        try (FileWriter file = new FileWriter("files/indexes/" + threadName + ".json")) {
 
            file.write(wordsJson.toJSONString());
            file.flush();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
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
	public void indexFiles(Queue<String> files, int _limitLinks) {
		int limit = 0;

		log("> Building direct index with " + files.size() + " text documents", true);
		
		while(!files.isEmpty() && (limit < _limitLinks || _limitLinks == 0)) {
		  String element = files.poll();
      	
		  processTextFile(element);
		  
		  limit++;
		}
	}
	
	// Create the inverse index from the direct indexing
	public void inverseIndex() {
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
	 * Functions for vectorial Search
	 */
	
	// calculate tf for documents
	public void calculateTfForDocs() {
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
    		return 0;
    	}
    	return sumProduct / (Math.sqrt(sumASq) * Math.sqrt(sumBSq));
    }
	
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
	
	@Override
	public void run() {
      log("> Running " +  threadName, true);

      this.indexFiles(files, links);
	  //parser.showDirectIndex();
		
	  this.calculateTfForDocs();
	  //parser.showTfForDocs();
		
	  this.inverseIndex();
	  //parser.showInverseIndex();
	  this.writeInverseIndexToFile();		
		
	  this.calculateIdf();
	  //parser.showIdfForTerms();

      System.out.println("> Thread " +  threadName + " exiting.");
   }
	
	public void start () {
      log("> Starting " +  threadName, true);
      if (t == null) {
         t = new Thread (this, threadName);
         t.start ();
      }
   }
}
