package riw;

import java.util.ArrayList;

class Link
{
	private String link;
	private int frequency;
	
	public Link(String _link)
	{
		link = _link;
		frequency = 1;
	}
	
	public void increaseFrequency()
	{
		frequency++;
	}
}

public class LinksList {
	private ArrayList<Link> list_strings;
	
	public LinksList()
	{
		list_strings = new ArrayList<Link>();
	}
	
	public void addLink(String _link)
	{
		list_strings.add(new Link(_link));
	}
	
	public static void main(String[] args) {

	}

}
