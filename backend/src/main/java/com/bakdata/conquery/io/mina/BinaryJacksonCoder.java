package com.bakdata.conquery.io.mina;

import java.io.File;
import java.util.UUID;

import javax.validation.Validator;

import com.bakdata.conquery.io.jackson.InternalOnly;
import com.bakdata.conquery.io.jackson.Jackson;
import com.bakdata.conquery.models.exceptions.ValidatorHelper;
import com.bakdata.conquery.models.messages.network.NetworkMessage;
import com.bakdata.conquery.models.worker.NamespaceCollection;
import com.bakdata.conquery.util.io.EndCheckableInputStream;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BinaryJacksonCoder implements CQCoder<NetworkMessage<?>> {

	private final Validator validator;
	private final ObjectWriter writer;
	private final ObjectReader reader;

	public BinaryJacksonCoder(NamespaceCollection namespaces, Validator validator) {
		this.validator = validator;
		this.writer = Jackson.BINARY_MAPPER
			.writerFor(NetworkMessage.class)
			.withView(InternalOnly.class);
		this.reader = namespaces
				.injectInto(Jackson.BINARY_MAPPER.readerFor(NetworkMessage.class))
				.without(Feature.AUTO_CLOSE_SOURCE)
				.withView(InternalOnly.class);
	}

	@Override
	public Chunkable encode(NetworkMessage<?> message) throws Exception {
		ValidatorHelper.failOnError(log, validator.validate(message), "encoding " + message.getClass().getSimpleName());

		UUID id = message.getMessageId();
		Chunkable chunkable = new Chunkable(id, writer, message);
		if(log.isTraceEnabled()) {
			Jackson.MAPPER.writerFor(NetworkMessage.class).with(SerializationFeature.INDENT_OUTPUT).writeValue(new File("dumps/out_"+id+".json"), message);
		}
		return chunkable;
	}

	@Override
	public NetworkMessage<?> decode(ChunkedMessage message) throws Exception {
		try(EndCheckableInputStream is = message.createInputStream()) {
			Object obj = reader.readValue(is);
			if(!is.isAtEnd()) {
				throw new IllegalStateException("After reading the JSON message "+obj+" the buffer has still bytes available");
			}
			ValidatorHelper.failOnError(log, validator.validate(obj), "decoding " + obj.getClass().getSimpleName());
			return (NetworkMessage<?>)obj;
		}
	}
}
