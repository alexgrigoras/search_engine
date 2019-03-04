/**
 * @title Search engine application
 * @author Alexandru Grigoras
 * @version 2.0 
 */
	
package riw;

import java.io.BufferedReader;
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class SearchEngine {
	/*
	 * Arguments
	 */
	// create hash for exceptions and stopwords
	private SpecialWords st_obj;
	private SpecialWords exc_obj;
	private IndexWords ind_obj;
	// queue for processing links
	private Queue<String> queueLinks;
	
	/*
	 * Methods
	 */
	public SearchEngine()
	{
		st_obj = new SpecialWords("stop_words.txt");
		exc_obj = new SpecialWords("exception_words.txt");
		ind_obj = new IndexWords();
		queueLinks = new LinkedList<String>();
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
	private void parse_text(String str) {
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
					ind_obj.addToHash(text);			
				} else if(!st_obj.hashContains(temp_str.toString().toLowerCase())) {
					// Dictionary
					ind_obj.addToHash(text);
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

	
	// Process the links
	private void processLink(String _link) {
		BufferedWriter writerText = openFile("fisier_text.txt");
		BufferedWriter writerLink = openFile("fisier_link.txt");
		Document doc;

		try {
			/*
			 * Extract data
			 */
			// document title
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
					queueLinks.add(parse_link(a.absUrl("href")));
				}	
			}
			
			// text from body
			write_to_file(doc.body().text(), writerText);
			
			/*
			 * Process data
			 */
			// parse text
			parse_text(doc.body().text());
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
		
		for(i=1; i<_level; i++) {
			limit = 0;

			while(!queueLinks.isEmpty() && (limit < _limitLinks || _limitLinks == 0)) {
			  String element = queueLinks.poll();
			  
			  processLink(element);
			  
			  limit++;
			}
		}
		
		System.out.print(ind_obj.hashWordsNr() + " -> ");
		ind_obj.showHash();
	}
	
	// MAIN function
	public static void main(String[] args) {
		SearchEngine parser = new SearchEngine();
		int level = 10;
		int links = 100;
		
		parser.indexLinks("http://en.wikipedia.org/", level, links);
	}

}
