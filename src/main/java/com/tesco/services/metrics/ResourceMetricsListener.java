package com.tesco.services.metrics;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.MetricsRegistryListener;

public class ResourceMetricsListener implements MetricsRegistryListener {

    private ResourceMetricsRegistry registry  = new ResourceMetricsRegistry();
    public static final String RESOURCE_GROUP = "PriceServices";

    public ResourceMetricsListener(){
        super();
    }

    //this constructor is defined at package level as its only purpose is to unit test this class
    ResourceMetricsListener (ResourceMetricsRegistry registryP) {
        registry = registryP;
    }

    @Override
    public void onMetricAdded(MetricName name, Metric metric) {
        if (name.getGroup().equals(RESOURCE_GROUP))
            registry.addMetric(name,metric);
    }


    @Override
    public void onMetricRemoved(MetricName name) {
        registry.removeMetric(name);
    }


    public MetricsRegistry getRegistry() {
        return registry;
    }
}
