package com.bakdata.conquery.models.types.specific;

import java.util.Iterator;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.types.CType;
import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@CPSType(base = CType.class, id = "STRING_SUFFIX") @ToString
public class StringTypeSuffix<SUB extends CType<Integer,Number>&IStringType> extends AChainedStringType<SUB> {

	@NonNull
	private String suffix;
	
	@JsonCreator
	public StringTypeSuffix(SUB subType, String suffix) {
		super(subType);
		this.suffix = suffix;
	}
	
	@Override
	public String getElement(int value) {
		return subType.getElement(value)+suffix;
	}
	
	@Override
	public String createScriptValue(Number value) {
		return subType.createScriptValue(value)+suffix;
	}

	@Override
	public int getId(String value) {
		if(value.endsWith(suffix)) {
			return subType.getId(value.substring(0, value.length()-suffix.length()));
		}
		else {
			return -1;
		}
	}

	@Override
	public Iterator<String> iterator() {
		Iterator<String> subIt = subType.iterator();
		return new Iterator<String>() {
			@Override
			public boolean hasNext() {
				return subIt.hasNext();
			}

			@Override
			public String next() {
				return subIt.next()+suffix;
			}
		};
	}
}
