package com.bakdata.conquery.models.concepts.select.specific;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.concepts.select.ColumnSelect;
import com.bakdata.conquery.models.query.queryplan.aggregators.Aggregator;
import com.bakdata.conquery.models.query.queryplan.aggregators.specific.QuartersInYearAggregator;
import com.bakdata.conquery.models.query.select.Select;

/**
 * Entity is included when the the number of quarters with events is within a specified range.
 */
@CPSType(id = "QUARTERS_IN_YEAR", base = Select.class)
public class QuartersInYearSelect extends ColumnSelect {
	@Override
	public Aggregator<?> createAggregator() {
		return new QuartersInYearAggregator(getId(), getColumn());
	}
}
