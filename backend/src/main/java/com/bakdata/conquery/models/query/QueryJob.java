package com.bakdata.conquery.models.query;

import java.util.concurrent.Callable;

import com.bakdata.conquery.models.query.entity.Entity;
import com.bakdata.conquery.models.query.queryplan.QueryPlan;
import com.bakdata.conquery.models.query.queryplan.clone.CloneContext;
import com.bakdata.conquery.models.query.results.EntityResult;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QueryJob implements Callable<EntityResult> {

	private final QueryContext ctx;
	private final ThreadLocal<QueryPlan> plan;
	private final Entity entity;
	
	
	@Override
	public EntityResult call() throws Exception {
		try {
			CloneContext cCtx = new CloneContext(ctx.getStorage());
			QueryPlan queryPlan = this.plan.get().clone(cCtx);
			
			return queryPlan.execute(ctx, entity);
		}
		catch(Exception e) {
			return EntityResult.failed(entity.getId(), e);
		}
	}
}
