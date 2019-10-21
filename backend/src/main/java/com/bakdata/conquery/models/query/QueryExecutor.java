package com.bakdata.conquery.models.query;

import com.bakdata.conquery.models.config.ConqueryConfig;
import com.bakdata.conquery.models.events.BlockManager;
import com.bakdata.conquery.models.identifiable.ids.specific.ManagedExecutionId;
import com.bakdata.conquery.models.query.entity.Entity;
import com.bakdata.conquery.models.query.queryplan.QueryPlan;
import com.bakdata.conquery.models.query.results.EntityResult;
import com.bakdata.conquery.models.query.results.ShardResult;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class QueryExecutor implements Closeable {

	private final ListeningExecutorService pool;
	
	public QueryExecutor(ConqueryConfig config) {
		this.pool = config.getQueries().getExecutionPool().createService("Query Executor %d");
	}

	public ShardResult execute(QueryPlanContext context, ManagedQuery query) {
		query.start();
		ThreadLocal<QueryPlan> plans = ThreadLocal.withInitial(() -> query.getQuery().createQueryPlan(context));
//		QueryPlan plan = ;
		return execute(
				context.getBlockManager(),
				new QueryContext(
					context.getWorker().getStorage()
				),
				query.getId(),
				plans,
				pool
		);
	}

	public static ShardResult execute(BlockManager blockManager, QueryContext context, ManagedExecutionId queryId, ThreadLocal<QueryPlan> plans, ListeningExecutorService executor) {

		Collection<Entity> entries = blockManager.getEntities().values();

		if(entries.isEmpty()) {
			log.warn("entries for query {} are empty", queryId);
		}
		ShardResult result = new ShardResult();
		result.setQueryId(queryId);
		
		List<ListenableFuture<EntityResult>> futures = entries
		.stream()
		.filter(entity -> plans.get().isOfInterest(entity))
		.map(entity -> executor.submit(new QueryJob(context, plans, entity)))
		.collect(Collectors.toList());
		
		result.setFuture(Futures.allAsList(futures));
		
		result.getFuture().addListener(result::finish, MoreExecutors.directExecutor());
		return result;
	}

	@Override
	public void close() throws IOException {
		pool.shutdown();
		try {
			boolean success = pool.awaitTermination(1, TimeUnit.DAYS);
			if (!success && log.isDebugEnabled()) {
				log.error("Timeout has elapsed before termination completed for executor {}", pool);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
