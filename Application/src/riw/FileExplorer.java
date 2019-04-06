/**
 * @title Search engine application
 * @author Alexandru Grigoras
 * @version 4.0 
 */

package riw;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

public class FileExplorer {
	/**
	 * Variables
	 */
	private Queue<String> filesQueue;				// queue for processing links
	private String extension;						// extension for searched files

	/**
	 * Class constructor
	 * @param _extension
	 */
	public FileExplorer(String _extension) {
		filesQueue = new LinkedList<String>();
		extension = _extension;
	}
	
	/**
	 * Returns the files queue
	 * @return the list of files
	 */
	public Queue<String> getFiles() { 
		return filesQueue;
	}

	/**
	 * Get a queue with file names and path
	 * @param nr_files
	 * @return a queue with files
	 */
	public Queue<String> getFiles(int nr_files) {
		Queue<String> oldfilesQueue = filesQueue;
		Queue<String> newfilesQueue = new LinkedList<String>();
		
		if(nr_files <= oldfilesQueue.size()) {
			for(int i=0; i< nr_files; i++) {
				newfilesQueue.add(oldfilesQueue.poll());
			}
			
			return newfilesQueue;
		}
		
		return oldfilesQueue;
	}
	
	/**
	 * Searches for the files in a specified folder recursively
	 * @param _folder_path
	 * @param max_level
	 * @param current_level
	 */
	public void searchFiles(String _folder_path, int max_level, int current_level) {
		if(current_level > max_level && max_level != 0) {
			return;
		}
		current_level++;
		
        File fileName = new File(_folder_path);
        File[] fileList = fileName.listFiles();
        for (File file: fileList) {
            if(file.isFile()) {
            	if(getFileExtension(file).equals(extension)) {
                	filesQueue.add(file.toString());
            	}
            }
            else if(file.isDirectory()) {
            	searchFiles(file.toString(), max_level, current_level);
            }
            
        }		
	}

	/**
	 * Returns the file extension from the file name
	 * @param file
	 * @return
	 */
	private String getFileExtension(File file) {
	    String name = file.getName();
	    int lastIndexOf = name.lastIndexOf(".");
	    if (lastIndexOf == -1) {
	        return ""; 								// empty extension
	    }
	    return name.substring(lastIndexOf + 1).toString();
	}
}
