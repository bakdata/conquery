package com.bakdata.conquery.models.identifiable.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.bakdata.conquery.models.dictionary.EncodedDictionary;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Mapping from Csv Entity Id to External Entity Id and back from the
 * combinations of Accessor + IDs to the Entity Id.
 */
@Getter
@EqualsAndHashCode
@Slf4j
@NoArgsConstructor
public class EntityIdMap {

	@Setter
	@JsonIgnore
	private EncodedDictionary dictionary;

	/**
	 * The map from csv entity ids to external entity ids.
	 */
	@JsonIgnore
	private final Map<String, EntityPrintId> internalToPrint = new HashMap<>();

	/**
	 * The map from external entity ids to csv entity ids.
	 */
	@JsonIgnore
	private final Map<ExternalId, String> external2Internal = new HashMap<>();


	/**
	 * Helper class for serialization.
	 */
	@Data
	@Getter
	@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
	private static class Container {
		private final List<ExternalId> keys;
		private final List<String> values;
		private final Map<String, EntityPrintId> internalToPrint;
	}

	/**
	 * Constructor to deserialize from {@link Container}.
	 */
	@JsonCreator
	private EntityIdMap(Container container) {

		for (int index = 0; index < container.keys.size(); index++) {
			getExternal2Internal().put(container.keys.get(index), container.values.get(index));
		}

		getInternalToPrint().putAll(container.internalToPrint);
	}

	/**
	 * JsonValue to Serialize as {@link Container}, as Jackson cannot handle complex classes as Map-keys (ie {@link ExternalId}.
	 */
	@JsonValue
	private Container getContainer() {
		final List<ExternalId> keys = new ArrayList<>(external2Internal.size());
		final List<String> values = new ArrayList<>(external2Internal.size());

		external2Internal.forEach((key, value) -> {
			keys.add(key);
			values.add(value);
		});

		return new Container(keys, values, internalToPrint);
	}

	/**
	 * Map an internal id to an external.
	 */
	public EntityPrintId toExternal(String internal) {
		return internalToPrint.get(internal);
	}

	public int resolve(String... external) {
		String value = external2Internal.get(new ExternalId(external));

		if (value != null) {
			return dictionary.getId(value);
		}

		return -1;
	}

	public void addOutputMapping(String csvEntityId, EntityPrintId externalEntityId) {
		final EntityPrintId prior = internalToPrint.put(csvEntityId, externalEntityId);

		if (prior != null && prior.equals(externalEntityId)) {
			log.warn("Duplicate mapping  for {} to {} and {}", csvEntityId, externalEntityId, prior);
		}
	}


	public void addInputMapping(String csvEntityId, String... externalEntityId) {

		final String prior = external2Internal.put(new ExternalId(externalEntityId), csvEntityId);

		if (prior != null && prior.equals(csvEntityId)) {
			log.warn("Duplicate mapping  for {} to {} and {}", externalEntityId, csvEntityId, prior);
		}
	}

	@Data
	@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
	private static class ExternalId {
		@Getter
		private final String[] parts;
	}

}
