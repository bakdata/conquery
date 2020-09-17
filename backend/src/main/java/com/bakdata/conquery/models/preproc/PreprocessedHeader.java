package com.bakdata.conquery.models.preproc;

import com.bakdata.conquery.models.common.daterange.CDateRange;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Header containing data about a Preprocessed Csv file. Generated by running {@link com.bakdata.conquery.commands.PreprocessorCommand}.
 *
 * @implSpec The Columns and their order must directly match the layout in the data.
 */
@Data @NoArgsConstructor @AllArgsConstructor @Slf4j
public class PreprocessedHeader {
	/**
	 * The name/tag of an import.
	 */
	private String name;

	/**
	 * The specific table id to be loaded into.
	 */
	private String table;

	// TODO: 14.07.2020 FK: Is this actually used? It doesn't seem so.
	private String suffix;

	/**
	 * Number of rows in the Preprocessed file.
	 */
	private long rows;

	/**
	 * Number of entities in the file.
	 */
	private long groups;

	/**
	 * Range spanning all data over all date based columns.
	 */
	private CDateRange eventRange;

	/**
	 * Name of the primary column.
	 *
	 * @implNote this is just used as a placeholder for dictionaries etc. about the Entities' primary columns.
	 */
	private PPColumn primaryColumn;

	/**
	 * The specific columns and their associated transformations+data.
	 */
	private PPColumn[] columns;

	/**
	 * A hash to check if any of the underlying files for generating this CQPP has changed.
	 */
	private int validityHash;

}
