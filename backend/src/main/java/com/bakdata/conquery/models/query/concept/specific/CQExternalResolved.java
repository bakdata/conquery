package com.bakdata.conquery.models.query.concept.specific;

import java.util.Map;

import javax.validation.constraints.NotNull;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.common.BitMapCDateSet;
import com.bakdata.conquery.models.query.QueryPlanContext;
import com.bakdata.conquery.models.query.concept.CQElement;
import com.bakdata.conquery.models.query.queryplan.ConceptQueryPlan;
import com.bakdata.conquery.models.query.queryplan.QPNode;
import com.bakdata.conquery.models.query.queryplan.specific.ExternalNode;
import com.bakdata.conquery.models.query.resultinfo.ResultInfoCollector;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@CPSType(id="EXTERNAL_RESOLVED", base=CQElement.class)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CQExternalResolved implements CQElement {

	@Getter @NotNull @NonNull
	private Map<Integer, BitMapCDateSet> values = ImmutableMap.of();
	
	@Override
	public QPNode createQueryPlan(QueryPlanContext context, ConceptQueryPlan plan) {
		return new ExternalNode(context.getStorage().getDataset().getAllIdsTableId(), values, plan.getSpecialDateUnion());
	}
	
	@Override
	public void collectResultInfos(ResultInfoCollector collector) {}
}
