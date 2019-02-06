package com.bakdata.conquery.models.query.queryplan.aggregators.specific;

import com.bakdata.conquery.models.common.CDate;
import com.bakdata.conquery.models.common.QuarterUtils;
import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.events.Block;
import com.bakdata.conquery.models.identifiable.ids.specific.SelectId;
import com.bakdata.conquery.models.query.queryplan.aggregators.SingleColumnAggregator;
import com.esotericsoftware.kryo.util.IntMap;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.IsoFields;
import java.util.EnumSet;

/**
 * Entity is included when the the number of quarters with events is within a
 * specified range.
 */
public class QuartersInYearAggregator extends SingleColumnAggregator<Long> {

	private final IntMap<EnumSet<Month>> quartersInYear = new IntMap<>();

	public QuartersInYearAggregator(SelectId id, Column column) {
		super(id, column);
	}

	@Override
	public void aggregateEvent(Block block, int event) {
		if (!block.has(event, getColumn())) {
			return;
		}

		LocalDate date = CDate.toLocalDate(block.getDate(event, getColumn()));

		EnumSet<Month> months = quartersInYear.get(date.getYear());
		int quarter = date.get(IsoFields.QUARTER_OF_YEAR);

		if (months == null) {
			months = EnumSet.of(QuarterUtils.getFirstMonthOfQuarter(quarter));
			quartersInYear.put(date.getYear(), months);
		}
		else {
			months.add(QuarterUtils.getFirstMonthOfQuarter(quarter));
		}
	}

	@Override
	public Long getAggregationResult() {
		long max = 0;

		for (EnumSet<Month> months : quartersInYear.values()) {
			long cardinality = months.size();
			if (cardinality > max) {
				max = cardinality;
			}

		}

		return max;
	}

	@Override
	public QuartersInYearAggregator clone() {
		return new QuartersInYearAggregator(getId(), getColumn());
	}
}
