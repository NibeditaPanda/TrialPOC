package com.tesco.services.DAO;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.tesco.services.Configuration;
import com.tesco.services.DBFactory;

import java.net.UnknownHostException;
import java.util.List;

public class PriceDAO {


    private final DBCollection price;

    public PriceDAO(Configuration configuration) throws UnknownHostException {
        DBFactory dbFactory = new DBFactory(configuration);
        price = dbFactory.getCollection("prices");
    }

    public List<DBObject> getPriceBy(String key, String value) {
        DBObject query = QueryBuilder.start(key).is(value).get();
        System.out.println(query);
        return price.find(query, new BasicDBObject("_id", 0 )).toArray();
    }

}
