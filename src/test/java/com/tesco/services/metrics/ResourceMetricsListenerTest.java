package com.tesco.services.metrics;

import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.Timer;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.times;

public class ResourceMetricsListenerTest {


    private ResourceMetricsRegistry registry = Mockito.mock(ResourceMetricsRegistry.class);
    private Timer metric = Mockito.mock(Timer.class);
    private MetricName metricName = Mockito.mock(MetricName.class);


    @Test
    public void shouldReturnAnInstanceOfResourceMetricsRegistry() throws Exception {
        ResourceMetricsListener listener = new ResourceMetricsListener();
        assertThat(listener.getRegistry(), instanceOf(ResourceMetricsRegistry.class));
    }

    @Test
    public void shouldAddResourceGroupMetricsToTheRegistry() throws Exception {
        Mockito.when(metricName.getGroup()).thenReturn("PriceServices");
        ResourceMetricsListener listener = new ResourceMetricsListener(registry);
        listener.onMetricAdded(metricName, metric);
        Mockito.verify(registry, times(1)).addMetric(metricName, metric);
    }

    @Test
    public void shouldRemoveTheMetricFromTheRegistry() throws Exception {
        ResourceMetricsListener listener = new ResourceMetricsListener(registry);
        listener.onMetricRemoved(metricName);
        Mockito.verify(registry, times(1)).removeMetric(metricName);
    }
}
