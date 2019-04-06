package riw;

import java.util.HashMap;

public class WordTf {
	private HashMap<String, Double> tf = null;
	
	/**
	 * Class constructor
	 */
	public WordTf() {
		tf = new HashMap<String, Double>();
	}
	
	public void insertWord(String _term, double _tf_val) {
		tf.put(_term, _tf_val);
	}
	
	public double getTfForWord(String word) {
		if(tf.containsKey(word)) {
			return tf.get(word);
		}
		else {
			return 0;
		}
	}
	
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
