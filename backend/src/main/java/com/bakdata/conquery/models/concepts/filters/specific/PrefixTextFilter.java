package com.bakdata.conquery.models.concepts.filters.specific;


import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.api.description.FEFilter;
import com.bakdata.conquery.models.api.description.FEFilterType;
import com.bakdata.conquery.models.concepts.filters.Filter;
import com.bakdata.conquery.models.concepts.filters.SingleColumnFilter;
import com.bakdata.conquery.models.query.concept.filter.FilterValue.CQStringFilter;
import com.bakdata.conquery.models.query.filter.event.PrefixTextFilterNode;
import com.bakdata.conquery.models.query.queryplan.aggregators.Aggregator;
import com.bakdata.conquery.models.query.queryplan.filter.FilterNode;
import com.bakdata.conquery.models.types.MajorTypeId;
import lombok.Getter;
import lombok.Setter;

import java.util.EnumSet;

@Getter
@Setter
@CPSType(id = "PREFIX_TEXT", base = Filter.class)
public class PrefixTextFilter extends SingleColumnFilter<CQStringFilter> {


	@Override
	public void configureFrontend(FEFilter f) {
		f.setType(FEFilterType.STRING);
	}
	
	@Override
	public EnumSet<MajorTypeId> getAcceptedColumnTypes() {
		return EnumSet.of(MajorTypeId.STRING);
	}

	@Override
	public FilterNode createFilter(CQStringFilter filterValue, Aggregator<?> aggregator) {
		return new PrefixTextFilterNode(this, filterValue);
	}

}
