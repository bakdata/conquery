package com.bakdata.conquery.models.query.concept.specific.temporal;

import com.bakdata.conquery.models.query.QueryResolveContext;
import com.bakdata.conquery.models.query.concept.CQElement;
import com.bakdata.conquery.models.query.resultinfo.ResultInfoCollector;
import com.bakdata.conquery.models.query.visitor.QueryVisitor;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Abstract data class specifying the data needed for a TemporalQuery.
 */
@Getter
@AllArgsConstructor
public abstract class CQAbstractTemporalQuery implements CQElement {

	/**
	 * The query being executed as reference for preceding.
	 */
	protected final CQSampled index;

	/**
	 * The query being executed, compared to index. Events in preceding will be cut-off to be always before index, or at the same day, depending on the queries specific implementations.
	 */
	protected final CQSampled preceding;

	@Override
	public abstract CQAbstractTemporalQuery resolve(QueryResolveContext context);
	
	@Override
	public void visit(QueryVisitor visitor) {
		index.getChild().visit(visitor);
		preceding.getChild().visit(visitor);
	}
	
	@Override
	public void collectResultInfos(ResultInfoCollector collector) {}
}
