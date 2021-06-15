package com.bakdata.conquery.models.query.queryplan.specific;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.bakdata.conquery.models.common.CDateSet;
import com.bakdata.conquery.models.common.daterange.CDateRange;
import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.events.CBlock;
import com.bakdata.conquery.models.query.QueryExecutionContext;
import com.bakdata.conquery.models.query.queryplan.QPChainNode;
import com.bakdata.conquery.models.query.queryplan.QPNode;
import com.bakdata.conquery.models.query.queryplan.clone.CloneContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DateRestrictingNode extends QPChainNode {

	protected final CDateSet restriction;
	protected Column validityDateColumn;
	protected Map<Bucket, CBlock> preCurrentRow = null;

	public DateRestrictingNode(CDateSet restriction, QPNode child) {
		super(child);
		this.restriction = restriction;
	}

	@Override
	public void nextTable(QueryExecutionContext ctx, Table currentTable) {
		//if there was no date restriction we can just use the restriction CDateSet
		if(ctx.getDateRestriction().isAll()) {
			ctx = ctx.withDateRestriction(CDateSet.create(restriction));
		}
		else {
			CDateSet dateRestriction = CDateSet.create(ctx.getDateRestriction());
			dateRestriction.retainAll(restriction);
			ctx = ctx.withDateRestriction(dateRestriction);
		}
		super.nextTable(ctx, currentTable);


		preCurrentRow = ctx.getBucketManager().getEntityCBlocksForConnector(getEntity(), context.getConnector());
		validityDateColumn = context.getValidityDateColumn();

		if (validityDateColumn != null && !validityDateColumn.getType().isDateCompatible()) {
			throw new IllegalStateException("The validityDateColumn " + validityDateColumn + " is not a DATE TYPE");
		}
	}

	@Override
	public boolean isOfInterest(Bucket bucket) {
		CBlock cBlock = Objects.requireNonNull(preCurrentRow.get(bucket));

		if (validityDateColumn == null) {
			// If there is no validity date set for a concept there is nothing to restrict
			return super.isOfInterest(bucket);
		}

		CDateRange range = cBlock.getEntityDateRange(entity.getId());

		return restriction.intersects(range) && super.isOfInterest(bucket);
	}

	@Override
	public Optional<Boolean> eventFiltersApply(Bucket bucket, int event) {
		if (validityDateColumn == null) {
			return Optional.empty();
		}
		if(!bucket.eventIsContainedIn(event, validityDateColumn, restriction)) {
			return Optional.of(Boolean.FALSE);
		}
		return getChild().eventFiltersApply(bucket, event);
	}

	@Override
	public void acceptEvent(Bucket bucket, int event) {
		// The duplicate logic of this and eventFiltersApply might be preventable
		// But an event that we rejected in eventFiltersApply can reappear here so we check again
		// before pushing down
		if (validityDateColumn == null) {
			getChild().acceptEvent(bucket, event);
			return;
		}
		if(!bucket.eventIsContainedIn(event, validityDateColumn, restriction)) {
			return;
		}
		getChild().acceptEvent(bucket, event);
	}

	@Override
	public QPNode doClone(CloneContext ctx) {
		return new DateRestrictingNode(restriction, ctx.clone(getChild()));
	}
}
