package com.bakdata.conquery.models.events;

import java.util.Arrays;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.bakdata.conquery.io.jackson.serializer.CBlockDeserializer;
import com.bakdata.conquery.models.concepts.Concept;
import com.bakdata.conquery.models.concepts.tree.ConceptTreeChild;
import com.bakdata.conquery.models.concepts.tree.ConceptTreeNode;
import com.bakdata.conquery.models.concepts.tree.TreeConcept;
import com.bakdata.conquery.models.identifiable.IdentifiableImpl;
import com.bakdata.conquery.models.identifiable.ids.specific.BucketId;
import com.bakdata.conquery.models.identifiable.ids.specific.CBlockId;
import com.bakdata.conquery.models.identifiable.ids.specific.ConnectorId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Metadata for connection of {@link Bucket} and {@link Concept}
 *
 * Pre-computed assignment of {@link TreeConcept}.
 */
@Getter @Setter @NoArgsConstructor
@JsonDeserialize(using = CBlockDeserializer.class)
public class CBlock extends IdentifiableImpl<CBlockId> {
	
	@Valid
	private BucketId bucket;
	@NotNull @Valid
	private ConnectorId connector;
	
	/**
	 * Bloom filter per entity for the first 64 {@link ConceptTreeChild}.
	 *
	 * Per Entity.
	 */
	private long[] includedConcepts;

	// TODO: 02.09.2020 FK: Chop this onto a per-column basis, making for a fine grained index.
	/**
	 * Statistic for fast lookup if entity is of interest.
	 * Int array for memory performance.
	 *
	 * Per Entity.
	 */
	private int[] minDate, maxDate;
	
	/**
	 * Represents the path in a {@link TreeConcept} to optimize lookup.
	 * Nodes in the tree are simply enumerated.
	 *
	 * Per Event.
	 */
	@Nullable
	private int[][] mostSpecificChildren = null;
	
	public CBlock(BucketId bucket, ConnectorId connector, int bucketSize) {
		this.bucket = bucket;
		this.connector = connector;
		includedConcepts = new long[bucketSize];
		minDate = new int[bucketSize];
		maxDate = new int[bucketSize];

		Arrays.fill(minDate, Integer.MAX_VALUE);
		Arrays.fill(maxDate, Integer.MIN_VALUE);
	}
	
	@Override @JsonIgnore
	public CBlockId createId() {
		return new CBlockId(bucket, connector);
	}

	public void addEntityIncludedConcept(int localEntity, ConceptTreeNode<?> node) {
		getIncludedConcepts()[localEntity] |= node.calculateBitMask();
	}
}
