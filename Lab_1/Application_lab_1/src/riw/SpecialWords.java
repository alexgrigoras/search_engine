/**
 * @title Search engine application
 * @author Alexandru Grigoras
 * @version 1.0 
 */

package riw;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

public class SpecialWords {

	// hash set with words
	private HashSet<String> s_words = null;
	// file name
	private String fileName;
	// file reader
	BufferedReader reader;
	
	// SpecialWords constructor
	public SpecialWords(String _fileName) {
		fileName = _fileName;
		s_words = new HashSet<String>();
		
		try {
			reader = new BufferedReader(new FileReader(fileName));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		makeHashTable();
	}
	
	// add word to hash
	private void addToHash(String text)
	{
		s_words.add(text);
	}
	
	// check if word exists in hash
	private boolean hashContains(String text)
	{
		return s_words.contains(text);
	}
	
	// show hash table
	public void showHashTable()
	{
		System.out.println(s_words.toString());
	}
	
	// create the hash table
	private void makeHashTable()
	{
		String stringLine = "";

		try {
			while ((stringLine = reader.readLine()) != null) {
				//System.out.print(stringLine + ' ');
				addToHash(stringLine);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// MAIN function
	public static void main(String[] args) {
		SpecialWords st_obj = new SpecialWords("stopwords.txt");
		st_obj.showHashTable();
	}

}
