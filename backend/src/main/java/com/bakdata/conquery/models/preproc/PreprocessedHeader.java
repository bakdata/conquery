package com.bakdata.conquery.models.preproc;

import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.events.MajorTypeId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Header containing data about a Preprocessed Csv file. Generated by running {@link com.bakdata.conquery.commands.PreprocessorCommand}.
 *
 * @implSpec The Columns and their order must directly match the layout in the data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class PreprocessedHeader {
	/**
	 * The name/tag of an import.
	 */
	private String name;

	/**
	 * The specific table id to be loaded into.
	 */
	private String table;

	/**
	 * Number of rows in the Preprocessed file.
	 */
	private long rows;

	/**
	 * The specific columns and their associated MajorType for validation.
	 */
	private PPColumn[] columns;

	/**
	 * A hash to check if any of the underlying files for generating this CQPP has changed.
	 */
	private int validityHash;


	/**
	 * Verify that the supplied table matches the preprocessed' data in shape.
	 */
	public void assertMatch(Table table) {
		StringJoiner errors = new StringJoiner("\n");

		if (table.getColumns().length != getColumns().length) {
			errors.add(String.format("Length=`%d` does not match table Length=`%d`", getColumns().length, table.getColumns().length));
		}

		final Map<String, MajorTypeId> typesByName = Arrays.stream(getColumns())
													   .collect(Collectors.toMap(PPColumn::getName, PPColumn::getType));

		for (int i = 0; i < Math.min(table.getColumns().length, getColumns().length); i++) {
			final Column column = table.getColumns()[i];

			if(!typesByName.containsKey(column.getName())){
				errors.add(String.format("Column[%s] is missing in Import.", column.getName()));
			}
			else if (!typesByName.get(column.getName()).equals(column.getType())) {
				errors.add(String.format("Column[%s] Types do not match %s != %s"
						, column.getName(),  typesByName.get(column.getName()), column.getType())
				);
			}
		}

		if (errors.length() != 0) {
			log.error(errors.toString());
			throw new IllegalArgumentException(String.format("Headers[%s.%s] do not match Table[%s]. More Info in Logs.", getTable(), getName(), table.getId()));
		}
	}


}
