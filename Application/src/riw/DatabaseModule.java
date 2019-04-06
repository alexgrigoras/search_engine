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
		DatabaseModule dm = new DatabaseModule("tf_values");
		
		/*
		List<Document> documents = new ArrayList<Document>();
		for (int i = 0; i < 100; i++) {
		    documents.add(new Document("i", i));
		}
		
		Document docum = new Document("doc", "abc")
				.append("terms", documents);
		
		dm.insertDoc(docum);
		*/		

		BasicDBObject criteria = new BasicDBObject();
		criteria.append("doc", "E:\\Facultate\\Anul IV - Facultate\\Semestrul I\\ALPD - Algoritmi paraleli si distribuiti\\Tema de casa\\test-files\\test-files\\2.txt");
		criteria.append("k", "cancel");
				
		Document myDoc = dm.collection.find(criteria).first();
		System.out.println(myDoc.get("tf"));

		//Document myDoc = collection.find().first();
		//System.out.println(myDoc.toJson());
	}

}
