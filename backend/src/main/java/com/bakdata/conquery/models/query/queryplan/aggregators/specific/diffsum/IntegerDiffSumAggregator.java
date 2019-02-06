package com.bakdata.conquery.models.query.queryplan.aggregators.specific.diffsum;

import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.events.Block;
import com.bakdata.conquery.models.identifiable.ids.specific.SelectId;
import com.bakdata.conquery.models.query.queryplan.aggregators.ColumnAggregator;
import lombok.Getter;

public class IntegerDiffSumAggregator extends ColumnAggregator<Long> {

	@Getter
	private Column addendColumn;
	@Getter
	private Column subtrahendColumn;
	private long sum = 0L;

	public IntegerDiffSumAggregator(SelectId id, Column addend, Column subtrahend) {
		super(id);
		this.addendColumn = addend;
		this.subtrahendColumn = subtrahend;
	}

	@Override
	public IntegerDiffSumAggregator clone() {
		return new IntegerDiffSumAggregator(getId(), getAddendColumn(), getSubtrahendColumn());
	}

	@Override
	public Column[] getRequiredColumns() {
		return new Column[]{getAddendColumn(), getSubtrahendColumn()};
	}

	@Override
	public void aggregateEvent(Block block, int event) {
		long addend = block.has(event, getAddendColumn()) ? block.getInteger(event, getAddendColumn()) : 0;
		long subtrahend = block.has(event, getSubtrahendColumn()) ? block.getInteger(event, getSubtrahendColumn()) : 0;

		sum = sum + addend - subtrahend;
	}

	@Override
	public Long getAggregationResult() {
		return sum;
	}
}
