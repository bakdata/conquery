package com.bakdata.conquery.apiv1;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FilterSearchItem implements Comparable<FilterSearchItem>, Serializable {

	private static final long serialVersionUID = 1L;

	private String id = UUID.randomUUID().toString().substring(0, 8);
	private String label;
	private String value;
	private String optionValue;
	private Map<String, String> templateValues = new LinkedHashMap<>();

	@Override
	public int compareTo(FilterSearchItem o) {
		return id.compareTo(o.getId());
	}

}
