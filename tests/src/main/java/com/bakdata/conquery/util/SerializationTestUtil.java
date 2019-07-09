package com.bakdata.conquery.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.validation.Validator;

import org.assertj.core.api.ObjectAssert;

import com.bakdata.conquery.io.jackson.InternalOnly;
import com.bakdata.conquery.io.jackson.Jackson;
import com.bakdata.conquery.models.exceptions.JSONException;
import com.bakdata.conquery.models.exceptions.ValidatorHelper;
import com.bakdata.conquery.models.identifiable.CentralRegistry;
import com.bakdata.conquery.models.identifiable.IdentifiableImpl;
import com.bakdata.conquery.models.worker.SingletonNamespaceCollection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.dropwizard.jersey.validation.Validators;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Accessors(chain = true, fluent = true)
@Slf4j
public class SerializationTestUtil<T> {
	
	private final JavaType type;
	private final Validator validator = Validators.newValidator();
	@Setter
	private List<Class<?>> ignoreClasses = Collections.emptyList();
	@Setter
	private CentralRegistry registry;
	
	public static <T> SerializationTestUtil<T> forType(TypeReference<T> type) {
		return new SerializationTestUtil<>(Jackson.MAPPER.getTypeFactory().constructType(type));
	}
	
	public static <T> SerializationTestUtil<T> forType(Class<? extends T> type) {
		return new SerializationTestUtil<>(Jackson.MAPPER.getTypeFactory().constructType(type));
	}
	
	public void test(T value) throws JSONException, IOException {
		test(
			value,
			Jackson.MAPPER
		);
		test(
			value,
			Jackson.BINARY_MAPPER
		);
	}
	
	private void test(T value, ObjectMapper mapper) throws JSONException, IOException {
		if(registry != null) {
			mapper = new SingletonNamespaceCollection(registry).injectInto(mapper);
		}
		ObjectWriter writer = mapper.writerFor(type).withView(InternalOnly.class);
		ObjectReader reader = mapper.readerFor(type).withView(InternalOnly.class);
		
		
		ValidatorHelper.failOnError(log, validator.validate(value));
		byte[] src = writer.writeValueAsBytes(value);
		T copy = reader.readValue(src);
		ValidatorHelper.failOnError(log, validator.validate(copy));
		
		//because IdentifiableImp cashes the hash
		if(value instanceof IdentifiableImpl) {
			assertThat(copy.hashCode()).isEqualTo(value.hashCode());
		}

		ObjectAssert<T> ass = assertThat(copy)
			.as("Unequal after copy.");
		for(Class<?> ig:ignoreClasses)
			ass.usingComparatorForType((a,b)->0, ig);
		
		ass.isEqualToComparingFieldByFieldRecursively(value);
	}
}
