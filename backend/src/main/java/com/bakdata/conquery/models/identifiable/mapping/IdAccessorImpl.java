package com.bakdata.conquery.models.identifiable.mapping;

import com.bakdata.conquery.io.xodus.NamespaceStorage;
import com.bakdata.conquery.models.query.concept.specific.CQExternal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The standard Implementation for an IdAccessor.
 */
@RequiredArgsConstructor
public class IdAccessorImpl implements IdAccessor {

	/**
	 * The corresponding accessor.
	 */
	@Getter
	private final IdMappingAccessor accessor;

	/**
	 * The mapping from column indices in the given CSV to the original uploaded csv.
	 */
	@Getter
	private final int[] applicationMapping;

	/**
	 * The used namespace storage.
	 */
	private final NamespaceStorage storage;

	/**
	 * removes all non Id Fields from a given CSV Line respective to the given formal Columns.
	 * @param csvLine A line from the csv.
	 * @param formatColumns The format description for the given Csv..
	 * @return The filtered csvLine.
	 */
	public static String[] removeNonIdFields(String[] csvLine, List<CQExternal.FormatColumn> formatColumns) {
		if(csvLine.length < formatColumns.size()) {
			throw new IllegalArgumentException(String.format("csvline `{}` of length {}, should be at least {}",csvLine, csvLine.length, formatColumns.size()));
		}

		List<String> result = new ArrayList<>();
		for (int i = 0; i < csvLine.length; i++) {
			if (formatColumns.get(i) == CQExternal.FormatColumn.ID) {
				result.add(csvLine[i]);
			}
		}

		return result.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
	}

	@Override
	public CsvEntityId getCsvEntityId(String[] csvLine) {
		String[] reorderedCsvLine = reorder(csvLine);
		return Optional
			.ofNullable(storage.getIdMapping())
			.map(PersistentIdMap::getExternalIdPartCsvIdMap)
			.map(m->m.get(new SufficientExternalEntityId(reorderedCsvLine)))
			// fallback: we join everything relevant together
			.orElseGet(()->accessor.getFallbackCsvId(reorderedCsvLine));
	}

	/**
	 * Reorder takes a line from an uploaded CSV an reorders the id columns, to match the specified format.
	 * @param csvLine The csv line.
	 * @return Outputs a csvLine like the one in the original uploaded id mapping CSV
	 */
	private String[] reorder(String[] csvLine) {
		String[] reorderedCsvLine = new String[accessor.getHeader().length];
		for (int i = 0; i < csvLine.length; i++) {
			int indexInHeader = applicationMapping[i];
			if (indexInHeader != -1) {
				reorderedCsvLine[indexInHeader] = csvLine[i];
			}
		}
		return reorderedCsvLine;
	}
}
