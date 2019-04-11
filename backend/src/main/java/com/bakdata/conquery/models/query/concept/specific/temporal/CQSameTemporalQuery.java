package com.bakdata.conquery.models.query.concept.specific.temporal;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.query.QueryPlanContext;
import com.bakdata.conquery.models.query.QueryResolveContext;
import com.bakdata.conquery.models.query.concept.CQElement;
import com.bakdata.conquery.models.query.queryplan.QPNode;
import com.bakdata.conquery.models.query.queryplan.QueryPlan;
import com.bakdata.conquery.models.query.queryplan.specific.temporal.SameTemporalMatcher;
import com.bakdata.conquery.models.query.queryplan.specific.temporal.TemporalQueryNode;

/**
 * Creates a query that will contain all entities where {@code preceding}
 * contains events that happened {@code days} at the same time as the events of
 * {@code index}. And the time where this has happened.
 */
@CPSType(id = "SAME", base = CQElement.class)
public class CQSameTemporalQuery extends CQAbstractTemporalQuery {

	public CQSameTemporalQuery(CQSampled index, CQSampled preceding) {
		super(index, preceding);
	}

	@Override
	public QPNode createQueryPlan(QueryPlanContext registry, QueryPlan plan) {
		return new TemporalQueryNode(
			index.createQueryPlan(registry, plan),
			preceding.createQueryPlan(registry, plan),
			new SameTemporalMatcher(),
			plan.getSpecialDateUnion());
	}

	@Override
	public CQSameTemporalQuery resolve(QueryResolveContext context) {
		return new CQSameTemporalQuery(index.resolve(context), preceding.resolve(context));
	}
}
