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
	
	public Link(String _link, int _frequency)
	{
		link = _link;
		frequency = _frequency;
	}
	
	public String getLink() {
		return link;
	}
	
	public boolean verifyLink(String _link_name) {
		if(link.equals(_link_name)) {
			return true;
		}
		else {
			return false;
		}
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
	
	public LinksList(Link _link) {
		list_strings = new ArrayList<Link>();
		list_strings.add(_link);
	}
	
	public void addLink(String _link)
	{
		list_strings.add(new Link(_link));
	}
	
	public Link hasLink(String _link_name) {
		for(Link l : list_strings) {
			if(l.verifyLink(_link_name)) {
				return l;
			}
		}
		return null;
	}
	
	public static void main(String[] args) {

	}

}
