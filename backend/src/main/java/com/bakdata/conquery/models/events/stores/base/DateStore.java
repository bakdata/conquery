package com.bakdata.conquery.models.events.stores.base;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.common.CDate;
import com.bakdata.conquery.models.common.daterange.CDateRange;
import com.bakdata.conquery.models.events.ColumnStore;
import com.bakdata.conquery.models.events.stores.ColumnStoreAdapter;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@CPSType(id = "DATES", base = ColumnStore.class)
@Getter
public class DateStore extends ColumnStoreAdapter<Integer> {

	private final ColumnStore<Long> store;

	@JsonCreator
	public DateStore(ColumnStore<Long> store) {
		this.store = store;
	}

	public static DateStore create(int size) {
		return new DateStore(IntegerStore.create(size));
	}

	public DateStore select(int[] starts, int[] ends) {
		return new DateStore(store.select(starts,ends));
	}

	@Override
	public void set(int event, Integer value) {
		if (value == null) {
			store.set(event, null);
			return;
		}

		store.set(event, (long) value);
	}

	@Override
	public boolean has(int event) {
		return store.has(event);
	}

	@Override
	public CDateRange getDateRange(int event) {
		return CDateRange.exactly(get(event));
	}

	@Override
	public Integer get(int event) {
		return (int) store.getInteger(event);
	}

	@Override
	public Object getAsObject(int event) {
		return CDate.toLocalDate(getDate(event));
	}
}