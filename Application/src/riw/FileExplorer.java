/**
 * @title Search engine application
 * @author Alexandru Grigoras
 * @version 3.0 
 */

package riw;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 
 */
public class FileExplorer {
	private Queue<String> filesQueue;						// queue for processing links
	
	// Constructor
	public FileExplorer() {
		filesQueue = new LinkedList<String>();
	}
	
	// Returns the files queue
	public Queue<String> getFiles() { 
		return filesQueue;
	}
	
	// Searches for the files in a specified folder recursively
	public void searchFiles(String _folder_path, int max_level, int current_level) {
		if(current_level > max_level && max_level != 0) {
			return;
		}
		current_level++;
		
        File fileName = new File(_folder_path);
        File[] fileList = fileName.listFiles();
        for (File file: fileList) {
            if(file.isFile()) {
            	if(getFileExtension(file).equals("txt")) {
                	filesQueue.add(file.toString());
            	}
            }
            else if(file.isDirectory()) {
            	searchFiles(file.toString(), max_level, current_level);
            }
            
        }		
	}
	
	// Returns the file extension from the file name
	private String getFileExtension(File file) {
	    String name = file.getName();
	    int lastIndexOf = name.lastIndexOf(".");
	    if (lastIndexOf == -1) {
	        return ""; 											// empty extension
	    }
	    return name.substring(lastIndexOf + 1).toString();
	}
}
