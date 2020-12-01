package com.bakdata.conquery.models.events.stores.specific;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.events.parser.MajorTypeId;
import com.bakdata.conquery.models.events.stores.ColumnStore;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@CPSType(base = ColumnStore.class, id = "DECIMAL_SCALED")
@Getter
public class DecimalTypeScaled extends ColumnStore<BigDecimal> {

	private final int scale;
	private final ColumnStore<Long> subType;

	@JsonCreator
	public DecimalTypeScaled(int scale, ColumnStore subType) {
		super(MajorTypeId.DECIMAL);
		this.scale = scale;
		this.subType = subType;
	}

		@Override
	public BigDecimal createScriptValue(BigDecimal value) {
		return null;
	}

	@Override
	public String toString() {
		return "DecimalTypeScaled[numberType=" + subType + "]";
	}

	@Override
	public long estimateEventBytes() {
		return subType.estimateEventBytes();
	}

	@Override
	public DecimalTypeScaled select(int[] starts, int[] length) {
		return new DecimalTypeScaled(scale, subType.select(starts, length));
	}

	@Override
	public void set(int event, BigDecimal value) {
		if (value == null) {
			subType.set(event, null);
		}
		else {
			subType.set(event, unscale(scale, value).longValue());
		}
	}

	public static BigInteger unscale(int scale, BigDecimal value) {
		return value.movePointRight(scale).toBigIntegerExact();
	}

	@Override
	public BigDecimal get(int event) {
		return scale(scale, subType.get(event));
	}

	public static BigDecimal scale(int scale, long value) {
		return BigDecimal.valueOf(value, scale);
	}

	@Override
	public boolean has(int event) {
		return subType.has(event);
	}
}