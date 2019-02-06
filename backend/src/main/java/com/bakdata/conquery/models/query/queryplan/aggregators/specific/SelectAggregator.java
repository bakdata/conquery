package com.bakdata.conquery.models.query.queryplan.aggregators.specific;

import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.events.Block;
import com.bakdata.conquery.models.identifiable.ids.specific.SelectId;
import com.bakdata.conquery.models.query.queryplan.aggregators.SingleColumnAggregator;
import com.bakdata.conquery.models.types.specific.IStringType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SelectAggregator extends SingleColumnAggregator<Long> {

	private final String selected;
	private long hits = 0;
	private int selectedId = -1;

	public SelectAggregator(SelectId id,  Column column, String selected) {
		super(id, column);

		this.selected = selected;
	}

	@Override
	public void nextBlock(Block block) {
		selectedId = ((IStringType) getColumn().getTypeFor(block)).getStringId(selected);
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
	public SelectAggregator clone() {
		return new SelectAggregator(getId(), getColumn(), selected);
	}
}
