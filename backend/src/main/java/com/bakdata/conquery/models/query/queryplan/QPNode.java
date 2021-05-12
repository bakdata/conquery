package com.bakdata.conquery.models.query.queryplan;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.bakdata.conquery.models.common.CDateSet;
import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.query.QueryExecutionContext;
import com.bakdata.conquery.models.query.entity.Entity;
import com.bakdata.conquery.models.query.queryplan.aggregators.Aggregator;
import com.bakdata.conquery.models.query.queryplan.clone.CtxCloneable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
public abstract class QPNode implements EventIterating, CtxCloneable<QPNode> {
	protected QueryExecutionContext context;
	protected Entity entity;

	/**
	 * Initialize the QueryPlan element for evaluation. eg.: Prefetching elements.
	 * @apiNote inheritors should always call super.
	 */
	public void init(Entity entity, QueryExecutionContext context) {
		setEntity(entity);
		setContext(context);
	}

	@Override
	public void nextTable(QueryExecutionContext ctx, Table currentTable) {
		setContext(ctx);
	}

	@Override
	public abstract void acceptEvent(Bucket bucket, int event);

	/**
	 * Checks if the logical composition of the aggregation filters applies to the entity.
	 * The result is trifold, be cause there might be no aggregation filter and thus no assumption can be made.
	 * @return
	 */
	public abstract Optional<Boolean> aggregationFiltersApply();

	public List<QPNode> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Retrieves all generated date Aggregator from the lower level of the tree.
	 * This is builds a parallel tree to the actual query tree to generate the dates column in the final result.
	 * The aggregator are registered in the date aggregator of the upper level (see @{@link DateAggregator#register(Collection)})
	 */
	public abstract Collection<Aggregator<CDateSet>> getDateAggregators();
}
