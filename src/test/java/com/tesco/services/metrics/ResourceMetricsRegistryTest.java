package com.tesco.services.metrics;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.spy;

public class ResourceMetricsRegistryTest {

    @Test
    public void shouldGetOrAddMetricBeenCalled() throws Exception {
        ResourceMetricsRegistry registry = new ResourceMetricsRegistry();
        MetricName metricName = Mockito.mock(MetricName.class);
        Metric metric = Mockito.mock(Metric.class);
        ResourceMetricsRegistry spy = spy(registry);
        registry.addMetric(metricName, metric);
        assertThat(registry.allMetrics().get(metricName), equalTo(metric));
    }
}
