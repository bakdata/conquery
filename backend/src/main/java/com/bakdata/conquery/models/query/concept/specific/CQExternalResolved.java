package com.bakdata.conquery.models.query.concept.specific;

import com.bakdata.conquery.ConqueryConstants;
import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.common.CDateSet;
import com.bakdata.conquery.models.identifiable.ids.specific.DatasetId;
import com.bakdata.conquery.models.identifiable.ids.specific.TableId;
import com.bakdata.conquery.models.query.QueryPlanContext;
import com.bakdata.conquery.models.query.concept.CQElement;
import com.bakdata.conquery.models.query.queryplan.ConceptQueryPlan;
import com.bakdata.conquery.models.query.queryplan.QPNode;
import com.bakdata.conquery.models.query.queryplan.specific.ExternalNode;
import com.bakdata.conquery.models.query.queryplan.specific.SpecialDateUnionAggregatorNode;
import com.bakdata.conquery.models.query.resultinfo.ResultInfoCollector;
import com.bakdata.conquery.models.query.visitor.QueryVisitor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.Map;

@CPSType(id="EXTERNAL_RESOLVED", base=CQElement.class)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CQExternalResolved implements CQElement {

	@Getter @NotNull @NonNull
	private Map<Integer, CDateSet> values;
	
	@Override
	public void visit(QueryVisitor visitor) {}

	@Override
	public QPNode createQueryPlan(QueryPlanContext context, ConceptQueryPlan plan) {
		DatasetId dataset = context.getDataset();
		return new ExternalNode(
			new SpecialDateUnionAggregatorNode(
				new TableId(
					dataset,
					ConqueryConstants.ALL_IDS_TABLE
				),
				plan.getSpecialDateUnion()
			),
			dataset,
			values);
	}
	
	@Override
	public void collectResultInfos(ResultInfoCollector collector) {}
}
