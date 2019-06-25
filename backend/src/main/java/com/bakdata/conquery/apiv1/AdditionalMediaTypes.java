package com.bakdata.conquery.apiv1;

import javax.ws.rs.core.MediaType;

public interface AdditionalMediaTypes {

	static final String JSON = "application/json; charset=utf-8";
	static final MediaType JSON_TYPE = MediaType.valueOf(JSON);

	static final String CSV = "text/csv; charset=utf-8";
	static final MediaType CSV_TYPE = MediaType.valueOf(CSV);
}
