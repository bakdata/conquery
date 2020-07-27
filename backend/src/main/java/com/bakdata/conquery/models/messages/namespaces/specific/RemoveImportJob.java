package com.bakdata.conquery.models.messages.namespaces.specific;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.identifiable.ids.specific.ImportId;
import com.bakdata.conquery.models.messages.namespaces.NamespacedMessage;
import com.bakdata.conquery.models.messages.namespaces.WorkerMessage;
import com.bakdata.conquery.models.worker.Worker;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;


@CPSType(id="REMOVE_IMPORT", base= NamespacedMessage.class)
@AllArgsConstructor(onConstructor_=@JsonCreator)  @Getter @Setter @ToString
@Slf4j
public class RemoveImportJob extends WorkerMessage.Slow {

	private ImportId importId;

	@Override
	public void react(Worker context) throws Exception {
		log.info("Deleting Import[{}]", importId);

		context.getStorage().removeImport(importId);
	}
}
