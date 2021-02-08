package com.bakdata.conquery.models.query.queryplan.specific;

import java.util.Set;

import com.bakdata.conquery.models.concepts.ConceptElement;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.events.CBlock;
import com.bakdata.conquery.models.identifiable.ids.specific.SecondaryIdDescriptionId;
import com.bakdata.conquery.models.identifiable.ids.specific.TableId;
import com.bakdata.conquery.models.query.QueryExecutionContext;
import com.bakdata.conquery.models.query.concept.filter.CQTable;
import com.bakdata.conquery.models.query.entity.Entity;
import com.bakdata.conquery.models.query.queryplan.QPChainNode;
import com.bakdata.conquery.models.query.queryplan.QPNode;
import com.bakdata.conquery.models.query.queryplan.clone.CloneContext;
import lombok.Getter;

@Getter
public class ConceptNode extends QPChainNode {

	private final ConceptElement<?>[] concepts;
	private final long requiredBits;
	private final CQTable table;
	private final SecondaryIdDescriptionId selectedSecondaryId;
	private boolean tableActive = false;
	private CBlock currentRow = null;


	public ConceptNode(ConceptElement[] concepts, long requiredBits, CQTable table, QPNode child, SecondaryIdDescriptionId selectedSecondaryId) {
		super(child);
		this.concepts = concepts;
		this.requiredBits = requiredBits;
		this.table = table;

		this.selectedSecondaryId = selectedSecondaryId;
	}

	@Override
	public void init(Entity entity, QueryExecutionContext context) {
		super.init(entity, context);
	}

	@Override
	public void nextTable(QueryExecutionContext ctx, TableId currentTable) {
		tableActive = table.getResolvedConnector().getTable().getId().equals(currentTable)
					  && ctx.getActiveSecondaryId() == selectedSecondaryId;
		if(tableActive) {
			super.nextTable(ctx.withConnector(table.getResolvedConnector()), currentTable);
		}
	}

	@Override
	public void nextBlock(Bucket bucket) {
		if (tableActive) {
			currentRow = bucket.getCBlocks().get(table.getId());
			super.nextBlock(bucket);
		}
	}


	@Override
	public boolean isOfInterest(Entity entity) {
		return context.getBucketManager().hasEntityCBlocksForConnector(entity, table.getId());
	}

	@Override
	public boolean isOfInterest(Bucket bucket) {
		if (!tableActive) {
			return false;
		}

		CBlock cBlock = bucket.getCBlocks().get(table.getId());

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
	public boolean isContained() {
		return getChild().isContained();
	}

	@Override
	public QPNode doClone(CloneContext ctx) {
		return new ConceptNode(concepts, requiredBits, table, ctx.clone(getChild()), selectedSecondaryId);
	}

	@Override
	public void collectRequiredTables(Set<TableId> requiredTables) {
		super.collectRequiredTables(requiredTables);
		requiredTables.add(table.getResolvedConnector().getTable().getId());
	}
}
