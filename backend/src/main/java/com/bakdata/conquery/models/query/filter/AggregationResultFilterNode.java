package com.bakdata.conquery.models.query.filter;

import com.bakdata.conquery.models.concepts.filters.Filter;
import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.events.Block;
import com.bakdata.conquery.models.identifiable.ids.specific.TableId;
import com.bakdata.conquery.models.query.QueryContext;
import com.bakdata.conquery.models.query.concept.filter.FilterValue;
import com.bakdata.conquery.models.query.queryplan.aggregators.Aggregator;
import com.bakdata.conquery.models.query.queryplan.filter.FilterNode;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@ToString
public abstract class AggregationResultFilterNode<AGGREGATOR extends Aggregator<?>, FILTER_VALUE extends FilterValue<?>, FILTER extends Filter<FILTER_VALUE>> extends FilterNode<FILTER_VALUE, FILTER> {


	@Getter
	private AGGREGATOR aggregator;

	public AggregationResultFilterNode(AGGREGATOR aggregator, FILTER filter, FILTER_VALUE filterValue) {
		super(filter, filterValue);
		this.aggregator = aggregator;
	}

	@Override
	public void collectRequiredTables(Set<TableId> out) {
		aggregator.collectRequiredTables(out);
	}

	@Override
	public void nextTable(QueryContext ctx, Table currentTable) {
		super.nextTable(ctx, currentTable);
//		aggregator.nextTable(ctx, currentTable);
	}

	@Override
	public void nextBlock(Block block) {
		super.nextBlock(block);
//		aggregator.nextBlock(block);
	}

	@Override
	public void acceptEvent(Block block, int event) {

	}

	@Override
	public abstract boolean isContained();
}
