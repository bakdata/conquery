package com.bakdata.conquery.io.jackson;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

import com.bakdata.conquery.models.identifiable.CentralRegistry;
import com.bakdata.conquery.models.identifiable.Identifiable;
import com.bakdata.conquery.models.identifiable.ids.IId;
import com.bakdata.conquery.models.identifiable.ids.NamespacedId;
import com.bakdata.conquery.models.worker.DatasetRegistry;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IdRefPathParamConverterProvider implements ParamConverterProvider {

	private final DatasetRegistry datasetRegistry;
	private final CentralRegistry metaRegistry;

	@Override
	public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
		if (!(Identifiable.class.isAssignableFrom(rawType))) {
			return null;
		}

		final Class<IId<T>> idClass = IId.findIdClass(rawType);

		if(idClass == null){
			return null;
		}

		final IId.Parser<IId<T>> parser = IId.createParser(idClass);


		if (NamespacedId.class.isAssignableFrom(idClass)) {
			return new NamespacedIdRefParamConverter(parser, datasetRegistry);
		}

		return new MetaIdRefParamConverter(parser, metaRegistry);
	}
}
