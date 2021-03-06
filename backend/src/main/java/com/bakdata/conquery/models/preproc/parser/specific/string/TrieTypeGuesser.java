package com.bakdata.conquery.models.preproc.parser.specific.string;

import java.util.UUID;

import com.bakdata.conquery.models.datasets.Dataset;
import com.bakdata.conquery.models.events.stores.root.IntegerStore;
import com.bakdata.conquery.models.events.stores.root.StringStore;
import com.bakdata.conquery.models.events.stores.specific.string.StringTypeDictionary;
import com.bakdata.conquery.models.events.stores.specific.string.StringTypeEncoded;
import com.bakdata.conquery.models.preproc.parser.specific.StringParser;
import com.bakdata.conquery.util.dict.SuccinctTrie;
import lombok.RequiredArgsConstructor;

/**
 * Construct a {@link SuccinctTrie} and estimate it's memory usage. Return
 */
@RequiredArgsConstructor
public class TrieTypeGuesser extends StringTypeGuesser {

	private final StringParser p;

	@Override
	public Guess createGuess() {
		IntegerStore indexType = p.decideIndexType();

		SuccinctTrie trie = new SuccinctTrie(Dataset.PLACEHOLDER, UUID.randomUUID().toString());
		StringTypeDictionary type = new StringTypeDictionary(indexType, trie);

		for (byte[] v : p.getDecoded()) {
			trie.add(v);
		}


		StringTypeEncoded result = new StringTypeEncoded(type, p.getEncoding());

		return new Guess(
				result,
				indexType.estimateMemoryConsumptionBytes(),
				trie.estimateMemoryConsumption()
		) {
			@Override
			public StringStore getType() {
				trie.compress();
				return super.getType();
			}
		};
	}

}
