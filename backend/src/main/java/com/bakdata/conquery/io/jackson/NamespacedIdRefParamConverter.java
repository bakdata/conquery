package com.bakdata.conquery.io.jackson;

import javax.ws.rs.ext.ParamConverter;

import com.bakdata.conquery.models.identifiable.Identifiable;
import com.bakdata.conquery.models.identifiable.ids.IId;
import com.bakdata.conquery.models.identifiable.ids.NamespacedId;
import com.bakdata.conquery.models.worker.DatasetRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NamespacedIdRefParamConverter<ID extends IId<VALUE> & NamespacedId, VALUE extends Identifiable<ID>> implements ParamConverter<VALUE> {

	private final IId.Parser<ID> idParser;
	@NonNull
	private final DatasetRegistry registry;

	@Override
	public VALUE fromString(String value) {
		final ID id = idParser.parse(value);

		return registry.resolve(id);
	}

	@Override
	public String toString(VALUE value) {
		return value.getId().toString();
	}
}
