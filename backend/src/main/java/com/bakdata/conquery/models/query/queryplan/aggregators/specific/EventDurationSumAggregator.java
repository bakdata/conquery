package com.bakdata.conquery.models.query.queryplan.aggregators.specific;

import java.util.Optional;

import javax.annotation.CheckForNull;

import com.bakdata.conquery.models.common.CDateSet;
import com.bakdata.conquery.models.common.daterange.CDateRange;
import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.externalservice.ResultType;
import com.bakdata.conquery.models.query.QueryExecutionContext;
import com.bakdata.conquery.models.query.queryplan.aggregators.Aggregator;
import com.bakdata.conquery.models.query.queryplan.clone.CloneContext;

/**
 * Aggregator, counting the number of days present.
 */
public class EventDurationSumAggregator implements Aggregator<Long> {

	private Optional<Aggregator<CDateSet>> queryDateAggregator = Optional.empty();
	private final CDateSet set = CDateSet.create();

	@CheckForNull
	private CDateSet dateRestriction;
	@CheckForNull
	private Column validityDateColumn;

	@Override
	public void nextTable(QueryExecutionContext ctx, Table currentTable) {
		dateRestriction = ctx.getDateRestriction();
		validityDateColumn = ctx.getValidityDateColumn();
		queryDateAggregator = ctx.getQueryDateAggregator();
	}

	@Override
	public void acceptEvent(Bucket bucket, int event) {
		if (validityDateColumn == null) {
			return;
		}

		if (!bucket.has(event, validityDateColumn)) {
			return;
		}

		final CDateRange value = bucket.getAsDateRange(event, validityDateColumn);

		if (value.isOpen()) {
			return;
		}


		set.maskedAdd(value, dateRestriction);
	}

	@Override
	public EventDurationSumAggregator doClone(CloneContext ctx) {
		return new EventDurationSumAggregator();
	}

	@Override
	public Long getAggregationResult() {

		queryDateAggregator
				.map(Aggregator::getAggregationResult)
				.ifPresent(
						set::retainAll
				);

		return set.countDays();
	}

	@Override
	public ResultType getResultType() {
		return ResultType.IntegerT.INSTANCE;
	}
}
