package com.bakdata.conquery.models.api.description;

import java.util.List;

import com.bakdata.conquery.apiv1.FilterTemplate;
import com.bakdata.conquery.models.identifiable.ids.specific.FilterId;

import lombok.Builder;
import lombok.Data;

/**
 * This class represents a concept filter as it is presented to the front end.
 */
@Data
@Builder
public class FEFilter {

	private FilterId id;
	private String label;
	private FEFilterType type;
	private String unit;
	private String description;
	private List<FEValue> options;
	private Integer min;
	private Integer max;
	private FilterTemplate template;
	private String pattern;
	private Boolean allowDropFile;
}
