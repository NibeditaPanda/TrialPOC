package com.tesco.services.healthChecks;

import com.tesco.services.Configuration;
import com.tesco.services.DBFactory;
import com.yammer.metrics.core.HealthCheck;

public class ServiceHealthCheck extends HealthCheck {

    private DBFactory db;

    public ServiceHealthCheck(Configuration configuration) {
        super("serviceHealthCheck");
        db = new DBFactory(configuration);
    }

    @Override
    protected Result check() throws Exception {
        try{
            db.getCollection("products");
            return Result.healthy();
        } catch (RuntimeException ex) {
            return Result.unhealthy(ex.getMessage());
        }
    }
}
