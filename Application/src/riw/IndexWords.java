/**
 * @title Search engine application
 * @author Alexandru Grigoras
 * @version 2.0 
 */

package riw;

import java.util.HashMap;

public class IndexWords {
	private HashMap<String, Integer> ind_words = null;				// hash set with words
	
	public IndexWords() {
	    ind_words = new HashMap<String, Integer>();
	}	
	
	// add word to hash
	public void addToHash(String _text)
	{
		int oldFreq = 0;
		
		if(!hashContains(_text)) {
			ind_words.put(_text, 1);
		} else {
			oldFreq = ind_words.get(_text);
			ind_words.replace(_text, oldFreq, oldFreq + 1);
		}
		
	}
	
	// check if word exists in hash
	private boolean hashContains(String _text)
	{
		return ind_words.containsKey(_text);
	}
	
	public void showHash() {
		//System.out.print(ind_words.toString());
		int nr = 0;
		System.out.print("{ ");  
		for (String word: ind_words.keySet()) {
			nr++;
            String key = word.toString();
            int value = ind_words.get(word);  
            System.out.print(key + ": " + value);  
            if(ind_words.size() > nr )
            {
            	System.out.print(", ");
            }
		} 
		System.out.print(" }");  
		
	}
	
	public int hashWordsNr() {
		return ind_words.size();
	}
	
	// MAIN function
	public static void main(String[] args) {
		
	}
}