package com.bakdata.conquery.models.query.concept.specific;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.io.jackson.serializer.NsIdRefCollection;
import com.bakdata.conquery.models.concepts.Concept;
import com.bakdata.conquery.models.concepts.ConceptElement;
import com.bakdata.conquery.models.concepts.select.Select;
import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.identifiable.CentralRegistry;
import com.bakdata.conquery.models.identifiable.ids.NamespacedId;
import com.bakdata.conquery.models.identifiable.ids.specific.ConceptElementId;
import com.bakdata.conquery.models.query.QueryPlanContext;
import com.bakdata.conquery.models.query.concept.CQElement;
import com.bakdata.conquery.models.query.concept.NamespacedIdHolding;
import com.bakdata.conquery.models.query.concept.filter.CQTable;
import com.bakdata.conquery.models.query.concept.filter.FilterValue;
import com.bakdata.conquery.models.query.queryplan.ConceptQueryPlan;
import com.bakdata.conquery.models.query.queryplan.QPNode;
import com.bakdata.conquery.models.query.queryplan.aggregators.Aggregator;
import com.bakdata.conquery.models.query.queryplan.filter.FilterNode;
import com.bakdata.conquery.models.query.queryplan.specific.ConceptNode;
import com.bakdata.conquery.models.query.queryplan.specific.FiltersNode;
import com.bakdata.conquery.models.query.queryplan.specific.OrNode;
import com.bakdata.conquery.models.query.queryplan.specific.ValidityDateNode;
import com.bakdata.conquery.models.query.resultinfo.ResultInfoCollector;
import com.bakdata.conquery.models.query.resultinfo.SelectResultInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;

@Getter @Setter
@CPSType(id="CONCEPT", base=CQElement.class)
@Slf4j
@FieldNameConstants
@JsonDeserialize(using = CQConceptDeserializer.class)
@ToString
public class CQConcept implements CQElement, NamespacedIdHolding {

	@ToString.Include
	private String label;
	@Valid @NotEmpty
	private List<ConceptElementId<?>> ids = Collections.emptyList();
	@Valid @NotEmpty @JsonManagedReference
	private List<CQTable> tables = Collections.emptyList();

	@Valid @NotNull
	@NsIdRefCollection
	private List<Select> selects = new ArrayList<>();

	private boolean excludeFromTimeAggregation = false;

	@Override
	public QPNode createQueryPlan(QueryPlanContext context, ConceptQueryPlan plan) {
		ConceptElement<?>[] concepts = resolveConcepts(ids, context.getCentralRegistry());

		List<Aggregator<?>> conceptAggregators = createConceptAggregators(plan, selects);

		Concept<?> concept = concepts[0].getConcept();

		List<QPNode> tableNodes = new ArrayList<>();
		for(CQTable table : tables) {
			try {
				table.setResolvedConnector(concept.getConnectorByName(table.getId().getConnector()));
			}
			catch (NoSuchElementException exc){
				log.warn("Unable to resolve connector `{}` in dataset `{}`.",table.getId().getConnector(), concept.getDataset(), exc);
				continue;
			}

			List<Select> resolvedSelects = table.getSelects();


			List<FilterNode<?>> filters = new ArrayList<>(table.getFilters().size());
			//add filter to children
			for(FilterValue f : table.getFilters()) {
				FilterNode agg = f.getFilter().createAggregator(f.getValue());
				if(agg != null) {
					filters.add(agg);
				}
			}

			List<Aggregator<?>> aggregators = new ArrayList<>();
			//add aggregators

			aggregators.addAll(conceptAggregators);
			aggregators.addAll(createConceptAggregators(plan, resolvedSelects));

			if(!excludeFromTimeAggregation && context.isGenerateSpecialDateUnion()) {
				aggregators.add(plan.getSpecialDateUnion());
			}

			final FiltersNode filtersNode = conceptChild(concept, context, filters, aggregators);

			tableNodes.add(
				new ConceptNode(
					concepts,
					calculateBitMask(concepts),
					table,
					new ValidityDateNode(
						selectValidityDateColumn(table),
						filtersNode
					)
				)
			);
		}

		if(tableNodes.isEmpty()){
			throw new IllegalStateException(String.format("Unable to resolve any connector for query `%s`", label));
		}

		return OrNode.of(tableNodes);
	}

	private long calculateBitMask(ConceptElement<?>[] concepts) {
		long mask = 0;
		for(ConceptElement<?> concept : concepts) {
			mask |= concept.calculateBitMask();
		}
		return mask;
	}

	public static ConceptElement[] resolveConcepts(List<ConceptElementId<?>> ids, CentralRegistry centralRegistry) {
		return
				ids
					.stream()
					.map(id -> centralRegistry.resolve(id.findConcept()).getElementById(id))
					.toArray(ConceptElement[]::new);
	}

	protected FiltersNode conceptChild(Concept<?> concept, QueryPlanContext context, List<FilterNode<?>> filters, List<Aggregator<?>> aggregators) {
		return FiltersNode.create(filters, aggregators);
	}

	private static List<Aggregator<?>> createConceptAggregators(ConceptQueryPlan plan, List<Select> select) {

		List<Aggregator<?>> nodes = new ArrayList<>();

		for (Select s : select) {
			Aggregator<?> agg = s.createAggregator();

			plan.addAggregator(agg);
			nodes.add(agg);
		}
		return nodes;
	}

	private Column selectValidityDateColumn(CQTable t) {
		if(t.selectedValidityDate() != null) {
			return t
				.getResolvedConnector()
				.getValidityDateColumn(t.selectedValidityDate());
		}

		//else use this first defined validity date column
		else if(!t.getResolvedConnector().getValidityDates().isEmpty()) {
			return t.getResolvedConnector().getValidityDates().get(0).getColumn();
		}
		else {
			return null;
		}
	}

	@Override
	public void collectResultInfos(ResultInfoCollector collector) {
		selects.forEach(sel -> collector.add(new SelectResultInfo(sel, this)));
		for (CQTable table : tables) {
			table.getSelects().forEach(sel -> collector.add(new SelectResultInfo(sel, this)));
		}
	}

	@Override
	public void collectNamespacedIds(Set<NamespacedId> namespacedIds) {
		checkNotNull(namespacedIds);
		namespacedIds.addAll(ids);
		selects.forEach(select -> namespacedIds.add(select.getId()));
		tables.forEach(table -> namespacedIds.add(table.getId()));
	}
}
