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
	
	public Link(Link _link)
	{
		link = _link.getLink();
		frequency = _link.getFrequency();
	}
	
	public Link(String _link, int _frequency)
	{
		link = _link;
		frequency = _frequency;
	}
	
	public String getLink() {
		return link;
	}
	
	public int getFrequency() {
		return frequency;
	}
	
	public void addToFrequency(int _frequency) {
		frequency += _frequency;
	}
	
	public String toString() {
		return link + ": " + frequency;
		
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
	
	public void addLink(String link_name) {
		list_strings.add(new Link(link_name));
	}
	
	public void addLink(Link _link)
	{
		list_strings.add(new Link(_link));
	}
	
	public boolean hasLink(String _link_name) {
		for(Link l : list_strings) {
			if(l.verifyLink(_link_name)) {
				return true;
			}
		}
		return false;
	}
	
	public void addFreqToLink(String _link_name, int frequency) {
		for(Link l : list_strings) {
			if(l.verifyLink(_link_name)) {
				l.addToFrequency(frequency);
			}
		}
	}
	
	public ArrayList<Link> getLinks() {
		return list_strings;
	}
	
	public void show() {
		int nr = 0;
		
        System.out.print("{ ");  
        for(Link l: list_strings) {
        	nr++;
        	System.out.print(l.toString());
            if(list_strings.size() > nr )
            {
            	System.out.print(", ");
            }           
        }
        System.out.print(" }");
	}
	
	public static void main(String[] args) {

	}

	public int size() {
		return list_strings.size();
	}

}
