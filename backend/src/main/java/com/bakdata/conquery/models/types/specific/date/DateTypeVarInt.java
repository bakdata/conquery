package com.bakdata.conquery.models.types.specific.date;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.common.CDate;
import com.bakdata.conquery.models.common.daterange.CDateRange;
import com.bakdata.conquery.models.events.ColumnStore;
import com.bakdata.conquery.models.events.stores.base.DateStore;
import com.bakdata.conquery.models.types.CType;
import com.bakdata.conquery.models.types.MajorTypeId;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.Setter;

@CPSType(base = ColumnStore.class, id = "DATE_COMPRESSED")
@Getter
@Setter
public class DateTypeVarInt extends CType<Integer, Integer> {

	private final DateStore store;

	@JsonCreator
	public DateTypeVarInt(DateStore store) {
		super(MajorTypeId.DATE);
		this.store = store;
	}

	@Override
	public Object createScriptValue(Integer value) {
		return CDate.toLocalDate(value);
	}

	@Override
	public Object createPrintValue(Integer value) {
		return CDate.toLocalDate(value);
	}

	@Override
	public long estimateMemoryBitWidth() {
		return 0;
	}


	@Override
	public DateTypeVarInt select(int[] starts, int[] length) {
		return new DateTypeVarInt(store.select(starts, length));
	}

	@Override
	public void set(int event, Integer value) {
		store.set(event, value);
	}

	@Override
	public Integer get(int event) {
		return store.get(event);
	}

	@Override
	public boolean has(int event) {
		return store.has(event);
	}

	@Override
	public CDateRange getDateRange(int event) {
		return CDateRange.exactly(get(event));
	}
}