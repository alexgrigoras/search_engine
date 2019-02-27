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
	public void addToHash(String text)
	{
		int oldFreq = 0;
		
		if(!hashContains(text)) {
			ind_words.put(text, 1);
		} else {
			oldFreq = ind_words.get(text);
			ind_words.replace(text, oldFreq, oldFreq + 1);
		}
		
	}
	
	// check if word exists in hash
	private boolean hashContains(String text)
	{
		return ind_words.containsKey(text);
	}
	
	public void showHash() {
		System.out.println(ind_words.toString());
	}
	
	// MAIN function
	public static void main(String[] args) {
		
	}
}