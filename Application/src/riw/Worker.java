/**
 * @title Search engine application
 * @author Alexandru Grigoras
 * @version 4.0 
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
	 * Variables
	 */
	private Thread t;										// thread object
	private String threadName;								// thread name
	private Queue<String> filesQueue;						// a queue for files to be processed
	private SpecialWords stopwordsObj;						// hash for stopwords
	private SpecialWords exceptionsObj;						// hash for exceptions
	private IndexWords indexObj;							//
	private HashMap<String, IndexWords> docKeys;			// local direct indexing and tf
	private HashMap<String, LinksList> wordLinks;			// local inverse indexing
	
	/**
	 * Class constructor
	 * @param _threadName: the thread name
	 * @param _files: a queue with files to process
	 */
	public Worker(String _threadName, Queue<String> _files)
	{
		stopwordsObj = new SpecialWords("./files/special_words/stop_words.txt");
		exceptionsObj = new SpecialWords("./files/special_words/exception_words.txt");
		docKeys = new HashMap<String, IndexWords>();
		wordLinks = new HashMap<String, LinksList>();
		indexObj = new IndexWords();
		threadName = _threadName;
		filesQueue = _files;
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
	 * Write a string to a specified file
	 * @param str: string to be written
	 * @param writer: the file writer opened for writing
	 * @throws IOException
	 */
	private void writeStringToFile(String str, BufferedWriter writer) 
		throws IOException {
	    	writer.append(str);
	    	writer.append("\n");
	}
	 
	/**
	 * Erase content from a file
	 * @param str: file name
	 */
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
	
	/**
	 * Open the file from disk
	 * @param fileName
	 * @return
	 */
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
	
	/**
	 * Close an opened file
	 * @param writer
	 */
	private void closeFile(BufferedWriter writer)
	{
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Close an opened file
	 * @param reader
	 */
	private static void closeFile(BufferedReader reader)
	{
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get words from text
	 * @param str
	 * @param indexWords
	 */
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
	
	/**
	 * Parse the links and remove the fragment
	 * @param str
	 * @return
	 */
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

	/**
	 * Porter algorithm
	 * @param text
	 * @return
	 */
	private String getCanonicalForm(String text) {
		PorterStemmer porterStemmer = new PorterStemmer();
		String stem = porterStemmer.stemWord(text);
		
		return stem;
	}
	
	/**
	 * Add word to hash
	 * @param _doc
	 * @param _words
	 */
	private void addToHash(String _doc, IndexWords _words)
	{
		if(!hashContains(_doc)) {
			docKeys.put(_doc, _words);
		}
	}
	
	/**
	 * Check if word exists in hash
	 * @param _doc
	 * @return
	 */
	private boolean hashContains(String _doc)
	{
		return docKeys.containsKey(_doc);
	}
	
	/**
	 * Display words from hash (direct indexing)
	 */
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
	 * Write inverse index to file
	 */
	public void writeInverseIndexToFile() {
		log("> Writing inverse index to file", true);
		
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

	/** 
	 * Process the HTML file
	 * @param _link
	 * @param _path
	 */
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
			writeStringToFile(doc.title(), writerData);
			
			// meta elements
			Elements metaElements = doc.select("meta");
			for (Element meta : metaElements) {
				if(meta.attr("name") == "keywords" || meta.attr("name") == "description") {
					writeStringToFile(meta.attr("content"), writerData);
				}
				if(meta.attr("name") == "robots") {
					writeStringToFile(meta.attr("content"), writerLink);
				}
			}
			
			// a attributes
			Elements aElements = doc.select("a");
			for (Element a : aElements) {
				if(a.absUrl("href") != "") {
					writeStringToFile(parseLink(a.absUrl("href")), writerLink);
				}	
			}
			
			// text from body
			writeStringToFile(doc.body().text(), writerData);
			
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
	
	/**
	 * Process a text file (.txt extension)
	 * @param _path
	 */
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

	/**
	 * Indexes the files in the queue
	 * @param files
	 */
	public void indexFiles(Queue<String> files) {
		log("> Building direct index with " + files.size() + " text documents", true);
		
		while(!files.isEmpty()) {
		  String element = files.poll();
		  processTextFile(element);
		}
	}
	
	/**
	 * Create the inverse index from the direct indexing
	 */
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
	
	/**
	 * Calculate tf for documents
	 */
	public void calculateTfForDocs() {
		for (String doc: docKeys.keySet()) {
            String key = doc.toString();
            IndexWords value = docKeys.get(doc);  
            value.calculateTf();
		} 
	}
	
	/**
	 * Show tf for documents
	 */
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
	
	/**
	 * Write tf to file
	 */
	private void writeTfToFile() {
		log("> Writing tf to file", true);
		
		JSONArray docsJson = new JSONArray();
		
		for (String doc: docKeys.keySet()) {
			
            String key = doc.toString();
            IndexWords value = docKeys.get(doc);
            
            HashMap<String, Double> termTf = value.getTf(); 
            
            JSONArray terms = new JSONArray();
            
            for (String word: termTf.keySet()) {
            	JSONObject term = new JSONObject();
            	term.put("k", word.toString());
            	term.put("tf", termTf.get(word));
            	
            	terms.add(term);
    		} 
            
            JSONObject docName = new JSONObject();
            docName.put("doc", key);
            docName.put("terms", terms);
            
            docsJson.add(docName);
		}
		
		//Write JSON file
        try (FileWriter file = new FileWriter("files/tf/" + threadName + ".json")) {
 
            file.write(docsJson.toJSONString());
            file.flush();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	/**
	 * Get the thread object
	 * @return thread
	 */
	public Thread getThread() {
		return t;
	}	
	
	/*
	 * Run method (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		log("> Running " +  threadName, true);

		this.indexFiles(filesQueue);
		// this.showDirectIndex();
		
		this.calculateTfForDocs();
		// this.showTfForDocs();
		this.writeTfToFile();	  
		
		this.inverseIndex();
		// this.showInverseIndex();
		this.writeInverseIndexToFile();		
		
		System.out.println("> Thread " +  threadName + " exiting");
	}
	
	/*
	 * Start method (non-Javadoc)
	 * @see java.lang.Thread#start()
	 */
	public void start () {
		log("> Starting " +  threadName, true);
		
		if (t == null) {
			t = new Thread (this, threadName);
			t.start ();
		}
   }
}
