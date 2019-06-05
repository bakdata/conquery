package com.bakdata.conquery.models.query.queryplan.specific;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.bakdata.conquery.models.identifiable.ids.specific.TableId;
import com.bakdata.conquery.models.query.queryplan.QPNode;
import com.bakdata.conquery.models.query.queryplan.QPParentNode;
import com.bakdata.conquery.models.query.queryplan.clone.CloneContext;
import com.google.common.collect.ListMultimap;

public class AndNode extends QPParentNode {

	public AndNode(List<QPNode> children) {
		super(children);
	}

	private AndNode(List<QPNode> children, ListMultimap<TableId, QPNode> childMap) {
		super(children, childMap);
	}

	@Override
	public QPNode doClone(CloneContext ctx) {
		Pair<List<QPNode>, ListMultimap<TableId, QPNode>> fields = createClonedFields(ctx);
		return new AndNode(fields.getLeft(), fields.getRight());
	}

	@Override
	public boolean isContained() {
		boolean currently = true;
		for (QPNode agg : getChildren()) {
			currently &= agg.isContained();
		}
		return currently;
	}

	public static QPNode of(Collection<QPNode> children) {
		switch (children.size()) {
			case 0:
				return new Leaf();
			case 1:
				return children.iterator().next();
			default:
				return new AndNode(new ArrayList<>(children));
		}
	}
}
