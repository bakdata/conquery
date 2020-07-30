package com.bakdata.conquery.models.query.queryplan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.identifiable.ids.specific.TableId;
import com.bakdata.conquery.models.query.QueryExecutionContext;
import com.bakdata.conquery.models.query.entity.Entity;
import com.bakdata.conquery.models.query.queryplan.clone.CloneContext;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

@Getter @Setter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class QPParentNode extends QPNode {

	private final List<QPNode> children;
	private final ListMultimap<TableId, QPNode> childMap;
	protected final List<QPNode> alwaysActiveChildren;

	protected List<QPNode> currentTableChildren;

	public QPParentNode(List<QPNode> children) {
		if(children == null || children.isEmpty()) {
			throw new IllegalArgumentException("A ParentAggregator needs at least one child.");
		}
		this.children = children;
		this.childMap = children
				.stream()
				.flatMap(
					c -> c.collectRequiredTables()
						.stream()
						.map(t -> Pair.of(t, c))
				)
				.collect(ImmutableListMultimap
					.toImmutableListMultimap(Pair::getLeft, Pair::getRight)
				);

		alwaysActiveChildren = children.stream()
									   .filter(c -> c.collectRequiredTables().isEmpty())
									   .collect(Collectors.toList());
	}

	@Override
	public void init(Entity entity) {
		super.init(entity);
		for (QPNode child : children) {
			child.init(entity);
		}
	}

	@Override
	public void collectRequiredTables(Set<TableId> requiredTables) {
		for (QPNode child : children) {
			child.collectRequiredTables(requiredTables);
		}
	}

	@Override
	public void nextTable(QueryExecutionContext ctx, TableId currentTable) {
		super.nextTable(ctx, currentTable);
		currentTableChildren = childMap.get(currentTable);


		currentTableChildren = childMap.get(currentTable);

		for (QPNode child : getCurrentTableChildren()) {
			child.nextTable(ctx, currentTable);
		}

		for (QPNode child : getAlwaysActiveChildren()) {
			child.nextTable(ctx, currentTable);
		}
	}

	@Override
	public void nextBlock(Bucket bucket) {
		for (QPNode child : getCurrentTableChildren()) {
			child.nextBlock(bucket);
		}

		for (QPNode child : getAlwaysActiveChildren()) {
			child.nextBlock(bucket);
		}
	}


	@Override
	public boolean isOfInterest(Bucket bucket) {
		for (QPNode child : getCurrentTableChildren()) {
			if (child.isOfInterest(bucket)) {
				return true;
			}
		}

		for (QPNode child : getAlwaysActiveChildren()) {
			if (child.isOfInterest(bucket)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isOfInterest(Entity entity) {
		for (QPNode child : children) {
			if (child.isOfInterest(entity)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void acceptEvent(Bucket bucket, int event) {
		for (QPNode currentTableChild : getCurrentTableChildren()) {
			currentTableChild.acceptEvent(bucket, event);
		}

		for (QPNode currentTableChild : getAlwaysActiveChildren()) {
			currentTableChild.acceptEvent(bucket, event);
		}
	}

	@Override
	public String toString() {
		return super.toString()+"[children = "+children+"]";
	}

	protected Pair<List<QPNode>, ListMultimap<TableId, QPNode>> createClonedFields(CloneContext ctx) {
		List<QPNode> clones = new ArrayList<>(getChildren());
		clones.replaceAll(ctx::clone);

		ArrayListMultimap<TableId, QPNode> cloneMap = ArrayListMultimap.create(childMap);

		for(Entry<TableId, Collection<QPNode>> e : cloneMap.asMap().entrySet()) {
			((List<QPNode>)e.getValue()).replaceAll(ctx::clone);
		}
		return Pair.of(clones, cloneMap);
	}
}
