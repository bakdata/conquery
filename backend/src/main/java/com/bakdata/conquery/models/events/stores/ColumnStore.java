package com.bakdata.conquery.models.events.stores;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.Function;

import javax.annotation.CheckForNull;

import com.bakdata.conquery.io.cps.CPSBase;
import com.bakdata.conquery.models.common.daterange.CDateRange;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "type")
@CPSBase
@ToString
public abstract class ColumnStore<JAVA_TYPE> {

	private int lines = 0;
	private int nullLines = 0;

	/**
	 * Helper method to select partitions of an array. Resulting array is of length sum(lengths). Incoming type T has to be of ArrayType or this will fail.
	 */
	public static <T> T selectArray(int[] starts, int[] lengths, T values, Function<Integer, T> provider) {
		int length = Arrays.stream(lengths).sum();

		final T out = provider.apply(length);

		int pos = 0;

		for (int index = 0; index < starts.length; index++) {
			System.arraycopy(values, starts[index], out, pos, lengths[index]);
			pos += lengths[index];
		}

		return out;
	}

	public Object createPrintValue(JAVA_TYPE value) {
		return value != null ? createScriptValue(value) : "";
	}

	public Object createScriptValue(JAVA_TYPE value) {
		return value;
	}

	public long estimateMemoryConsumption() {
		long bytes = estimateEventBits();

		return getLines() * bytes;
	}

	public abstract long estimateEventBits();

	public long estimateTypeSize() {
		return 0;
	}

	/**
	 * Select the partition of this store.
	 * The returning store has to accept queries up to {@code sum(lenghts)}, values may not be reordered.
	 */
	public abstract ColumnStore<JAVA_TYPE> select(int[] starts, int[] lengths);

	/**
	 * Create an empty store that's only a description of the transformation.
	 */
	public ColumnStore<JAVA_TYPE> createDescription() {
		final ColumnStore<JAVA_TYPE> select = select(new int[0], new int[0]);
		select.setLines(getLines());
		select.setNullLines(getNullLines());
		return select;
	}

	/**
	 * Set the event. If null, the store will store a null value, making {@link #has(int)} return false.
	 */
	public abstract void set(int event, @CheckForNull JAVA_TYPE value);

	/**
	 * Generic getter for storage. May not be called when {@link #has(int)} is false.
	 */
	public abstract JAVA_TYPE get(int event);

	/**
	 * Test if the store has the event.
	 */
	public abstract boolean has(int event);


	public int getString(int event) {
		throw new IllegalStateException("%s does not implement this method");
	}

	public long getInteger(int event) {
		throw new IllegalStateException("%s does not implement this method");
	}

	public boolean getBoolean(int event) {
		throw new IllegalStateException("%s does not implement this method");
	}

	public double getReal(int event) {
		throw new IllegalStateException("%s does not implement this method");
	}

	public BigDecimal getDecimal(int event) {
		throw new IllegalStateException("%s does not implement this method");
	}

	public long getMoney(int event) {
		throw new IllegalStateException("%s does not implement this method");
	}

	public int getDate(int event) {
		throw new IllegalStateException("%s does not implement this method");
	}

	public CDateRange getDateRange(int event) {
		throw new IllegalStateException("%s does not implement this method");
	}

}