/**
 * @title Search engine application
 * @author Alexandru Grigoras
 * @version 4.0 
 */

package riw;

import java.util.HashMap;

public class WordTf {
	/**
	 * Variables
	 */
	private HashMap<String, Double> tf = null;
	
	/**
	 * Class constructor
	 */
	public WordTf() {
		tf = new HashMap<String, Double>();
	}
	
	/**
	 * Insert a word in the hash map
	 * @param _term: word name
	 * @param _tf_val: tf value
	 */
	public void insertWord(String _term, double _tf_val) {
		tf.put(_term, _tf_val);
	}
	
	/**
	 * Get the tf value for a specified word
	 * @param word: word name
	 * @return the tf value for the word
	 */
	public double getTfForWord(String word) {
		if(tf.containsKey(word)) {
			return tf.get(word);
		}
		else {
			return 0;
		}
	}
	
	/**
	 * Displays the hash map
	 */
	public void show() {
		int nr = 0;
		System.out.print("{");
		for (String term: tf.keySet()) {
            nr++;
            
            String key = term.toString();
            double value = tf.get(term);
            
            System.out.print(key + ": " + value);

            if(tf.size() > nr)
            {
            	System.out.print(", ");
            }            
		}
		System.out.print("}");
	}
}
