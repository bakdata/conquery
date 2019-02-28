package com.bakdata.conquery.models.concepts.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bakdata.conquery.models.concepts.conditions.CTCondition;
import com.bakdata.conquery.models.concepts.conditions.OrCondition;
import com.bakdata.conquery.models.concepts.conditions.PrefixCondition;
import com.bakdata.conquery.models.concepts.conditions.PrefixRangeCondition;
import com.bakdata.conquery.util.CalculatedValue;
import com.bakdata.conquery.util.dict.BytesTTMap;
import com.bakdata.conquery.util.dict.ValueNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TreeChildPrefixIndex {

	@JsonIgnore
	private BytesTTMap valueToChildIndex = new BytesTTMap();
	@JsonIgnore
	private ConceptTreeChild[] treeChildren;

	public ConceptTreeChild findMostSpecificChild(String stringValue, CalculatedValue<Map<String, Object>> rowMap) {
		ValueNode nearestNode = valueToChildIndex.getNearestNode(stringValue.getBytes());

		if(nearestNode != null) {
			return treeChildren[nearestNode.getValue()];
		}

		return null;
	}

	/***
	 * Test if the condition maintains a prefix structure, this is necessary for creating an index.
	 * @param cond
	 * @return
	 */
	private static boolean isPrefixMaintainigCondition(CTCondition cond) {
		return cond instanceof PrefixCondition
				|| cond instanceof PrefixRangeCondition
				|| (cond instanceof OrCondition
					&& ((OrCondition) cond).getConditions().stream().allMatch(TreeChildPrefixIndex::isPrefixMaintainigCondition))
				;
	}

	public static void putIndexInto(ConceptTreeNode root) {
		if(root.getChildIndex() != null) {
			return;
		}
		synchronized (root) {
			if(root.getChildIndex() != null) {
				return;
			}
			
			if(root.getChildren().isEmpty()) {
				return;
			}

			TreeChildPrefixIndex index = new TreeChildPrefixIndex();

			List<ConceptTreeChild> treeChildrenOrig = new ArrayList<>();
			treeChildrenOrig.addAll(root.getChildren());

			// collect all prefix children that are itself children of prefix nodes
			List<ConceptTreeChild> gatheredPrefixChildren = new ArrayList<>();

			for (int i = 0; i < treeChildrenOrig.size(); i++) {
				ConceptTreeChild child = treeChildrenOrig.get(i);
				CTCondition condition = child.getCondition();

				if(isPrefixMaintainigCondition(condition)) {
					treeChildrenOrig.addAll(child.getChildren());

					if (condition instanceof PrefixCondition) {
						gatheredPrefixChildren.add(child);
					}
				}
				else {
					putIndexInto(child);
				}
			}

			// Insert children into index and build resolving list
			List<ConceptTreeChild> gatheredChildren = new ArrayList<>();
			Multimap<String, ConceptTreeChild> prefixes = new HashMultimap<>();

			for (ConceptTreeChild child : gatheredPrefixChildren) {
				CTCondition condition = child.getCondition();

				for (String prefix : ((PrefixCondition) condition).getPrefixes()) {

					if(!prefixes.put(prefix, child))
						continue;

					if(index.valueToChildIndex.put(prefix.getBytes(), gatheredChildren.size()) != -1)
						log.error("Duplicate Prefix '{}' in '{}' of '{}'", prefix, condition, root);

					gatheredChildren.add(child);
				}
			}

			for (String key : prefixes.keys()) {
				if(prefixes.get(key).size() > 1)
					log.warn("Multiple ConceptChildren for '{}': {}", key, prefixes.get(key));
			}


			index.valueToChildIndex.balance();

			index.treeChildren = gatheredChildren.toArray(new ConceptTreeChild[0]);

			log.trace("Index below {} contains {} nodes", root.getId(), index.treeChildren.length);

			if(index.treeChildren.length == 0) {
				return;
			}

			// add lookup only if it contains any elements
			root.setChildIndex(index);
		}
	}
}
