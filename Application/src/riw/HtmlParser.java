/**
 * @title Search engine application
 * @author Alexandru Grigoras
 * @version 1.0 
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
		int i = 0;
		boolean setFlag = true;
		StringBuilder temp_str = new StringBuilder("");
		
		for(i=0; i<str.length();i++){
			if(str.charAt(i) == ' ' && setFlag){
				System.out.println(temp_str);
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
	
	// MAIN function
	public static void main(String[] args) {
		// open files to write
		BufferedWriter writer1 = openFile("fisier_text.txt");
		BufferedWriter writer2 = openFile("fisier_link.txt");

		// create document
		Document doc;
		
		// parse document
		try {
			// document title
			doc = Jsoup.connect("http://en.wikipedia.org/").get();
			write_to_file(doc.title(), writer1);
			
			// meta elements
			Elements metaElements = doc.select("meta");
			for (Element meta : metaElements) {
				if(meta.attr("name") == "keywords" || meta.attr("name") == "description") {
					write_to_file(meta.attr("content"), writer1);
				}
				if(meta.attr("name") == "robots") {
					write_to_file(meta.attr("content"), writer2);
				}
			}
			
			// a attributes
			Elements aElements = doc.select("a");
			for (Element a : aElements) {
				//log("%s\n\t%s", a.attr("title"), a.absUrl("href"));				
				write_to_file(parse_link(a.absUrl("href")), writer2);
			}
			
			// html text
			write_to_file(doc.body().text(), writer1);
			
			// parse_text 
			parse_text(doc.body().text());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// close files
		closeFile(writer1);
		closeFile(writer2);
	}

}
