package com.bakdata.conquery.models.identifiable.mapping;

import lombok.Data;

@Data
public class ExternalIdPart {
	private final IdMappingAccessor idMappingAccessor;
	private final String[] externalIdPart;
}
