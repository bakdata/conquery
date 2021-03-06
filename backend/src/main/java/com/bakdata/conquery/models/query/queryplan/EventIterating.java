package com.bakdata.conquery.models.query.queryplan;

import java.util.HashSet;
import java.util.Set;

import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.query.QueryExecutionContext;
import com.bakdata.conquery.models.query.entity.Entity;

public interface EventIterating {
	
	default void collectRequiredTables(Set<Table> requiredTables) {}
	
	default Set<Table> collectRequiredTables() {
		HashSet<Table> out = new HashSet<>();
		this.collectRequiredTables(out);
		return out;
	}
	
	default void nextTable(QueryExecutionContext ctx, Table currentTable) {}
	
	default void nextBlock(Bucket bucket) {}

	void acceptEvent(Bucket bucket, int event);


	default boolean isOfInterest(Bucket bucket){ return true; }
	
	default boolean isOfInterest(Entity entity){ return true; }
}
