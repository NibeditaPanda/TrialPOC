package com.tesco.services.healthChecks;

import com.yammer.metrics.core.HealthCheck;

public class MongodbHealthCheck  extends HealthCheck {

    public MongodbHealthCheck() {
        super("serviceHealthCheck");
    }

    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}
