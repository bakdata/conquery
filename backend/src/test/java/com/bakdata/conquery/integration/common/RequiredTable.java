package com.bakdata.conquery.integration.common;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.bakdata.conquery.integration.IntegrationTest;
import com.bakdata.conquery.io.jackson.Jackson;
import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.datasets.Table;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequiredTable {

	@NotNull
	@NotEmpty
	private String name;
	@NotNull
	private ResourceFile csv;
	@NotNull
	@Valid
	private RequiredColumn primaryColumn;
	@NotEmpty
	@Valid
	private RequiredColumn[] columns;

	public Table toTable() {
		Table table = new Table();
		table.setName(name);
		table.setPrimaryColumn(primaryColumn.toColumn(table));
		table.setColumns(Arrays.stream(columns).map(col -> col.toColumn(table)).toArray(Column[]::new));
		return table;
	}
	
	@JsonCreator
	public static RequiredTable fromFile(String fileResource) throws JsonParseException, JsonMappingException, IOException {
		return Jackson.MAPPER.readValue(
			Objects.requireNonNull(
				IntegrationTest.class.getResourceAsStream(fileResource),
				fileResource+" not found"
			),
			RequiredTable.class
		);
	}
}
