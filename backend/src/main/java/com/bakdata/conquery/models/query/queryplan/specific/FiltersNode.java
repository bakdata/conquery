package com.bakdata.conquery.models.query.queryplan.specific;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.identifiable.ids.specific.TableId;
import com.bakdata.conquery.models.query.QueryExecutionContext;
import com.bakdata.conquery.models.query.queryplan.QPNode;
import com.bakdata.conquery.models.query.queryplan.aggregators.Aggregator;
import com.bakdata.conquery.models.query.queryplan.aggregators.specific.ExistsAggregator;
import com.bakdata.conquery.models.query.queryplan.aggregators.specific.UniversalAggregator;
import com.bakdata.conquery.models.query.queryplan.clone.CloneContext;
import com.bakdata.conquery.models.query.queryplan.filter.EventFilterNode;
import com.bakdata.conquery.models.query.queryplan.filter.FilterNode;
import lombok.Getter;
import lombok.ToString;


@ToString(of = {"filters", "aggregators"})
public class FiltersNode extends QPNode {

	private boolean hit = false;

	@Getter
	private final List<? extends FilterNode<?>> filters;
	private final List<EventFilterNode<?>> eventFilters;
	private final List<Aggregator<?>> aggregators;

	private FiltersNode(List<? extends FilterNode<?>> filters, List<EventFilterNode<?>> eventFilters, List<Aggregator<?>> aggregators) {
		this.filters = filters;
		this.eventFilters = eventFilters;
		this.aggregators = aggregators;

		// Exists Aggregators return true when this FiltersNode is true, so they should not have their own logic for it.
		// This links them up as a back-reference.
		for (Aggregator<?> aggregator : aggregators) {
			if (aggregator instanceof ExistsAggregator) {
				((ExistsAggregator) aggregator).setParent(this);
			}
		}
	}

	public static FiltersNode create(List<? extends FilterNode<?>> filters, List<Aggregator<?>> aggregators) {
		final ArrayList<EventFilterNode<?>> eventFilters = new ArrayList<>(filters.size());


		// Select only Event Filtering nodes as they are used differently.
		for (FilterNode<?> filter : filters) {
			if (!(filter instanceof EventFilterNode)) {
				continue;
			}

			eventFilters.add((EventFilterNode<?>) filter);
		}

		return  new FiltersNode(filters, eventFilters, aggregators);
	}


	@Override
	public void nextTable(QueryExecutionContext ctx, TableId currentTable) {
		super.nextTable(ctx, currentTable);
		filters.forEach(f -> f.nextTable(ctx, currentTable));
		aggregators.forEach(a -> a.nextTable(ctx, currentTable));
	}
	
	@Override
	public void nextBlock(Bucket bucket) {
		super.nextBlock(bucket);
		filters.forEach(f -> f.nextBlock(bucket));
		aggregators.forEach(a -> a.nextBlock(bucket));
	}
	
	@Override
	public final void acceptEvent(Bucket bucket, int event) {
		for(EventFilterNode<?> f : eventFilters) {
			if (!f.checkEvent(bucket, event)) {
				return;
			}
		}

		filters.forEach(f -> f.acceptEvent(bucket, event));
		aggregators.forEach(a -> a.acceptEvent(bucket, event));

		hit = true;
	}

	@Override
	public boolean isContained() {
		for(FilterNode<?> f : filters) {
			if (!f.isContained()) {
				return false;
			}
		}

		return hit;
	}
	
	@Override
	public FiltersNode doClone(CloneContext ctx) {
		List<FilterNode<?>> filters = new ArrayList<>(this.filters);
		filters.replaceAll(ctx::clone);

		List<EventFilterNode<?>> eventFilters = new ArrayList<>(this.eventFilters);
		eventFilters.replaceAll(ctx::clone);

		List<Aggregator<?>> aggregators = new ArrayList<>(this.aggregators);
		aggregators.replaceAll(ctx::clone);

		return new FiltersNode(filters, eventFilters, aggregators);
	}

	@Override
	public void collectRequiredTables(Set<TableId> requiredTables) {
		super.collectRequiredTables(requiredTables);

		filters.forEach(f -> f.collectRequiredTables(requiredTables));
		aggregators.forEach(a -> a.collectRequiredTables(requiredTables));
	}
	
	@Override
	public boolean isOfInterest(Bucket bucket) {
		return true;
	}

	@Override
	public boolean isAlwaysActive() {
		for (Aggregator<?> aggregator : aggregators) {
			if (aggregator instanceof UniversalAggregator) {
				return true;
			}
		}

		return false;
	}
}
