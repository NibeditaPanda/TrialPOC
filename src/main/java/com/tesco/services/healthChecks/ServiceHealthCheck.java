package com.tesco.services.healthChecks;

import com.tesco.services.repositories.CouchbaseConnectionManager;
import com.yammer.metrics.core.HealthCheck;

public class ServiceHealthCheck extends HealthCheck {

    private CouchbaseConnectionManager couchbaseConnectionManager;

    public ServiceHealthCheck(CouchbaseConnectionManager couchbaseConnectionManager) {
        super("serviceHealthCheck");
        this.couchbaseConnectionManager = couchbaseConnectionManager;
    }

    @Override
    protected Result check() throws Exception {
        try{
            couchbaseConnectionManager.getCouchbaseClient();
            return Result.healthy();
        } catch (RuntimeException ex) {
            return Result.unhealthy(ex.getMessage());
        }
    }
}
