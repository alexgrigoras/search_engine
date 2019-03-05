/**
 * @title Search engine application
 * @author Alexandru Grigoras
 * @version 2.0 
 */
	
package riw;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import riw.SpecialWords;
import riw.IndexWords;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class SearchEngine {
	/*
	 * Arguments
	 */
	// create hash for exceptions and stopwords
	private SpecialWords st_obj;
	private SpecialWords exc_obj;
	// queue for processing links
	private Queue<String> queue_links;
	private HashMap<String, IndexWords> doc_keys;	
	private HashMap<String, LinksList> word_links;
	/*
	 * Methods
	 */
	public SearchEngine()
	{
		st_obj = new SpecialWords("stop_words.txt");
		exc_obj = new SpecialWords("exception_words.txt");
		doc_keys = new HashMap<String, IndexWords>();
		queue_links = new LinkedList<String>();
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
	private void write_to_file(String str, BufferedWriter writer) 
		throws IOException {
	    	writer.append(str);
	    	writer.append("\n");
	}
	
	// Erase content from a file
	private void erase_file(String str) {
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
	private void parse_text(String str, IndexWords index_words) {
		// variables for extracting words
		int i = 0;
		boolean setFlag = true;
		StringBuilder temp_str = new StringBuilder("");
		String text = "";
		
		// parse text
		for(i=0; i<str.length();i++) {
			if(str.charAt(i) == ' ' && setFlag) {
				text = temp_str.toString().toLowerCase();
				if(exc_obj.hashContains(temp_str.toString().toLowerCase())) {
					// Exception
					index_words.addToHash(text);			
				} else if(!st_obj.hashContains(temp_str.toString().toLowerCase())) {
					// Dictionary
					index_words.addToHash(text);
				}
				
				temp_str.delete(0, temp_str.length());
				setFlag = false;
			}
			else if(Character.isLetter(str.charAt(i))) {
					temp_str = new StringBuilder(temp_str);  
					temp_str.append(str.charAt(i));
					setFlag = true;
			}
		}
	}
	
	// Parse the links and remove the fragment
	private String parse_link(String str) {
		int i = 0;
		boolean setFlag = true;
		StringBuilder temp_str = new StringBuilder("");
		
		for(i=0; i<str.length() && setFlag;i++){
			if(str.charAt(i) != '#'){
				temp_str.append(str.charAt(i));
			}
			else 
			{
				setFlag = false;
			}
		}
		
		return temp_str.toString();
	}
	
	// Open the file from disk
	private BufferedWriter openFile(String fileName)
	{
		BufferedWriter writer = null;
		
		erase_file(fileName);
		
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
	
	// add word to hash
	public void addToHash(String _doc, IndexWords _words)
	{
		if(!hashContains(_doc)) {
			doc_keys.put(_doc, _words);
		}		
	}
	
	// check if word exists in hash
	private boolean hashContains(String _doc)
	{
		return doc_keys.containsKey(_doc);
	}
	
	public void showHash() {
		for (String doc: doc_keys.keySet()) {
            String key = doc.toString();
            IndexWords value = doc_keys.get(doc);  
            System.out.print("<" + key + ", ");  
            value.showHash();
            System.out.println(">");
		} 
	}
	
	// Process the links
	private void processLink(String _link) {
		File input = new File("./files/input/wikipedia.html");
		BufferedWriter writerText = openFile("./files/output/fisier_text.txt");
		BufferedWriter writerLink = openFile("./files/output/fisier_link.txt");
		Document doc;
		IndexWords ind_obj = new IndexWords();

		try {
			/*
			 * Extract data
			 */
			// document title
			//doc = Jsoup.parse(input, null, _link);
			doc = Jsoup.connect(_link).get();
			write_to_file(doc.title(), writerText);
			
			// meta elements
			Elements metaElements = doc.select("meta");
			for (Element meta : metaElements) {
				if(meta.attr("name") == "keywords" || meta.attr("name") == "description") {
					write_to_file(meta.attr("content"), writerText);
				}
				if(meta.attr("name") == "robots") {
					write_to_file(meta.attr("content"), writerLink);
				}
			}
			
			// a attributes
			Elements aElements = doc.select("a");
			for (Element a : aElements) {
				if(a.absUrl("href") != "") {
					write_to_file(parse_link(a.absUrl("href")), writerLink);
					queue_links.add(parse_link(a.absUrl("href")));
				}	
			}
			
			// text from body
			write_to_file(doc.body().text(), writerText);
			
			/*
			 * Process data
			 */
			// parse text
			parse_text(doc.body().text(), ind_obj);
			// add to hash
			addToHash(_link, ind_obj);
			
		} catch (IOException e) {
			//e.printStackTrace();
		}
		
		// close files
		closeFile(writerText);
		closeFile(writerLink);
	}
	
	// processes the links and indexes the words
	public void indexLinks(String _link, int _level, int _limitLinks) {
		String link = _link;												
		int i;
		int limit;

		processLink(link);
		
		for(i=1; i<=_level; i++) {
			limit = 0;

			while(!queue_links.isEmpty() && (limit < _limitLinks || _limitLinks == 0)) {
			  String element = queue_links.poll();
			  
			  processLink(element);
			  
			  limit++;
			}
		}
		
		showHash();
	}
	
	// MAIN function
	public static void main(String[] args) {
		SearchEngine parser = new SearchEngine();
		int level = 0;
		int links = 10;
		
		parser.indexLinks("http://en.wikipedia.org/", level, links);
	}

}
