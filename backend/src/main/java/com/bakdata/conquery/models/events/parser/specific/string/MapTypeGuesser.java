package com.bakdata.conquery.models.events.parser.specific.string;

import com.bakdata.conquery.models.dictionary.MapDictionary;
import com.bakdata.conquery.models.events.parser.specific.StringParser;
import com.bakdata.conquery.models.events.stores.root.IntegerStore;
import com.bakdata.conquery.models.events.stores.root.StringStore;
import com.bakdata.conquery.models.events.stores.specific.string.StringTypeDictionary;
import com.bakdata.conquery.models.events.stores.specific.string.StringTypeEncoded;
import lombok.RequiredArgsConstructor;

/**
 * Map implementation using {@link MapDictionary} implementation.
 */
@RequiredArgsConstructor
public class MapTypeGuesser extends StringTypeGuesser {

	private final StringParser p;

	@Override
	public Guess createGuess() {
		IntegerStore indexType = p.decideIndexType();

		final MapDictionary dictionaryEntries = new MapDictionary(null, "");

		StringTypeDictionary type = new StringTypeDictionary(indexType, dictionaryEntries, dictionaryEntries.getName());
		long mapSize = MapDictionary.estimateMemoryConsumption(
				p.getStrings().size(),
				p.getDecoded().stream().mapToLong(s -> s.length).sum()
		);


		StringTypeEncoded result = new StringTypeEncoded(type, p.getEncoding());

		return new Guess(
				result,
				indexType.estimateMemoryConsumptionBytes(),
				mapSize
		) {
			@Override
			public StringStore getType() {
				MapDictionary map = new MapDictionary(null, "");
				for (byte[] v : p.getDecoded()) {
					map.add(v);
				}
				type.setDictionary(map);
				return super.getType();
			}
		};
	}

}
