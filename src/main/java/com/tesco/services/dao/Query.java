package com.tesco.services.dao;

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
        List<DBObject> results = execute(QueryBuilder.start(key).is(value).get());
        return Optional.fromNullable(results.isEmpty() ? null : results.get(0));
    }

    public Optional<List<DBObject>> findMany(String key, List<String> ids) {
        List<DBObject> results = execute(QueryBuilder.start(key).in(ids).get());
        return Optional.fromNullable(results.isEmpty() ? null : results);
    }

    public List<DBObject> execute(DBObject query) {
        return collection.find(query, new BasicDBObject("_id", 0)).toArray();
    }
}
