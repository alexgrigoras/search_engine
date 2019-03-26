/**
 * @title Search engine application
 * @author Alexandru Grigoras
 * @version 3.0 
 */

package riw;

import java.util.HashMap;

/**
 * 
 */
public class IndexWords {
	private HashMap<String, Integer> indWords = null;				// hash set with words
	private HashMap<String, Double> tf = null;
	
	public IndexWords() {
	    indWords = new HashMap<String, Integer>();
	    tf = new HashMap<String, Double>();
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
	
	// displays the hash table
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
	
	// displays the hash table
	public void showTf() {
		int nr = 0;
		System.out.print("{ ");  
		for (String word: tf.keySet()) {
			nr++;
            String key = word.toString();
            Double value = tf.get(word);  
            System.out.print(key + ": " + value);  
            if(tf.size() > nr )
            {
            	System.out.print(", ");
            }
		} 
		System.out.print(" }");
	}
	
	// displays the hash table
	public void calculateTf() {
		for (String word: indWords.keySet()) {
            String key = word.toString();
            int value = indWords.get(word);  
            tf.put(key, 1.0 / getNrWords());
		} 
	}
	
	// returns the hash map
	public HashMap<String, Integer> getHashMap() {
		return indWords;
	}
	
	// returns the size of the hash table
	public int getNrWords() {
		return indWords.size();
	}

}