package com.bakdata.conquery.io.csv;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;

import com.bakdata.conquery.models.config.ConqueryConfig;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvWriter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for reading and writing CSVs with the global settings from {@link ConqueryConfig}.
 */
@UtilityClass @Slf4j
public class CsvIo {

	/**
	 * Creates a new CSV parser using the global settings from {@link ConqueryConfig}.
	 * @return The newly created parser.
	 */
	public static CsvParser createParser() {
		return new CsvParser(ConqueryConfig.getInstance().getCsv().createCsvParserSettings());
	}
	
	/**
	 * Creates a new CSV writer using the global settings from {@link ConqueryConfig}.
	 * @return The newly created writer.
	 */
	public static CsvWriter createWriter() {
		return new CsvWriter(ConqueryConfig.getInstance().getCsv().createCsvWriterSettings());
	}

	/**
	 * Creates a new CSV writer using the global settings from {@link ConqueryConfig} and an existing writer object to write through.
	 * @param writer The writer to write through.
	 * @return The newly created writer.
	 */
	public static CsvWriter createWriter(@NonNull Writer writer) {
		return new CsvWriter(writer, ConqueryConfig.getInstance().getCsv().createCsvWriterSettings());
	}

	/**
	 * Checks if the provided file is gzipped.
	 * @param file The file to check.
	 * @return True if it was gzipped.
	 * @throws IOException if an I/O error occurs.
	 */
	public static boolean isGZipped(@NonNull File file) throws IOException {
		String contentType = Files.probeContentType(file.toPath());
		log.trace("File[{}] Content-Type = {}", file, contentType);
		return contentType != null && contentType.contains("gzip");
	}
}
