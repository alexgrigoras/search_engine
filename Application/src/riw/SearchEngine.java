/**
 * @title Search engine application
 * @author Alexandru Grigoras
 * @version 3.0 
 */
	
package riw;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Map;
import java.util.List;
import java.util.Comparator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * 
 */
public class SearchEngine {
	private SpecialWords stopwordsObj;						// create hash for exceptions and stopwords
	private SpecialWords exceptionsObj;
	
	/**
	 * Methods
	 */
	// Class constructor
	public SearchEngine() {
		stopwordsObj = new SpecialWords("./files/special_words/stop_words.txt");
		exceptionsObj = new SpecialWords("./files/special_words/exception_words.txt");
	}
	
	// Log data to console
	private void log(String msg, boolean newLine) {
		if(newLine) {
			System.out.println(msg);
        }
		else {
			System.out.print(msg);
		}
	}
	private void log(int number, boolean newLine) {
		StringBuilder sb = new StringBuilder();
		sb.append(number);
		if(newLine) {
			System.out.println(sb.toString());
        }
		else {
			System.out.print(sb.toString());
		}
	}
	private void log(double number, boolean newLine) {
		StringBuilder sb = new StringBuilder();
		sb.append(number);
		if(newLine) {
			System.out.println(sb.toString());
        }
		else {
			System.out.print(sb.toString());
		}
	}

	// Porter algorithm
	private String getCanonicalForm(String text) {
		PorterStemmer porterStemmer = new PorterStemmer();
		String stem = porterStemmer.stemWord(text);
		
		return stem;
	}
	
	// Parse the keywords from console to a format with words and associated operation
	private ArrayList<WordOperation> parseKeywords(String keywords) {
		// variables for extracting words
		int i = 0;
		boolean setFlag = true;
		StringBuilder tempStr = new StringBuilder("");
		String text = "";
		ArrayList<WordOperation> keywords_list = new ArrayList<WordOperation>();
		WordOperation word = null;
		
		// parse text
		for(i=0; i<keywords.length();i++) {
			if(i == keywords.length()-1)
			{
				tempStr.append(keywords.charAt(i));
			}
			if((keywords.charAt(i) == ' ' || keywords.charAt(i) == '+' || keywords.charAt(i) == '-' ||
					i == keywords.length()-1) && setFlag) {
				text = tempStr.toString().toLowerCase();
				word = new WordOperation(getCanonicalForm(text));
				if(exceptionsObj.hashContains(tempStr.toString().toLowerCase())) {
					// Exception
					keywords_list.add(word);
					switch (keywords.charAt(i)) {
						case ' ':
							word.setOperation(OpType.OR);
							break;
						case '+':
							word.setOperation(OpType.AND);
							break;
						case '-':
							word.setOperation(OpType.NOT);
							break;
					}		
				} else if(!stopwordsObj.hashContains(tempStr.toString().toLowerCase())) {
					// Dictionary
					keywords_list.add(word);
					switch (keywords.charAt(i)) {
						case ' ':
							word.setOperation(OpType.OR);
							break;
						case '+':
							word.setOperation(OpType.AND);
							break;
						case '-':
							word.setOperation(OpType.NOT);
							break;
					}		
				}
				tempStr = new StringBuilder("");  
				setFlag = false;
			}
			else if(Character.isLetter(keywords.charAt(i))) {
				tempStr.append(keywords.charAt(i));
				setFlag = true;
			}
		}
		
		return keywords_list;
	}
	
	// Read keywords from console
	private String readKeywords() {
		Scanner scanner = new Scanner(System.in);
		String keywords = scanner.nextLine();		
		return keywords;
	}
		
	/*
	 * Search
	 */
	/*
	// Binary Search
	private void binarySearch() {
		boolean exit = false;
		while(exit == false) {
			log("> Search: ", false);
			
			String keywords = readKeywords();			
			ArrayList<WordOperation> kw_list = new ArrayList<WordOperation>();
			int list_dimension = 0;
			LinksList words_list = new LinksList();
			StringBuilder words_ops = new StringBuilder();
			
			if(keywords.equals("exit")) {
				exit = true;
				break;
			}
			
			log("> Searched keywords: ", false);
			
			kw_list = parseKeywords(keywords);
			
			list_dimension = kw_list.size();
			
			if(list_dimension == 0) {
				log("Nothing typed!", false);
			}
			else if(list_dimension == 1) {
				String word = kw_list.get(0).getWord();
				
				log(word + " -> ", false);
				
				LinksList list = getWordLocations(word);
				list.show();
			}
			else {
				for(int i = 0; i < list_dimension - 1; i++) {
					String word_1 = kw_list.get(i).getWord();
					String word_2 = kw_list.get(i+1).getWord();
					OpType operation = kw_list.get(i).getOperation();
					
					if(i == 0) {
						words_ops.append(word_1 + " " + operation + " " + word_2);
					}
					else {
						words_ops.append(" " + operation + " " + word_2);
					}
					
					LinksList list_1 = null;	
					LinksList list_2 = null;
					if(words_list.size() == 0) {
						try {
							list_1 = getWordLocations(word_1);
						}
						catch(NullPointerException e) {
							list_1 = new LinksList();
						}
					}
					else {
						list_1 = words_list;
					}			
					
					try {
						list_2 = getWordLocations(word_2);
					}
					catch(NullPointerException e) {
						list_2 = new LinksList();
					}
					
					words_list = new LinksList();
					
					if(operation == OpType.OR) {
						if(list_1 != null && list_2 != null && list_1.size() >= list_2.size()) {
							try {
								for(Link l: list_1.getLinks()) {
									if(!words_list.hasLink(l.getLink())) {
										words_list.addLink(l);
									}
									else {
										words_list.addFreqToLink(l.getLink(), l.getFrequency());
									}
								}
							}
							catch(NullPointerException e) {}
							
							try {
								for(Link l: list_2.getLinks()) {
									if(!words_list.hasLink(l.getLink())) {
										words_list.addLink(l);
									}
									else {
										words_list.addFreqToLink(l.getLink(), l.getFrequency());
									}
								}
							}
							catch(NullPointerException e) {}
						}
						else {
							try {
								for(Link l: list_2.getLinks()) {
									if(!words_list.hasLink(l.getLink())) {
										words_list.addLink(l);
									}
									else {
										words_list.addFreqToLink(l.getLink(), l.getFrequency());
									}
								}
							}
							catch(NullPointerException e) {}
							
							try {
								for(Link l: list_1.getLinks()) {
									if(!words_list.hasLink(l.getLink())) {
										words_list.addLink(l);
									}
									else {
										words_list.addFreqToLink(l.getLink(), l.getFrequency());
									}
								}
							}
							catch(NullPointerException e) {}
						}
					}
					else if(operation == OpType.AND) {						
						
						if(list_1 != null && list_2 != null && list_1.size() <= list_2.size()) {
							try {
								for(Link l: list_1.getLinks()) {
									if(list_2.hasLink(l.getLink())) {
										if(!words_list.hasLink(l.getLink())) {
											words_list.addLink(l);
										}
										else {
											words_list.addFreqToLink(l.getLink(), l.getFrequency());
										}
									}
								}
							}
							catch(NullPointerException e) {}
						}
						else {
							try {
								for(Link l: list_2.getLinks()) {
									if(list_1.hasLink(l.getLink())) {
										if(!words_list.hasLink(l.getLink())) {
											words_list.addLink(l);
										}
										else {
											words_list.addFreqToLink(l.getLink(), l.getFrequency());
										}
									}
								}
							}
							catch(NullPointerException e) {}
						}										
					}
					else if(operation == OpType.NOT) {
						
						for(Link l: list_1.getLinks()) {
							if(!list_2.hasLink(l.getLink())) {
								if(!words_list.hasLink(l.getLink())) {
									words_list.addLink(l);
								}
								else {
									words_list.addFreqToLink(l.getLink(), l.getFrequency());
								}
							}
						}
						
					}				
				}
				
				log(words_ops + " -> ", false);
				
				if(words_list.size() == 0) {
					log("No results!", false);
				}
				else {
					words_list.show();
				}
			}
			
			log("", true);
		}
	}
	
	// Returns a vector with tf*idf for searched words
	private ArrayList<Double> calculateQueryVector(ArrayList<WordOperation> words_list) {
		int nrQueryWords = words_list.size();
		ArrayList<Double> vector = new ArrayList<Double>();
		
		int nrDocuments = docKeys.size();
		
		for(WordOperation word: words_list) {
			double tf = 1.0 / (double)nrQueryWords;
			double idf;
			LinksList listDocs = getWordLocations(word.getWord());
			double nrWordDocs = 1;
			try {
				nrWordDocs += listDocs.size();
			}
			catch(NullPointerException ex){}
			
			double res = nrDocuments / nrWordDocs;
			
			idf = Math.log(res);
			
			vector.add(tf*idf);
		}
		
		return vector;
	}
	
	// Vectorial Search
	private void vectorialSearch() {
		boolean exit = false;
		while(exit == false) {
			log("> Search: ", false);
			
			String keywords = readKeywords();
			
			ArrayList<WordOperation> kw_list = new ArrayList<WordOperation>();
			int list_dimension = 0;
			LinksList docs_list = new LinksList();
			StringBuilder words_ops = new StringBuilder();
			
			if(keywords.equals("exit")) {
				exit = true;
				break;
			}
			
			kw_list = parseKeywords(keywords);
			list_dimension = kw_list.size();
			
			ArrayList<Double> vector = new ArrayList<Double>();
			vector = calculateQueryVector(kw_list);
			
			log("> ", false);			
			
			if(list_dimension == 0) {
				log("Nothing typed!", false);
			}
			else if(list_dimension == 1) {
				String word = kw_list.get(0).getWord();
				
				log(word + " has ", false);
				LinksList list = getWordLocations(word);
				log(list.size(), false);
				log(" results: ", true);				
				
				HashMap<String, Double> cosSimVal = new HashMap<String, Double>(); 
				
				double idf = getInverseDocumentFrequency(word);
				for(Link l: list.getLinks()) {
					String link = l.getLink();
					if(hashContains(link)) {
						double tf = docKeys.get(link).getTfOfWord(word);
						cosSimVal.put(link, cosineSimilarity(vector.get(0), tf*idf));
					}
				}				
				
				showResults(cosSimVal);
			}
			else {
				for(int i = 0; i < list_dimension - 1; i++) {
					String word_1 = getCanonicalForm(kw_list.get(i).getWord());
					String word_2 = getCanonicalForm(kw_list.get(i+1).getWord());
					OpType operation = kw_list.get(i).getOperation();
					
					if(i == 0) {
						words_ops.append(word_1 + " " + operation + " " + word_2);
					}
					else {
						words_ops.append(" " + operation + " " + word_2);
					}
					
					LinksList list_1 = null;	
					LinksList list_2 = null;
					if(docs_list.size() == 0) {
						try {
							list_1 = getWordLocations(word_1);
						}
						catch(NullPointerException e) {
							list_1 = new LinksList();
						}
					}
					else {
						list_1 = docs_list;
					}			
					
					try {
						list_2 = getWordLocations(word_2);
					}
					catch(NullPointerException e) {
						list_2 = new LinksList();
					}
					
					docs_list = new LinksList();
					
					if(operation == OpType.OR) {						
						if(list_1 != null && list_2 != null && list_1.size() >= list_2.size()) {
							try {
								for(Link l: list_1.getLinks()) {
									if(!docs_list.hasLink(l.getLink())) {
										docs_list.addLink(l);
									}
									else {
										docs_list.addFreqToLink(l.getLink(), l.getFrequency());
									}
								}
							}
							catch(NullPointerException e) {}
							
							try {
								for(Link l: list_2.getLinks()) {
									if(!docs_list.hasLink(l.getLink())) {
										docs_list.addLink(l);
									}
									else {
										docs_list.addFreqToLink(l.getLink(), l.getFrequency());
									}
								}
							}
							catch(NullPointerException e) {}
						}
						else {
							try {
								for(Link l: list_2.getLinks()) {
									if(!docs_list.hasLink(l.getLink())) {
										docs_list.addLink(l);
									}
									else {
										docs_list.addFreqToLink(l.getLink(), l.getFrequency());
									}
								}
							}
							catch(NullPointerException e) {}
							
							try {
								for(Link l: list_1.getLinks()) {
									if(!docs_list.hasLink(l.getLink())) {
										docs_list.addLink(l);
									}
									else {
										docs_list.addFreqToLink(l.getLink(), l.getFrequency());
									}
								}
							}
							catch(NullPointerException e) {}
						}
					}
					else if(operation == OpType.NOT) {
						
						for(Link l: list_1.getLinks()) {
							if(!list_2.hasLink(l.getLink())) {
								if(!docs_list.hasLink(l.getLink())) {
									docs_list.addLink(l);
								}
								else {
									docs_list.addFreqToLink(l.getLink(), l.getFrequency());
								}
							}
						}
						
					}				
				}
				
				log(words_ops + " has ", false);
				log(docs_list.size(), false);
				log(" results: ", true);
				
				HashMap<String, Double> cosSimVal = new HashMap<String, Double>(); 
				
				ArrayList<Double> link_vectors = new ArrayList<Double>();
				
				for(WordOperation w: kw_list) {
					double idf = getInverseDocumentFrequency(w.getWord());
					for(Link l: docs_list.getLinks()) {
						String link = l.getLink();
						if(hashContains(link)) {
							double tf = docKeys.get(link).getTfOfWord(w.getWord());
							link_vectors.add(tf*idf);
						}
					}				
				}
				
				for(int i=0;i<docs_list.size();i++)
				{
					ArrayList<Double> cosVector = new ArrayList<Double>();
					for(int j=0; j<kw_list.size(); j++)
					{
						cosVector.add(link_vectors.get(i+j*docs_list.size()));
					}
					
					cosSimVal.put(docs_list.getLinks().get(i).getLink(), cosineSimilarity(vector, cosVector));
				}
				
				showResults(cosSimVal);
			}
		}
	}
	*/	
	// main function
	public static void main(String[] args) {
		SearchEngine se = new SearchEngine();
		FileExplorer fileExp = new FileExplorer();
		
		Queue<String> files;
		int level = 0;									// how many levels to search recursively
		int links = 10;									// limit the number of links from the queue
		String directory;
		
		int nr_threads = 5;
		
		long startTime = System.nanoTime();
		
		//parser.processHTML(link, path);
		
		//parser.log("> Type the selected directory: ", false);
		//directory = parser.readKeywords();
		
		//directory = "D:\\Facultate\\Anul 4\\Semestrul I\\ALPD\\Tema de casa\\test_files";
		directory = "E:\\Facultate\\Anul IV - Facultate\\Semestrul I\\ALPD - Algoritmi paraleli si distribuiti\\Tema de casa\\test-files\\test-files";
		
		se.log("> Getting files from folder: " + directory, true);
				
		fileExp.searchFiles(directory, level, 0);
		files = fileExp.getFiles();
		
		int nr_files_in_queue = files.size();
		
		int nr_files_per_thread;
		
		if(nr_files_in_queue > nr_threads) { 
			nr_files_per_thread = nr_files_in_queue / nr_threads;
		}
		else {
			se.log("> Nr of threads is bigger than files in queue", true);
			return;
		}
		
		ArrayList<Worker> WorkerPool = new ArrayList<Worker>();
		
		for(int i=0; i < nr_threads; i++) {
			Queue<String> temp_files = new LinkedList<String>();
			for(int j = 0; j<nr_files_per_thread; j++) {
				String element = files.poll();
				temp_files.add(element);
			}
			Worker T = new Worker("worker_" + i, temp_files, links);
			WorkerPool.add(T);
		}
		
		if(files.size() > 0) {
			Worker T = new Worker("worker_last", files, links);
			WorkerPool.add(T);
		}
		
		for(Worker w: WorkerPool) {
			w.start();
		}
		for(Worker w: WorkerPool) {
			try {
				w.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		long endTime = System.nanoTime();

		// get difference of two nanoTime values
		long timeElapsed = endTime - startTime;

		System.out.println("> Execution time in milliseconds : " + timeElapsed / 1000000);
		
		//parser.binarySearch();
		
		//parser.vectorialSearch();
	}

}
