/**
 * @title Search engine application
 * @author Alexandru Grigoras
 * @version 4.0 
 */

package riw;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class DatabaseModule {
	private MongoClient mongoClient;
	private MongoDatabase database;
	public MongoCollection<Document> collection;
	
	/**
	 * Class constructor
	 */
	public DatabaseModule() {
		if (mongoClient == null) {
			mongoClient = new MongoClient("localhost", 27017) ;
			System.out.println("> New mongo connection");
		}
		database = mongoClient.getDatabase("search_engine");
	}
	
	/**
	 * Class constructor
	 * @param _collectionName: name of the collection
	 */
	public DatabaseModule(String _collectionName) {
		if (mongoClient == null) {
			mongoClient = new MongoClient("localhost", 27017) ;
			System.out.println("> New mongo connection");
		}
		database = mongoClient.getDatabase("search_engine");
		collection = database.getCollection(_collectionName);
	}
	
	public void setCollection(String _collectionName) {
		collection = database.getCollection(_collectionName);
	}
	
	public void insertDoc(Document doc) {
		collection.insertOne(doc);
	}
	
	public void insertMultipleDocs(List<Document> docs) {
		collection.insertMany(docs);
	}

	public static void main(String[] args) {
		DatabaseModule dm = new DatabaseModule("inverse_index_values");
		
		// TESTING module
		
		/*
		List<Document> documents = new ArrayList<Document>();
		for (int i = 0; i < 100; i++) {
		    documents.add(new Document("i", i));
		}
		
		Document docum = new Document("doc", "abc")
				.append("terms", documents);
		
		dm.insertDoc(docum);
		*/		

		/*
		BasicDBObject criteria = new BasicDBObject();
		criteria.append("doc", "E:\\Facultate\\Anul IV - Facultate\\Semestrul I\\ALPD - Algoritmi paraleli si distribuiti\\Tema de casa\\test-files\\test-files\\2.txt");
		criteria.append("k", "cancel");
				
		Document myDoc = dm.collection.find(criteria).first();
		System.out.println(myDoc.get("tf"));
		*/
		
		/*
		Document myDoc = dm.collection.find(eq("term", "mistreat")).first();
		try {
			List<Document> foundDoc = (List<Document>)myDoc.get("docs");
			
			Document docum = new Document("d", "abc")
         			.append("c", 44);
			
			foundDoc.add(docum);
			
			System.out.println(foundDoc.toString());
			
			Document newDocument = new Document("term", "mistreat")
					.append("docs", foundDoc);
			
			//dm.collection.updateOne(eq("term", "mistreat"), new Document("$set", newDocument));
		}
		catch(NullPointerException ex) {
			System.out.println("not found");
		}		
		*/
		
		/*
		Document myDoc = dm.collection.find(eq("term", "illumin")).first();
		
		List<Document> links = (List<Document>) myDoc.get("docs");
		
		for (Document link : links) {
			System.out.println(link.get("d"));
			System.out.println(link.get("c"));
		}
		
		System.out.println();
		*/
		
		/*
		MongoCursor<Document> cursor = dm.collection.find(eq("term", "mistreat")).iterator();
		try {
		    while (cursor.hasNext()) {
		        System.out.println(cursor.next().toJson());
		    }
		} finally {
		    cursor.close();
		}
		*/

		//Document myDoc = collection.find().first();
		//System.out.println(myDoc.toJson());
	}

}
