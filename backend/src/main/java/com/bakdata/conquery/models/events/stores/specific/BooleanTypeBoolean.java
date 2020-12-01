package com.bakdata.conquery.models.events.stores.specific;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.events.parser.MajorTypeId;
import com.bakdata.conquery.models.events.stores.ColumnStore;
import com.bakdata.conquery.models.events.stores.base.BooleanStore;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@CPSType(base= ColumnStore.class, id="BOOLEAN_BOOLEAN")
@Getter
public class BooleanTypeBoolean extends ColumnStore<Boolean> {

	private final BooleanStore store;

	@JsonCreator
	public BooleanTypeBoolean(BooleanStore store) {
		super(MajorTypeId.BOOLEAN);
		this.store = store;
	}

	@Override
	public long estimateEventBytes() {
		return Byte.SIZE;
	}

	@Override
	public BooleanTypeBoolean select(int[] starts, int[] length) {
		return new BooleanTypeBoolean(store.select(starts, length));
	}

	@Override
	public void set(int event, Boolean value) {
		store.set(event, value);
	}

	@Override
	public Boolean get(int event) {
		return store.get(event);
	}

	@Override
	public boolean has(int event) {
		return store.has(event);
	}
}