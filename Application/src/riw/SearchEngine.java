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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

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
	public SearchEngine()
	{
		stopwordsObj = new SpecialWords("./files/special_words/stop_words.txt");
		exceptionsObj = new SpecialWords("./files/special_words/exception_words.txt");
		indexObj = new IndexWords();
		docKeys = new HashMap<String, IndexWords>();
		filesQueue = new LinkedList<String>();
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
	
	// add word to hash
	private void addToHash(String _doc, IndexWords _words)
	{
		if(!hashContains(_doc)) {
			docKeys.put(_doc, _words);
		}		
	}
	
	// check if word exists in hash
	private boolean hashContains(String _doc)
	{
		return docKeys.containsKey(_doc);
	}
	
	private void showHash() {
		for (String doc: docKeys.keySet()) {
            String key = doc.toString();
            IndexWords value = docKeys.get(doc);  
            System.out.print("<" + key + ", ");  
            value.showHash();
            System.out.println(">");
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
			doc = Jsoup.parse(input, null);			// get document from local file	
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
	
	// Process the HTML file
	private void processTextFile(String _path) {
		StringBuffer stringBuffer = new StringBuffer();
		String line = null;
		BufferedReader reader = null;									

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
	
	private String getFileExtension(File file) {
	    String name = file.getName();
	    int lastIndexOf = name.lastIndexOf(".");
	    if (lastIndexOf == -1) {
	        return ""; // empty extension
	    }
	    return name.substring(lastIndexOf + 1).toString();
	}
	
	// processes the links and indexes the words
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

	public void indexFiles(int _limitLinks) {
		int limit;
		int i;

		limit = 0;

		while(!filesQueue.isEmpty() && (limit < _limitLinks || _limitLinks == 0)) {
		  String element = filesQueue.poll();
      	
		  processTextFile(element);
		  
		  limit++;
		}
	}
	
	// MAIN function
	public static void main(String[] args) {
		SearchEngine parser = new SearchEngine();
		int level = 1;
		int links = 0;
		String link = "http://en.wikipedia.org/";
		String path = "./files/input/wikipedia_org.html";
		
		//parser.processHTML(link, path);
		
		parser.getFiles("F:\\Materiale_an_4_ac\\eclipseriw", level, 0);
		
		parser.indexFiles(links);
		
		parser.showHash();
	}

}
