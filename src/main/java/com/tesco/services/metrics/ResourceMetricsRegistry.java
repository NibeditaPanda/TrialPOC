package com.tesco.services.metrics;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

public class ResourceMetricsRegistry extends MetricsRegistry{

    public void addMetric (MetricName name, Metric metric){
        super.getOrAdd(name,metric);
    }
}
