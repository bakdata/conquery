package com.bakdata.conquery.models.types;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.bakdata.conquery.io.cps.CPSTypeIdResolver;
import com.bakdata.conquery.models.dictionary.Dictionary;
import com.bakdata.conquery.models.exceptions.JSONException;
import com.bakdata.conquery.models.types.specific.BooleanTypeBoolean;
import com.bakdata.conquery.models.types.specific.DateRangeTypeDateRange;
import com.bakdata.conquery.models.types.specific.DateRangeTypePacked;
import com.bakdata.conquery.models.types.specific.DateRangeTypeQuarter;
import com.bakdata.conquery.models.types.specific.DateTypeVarInt;
import com.bakdata.conquery.models.types.specific.DecimalTypeBigDecimal;
import com.bakdata.conquery.models.types.specific.DecimalTypeScaled;
import com.bakdata.conquery.models.types.specific.IntegerTypeLong;
import com.bakdata.conquery.models.types.specific.IntegerTypeVarInt;
import com.bakdata.conquery.models.types.specific.MoneyTypeLong;
import com.bakdata.conquery.models.types.specific.MoneyTypeVarInt;
import com.bakdata.conquery.models.types.specific.RealTypeDouble;
import com.bakdata.conquery.models.types.specific.StringTypeDictionary;
import com.bakdata.conquery.models.types.specific.StringTypeEncoded;
import com.bakdata.conquery.models.types.specific.StringTypeEncoded.Encoding;
import com.bakdata.conquery.models.types.specific.StringTypePrefix;
import com.bakdata.conquery.models.types.specific.StringTypeSingleton;
import com.bakdata.conquery.models.types.specific.StringTypeSuffix;
import com.bakdata.conquery.models.types.specific.VarIntTypeByte;
import com.bakdata.conquery.models.types.specific.VarIntTypeInt;
import com.bakdata.conquery.models.types.specific.VarIntTypeShort;
import com.bakdata.conquery.util.SerializationTestUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class SerializationTest {

	@SuppressWarnings("rawtypes")
	public static List<CType<?,?>> createCTypes() {
		return Arrays.asList(
			new DecimalTypeScaled(13, new IntegerTypeLong(-1,1)),
			new IntegerTypeVarInt(new VarIntTypeInt(-1, +1)),
			new MoneyTypeLong(),
			new DecimalTypeBigDecimal(),
			new BooleanTypeBoolean(),
			new MoneyTypeVarInt(new VarIntTypeInt(-1, +1)),
			new RealTypeDouble(),
			new DateTypeVarInt(new VarIntTypeInt(-1, +1)),
			new StringTypeDictionary(new VarIntTypeInt(-1, +1)),
			new StringTypeEncoded(new StringTypeDictionary(new VarIntTypeInt(-1, +1)),Encoding.Base16LowerCase),
			new StringTypePrefix(new StringTypeEncoded(new StringTypeDictionary(new VarIntTypeInt(-1, +1)),Encoding.Base16LowerCase), "a"),
			new StringTypeSuffix(new StringTypeEncoded(new StringTypeDictionary(new VarIntTypeInt(-1, +1)),Encoding.Base16LowerCase), "a"),
			new StringTypeSingleton("a"),
			new IntegerTypeLong(-1,+1),
			new DateRangeTypeDateRange(),
			new DateRangeTypeQuarter(),
			new DateRangeTypePacked(),
			new DateTypeVarInt(new VarIntTypeInt(-1, +1)),
			new VarIntTypeInt(-1, +1),
			new VarIntTypeByte((byte)-1, (byte)+1),
			new VarIntTypeShort((short)-1, (short)+1)
		);
	}
	
	@Test @SuppressWarnings({ "unchecked", "rawtypes" })
	public void testAllTypesCovered() {
		assertThat(
			createCTypes()
				.stream()
				.map(Object::getClass)
				.collect(Collectors.toSet())
		)
		.containsAll(
			(Set)CPSTypeIdResolver.listImplementations(CType.class)
		);
	}

	@ParameterizedTest @MethodSource("createCTypes")
	public void testSerialization(CType<?,?> type) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException, JSONException {
		SerializationTestUtil
			.forType(CType.class)
			.ignoreClasses(Arrays.asList(Dictionary.class))
			.test(type);
	}
}
