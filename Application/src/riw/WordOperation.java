package riw;

public class WordOperation {
	private String word;
	private OpType operation;
	
	public enum OpType 
	{ 
	    ADD,
	    OR, 
	    NOT
	}
	
	public WordOperation(String _word) {
		word = _word;
		operation = null;
	}
	
	public void setOperation(OpType _operation)	{
		operation = _operation;
	}
	
	public String getWord()	{
		return word;
	}
	
	public OpType getOperation() {
		return operation;
	}
	
	public String toString() {
		if(operation != null) {
			return word + "(" + operation.toString() + ")"; 
		}
		else {
			return word;
		}
	}
}
