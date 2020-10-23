package com.bakdata.conquery.models.types;

import java.io.IOException;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.util.function.Consumer;

import com.bakdata.conquery.io.xodus.NamespacedStorage;
import com.bakdata.conquery.models.dictionary.Dictionary;
import com.bakdata.conquery.models.events.stores.ColumnStoreAdapter;
import com.bakdata.conquery.models.identifiable.ids.specific.DatasetId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.google.common.math.LongMath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter @Setter @RequiredArgsConstructor
public abstract class CType<MAJOR_JAVA_TYPE, JAVA_TYPE> extends ColumnStoreAdapter<JAVA_TYPE> implements MajorTypeIdHolder {

	@JsonIgnore
	private transient final MajorTypeId typeId;

	private int lines = 0;
	private int nullLines = 0;

	public void init(DatasetId dataset) {}

	public Object createScriptValue(JAVA_TYPE value) {
		return value;
	}

	public Object createPrintValue(JAVA_TYPE value) { return value != null ? createScriptValue(value) : ""; }

	public void writeHeader(OutputStream out) throws IOException {}
	public void readHeader(JsonParser input) throws IOException {}
	public void storeExternalInfos(Consumer<Dictionary> dictionaryConsumer) {}
	public void loadExternalInfos(NamespacedStorage storage) {}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}


	public long estimateMemoryConsumption() {
		long width = estimateMemoryBitWidth();

		return LongMath.divide(
			(lines-nullLines) * width + nullLines * Math.min(Long.SIZE, width),
			8, RoundingMode.CEILING
		); 
	}

	public abstract long estimateMemoryBitWidth();
	
	public long estimateTypeSize() {
		return 0;
	}

	public abstract CType<MAJOR_JAVA_TYPE, JAVA_TYPE> select(int[] starts, int[] lengths);

}
