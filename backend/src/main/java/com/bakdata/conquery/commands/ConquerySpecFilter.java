package com.bakdata.conquery.commands;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.swagger.v3.core.filter.AbstractSpecFilter;
import io.swagger.v3.core.model.ApiDescription;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

public class ConquerySpecFilter extends AbstractSpecFilter {
	@Override
	public Optional<Parameter> filterParameter(Parameter parameter, Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
		return super.filterParameter(parameter, operation, api, params, cookies, headers);
	}

	@Override
	public Optional<Schema> filterSchema(Schema schema, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
		return super.filterSchema(schema, params, cookies, headers);
	}

	@Override
	public Optional<Schema> filterSchemaProperty(Schema property, Schema schema, String propName, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {

		if (property.get$ref() == null) {
			return super.filterSchemaProperty(property, schema, propName, params, cookies, headers);
		}

		if(property.get$ref().endsWith("JsonNode")){
			property.set$ref(null);
			property.setType("object");
			return Optional.of(property);
		}

		return super.filterSchemaProperty(property, schema, propName, params, cookies, headers);
	}
}
