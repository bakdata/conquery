package com.bakdata.conquery.models.dictionary;


import com.bakdata.conquery.models.events.stores.root.IntegerStore;
import com.bakdata.conquery.models.events.stores.root.StringStore;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Create a mapping from one {@link Dictionary} to the other (Map source to target). Adding all ids in target, not in source, to source.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Slf4j
@ToString
public class DictionaryMapping {

	private final Dictionary sourceDictionary;
	private final Dictionary targetDictionary;

	private final Int2IntMap source2Target;
	private final Int2IntMap target2Source;

	private final int numberOfNewIds;

	public static DictionaryMapping createAndImport(Dictionary from, Dictionary to) {

		int newIds = 0;

		Int2IntMap source2Target = new Int2IntOpenHashMap(from.size());
		Int2IntMap target2Source = new Int2IntOpenHashMap(from.size());

		for (int id = 0; id < from.size(); id++) {

			byte[] value = from.getElement(id);
			int targetId = to.getId(value);

			//if id was unknown until now
			if (targetId == -1L) {
				targetId = to.add(value);
				newIds++;
			}

			source2Target.put(id, targetId);
			target2Source.put(targetId, id);

		}

		return new DictionaryMapping(from, to, source2Target, target2Source, newIds);
	}

	public int source2Target(int sourceId) {
		return source2Target.get(sourceId);
	}

	public int target2Source(int targetId) {
		return target2Source.get(targetId);
	}

	/**
	 * Mutably applies mapping to store.
	 */
	public void applyToStore(StringStore from, IntegerStore to) {
		for (int event = 0; event < from.getLines(); event++) {
			if (!from.has(event)) {
				to.setNull(event);
				continue;
			}

			final int string = from.getString(event);

			to.setInteger(event, source2Target(string));
		}
	}

}
