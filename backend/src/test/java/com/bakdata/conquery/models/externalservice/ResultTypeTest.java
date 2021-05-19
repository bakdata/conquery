package com.bakdata.conquery.models.externalservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Locale;
import java.util.stream.Stream;

import com.bakdata.conquery.models.forms.util.DateContext;
import com.bakdata.conquery.io.jackson.Jackson;
import com.bakdata.conquery.models.config.ConqueryConfig;
import com.bakdata.conquery.models.i18n.I18n;
import com.bakdata.conquery.models.query.PrintSettings;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ResultTypeTest {
	
	static {
		// Initialization of the internationalization
		I18n.init();
	}

	public static final ConqueryConfig CONFIG = new ConqueryConfig();
	private static final PrintSettings PRETTY = new PrintSettings(true, Locale.ENGLISH, null, CONFIG, null);
	private static final PrintSettings PRETTY_DE = new PrintSettings(true, Locale.GERMAN, null, CONFIG, null);
	private static final PrintSettings PLAIN = new PrintSettings(false, Locale.ENGLISH, null, CONFIG, null);

	@SuppressWarnings("unused")
	public static Stream<Arguments> testData() {
		//init global default config
		ConqueryConfig cfg = new ConqueryConfig();
		cfg.getPreprocessor().getParsers().setCurrency(Currency.getInstance("EUR"));
		return Stream.of(
			Arguments.of(PRETTY, ResultType.BooleanT.INSTANCE, true,	"Yes"),
			Arguments.of(PRETTY, ResultType.BooleanT.INSTANCE, false,	"No"),
			Arguments.of(PRETTY, ResultType.CategoricalT.INSTANCE, "test", "test"),
			Arguments.of(PRETTY, ResultType.ResolutionT.INSTANCE, DateContext.Resolution.COMPLETE.name(), "complete"),
			Arguments.of(PRETTY_DE, ResultType.ResolutionT.INSTANCE, DateContext.Resolution.COMPLETE.name(), "Gesamt"),
			Arguments.of(PRETTY, ResultType.DateT.INSTANCE, LocalDate.of(2013, 7, 12).toEpochDay(), "2013-07-12"),
			Arguments.of(PRETTY, ResultType.IntegerT.INSTANCE, 51839274, "51,839,274"),
			Arguments.of(PRETTY_DE, ResultType.IntegerT.INSTANCE, 51839274, "51.839.274"),
			Arguments.of(PRETTY, ResultType.MoneyT.INSTANCE, 51839274L, "518,392.74"),
			Arguments.of(PRETTY_DE, ResultType.MoneyT.INSTANCE, 51839274L, "518.392,74"),
			Arguments.of(PRETTY, ResultType.NumericT.INSTANCE, 0.2, "0.2"),
			Arguments.of(PRETTY_DE, ResultType.NumericT.INSTANCE, 0.2, "0,2"),
			Arguments.of(PRETTY, ResultType.NumericT.INSTANCE, new BigDecimal("716283712389817246892743124.12312"), "716,283,712,389,817,246,892,743,124.12312"),
			Arguments.of(PRETTY_DE, ResultType.NumericT.INSTANCE, new BigDecimal("716283712389817246892743124.12312"), "716.283.712.389.817.246.892.743.124,12312"),
			Arguments.of(PRETTY, ResultType.StringT.INSTANCE, "test", "test"),
			
			Arguments.of(PLAIN, ResultType.BooleanT.INSTANCE, true,	"1"),
			Arguments.of(PLAIN, ResultType.BooleanT.INSTANCE, false,	"0"),
			Arguments.of(PLAIN, ResultType.CategoricalT.INSTANCE, "test", "test"),
			Arguments.of(PLAIN, ResultType.DateT.INSTANCE, LocalDate.of(2013, 7, 12).toEpochDay(), "2013-07-12"),
			Arguments.of(PLAIN, ResultType.IntegerT.INSTANCE, 51839274, "51839274"),
			Arguments.of(PLAIN, ResultType.MoneyT.INSTANCE, 51839274L, "51839274"),
			Arguments.of(PLAIN, ResultType.NumericT.INSTANCE, 0.2, "0.2"),
			Arguments.of(PLAIN, ResultType.NumericT.INSTANCE, new BigDecimal("716283712389817246892743124.12312"), "716283712389817246892743124.12312"),
			Arguments.of(PLAIN, ResultType.StringT.INSTANCE, "test", "test"),
			Arguments.of(PLAIN, ResultType.CategoricalT.INSTANCE, DateContext.Resolution.COMPLETE.name(), "COMPLETE"),
			Arguments.of(PLAIN, ResultType.StringT.INSTANCE, ImmutableMap.of("a", 2, "c", 1), "{a=2, c=1}")
		);
	}
	
	
	@ParameterizedTest(name="{0} {1}: {2} -> {3}") @MethodSource("testData")
	public void testPrinting(PrintSettings cfg, ResultType type, Object value, String expected) throws IOException {
		assertThat(type.printNullable(cfg, value)).isEqualTo(expected);
		String str = Jackson.MAPPER.writeValueAsString(value);
		Object copy = Jackson.MAPPER.readValue(str, Object.class);
		assertThat(type.printNullable(cfg, copy)).isEqualTo(expected);
	}
	
	@ParameterizedTest(name="{1}: {2}") @MethodSource("testData")
	public void testBinaryPrinting(PrintSettings cfg, ResultType type, Object value, String expected) throws IOException {
		assertThat(type.printNullable(cfg, value)).isEqualTo(expected);
		byte[] bytes = Jackson.BINARY_MAPPER.writeValueAsBytes(value);
		Object copy = Jackson.BINARY_MAPPER.readValue(bytes, Object.class);
		assertThat(type.printNullable(cfg, copy)).isEqualTo(expected);
	}
}
