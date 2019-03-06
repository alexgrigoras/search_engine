/**
 * @title Search engine application
 * @author Alexandru Grigoras
 * @version 2.0 
 */

package riw;

import java.util.HashMap;

public class IndexWords {
	private HashMap<String, Integer> indWords = null;				// hash set with words
	
	public IndexWords() {
	    indWords = new HashMap<String, Integer>();
	}	
	
	// add word to hash
	public void addToHash(String _text)
	{
		int oldFreq = 0;
		
		if(!hashContains(_text)) {
			indWords.put(_text, 1);
		} else {
			oldFreq = indWords.get(_text);
			indWords.replace(_text, oldFreq, oldFreq + 1);
		}
	}
	
	// check if word exists in hash
	private boolean hashContains(String _text)
	{
		return indWords.containsKey(_text);
	}
	
	public void showHash() {
		int nr = 0;
		System.out.print("{ ");  
		for (String word: indWords.keySet()) {
			nr++;
            String key = word.toString();
            int value = indWords.get(word);  
            System.out.print(key + ": " + value);  
            if(indWords.size() > nr )
            {
            	System.out.print(", ");
            }
		} 
		System.out.print(" }");
	}
	
	public HashMap<String, Integer> getHashMap() {
		return indWords;
	}
	
	public int hashWordsNr() {
		return indWords.size();
	}
	
	// MAIN function
	public static void main(String[] args) {
		
	}
}