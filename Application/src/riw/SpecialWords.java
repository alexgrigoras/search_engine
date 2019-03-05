/**
 * @title Search engine application
 * @author Alexandru Grigoras
 * @version 2.0 
 */

package riw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class SpecialWords {
	private HashSet<String> s_words = null;					// hash set with words
	private String fileName;								// file name
	
	// SpecialWords constructor
	public SpecialWords(String _fileName) {
		fileName = _fileName;
		s_words = new HashSet<String>();		
		makeHashTable();
	}
	
	// add word to hash
	private void addToHash(String _text)
	{
		s_words.add(_text);
	}
	
	// check if word exists in hash
	public boolean hashContains(String _text)
	{
		return s_words.contains(_text);
	}
	
	// show hash table
	public void showHashTable()
	{
		System.out.println(s_words.toString());
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
	
	// create the hash table
	private void makeHashTable()
	{
		BufferedReader reader = null;									
		String stringLine = "";

		try {
			reader = new BufferedReader(new FileReader(fileName));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			while ((stringLine = reader.readLine()) != null) {
				addToHash(stringLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		closeFile(reader);
	}
	
	// MAIN function
	public static void main(String[] args) {
		SpecialWords st_obj = new SpecialWords("stop_words.txt");
		st_obj.showHashTable();
		SpecialWords exc_obj = new SpecialWords("exception_words.txt");
		exc_obj.showHashTable();
	}
}
