package com.bakdata.conquery.models.messages.namespaces.specific;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.messages.namespaces.NamespacedMessage;
import com.bakdata.conquery.models.messages.namespaces.WorkerMessage;
import com.bakdata.conquery.models.worker.Worker;
import com.bakdata.conquery.models.worker.WorkerInformation;
import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@CPSType(id="UPDATE_SLAVE_IDENTITY", base=NamespacedMessage.class) @Slf4j
@RequiredArgsConstructor(onConstructor_=@JsonCreator) @Getter
public class UpdateWorkerBucket extends WorkerMessage {

	private final WorkerInformation info;
	
	@Override
	public void react(Worker context) throws Exception {
		//new included buckets from master
		context.getInfo().setIncludedBuckets(info.getIncludedBuckets());
		context.getStorage().updateWorker(info);
	}
}
