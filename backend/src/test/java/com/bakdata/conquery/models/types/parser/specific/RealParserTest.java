package com.bakdata.conquery.models.types.parser.specific;

import static org.assertj.core.api.Assertions.assertThat;

import com.bakdata.conquery.models.config.ParserConfig;
import com.bakdata.conquery.models.types.specific.RealTypeDouble;
import com.bakdata.conquery.models.types.specific.RealTypeFloat;
import org.junit.jupiter.api.Test;

class RealParserTest {

	@Test
	public void ulpInRange() {
		final ParserConfig parserConfig = new ParserConfig();
		parserConfig.setMinPrecision(Math.ulp(10));

		final RealParser realParser = new RealParser(parserConfig);

		realParser.registerValue(1d);
		realParser.registerValue(2d);
		realParser.registerValue(-3d);

		// ULP is symmetric on the 0 axis and monotonic, so larger values have a larger ULP

		assertThat(realParser.decideType().getType()).isInstanceOf(RealTypeFloat.class);

		realParser.registerValue(3000d);

		assertThat(realParser.decideType().getType()).isInstanceOf(RealTypeDouble.class);
	}

}