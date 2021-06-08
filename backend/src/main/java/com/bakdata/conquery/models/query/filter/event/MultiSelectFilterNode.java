package com.bakdata.conquery.models.query.filter.event;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.swing.text.html.Option;
import javax.validation.constraints.NotNull;

import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.datasets.Import;
import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.events.stores.root.StringStore;
import com.bakdata.conquery.models.query.queryplan.clone.CloneContext;
import com.bakdata.conquery.models.query.queryplan.filter.EventFilterNode;
import lombok.Getter;
import lombok.Setter;

/**
 * Event is included when the value in column is one of many selected.
 */
public class MultiSelectFilterNode extends EventFilterNode<String[]> {


	@NotNull
	@Getter
	@Setter
	private Column column;

	/**
	 * Shared between all executing Threads to maximize utilization.
	 */
	private ConcurrentMap<Import, int[]> selectedValuesCache;
	private int[] selectedValues;

	public MultiSelectFilterNode(Column column, String[] filterValue) {
		super(filterValue);
		this.column = column;
		selectedValuesCache = new ConcurrentHashMap<>();
	}

	private MultiSelectFilterNode(Column column, String[] filterValue, ConcurrentMap<Import, int[]> cache) {
		this(column, filterValue);
		selectedValuesCache = cache;
	}

	@Override
	public void setFilterValue(String[] strings) {
		selectedValuesCache = new ConcurrentHashMap<>();
		selectedValues = null;
		super.setFilterValue(strings);
	}

	@Override
	public void nextBlock(Bucket bucket) {
		selectedValues = selectedValuesCache.computeIfAbsent(bucket.getImp(),imp -> findIds(bucket, filterValue));
	}

	private int[] findIds(Bucket bucket, String[] values) {
		int[] selectedValues = new int[values.length];

		StringStore type = (StringStore) bucket.getStore(getColumn());

		for (int index = 0; index < values.length; index++) {
			String select = values[index];
			int parsed = type.getId(select);
			selectedValues[index] = parsed;
		}

		return selectedValues;
	}


	@Override
	public Optional<Boolean> eventFiltersApply(Bucket bucket, int event) {
		if(selectedValues == null){
			throw new IllegalStateException("No selected values  were set.");
		}
		if (!bucket.has(event, getColumn())) {
			return Optional.of(Boolean.FALSE);
		}

		int stringToken = bucket.getString(event, getColumn());

		for (int selectedValue : selectedValues) {
			if (selectedValue == stringToken) {
				return Optional.of(Boolean.TRUE);
			}
		}

		return Optional.of(Boolean.FALSE);
	}

	@Override
	public MultiSelectFilterNode doClone(CloneContext ctx) {
		// We reuse the cache
		return new MultiSelectFilterNode(getColumn(), filterValue, selectedValuesCache);
	}

	@Override
	public boolean isOfInterest(Bucket bucket) {
		for (String selected : getFilterValue()) {
			if(((StringStore) bucket.getStores()[getColumn().getPosition()]).getId(selected) != -1) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void collectRequiredTables(Set<Table> requiredTables) {
		requiredTables.add(column.getTable());
	}

}
