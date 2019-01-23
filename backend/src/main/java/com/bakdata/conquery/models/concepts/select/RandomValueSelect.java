package com.bakdata.conquery.models.concepts.select;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.query.queryplan.aggregators.Aggregator;
import com.bakdata.conquery.models.query.queryplan.aggregators.specific.value.LastValueAggregator;
import com.bakdata.conquery.models.query.select.Select;

@CPSType(id = "LAST", base = Select.class)
public class RandomValueSelect extends ColumnSelect {

	@Override
	protected Aggregator<?> createAggregator() {
		return new LastValueAggregator(getColumn());
	}
}
