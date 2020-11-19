package com.bakdata.conquery.models.types;

import java.util.function.Function;

import com.bakdata.conquery.models.config.ParserConfig;
import com.bakdata.conquery.models.types.parser.Parser;
import com.bakdata.conquery.models.types.parser.specific.BooleanParser;
import com.bakdata.conquery.models.types.parser.specific.DateParser;
import com.bakdata.conquery.models.types.parser.specific.DateRangeParser;
import com.bakdata.conquery.models.types.parser.specific.DecimalParser;
import com.bakdata.conquery.models.types.parser.specific.IntegerParser;
import com.bakdata.conquery.models.types.parser.specific.MoneyParser;
import com.bakdata.conquery.models.types.parser.specific.RealParser;
import com.bakdata.conquery.models.types.parser.specific.string.StringParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MajorTypeId implements MajorTypeIdHolder {

	STRING(false, StringParser::new),
	INTEGER(false, IntegerParser::new),
	BOOLEAN(false, BooleanParser::new),
	REAL(false, RealParser::new),
	DECIMAL(false, DecimalParser::new),
	MONEY(false, MoneyParser::new),
	DATE(true, DateParser::new),
	DATE_RANGE(true, DateRangeParser::new);

	@Getter
	private final boolean dateCompatible;
	private final Function<ParserConfig, Parser<?>> supplier;

	public Parser<?> createParser(ParserConfig config) {
		return supplier.apply(config);
	}

	@Override
	public MajorTypeId getTypeId() {
		return this;
	}
}
