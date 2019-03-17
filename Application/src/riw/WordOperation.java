package riw;

enum OpType 
{ 
    AND,
    OR, 
    NOT
}

public class WordOperation {
	private String word;
	private OpType operation;
		
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
