package com.bakdata.conquery.models.query.filter.event;

import java.util.Set;

import javax.validation.constraints.NotNull;

import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.events.stores.root.StringStore;
import com.bakdata.conquery.models.query.queryplan.clone.CloneContext;
import com.bakdata.conquery.models.query.queryplan.filter.EventFilterNode;
import lombok.Getter;
import lombok.Setter;

/**
 * Single events are filtered, and included if they start with a given prefix. Entity is only included if it has any event with prefix.
 */
public class PrefixTextFilterNode extends EventFilterNode<String> {

	@NotNull
	@Getter
	@Setter
	private Column column;

	private StringStore store;

	public PrefixTextFilterNode(Column column, String filterValue) {
		super(filterValue);
		this.column = column;
	}

	@Override
	public PrefixTextFilterNode doClone(CloneContext ctx) {
		return new PrefixTextFilterNode(getColumn(), getFilterValue());
	}

	@Override
	public void nextBlock(Bucket bucket) {
		store = (StringStore) bucket.getStore(getColumn());
	}

	@Override
	public boolean checkEvent(Bucket bucket, int event) {
		if (!bucket.has(event, getColumn())) {
			return false;
		}

		final int id = store.getString(event);
		String value = store.getElement(id);

		//if performance is a problem we could find the filterValue once in the dictionary and then only check the values
		return value.startsWith(getFilterValue());
	}

	@Override
	public void collectRequiredTables(Set<Table> requiredTables) {
		requiredTables.add(column.getTable());
	}

}
