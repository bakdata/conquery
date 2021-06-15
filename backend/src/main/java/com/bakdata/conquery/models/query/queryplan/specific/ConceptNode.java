package com.bakdata.conquery.models.query.queryplan.specific;

import java.util.*;

import com.bakdata.conquery.models.concepts.ConceptElement;
import com.bakdata.conquery.models.datasets.SecondaryIdDescription;
import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.events.CBlock;
import com.bakdata.conquery.models.query.QueryExecutionContext;
import com.bakdata.conquery.models.query.concept.filter.CQTable;
import com.bakdata.conquery.models.query.entity.Entity;
import com.bakdata.conquery.models.query.queryplan.QPChainNode;
import com.bakdata.conquery.models.query.queryplan.QPNode;
import com.bakdata.conquery.models.query.queryplan.clone.CloneContext;
import lombok.Getter;

@Getter
public class ConceptNode extends QPChainNode {

	private final List<ConceptElement<?>> concepts;
	private final long requiredBits;
	private final CQTable table;
	private final SecondaryIdDescription selectedSecondaryId;
	private boolean tableActive = false;
	private Map<Bucket, CBlock> preCurrentRow = null;
	private CBlock currentRow = null;


	public ConceptNode(List<ConceptElement<?>> concepts, long requiredBits, CQTable table, QPNode child, SecondaryIdDescription selectedSecondaryId) {
		super(child);
		this.concepts = concepts;
		this.requiredBits = requiredBits;
		this.table = table;

		this.selectedSecondaryId = selectedSecondaryId;
	}

	@Override
	public void init(Entity entity, QueryExecutionContext context) {
		super.init(entity, context);
		preCurrentRow = context.getBucketManager().getEntityCBlocksForConnector(getEntity(),table.getConnector());
	}

	@Override
	public void nextTable(QueryExecutionContext ctx, Table currentTable) {
		tableActive = table.getConnector().getTable().equals(currentTable)
					  && ctx.getActiveSecondaryId() == selectedSecondaryId;
		if(tableActive) {
			super.nextTable(ctx.withConnector(table.getConnector()), currentTable);
		}
	}

	@Override
	public void nextBlock(Bucket bucket) {
		if (tableActive) {
			currentRow = Objects.requireNonNull(preCurrentRow.get(bucket));
			super.nextBlock(bucket);
		}
	}


	@Override
	public boolean isOfInterest(Entity entity) {
		return context.getBucketManager().hasEntityCBlocksForConnector(entity, table.getConnector());
	}

	@Override
	public boolean isOfInterest(Bucket bucket) {
		if (!tableActive) {
			return false;
		}

		CBlock cBlock = Objects.requireNonNull(preCurrentRow.get(bucket));

		if(cBlock.isConceptIncluded(entity.getId(), requiredBits)) {
			return super.isOfInterest(bucket);
		}
		return false;
	}

	@Override
	public void acceptEvent(Bucket bucket, int event) {
		if (!tableActive) {
			return;
		}

		//check concepts
		int[] mostSpecificChildren = currentRow.getEventMostSpecificChild(event);
		if (mostSpecificChildren == null) {
			for (ConceptElement ce : concepts) {
				// having no specific child set maps directly to root.
				// This means we likely have a VirtualConcept
				if (ce.getConcept() == ce) {
					getChild().acceptEvent(bucket, event);
				}
			}
			return;
		}

		for (ConceptElement<?> ce : concepts) {
			//see #177  we could improve this by building a a prefix tree over concepts.prefix
			if (ce.matchesPrefix(mostSpecificChildren)) {
				getChild().acceptEvent(bucket, event);
			}
		}
	}

	@Override
	public Optional<Boolean> aggregationFiltersApply() {
		return getChild().aggregationFiltersApply();
	}

	@Override
	public QPNode doClone(CloneContext ctx) {
		return new ConceptNode(concepts, requiredBits, table, ctx.clone(getChild()), selectedSecondaryId);
	}

	@Override
	public void collectRequiredTables(Set<Table> requiredTables) {
		super.collectRequiredTables(requiredTables);
		requiredTables.add(table.getConnector().getTable());
	}
}
