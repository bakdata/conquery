package com.bakdata.conquery.models.messages.namespaces.specific;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.messages.namespaces.NamespaceMessage;
import com.bakdata.conquery.models.messages.namespaces.NamespacedMessage;
import com.bakdata.conquery.models.query.results.ShardResult;
import com.bakdata.conquery.models.worker.Namespace;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Workers send their part of the query result to master for assembly.
 */
@CPSType(id="COLLECT_QUERY_RESULT", base=NamespacedMessage.class)
@AllArgsConstructor @NoArgsConstructor @Getter @Setter @ToString(of = "result")
public class CollectQueryResult extends NamespaceMessage.Slow {

	private ShardResult result;

	@Override
	public void react(Namespace context) throws Exception {
		context.getQueryManager().addQueryResult(result);
	}
}
