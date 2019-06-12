<#import "/com/bakdata/conquery/models/events/generation/Helper.ftl" as f/>
package com.bakdata.conquery.models.events.generation;

import java.io.InputStream;
import java.io.IOException;

import com.bakdata.conquery.models.datasets.Import;
import com.bakdata.conquery.models.events.generation.BlockFactory;
import com.bakdata.conquery.io.DeserHelper;
import java.math.BigDecimal;
import com.google.common.collect.Range;

import com.bakdata.conquery.util.io.SmallIn;
import com.bakdata.conquery.util.io.SmallOut;
import java.time.LocalDate;

import java.lang.Integer;
import com.bakdata.conquery.models.common.daterange.CDateRange;

import java.util.List;

import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.BitWriter;
import com.tomgibara.bits.Bits;
import com.bakdata.conquery.util.PackedUnsigned1616;
import com.bakdata.conquery.models.common.CQuarter;
import com.google.common.primitives.Ints;

import java.util.Arrays;
import it.unimi.dsi.fastutil.ints.IntList;
import com.bakdata.conquery.models.events.Bucket;

public class BlockFactory_${suffix} extends BlockFactory {

	@Override
	public Bucket_${suffix} create(Import imp, List<Object[]> events) {
		BitStore nullBits = Bits.store(${imp.nullWidth}*events.size());
		Bucket_${suffix} block = new Bucket_${suffix}(0, imp, new int[]{0});
		block.initFields(events.size());
		for(int event = 0; event < events.size(); event++){
			<#list imp.columns as col>
			<#import "/com/bakdata/conquery/models/events/generation/types/${col.type.class.simpleName}.ftl" as t/>
			//${col.name} : ${col.type.class.simpleName}
			<#if col.type.lines == col.type.nullLines>
			<#-- do nothing, column is null everywhere-->
			<#else>
			if(events.get(event)[${col_index}]==null){
			<#if col.type.canStoreNull()> //TODO implement this with t.nullValue?has_content, else throw exception to consolidate concerns of parsing into file.
				block.<@f.set col/>(event, <@t.nullValue type=col.type/>);	
			<#else>
				nullBits.setBit(${imp.nullWidth}*event+${col.nullPosition}, true);
			</#if>
			}
			else{
				${col.type.primitiveType.name} value;
				<#if t.unboxValue??>
				value = <@t.unboxValue col.type>events.get(event)[${col_index}]</@t.unboxValue>;
				<#else>
				value = (${col.type.primitiveType.name}) events.get(event)[${col_index}];
				</#if>
				block.<@f.set col/>(event, value);
			}
			</#if>
			</#list>
		}
		block.setNullBits(nullBits);
		return block;
	}
	
	@Override
	public Bucket_${suffix} construct(int bucketNumber, Import imp, int[] offsets) {
		return new Bucket_${suffix}(bucketNumber, imp, offsets);
	}
	
	@Override
	public Bucket_${suffix} combine(IntList includedEntities, Bucket[] buckets) {
		int[] offsets = new int[${bucketSize}];
		Arrays.fill(offsets, -1);
		int offset = 0;
		for(int i=0;i<includedEntities.size();i++) {
			offsets[includedEntities.get(i) - ${bucketSize}*buckets[0].getBucket()]=offset;
			offset+=buckets[i].getNumberOfEvents();
		}
		
		Bucket_${suffix} result = construct(
			buckets[0].getBucket(),
			buckets[0].getImp(),
			offsets
		);
		result.initFields(Arrays.stream(buckets).mapToInt(Bucket::getNumberOfEvents).sum());
		BitStore bits = Bits.store(${imp.nullWidth}*result.getNumberOfEvents());
		offset = 0;
		for(Bucket bucket : buckets) {
			Bucket_${suffix} cast = (Bucket_${suffix})bucket;
			bits.setStore(
				offset*${imp.nullWidth}, 
				bucket.getNullBits().rangeTo(bucket.getNumberOfEvents()*${imp.nullWidth})
			);
			for(int event =0;event<bucket.getNumberOfEvents();event++) {
				<#list imp.columns as column>
				<#if column.type.lines != column.type.nullLines>
				result.<@f.set column/>(offset, cast.<@f.get column/>(event));
				</#if>
				</#list>
				offset++;
			}
		}
		result.setNullBits(bits);
		return result;
	}
}