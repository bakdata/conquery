package com.bakdata.conquery.models.datasets.concepts.filters.specific;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.apiv1.frontend.FEFilterType;
import com.bakdata.conquery.models.datasets.concepts.filters.Filter;
import com.bakdata.conquery.models.query.filter.event.MultiSelectFilterNode;
import com.bakdata.conquery.models.query.queryplan.filter.FilterNode;
import lombok.Getter;
import lombok.Setter;

/**
 * This filter represents a select in the front end. This means that the user can select one or more values from a list of values.
 */
@Getter
@Setter
@CPSType(id = "BIG_MULTI_SELECT", base = Filter.class)
public class BigMultiSelectFilter extends AbstractSelectFilter<String[]> {

	public BigMultiSelectFilter() {
		super(-1, FEFilterType.BIG_MULTI_SELECT);
	}

	@Override
	public FilterNode createFilterNode(String[] value) {
		return new MultiSelectFilterNode(getColumn(), value);
	}
}
