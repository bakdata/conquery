package com.bakdata.conquery.models.query.queryplan.aggregators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.externalservice.ResultType;
import com.bakdata.conquery.models.query.queryplan.clone.CloneContext;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Helper Aggregator, forwarding only events with distinct values to {@code aggregator}.
 */
public class MultiDistinctValuesWrapperAggregator<VALUE> extends ColumnAggregator<VALUE> {

	private final ColumnAggregator<VALUE> aggregator;
	private Set<List<Object>> observed = new HashSet<>();

	@Getter
	private final Column[] columns;

	public MultiDistinctValuesWrapperAggregator(ColumnAggregator<VALUE> aggregator, Column[] columns) {
		this.columns = columns;
		this.aggregator = aggregator;
	}

	@Override
	public VALUE doGetAggregationResult() {
		// We can call doGetAggregationResult directly because the hit state for this aggregator and the wrapped one is the same
		return aggregator.doGetAggregationResult();
	}

	@Override
	public Column[] getRequiredColumns() {
		return ArrayUtils.addAll(aggregator.getRequiredColumns(), getColumns());
	}

	@Override
	public void acceptEvent(Bucket bucket, int event) {
		List<Object> tuple = new ArrayList<>(columns.length);
		for(Column column : columns) {
			tuple.add(bucket.getAsObject(event, column));
		}
		if (observed.add(tuple)) {
			aggregator.acceptEvent(bucket, event);
			if(aggregator.isHit()) {
				setHit();
			}
		}
	}

	@Override
	public Aggregator<VALUE> doClone(CloneContext ctx) {
		return new MultiDistinctValuesWrapperAggregator<>(aggregator.clone(ctx), columns);
	}
	
	@Override
	public ResultType getResultType() {
		return ResultType.INTEGER;
	}
}