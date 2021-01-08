package com.bakdata.conquery.commands;

import java.io.File;
import java.util.List;

import com.bakdata.conquery.models.config.ConqueryConfig;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.util.DataSize;
import jetbrains.exodus.env.Cursor;
import jetbrains.exodus.env.EnvironmentConfig;
import jetbrains.exodus.env.Environments;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.StoreConfig;
import jetbrains.exodus.env.Transaction;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

@Slf4j
public class RecodeStoreCommand extends ConfiguredCommand<ConqueryConfig> {


	public RecodeStoreCommand() {
		super("xodus-recode", "Recode a Xodus-Store to a new log-size.");
	}

	@Override
	public void configure(Subparser subparser) {
		subparser
				.addArgument("--in")
				.help("Input store.")
				.required(true)
				.type(Arguments.fileType().verifyIsDirectory());

		subparser
				.addArgument("--out")
				.help("Output store.")
				.required(true)
				.type(Arguments.fileType().verifyIsDirectory());

		subparser
				.addArgument("--in_logsize")
				.help("Logsize of incoming store.")
				.required(true);

		subparser
				.addArgument("--out_logsize")
				.help("Logsize of outgoing store.")
				.required(true);

		super.configure(subparser);
	}

	@Override
	protected void run(Bootstrap<ConqueryConfig> bootstrap, Namespace namespace, ConqueryConfig configuration) throws Exception {

		final File inStoreDirectory = namespace.get("in");
		final long inLogSize = DataSize.parse(namespace.get("in_logsize")).toKilobytes();

		final File outStoreDirectory = namespace.get("out");
		final long outLogSize = DataSize.parse(namespace.get("out_logsize")).toKilobytes();

		log.info("Recoding Environment at `{} ({})` to `{} ({})`", inStoreDirectory, inLogSize, outStoreDirectory, outLogSize);

		final jetbrains.exodus.env.Environment inEnvironment = Environments.newInstance(
				inStoreDirectory,
				new EnvironmentConfig().setLogFileSize(inLogSize)
									   .setEnvIsReadonly(true)
									   .setEnvCompactOnOpen(false)
									   .setGcEnabled(false)
		);

		final jetbrains.exodus.env.Environment outEnvironment = Environments.newInstance(
				outStoreDirectory,
				new EnvironmentConfig().setLogFileSize(outLogSize)
									   .setGcEnabled(false) // we dump first, then enable GC.
		);


		final List<String> stores = inEnvironment.computeInReadonlyTransaction(inEnvironment::getAllStoreNames);

		log.info("Environment contains {} Stores {}", stores.size(), stores);
		// Clear it before writing
		outEnvironment.clear();

		for (String store : stores) {
			final Store inStore =
					inEnvironment.computeInExclusiveTransaction(tx -> inEnvironment.openStore(store, StoreConfig.USE_EXISTING, tx, false));

			if (inStore == null) {
				log.error("{} does not exist, aborting.", store);
				return;
			}

			final Store outStore =
					outEnvironment.computeInExclusiveTransaction(tx -> outEnvironment.openStore(store, StoreConfig.WITHOUT_DUPLICATES_WITH_PREFIXING, tx, true));

			if (outEnvironment.computeInReadonlyTransaction(outStore::count) > 0) {
				log.warn("Store is not empty, it will be cleared.");
			}

			doRecode(inEnvironment, inStore, outEnvironment, outStore);
			log.info("Done writing {}. Commiting, then GC.", store);
		}

		outEnvironment.gc();
	}

	private void doRecode(jetbrains.exodus.env.Environment inEnvironment, Store inStore, jetbrains.exodus.env.Environment outEnvironment, Store outStore) {
		final Transaction readTx = inEnvironment.beginReadonlyTransaction();
		final Transaction writeTx = outEnvironment.beginExclusiveTransaction();

		final long count = inStore.count(readTx);
		long processed = 0;

		log.info("Contains {} Entries", count);

		final Cursor cursor = inStore.openCursor(readTx);

		while (cursor.getNext()) {
			outStore.putRight(writeTx, cursor.getKey(), cursor.getValue());
			if (processed++ % 1000 == 0) {
				log.info("Processed {} / {} ({}%)", processed, count, Math.round(100f * (float) processed / (float) count));
			}
		}

		writeTx.commit();
	}

}
