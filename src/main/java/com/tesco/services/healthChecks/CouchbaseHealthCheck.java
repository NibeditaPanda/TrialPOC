package com.tesco.services.healthChecks;

import com.tesco.couchbase.AsyncCouchbaseWrapper;
import com.tesco.couchbase.exceptions.CouchbaseConnectionUnavailableException;
import com.yammer.metrics.core.HealthCheck;
import org.slf4j.Logger;

import java.net.SocketAddress;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class CouchbaseHealthCheck extends HealthCheck {
    private final Logger logger = getLogger(CouchbaseHealthCheck.class);

    public static final String NAME = "Couchbase Health Check";
    public static final String EP_DEGRADED_MODE = "ep_degraded_mode";
    private final AsyncCouchbaseWrapper asyncCouchbaseWrapper;

    public CouchbaseHealthCheck(AsyncCouchbaseWrapper asyncCouchbaseWrapper) {
        super(NAME);
        this.asyncCouchbaseWrapper = asyncCouchbaseWrapper;
    }

    @Override
    protected Result check() throws Exception {

        if(couchbaseReady()){
            return Result.healthy();
        }

        return Result.unhealthy("CouchbaseServer not ready");
    }

    private boolean couchbaseReady(){
        try {
            Map<SocketAddress, Map<String, String>> stats = asyncCouchbaseWrapper.getStats();
            for (Map.Entry<SocketAddress, Map<String, String>> server: stats.entrySet()) {
                Map<String, String> serverStats = server.getValue();
                if (!statsAvailable(serverStats) || !serverWarmedUp(serverStats)){
                    return false;
                }
            }
            return true;
        } catch (CouchbaseConnectionUnavailableException e) {
            logger.error("Connection lost due to :" + e.getMessage());
            return false;
        }
    }

    private boolean serverWarmedUp(Map<String, String> serverStats) {
        return serverStats.get(EP_DEGRADED_MODE).equals("0");
    }

    private boolean statsAvailable(Map<String, String> serverStats) {
        return serverStats.containsKey(EP_DEGRADED_MODE);
    }
}
