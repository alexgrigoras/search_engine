/*
 * Title: Search engine application
 * Author: Alexandru Grigoras
 */

package riw;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlParser {
	
	private static void log(String msg, String... vals) {
        System.out.println(String.format(msg, vals));
	}
	
	public static void write_to_file(String str, BufferedWriter writer) 
		throws IOException {
	    	writer.append(str);
	    	writer.append("\n");
	}
	
	public static void erase_file(String str) {
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
	
	public static void parse_text(String str) {
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
	
	public static String parse_link(String str) {
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

	public static void main(String[] args) {
		// file names
		String fileNameText = "fisier_text.txt";
		String fileNameLink = "fisier_link.txt";
		
		// open files to write
		BufferedWriter writer1 = null;
		BufferedWriter writer2 = null;
		
		erase_file(fileNameText);
		erase_file(fileNameLink);
		
		try {
			writer1 = new BufferedWriter(new FileWriter(fileNameText, true));
			writer2 = new BufferedWriter(new FileWriter(fileNameLink, true));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// create document
		Document doc;
		
		// parse document
		try {
			// document title
			doc = Jsoup.connect("http://en.wikipedia.org/").get();
			//log(doc.title());
			write_to_file(doc.title(), writer1);
			
			// meta elements
			Elements metaElements = doc.select("meta");
			for (Element meta : metaElements) {
				if(meta.attr("name") == "keywords" || meta.attr("name") == "description") {
					//log("\n\t%s", meta.attr("content"));
					write_to_file(meta.attr("content"), writer1);
				}
				if(meta.attr("name") == "robots") {
					//log("\n\t%s", meta.attr("content"));
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
		try {
			writer1.close();
			writer2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
