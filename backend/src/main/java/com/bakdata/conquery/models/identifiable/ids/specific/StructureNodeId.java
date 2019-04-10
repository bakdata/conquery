package com.bakdata.conquery.models.identifiable.ids.specific;

import java.util.List;

import com.bakdata.conquery.models.concepts.StructureNode;
import com.bakdata.conquery.models.identifiable.ids.AId;
import com.bakdata.conquery.models.identifiable.ids.IId;
import com.bakdata.conquery.models.identifiable.ids.NamespacedId;
import com.google.common.collect.PeekingIterator;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StructureNodeId extends AId<StructureNode> implements NamespacedId {

	private final DatasetId dataset;
	private final StructureNodeId parent;
	private final String structureNode;

	@Override
	public void collectComponents(List<Object> components) {
		if (parent != null) {
			parent.collectComponents(components);
		}
		else {
			dataset.collectComponents(components);
		}
		components.add(structureNode);
	}

	public static enum Parser implements IId.Parser<StructureNodeId> {
		INSTANCE;

		@Override
		public StructureNodeId parse(PeekingIterator<String> parts) {
			DatasetId dataset = DatasetId.Parser.INSTANCE.parse(parts);
			StructureNodeId result = new StructureNodeId(dataset, null, parts.next());
			while (parts.hasNext()) {
				result = new StructureNodeId(dataset, result, parts.next());
			}
			return result;
		}
	}
}
