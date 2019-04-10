package com.bakdata.conquery.integration.common;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.types.MajorTypeId;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequiredColumn {

	@NotEmpty
	private String name;
	@NotNull
	private MajorTypeId type;

	public Column toColumn(Table t) {
		Column col = new Column();
		col.setName(name);
		col.setType(type);
		col.setTable(t);
		return col;
	}
}
