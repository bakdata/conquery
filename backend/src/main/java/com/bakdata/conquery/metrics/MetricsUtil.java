package com.bakdata.conquery.metrics;

import java.util.concurrent.LinkedBlockingDeque;

import com.bakdata.conquery.io.xodus.ConqueryStorage;
import com.bakdata.conquery.models.jobs.Job;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;

public class MetricsUtil {

	public static Timer.Context getStoreLoadingTimer() {
		return SharedMetricRegistries.getDefault().timer(MetricRegistry.name(ConqueryStorage.class, "loading")).time();
	}

	public static void createJobQueueGauge(String name, LinkedBlockingDeque<Job> jobs) {
		SharedMetricRegistries.getDefault().register(MetricRegistry.name("jobs", name, "queue"), (Gauge<Integer>) jobs::size);
	}

	public static void removeJobQueueSizeGauge(String name) {
		SharedMetricRegistries.getDefault().remove(MetricRegistry.name("jobs", name, "queue"));
	}

	public static Timer.Context getJobExecutorTimer(String name, Job job) {
		return SharedMetricRegistries.getDefault().timer(MetricRegistry.name(job.getClass(),name, "execute")).time();
	}
}
