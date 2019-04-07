package riw;


import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;

public class mongo_test {

	public static void main(String[] args) {
		MongoClient mongoClient = new MongoClient("localhost", 27017) ;
		MongoDatabase database = mongoClient.getDatabase("mydb");
		MongoCollection<Document> collection = database.getCollection("test");
		Document doc = new Document("name", "MongoDB")
				.append("type", "database")
				.append("count", 1)
				.append("info", new Document("x", 203).append("y", 102));
		
		collection.insertOne(doc);
		
		Document myDoc = collection.find(eq("i", 71)).first();
		try {
			System.out.println(myDoc.toJson());
		}
		catch(NullPointerException ex) {
			System.out.println("Error: " + ex.getMessage());
		}
		
	}

}
