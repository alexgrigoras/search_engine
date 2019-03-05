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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import riw.SpecialWords;
import riw.IndexWords;

public class HtmlParser {
	// Log data to console
	private void log(String msg, String... vals) {
        System.out.println(String.format(msg, vals));
	}
	
	// Write data to a specified file
	private static void write_to_file(String str, BufferedWriter writer) 
		throws IOException {
	    	writer.append(str);
	    	writer.append("\n");
	}
	
	// Erase content from a file
	private static void erase_file(String str) {
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
	private static void parse_text(String str) {
		// variables for extracting words
		int i = 0;
		boolean setFlag = true;
		StringBuilder temp_str = new StringBuilder("");
		String text = "";
		
		// create hash for exceptions and stopwords
		SpecialWords st_obj = new SpecialWords("stop_words.txt");
		SpecialWords exc_obj = new SpecialWords("exception_words.txt");
		IndexWords ind_obj = new IndexWords();
		
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
		
		ind_obj.showHash();
	}
	
	// Parse the links and remove the fragment
	private static String parse_link(String str) {
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
	private static BufferedWriter openFile(String fileName)
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
	private static void closeFile(BufferedWriter writer)
	{
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Close an opened file
	private static void closeFile(BufferedReader reader)
	{
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private static void processLink(String _link) {
		BufferedWriter writerText = openFile("fisier_text.txt");
		BufferedWriter writerLink = openFile("fisier_link.txt");
		Document doc;
		
		// parse document
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
				write_to_file(parse_link(a.absUrl("href")), writerLink);
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
	
	private static void indexLinks(String _link, int _level, int _limitLinks) {
		String link = _link;												
		BufferedReader reader = null;
		String stringLine = "";
		int i;
		int limit;
		
		try {
			reader = new BufferedReader(new FileReader("fisier_link.txt"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		processLink(link);
		
		for(i=0; i<_level; i++) {
			
			limit = 0;
			
			try {
				while (((stringLine = reader.readLine()) != null) && (limit < _limitLinks)) {
					if(stringLine != null && stringLine.length() != 0) {
						// System.out.println(stringLine);
						processLink(stringLine);
						limit++;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		closeFile(reader);
	}
	
	// MAIN function
	public static void main(String[] args) {
		indexLinks("http://en.wikipedia.org/", 1, 5);
	}

}
