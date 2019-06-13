package com.bakdata.eva.models.preproc.output.daysinrange;


import com.bakdata.conquery.models.common.daterange.CDateRange;
import com.bakdata.conquery.models.preproc.PPColumn;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class PatientEvent {

	private final int primaryId;

	private final Identifier identifier;
	private final String type;

	private final PPColumn[] columns;

	private final CDateRange range;
}
