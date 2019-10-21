package com.bakdata.conquery.models.query.queryplan;

import java.util.Collection;
import java.util.stream.Stream;

import com.bakdata.conquery.models.query.QueryContext;
import com.bakdata.conquery.models.query.QueryJob;
import com.bakdata.conquery.models.query.entity.Entity;
import com.bakdata.conquery.models.query.queryplan.clone.CloneContext;
import com.bakdata.conquery.models.query.queryplan.clone.Resetable;
import com.bakdata.conquery.models.query.results.EntityResult;

public interface QueryPlan extends Resetable {
	
	@Deprecated
	QueryPlan clone(CloneContext ctx);

	default Stream<QueryJob> executeOn(QueryContext context, Collection<Entity> entities) {
		return entities
			.stream()
			.filter(this::isOfInterest)
			.map(entity -> new QueryJob(context, this, entity));
	}
	
	EntityResult execute(QueryContext ctx, Entity entity);
	
	boolean isOfInterest(Entity entity);
}