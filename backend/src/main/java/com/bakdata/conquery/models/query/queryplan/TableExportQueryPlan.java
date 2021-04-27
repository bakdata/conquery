package com.bakdata.conquery.models.query.queryplan;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.bakdata.conquery.models.common.CDateSet;
import com.bakdata.conquery.models.common.daterange.CDateRange;
import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.query.QueryExecutionContext;
import com.bakdata.conquery.models.query.entity.Entity;
import com.bakdata.conquery.models.query.queryplan.clone.CloneContext;
import com.bakdata.conquery.models.query.results.EntityResult;
import com.bakdata.conquery.models.query.results.MultilineEntityResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The QueryPlan creates a full dump of the given table within a certain
 * date range.
 */
@RequiredArgsConstructor
public class TableExportQueryPlan implements QueryPlan<MultilineEntityResult> {

	private final QueryPlan subPlan;
	private final CDateRange dateRange;
	private final List<TableExportDescription> tables;
	private final int totalColumns;

	@Override
	public QueryPlan clone(CloneContext ctx) {
		return new TableExportQueryPlan(subPlan.clone(ctx), dateRange, tables, totalColumns);
	}

	@Override
	public boolean isOfInterest(Entity entity) {
		return subPlan.isOfInterest(entity);
	}

	@Override
	public CDateSet getValidityDates(MultilineEntityResult result) {
		// TODO figure out where the dates are
		return CDateSet.create();
	}

	@Override
	public Optional<MultilineEntityResult> execute(QueryExecutionContext ctx, Entity entity) {
		Optional<EntityResult> result = subPlan.execute(ctx, entity);

		if (result.isEmpty() || tables.isEmpty()) {
			return Optional.empty();
		}

		List<Object[]> results = new ArrayList<>();
		for (TableExportDescription exportDescription : tables) {


			for (Bucket bucket : ctx.getEntityBucketsForTable(entity, exportDescription.getTable())) {

				int entityId = entity.getId();

				if (!bucket.containsEntity(entityId)) {
					continue;
				}

				int start = bucket.getEntityStart(entityId);
				int end = bucket.getEntityEnd(entityId);

				for (int event = start; event < end; event++) {

					// Export Full-table if it has no validity date.
					if (exportDescription.getValidityDateColumn() != null && !bucket.eventIsContainedIn(event, exportDescription.getValidityDateColumn(), CDateSet.create(dateRange))) {
						continue;
					}

					Object[] entry = new Object[totalColumns];
					for (int col = 0; col < exportDescription.getTable().getColumns().length; col++) {
						final Column column = exportDescription.getTable().getColumns()[col];

						if (!bucket.has(event, column)) {
							continue;
						}

						// depending on context use pretty printing or script value
						entry[exportDescription.getColumnOffset() + col] = bucket.createScriptValue(event, column);
					}

					results.add(entry);
				}
			}
		}

		return Optional.of(new MultilineEntityResult(
				entity.getId(),
				results
		));
	}

	@RequiredArgsConstructor
	@Getter
	public static class TableExportDescription {
		private final Table table;
		@Nullable
		private final Column validityDateColumn;
		private final int columnOffset;
	}
}
