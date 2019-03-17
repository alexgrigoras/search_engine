/**
 * @title Search engine application
 * @author Alexandru Grigoras
 * @version 2.0 
 */
	
package riw;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SearchEngine {
	/*
	 * Arguments
	 */
	private SpecialWords stopwordsObj;						// create hash for exceptions and stopwords
	private SpecialWords exceptionsObj;
	private IndexWords indexObj;
	
	private Queue<String> filesQueue;						// queue for processing links
	private HashMap<String, IndexWords> docKeys;	
	private HashMap<String, LinksList> wordLinks;
	
	/*
	 * Methods
	 */
	// Class constructor
	public SearchEngine()
	{
		stopwordsObj = new SpecialWords("./files/special_words/stop_words.txt");
		exceptionsObj = new SpecialWords("./files/special_words/exception_words.txt");
		docKeys = new HashMap<String, IndexWords>();
		filesQueue = new LinkedList<String>();
		wordLinks = new HashMap<String, LinksList>();
		indexObj = new IndexWords();
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
					indexWords.addToHash(text);			
				} else if(!stopwordsObj.hashContains(tempStr.toString().toLowerCase())) {
					// Dictionary
					indexWords.addToHash(text);
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
				word = new WordOperation(text);		
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
		for (String doc: docKeys.keySet()) {
            String key = doc.toString();
            IndexWords value = docKeys.get(doc);  
            System.out.print("<" + key + ", ");  
            value.showHash();
            System.out.println(">");
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
		System.out.print("< ");  
		for (String doc: wordLinks.keySet()) {
			nr++;
            String key = doc.toString();
            LinksList value = wordLinks.get(doc);
            System.out.print(key + ": ");
            value.show();
            if(wordLinks.size() > nr )
            {
            	System.out.print(", ");
            }            
		}
		System.out.println(">");
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
			System.out.println(e.getMessage());
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
	
	// Returns the file extension from the file name
	private String getFileExtension(File file) {
	    String name = file.getName();
	    int lastIndexOf = name.lastIndexOf(".");
	    if (lastIndexOf == -1) {
	        return ""; 											// empty extension
	    }
	    return name.substring(lastIndexOf + 1).toString();
	}
	
	// Searches for the files in a specified folder recursively
	private void getFiles(String _folder_path, int max_level, int current_level) {		
		if(current_level > max_level && max_level != 0) {
			return;
		}
		
		current_level++;
		
        File fileName = new File(_folder_path);
        File[] fileList = fileName.listFiles();
        
        for (File file: fileList) {
            if(file.isFile()) {
            	if(getFileExtension(file).equals("txt")) {
                	filesQueue.add(file.toString());
            	}
            }
            else if(file.isDirectory()) {
            	getFiles(file.toString(), max_level, current_level);
            }
            
        }		
	}

	// Indexes the files in the queue
	private void indexFiles(int _limitLinks) {
		int limit = 0;
		int i;

		log("> Building direct index", true);
		
		while(!filesQueue.isEmpty() && (limit < _limitLinks || _limitLinks == 0)) {
		  String element = filesQueue.poll();
      	
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
	
	private String readKeywords() {		
		Scanner scanner = new Scanner(System.in);
		String keywords = scanner.nextLine();		
		return keywords;
	}
	
	private void searchKeywords() {
		boolean exit = false;
		while(exit == false) {
			System.out.print("> Search: ");
			
			String keywords = readKeywords();			
			ArrayList<WordOperation> kw_list = new ArrayList<WordOperation>();
			ArrayList<String> temp_list = new ArrayList<String>();
			int list_dimension = 0;
			LinksList words_list = new LinksList();
			
			if(keywords.equals("exit")) {
				exit = true;
				break;
			}
			
			System.out.println("> Searched keywords: ");
			
			kw_list = parseKeywords(keywords);
			
			list_dimension = kw_list.size();
			
			if(list_dimension == 0) {
				System.out.println("No keywords");
			}
			else if(list_dimension == 1) {
				String word = kw_list.get(0).getWord();
				
				System.out.print(word + " -> ");
				
				LinksList list = getWordLocations(word);
				list.show();
				
				System.out.println();
			}
			else {
				for(int i = 0; i < list_dimension - 1; i++) {
					if(kw_list.get(i).getOperation() == OpType.OR) {
						String word_1 = kw_list.get(i).getWord();
						String word_2 = kw_list.get(i+1).getWord();
						System.out.print(word_1 + " OR ");
						System.out.println(word_2 + " -> ");
						LinksList list_1 = getWordLocations(word_1);
						LinksList list_2 = getWordLocations(word_2);
						
						for(Link l: list_1.getLinks()) {
							if(!words_list.hasLink(l.getLink())) {
								words_list.addLink(l);
							}
							else {
								words_list.addFreqToLink(l.getLink(), l.getFrequency());
							}
						}
						for(Link l: list_2.getLinks()) {
							if(!words_list.hasLink(l.getLink())) {
								words_list.addLink(l);
							}
							else {
								words_list.addFreqToLink(l.getLink(), l.getFrequency());
							}
						}
						list_1.show();
						System.out.println();
						list_2.show();
						System.out.println();
						words_list.show();						
						System.out.println();
					}
					else if(kw_list.get(i).getOperation() == OpType.AND) {
						System.out.print(kw_list.get(i).getWord() + " AND ");
						System.out.println(kw_list.get(i+1).getWord());				
					}
					else if(kw_list.get(i).getOperation() == OpType.NOT) {
						System.out.println("NOT " + kw_list.get(i+1).getWord());
					}				
				}
			}
		}
	}
	
	// MAIN function
	public static void main(String[] args) {
		SearchEngine parser = new SearchEngine();
		int level = 0;
		int links = 10;
		String link = "http://en.wikipedia.org/";
		String path = "./files/input/wikipedia_org.html";
		String directory = "E:\\Facultate\\Anul IV - Facultate\\Semestrul I\\ALPD - Algoritmi paraleli si distribuiti\\Tema de casa\\test-files\\test-files";
		
		//parser.processHTML(link, path);
		
		parser.log("> Getting files from folder: " + directory, true);		
		parser.getFiles(directory, level, 0);
		
		parser.indexFiles(links);
		
		//parser.showDirectIndex();
		
		parser.inverseIndex();
		
		//parser.showInverseIndex();

		parser.searchKeywords();
	}

}
