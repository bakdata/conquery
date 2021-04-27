package com.bakdata.conquery.models.query.concept;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.bakdata.conquery.apiv1.QueryDescription;
import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.common.Range;
import com.bakdata.conquery.models.common.daterange.CDateRange;
import com.bakdata.conquery.models.concepts.Connector;
import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.execution.ManagedExecution;
import com.bakdata.conquery.models.externalservice.ResultType;
import com.bakdata.conquery.models.query.IQuery;
import com.bakdata.conquery.models.query.QueryPlanContext;
import com.bakdata.conquery.models.query.QueryResolveContext;
import com.bakdata.conquery.models.query.Visitable;
import com.bakdata.conquery.models.query.concept.filter.CQUnfilteredTable;
import com.bakdata.conquery.models.query.queryplan.TableExportQueryPlan;
import com.bakdata.conquery.models.query.queryplan.TableExportQueryPlan.TableExportDescription;
import com.bakdata.conquery.models.query.resultinfo.ResultInfoCollector;
import com.bakdata.conquery.models.query.resultinfo.SimpleResultInfo;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


/**
 * A TABLE_EXPORT creates a full export of the given tables. It ignores selects completely.
 */
@Slf4j
@Getter
@Setter
@CPSType(id = "TABLE_EXPORT", base = QueryDescription.class)
@RequiredArgsConstructor(onConstructor = @__({@JsonCreator}))
public class TableExportQuery extends IQuery {

	@Valid
	@NotNull
	@NonNull
	protected IQuery query;
	@NotNull
	private Range<LocalDate> dateRange = Range.all();
	@NotEmpty
	@Valid
	private List<CQUnfilteredTable> tables;

	@JsonIgnore
	private List<Column> resolvedHeader;

	@Override
	public TableExportQueryPlan createQueryPlan(QueryPlanContext context) {
		int totalColumns = 0;
		List<TableExportDescription> resolvedConnectors = new ArrayList<>();
		for (CQUnfilteredTable table : tables) {
			Connector connector = table.getTable();
			final Column validityDateColumn;

			// if no dateColumn is provided, we use the default instead which is always the first one.
			// Set to null if none-available in the connector.
			if (table.getDateColumn() != null) {
				validityDateColumn = table.getDateColumn().getValue().getColumn();
			}
			else if (!connector.getValidityDates().isEmpty()) {
				validityDateColumn = connector.getValidityDates().get(0).getColumn();
			}
			else {
				validityDateColumn = null;
			}

			final TableExportDescription exportDescription = new TableExportDescription(
					connector.getTable(),
					validityDateColumn,
					totalColumns
			);

			resolvedConnectors.add(exportDescription);

			totalColumns += connector.getTable().getColumns().length;
		}

		return new TableExportQueryPlan(
				query.createQueryPlan(context),
				CDateRange.of(dateRange),
				resolvedConnectors,
				totalColumns
		);
	}

	@Override
	public void collectRequiredQueries(Set<ManagedExecution> requiredQueries) {
		query.collectRequiredQueries(requiredQueries);
	}

	@Override
	public void resolve(QueryResolveContext context) {
		query.resolve(context);
		resolvedHeader = new ArrayList<>();

		for (CQUnfilteredTable table : tables) {
			Connector connector = table.getTable();

			resolvedHeader.addAll(Arrays.asList(connector.getTable().getColumns()));
		}
	}

	@Override
	public void collectResultInfos(ResultInfoCollector collector) {
		for (Column col : resolvedHeader) {
			collector.add(new SimpleResultInfo(col.getId().toStringWithoutDataset(), ResultType.resolveResultType(col.getType())));
		}
	}

	@Override
	public void visit(Consumer<Visitable> visitor) {
		visitor.accept(this);
		query.visit(visitor);
	}
}