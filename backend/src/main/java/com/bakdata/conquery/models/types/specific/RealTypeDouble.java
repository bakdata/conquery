package com.bakdata.conquery.models.types.specific;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.types.CType;
import com.bakdata.conquery.models.types.MajorTypeId;

@CPSType(base=CType.class, id="REAL_DOUBLE")
public class RealTypeDouble extends CType<Double, Double> {

	public RealTypeDouble() {
		super(MajorTypeId.REAL, double.class);
	}

	@Override
	public boolean canStoreNull() {
		return true;
	}
	
	@Override
	public long estimateMemoryBitWidth() {
		return Double.SIZE;
	}
}