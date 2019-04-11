package com.bakdata.conquery.models.api.description;

import java.util.List;

import com.bakdata.conquery.models.identifiable.ids.specific.ConnectorId;
import com.bakdata.conquery.models.identifiable.ids.specific.TableId;

import lombok.Builder;
import lombok.Data;

/**
 * This class represents a concept filter parameter as it is presented to the front end.
 */
@Data @Builder
public class FETable {
	private TableId id;
	private ConnectorId connectorId;
	private String label;
	private List<FEFilter> filters;
	private List<FESelect> selects;
}
