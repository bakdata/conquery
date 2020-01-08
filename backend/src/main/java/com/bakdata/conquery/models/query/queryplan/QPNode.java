package com.bakdata.conquery.models.query.queryplan;

import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.query.QueryExecutionContext;
import com.bakdata.conquery.models.query.entity.Entity;
import com.bakdata.conquery.models.query.queryplan.clone.CtxCloneable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
public abstract class QPNode implements EventIterating, CtxCloneable<QPNode> {
	protected QueryExecutionContext context;
	protected Entity entity;

	public void init(Entity entity) {
		this.entity = entity;
		init();
	}

	protected void init() {
	}

	@Override
	public void nextTable(QueryExecutionContext ctx, Table currentTable) {
		setContext(ctx);
	}

	public abstract void nextEvent(Bucket bucket, int event);

	public abstract boolean isContained();

	public List<QPNode> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
