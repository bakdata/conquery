package com.bakdata.conquery.models.query.queryplan.aggregators.specific.sum;

import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.events.Block;
import com.bakdata.conquery.models.identifiable.ids.specific.SelectId;
import com.bakdata.conquery.models.query.queryplan.aggregators.SingleColumnAggregator;

public class MoneySumAggregator extends SingleColumnAggregator<Long> {


	private long sum = 0L;

	public MoneySumAggregator(SelectId id, Column column) {
		super(id, column);
	}

	@Override
	public MoneySumAggregator clone() {
		return new MoneySumAggregator(getId(), getColumn());
	}

	@Override
	public void aggregateEvent(Block block, int event) {
		if (!block.has(event, getColumn())) {
			return;
		}

		long addend = block.getMoney(event, getColumn());

		sum = sum + addend;
	}

	@Override
	public Long getAggregationResult() {
		return sum;
	}
}
