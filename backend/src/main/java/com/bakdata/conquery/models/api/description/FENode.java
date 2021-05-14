package com.bakdata.conquery.models.api.description;

import com.bakdata.conquery.models.common.KeyValue;
import com.bakdata.conquery.models.common.Range;
import com.bakdata.conquery.models.identifiable.ids.Id;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * This class represents a concept as it is presented to the front end.
 */
@Data @Builder
public class FENode {
	private Id<?> parent;
	private String label;
	private String description;
	private Boolean active;
	private Id<?>[] children;
	private List<KeyValue> additionalInfos;
	private long matchingEntries;
	private Range<LocalDate> dateRange;
	private List<FETable> tables;
	private Boolean detailsAvailable;
	private boolean codeListResolvable;
	private List<FESelect> selects;
}
