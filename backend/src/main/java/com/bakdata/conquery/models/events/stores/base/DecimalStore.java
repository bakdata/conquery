package com.bakdata.conquery.models.events.stores.base;

import java.math.BigDecimal;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.events.parser.MajorTypeId;
import com.bakdata.conquery.models.events.stores.ColumnStore;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.ToString;

@CPSType(id = "DECIMALS", base = ColumnStore.class)
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class DecimalStore extends ColumnStore<BigDecimal> {

	private final BigDecimal[] values;

	@JsonCreator
	public DecimalStore(BigDecimal[] values) {
		super(MajorTypeId.DECIMAL);
		this.values = values;
	}

	public static DecimalStore create(int size) {
		return new DecimalStore(new BigDecimal[size]);
	}

	@Override
	public long estimateEventBytes() {
		return 64; // TODO no clue!
	}

	public DecimalStore select(int[] starts, int[] ends) {
		return new DecimalStore(ColumnStore.selectArray(starts, ends, values, BigDecimal[]::new));
	}

	@Override
	public void set(int event, BigDecimal value) {
		if(value == null){
			values[event] = null;
			return;
		}

		values[event] = value;
	}

	@Override
	public boolean has(int event) {
		return values[event] != null;
	}


	@Override
	public BigDecimal get(int event) {
		return values[event];
	}


}