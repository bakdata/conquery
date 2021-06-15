package com.bakdata.conquery.models.query.queryplan;

import com.bakdata.conquery.models.common.CDateSet;
import com.bakdata.conquery.models.query.QueryExecutionContext;
import com.bakdata.conquery.models.query.entity.Entity;
import com.bakdata.conquery.models.query.queryplan.aggregators.Aggregator;
import com.bakdata.conquery.models.query.queryplan.clone.CtxCloneable;
import com.bakdata.conquery.models.query.results.EntityResult;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface QueryPlan<RESULT extends EntityResult> extends CtxCloneable<QueryPlan<RESULT>> {

	Optional<RESULT> execute(QueryExecutionContext ctx, Entity entity);

	boolean isOfInterest(Entity entity);

	@NotNull
	Optional<Aggregator<CDateSet>> getValidityDateAggregator();

}