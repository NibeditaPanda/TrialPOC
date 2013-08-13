package com.tesco.services.DAO;

import com.google.common.base.Optional;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

import java.util.List;

public class Query {

    private DBCollection collection;

    private Query(DBCollection collection){
        this.collection = collection;
    }

    public static Query on(DBCollection collection) {
        return new Query(collection);
    }

    public Optional<DBObject> findOne(String key, String value) {
        DBObject query = QueryBuilder.start(key).is(value).get();
        List<DBObject> result = collection.find(query, new BasicDBObject("_id", 0)).toArray();
        return Optional.fromNullable(result.isEmpty() ? null : result.get(0));
    }
}
