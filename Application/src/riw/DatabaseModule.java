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
		// Testing the module
		DatabaseModule dm = new DatabaseModule("inverse_index_values");

		Document myDoc = dm.collection.find(eq("term", "appl")).first();

		List<Document> links = (List<Document>) myDoc.get("docs");
		
		for (Document link : links) {
			System.out.println(link.get("d"));
			System.out.println(link.get("c"));
		}
		
		LinksList list = new LinksList();
		
		MongoCursor<Document> cursor = dm.collection.find(eq("term", "illumin")).iterator();
		try {
			while (cursor.hasNext()) {
				//System.out.println(cursor.next().get("docs"));
				List<Document> linksDoc = (List<Document>) cursor.next().get("docs");
				
				for (Document link : linksDoc) {
					Link l = new Link((String)link.get("d"), (int)link.get("c"));
					list.addLink(l);
					
					System.out.println(l.toString());
				}
			}
		} finally {
		    cursor.close();
		}
	}
}
