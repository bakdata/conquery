package com.bakdata.conquery.models.query.queryplan.aggregators.specific;

import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.events.Block;
import com.bakdata.conquery.models.externalservice.ResultType;
import com.bakdata.conquery.models.query.queryplan.aggregators.SingleColumnAggregator;
import com.bakdata.conquery.models.query.queryplan.clone.CloneContext;
import com.bakdata.conquery.models.types.specific.IStringType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SelectAggregator extends SingleColumnAggregator<Long> {

	private final String selected;
	private long hits = 0;
	private int selectedId = -1;

	public SelectAggregator(Column column, String selected) {
		super(column);
		this.selected = selected;
	}

	@Override
	public void nextBlock(Block block) {
		selectedId = ((IStringType) getColumn().getTypeFor(block)).getId(selected);
	}

	@Override
	public void aggregateEvent(Block block, int event) {
		if (selectedId == -1) {
			return;
		}

		if (!block.has(event, getColumn())) {
			return;
		}

		int value = block.getString(event, getColumn());

		if (value == selectedId) {
			hits++;
		}
	}

	@Override
	public Long getAggregationResult() {
		return hits;
	}

	@Override
	public SelectAggregator doClone(CloneContext ctx) {
		return new SelectAggregator(getColumn(), selected);
	}
	
	@Override
	public ResultType getResultType() {
		return ResultType.INTEGER;
	}
}
