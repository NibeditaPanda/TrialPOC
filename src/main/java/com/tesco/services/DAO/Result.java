package com.tesco.services.DAO;

import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;

public class Result<Items> {

    private List<Items> results =  new ArrayList<>();

    public Result(Optional<List<Items>> results){
        if(results.isPresent()) {
            this.results = results.get();
        }
    }

    public Result(List<Items> results){
        this.results = results;
    }

    private Result(){
    }

    public boolean isEmpty(){
        return results.isEmpty();
    }

    public List<Items> items() {
        return results;
    }

    public static Result empty() {
        return new Result();
    }
}
