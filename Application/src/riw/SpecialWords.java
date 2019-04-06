/**
 * @title Search engine application
 * @author Alexandru Grigoras
 * @version 4.0 
 */

package riw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class SpecialWords {
	/**
	 * Variables
	 */
	private HashSet<String> sWords = null;					// hash set with words
	private String fileName;								// file name
	
	/**
	 * SpecialWords constructor
	 * @param _fileName
	 */
	public SpecialWords(String _fileName) {
		fileName = _fileName;
		sWords = new HashSet<String>();		
		makeHashTable();
	}
	
	/**
	 * add word to hash
	 * @param _text
	 */
	private void addToHash(String _text)
	{
		sWords.add(_text);
	}
	
	/**
	 * check if word exists in hash
	 * @param _text
	 * @return
	 */
	public boolean hashContains(String _text)
	{
		return sWords.contains(_text);
	}
	
	/**
	 * show hash table
	 */
	public void showHashTable()
	{
		System.out.println(sWords.toString());
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
	 * create the hash table
	 */
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
	
	/**
	 * main function
	 * @param args
	 */
	public static void main(String[] args) {
		SpecialWords st_obj = new SpecialWords("stop_words.txt");
		st_obj.showHashTable();
		SpecialWords exc_obj = new SpecialWords("exception_words.txt");
		exc_obj.showHashTable();
	}
}
