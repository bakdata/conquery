package com.bakdata.conquery.models.preproc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.validation.Validator;

import org.apache.commons.io.FileUtils;

import com.bakdata.conquery.ConqueryConstants;
import com.bakdata.conquery.io.HCFile;
import com.bakdata.conquery.io.csv.CSV;
import com.bakdata.conquery.io.jackson.Jackson;
import com.bakdata.conquery.models.config.ConqueryConfig;
import com.bakdata.conquery.models.config.PreprocessingDirectories;
import com.bakdata.conquery.models.exceptions.JSONException;
import com.bakdata.conquery.models.exceptions.ParsingException;
import com.bakdata.conquery.models.preproc.outputs.AutoOutput;
import com.bakdata.conquery.models.preproc.outputs.Output;
import com.bakdata.conquery.models.types.CType;
import com.bakdata.conquery.models.types.specific.StringType;
import com.bakdata.conquery.util.io.ConqueryFileUtil;
import com.bakdata.conquery.util.io.ConqueryMDC;
import com.bakdata.conquery.util.io.LogUtil;
import com.google.common.base.Predicates;
import com.google.common.primitives.Primitives;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Preprocessor {
	
	private static final long MAX_ERROR_PRINTING = 50;
	
	private AtomicLong errorCounter = new AtomicLong(0L);

	public List<ImportDescriptor> findInitialDescriptors(PreprocessingDirectories dirs, Validator validator) throws IOException, JSONException {
		List<ImportDescriptor> l = new ArrayList<>();
		File in = dirs.getDescriptions().getAbsoluteFile();
		for(File descriptionFile:in.listFiles()) {
			if(descriptionFile.getName().endsWith(ConqueryConstants.EXTENSION_DESCRIPTION)) {
				InputFile file = InputFile.fromDescriptionFile(descriptionFile, dirs);
				try {
					ImportDescriptor descr = file.readDescriptor(validator);
					descr.setInputFile(file);
					l.add(descr);
				} catch(Exception e) {
					log.error("Failed to process "+LogUtil.printPath(descriptionFile), e);
				}
			}
		}
		return l;
	}

	public void preprocess(ImportDescriptor descriptor, ConqueryConfig config) throws IOException, JSONException, ParsingException {
		
		if (checkExistingHash(descriptor)) {
			return;
		}

		File tmp = ConqueryFileUtil.createTempFile(descriptor.getInputFile().getPreprocessedFile().getName(), ConqueryConstants.EXTENSION_PREPROCESSED.substring(1));
		if(!Files.isWritable(tmp.getParentFile().toPath())) {
			throw new IllegalArgumentException("No write permission in "+LogUtil.printPath(tmp.getParentFile()));
		}
		if(!Files.isWritable(descriptor.getInputFile().getPreprocessedFile().toPath().getParent())) {
			throw new IllegalArgumentException("No write permission in "+LogUtil.printPath(descriptor.getInputFile().getPreprocessedFile().toPath().getParent()));
		}
		if(descriptor.getInputFile().getPreprocessedFile().exists()) {
			FileUtils.forceDelete(descriptor.getInputFile().getPreprocessedFile());
		}


		log.info("PREPROCESSING START in {}", descriptor.getInputFile().getDescriptionFile());
		Preprocessed result = new Preprocessed(config.getPreprocessor(), descriptor);

		try (HCFile outFile = new HCFile(tmp, true)) {
			try (com.esotericsoftware.kryo.io.Output out = new com.esotericsoftware.kryo.io.Output(outFile.writeContent())) {
				result.setBlockOut(out);
				long lineId = config.getCsv().isSkipHeader()?1:0;
				for(int inputSource=0;inputSource<descriptor.getInputs().length;inputSource++) {
					Input input = descriptor.getInputs()[inputSource];
					final String name = descriptor.toString()+":"+descriptor.getTable()+"["+inputSource+"]";
					ConqueryMDC.setLocation(name);

					try(CSV csv = new CSV(config.getCsv(), input.getSourceFile())) {
						Iterator<String[]> it = csv.iterateContent(log);

						while(it.hasNext()) {
							String[] row = it.next();
							Integer primary = getPrimary((StringType) result.getPrimaryColumn().getType(), row, lineId, inputSource, input.getPrimary());
							if(primary != null) {
								int primaryId = result.addPrimary(primary);
								parseRow(primaryId, result.getColumns(), row, input, lineId, result, inputSource);
							}
						}

						if(input.checkAutoOutput()) {
							List<AutoOutput.OutRow> outRows = input.getAutoOutput().finish();
							for (AutoOutput.OutRow outRow : outRows) {
								result.addRow(outRow.getPrimaryId(), outRow.getTypes(), outRow.getData());
							}
						}
					}
				}
				//find the optimal subtypes
				log.info("finding optimal column types");
				log.info("{}.{}: {} -> {}", result.getName(), result.getPrimaryColumn().getName(), result.getPrimaryColumn().getOriginalType(), result.getPrimaryColumn().getType());
				
				for(PPColumn c:result.getColumns()) {
					c.findBestType();
					log.info("{}.{}: {} -> {}", result.getName(), c.getName(), c.getOriginalType(), c.getType());
				}

				result.writeToFile();
			}

			try (OutputStream out = outFile.writeHeader()) {
				result.writeHeader(out);
			}
		}


		FileUtils.moveFile(tmp, result.getFile().getPreprocessedFile());

		log.info("PREPROCESSING DONE in {}", descriptor.getInputFile().getDescriptionFile());

	}

	private void parseRow(int primaryId, PPColumn[] columns, String[] row, Input input, long lineId, Preprocessed result, int inputSource) throws ParsingException {
		if (input.checkAutoOutput()) {
			List<AutoOutput.OutRow> outRows = input.getAutoOutput().createOutput(primaryId, row, columns, inputSource, lineId);
			for (AutoOutput.OutRow outRow : outRows) {
				result.addRow(primaryId, columns, outRow.getData());
			}
		} else {
			if (input.filter(row)) {
				try {
					for (Object[] outRow : generateOutput(input, columns, row, inputSource, lineId)) {
						result.addRow(primaryId, columns, outRow);
					}
				} catch (ParsingException e) {
					long errors = errorCounter.getAndIncrement();
					if (errors < MAX_ERROR_PRINTING) {
						log.warn("Failed to parse line:" + lineId + " content:" + Arrays.toString(row), e);
					}
					else if (errors == MAX_ERROR_PRINTING) {
						log.warn("More erroneous lines occurred. Only the first "+MAX_ERROR_PRINTING+" were printed.");
					}

				} catch(Exception e) {
					throw new IllegalStateException("failed while processing line:"+lineId+" content:"+Arrays.toString(row), e);
				}
			}
		}
	}

	private Integer getPrimary(StringType primaryType, String[] row, long lineId, int source, Output primaryOutput) {
		try {
			List<Object> primary = primaryOutput.createOutput(primaryType, row, source, lineId);
			if(primary.size()!=1 || !(primary.get(0) instanceof Integer)) {
				throw new IllegalStateException("The returned primary was the illegal value "+primary+" in "+Arrays.toString(row));
			}
			return (int)primary.get(0);
		} catch (ParsingException e) {
			long errors = errorCounter.getAndIncrement();
			if(errors<MAX_ERROR_PRINTING) {
				log.warn("Failed to parse primary from line:"+lineId+" content:"+Arrays.toString(row), e);
			}
			else if(errors == MAX_ERROR_PRINTING) {
				log.warn("More erroneous lines occurred. Only the first "+MAX_ERROR_PRINTING+" were printed.");
			}
			return null;
		}
	}

	private static boolean checkExistingHash(ImportDescriptor descriptor) throws IOException {
		if(descriptor.getInputFile().getPreprocessedFile().exists()) {
			log.info("EXISTS ALREADY");
			int currentHash = descriptor.calculateValidityHash();
			try (HCFile outFile = new HCFile(descriptor.getInputFile().getPreprocessedFile(), false)) {
				try (InputStream is = outFile.readHeader()) {
					PPHeader header = Jackson.BINARY_MAPPER.readValue(is, PPHeader.class);
					if(header.getValidityHash()==currentHash) {
						log.info("HASH STILL VALID");
						return true;
					}
					else {
						log.info("HASH OUTDATED");
					}
				} catch(Exception e) {
					log.warn("HEADER READING FAILED", e);
				}
			}
		}
		return false;
	}

	private static List<Object[]> generateOutput(Input input, PPColumn[] columns, String[] row, int source, long lineId) throws ParsingException {
		List<Object[]> resultRows = new ArrayList<>();
		int oid = 0;
		for(int c = 0; c<input.getOutput().length; c++) {
			Output out = input.getOutput()[c];
			CType<?,?> type = columns[c].getType();
			Class<?> jType = Primitives.wrap(type.getPrimitiveType());
			
			List<Object> result;
			result = out.createOutput(type, row, source, lineId);
			if(result==null) {
				throw new IllegalStateException(out+" returned null result for "+Arrays.toString(row));
			}
			else if(result.stream().filter(Objects::nonNull).anyMatch(Predicates.not(jType::isInstance))) {
				throw new IllegalStateException(
						out
						+ " returned result with wrong types for "
						+ Arrays.toString(row)
						+ " "
						+ result.stream()
							.filter(Objects::nonNull)
							.filter(Predicates.not(jType::isInstance))
							.collect(Collectors.toList())
				);
			}
			
			
			//if the result is a single NULL and we don't want to include such rows
			if(result.size()==1 && result.get(0)==null && out.isRequired()) {
				return Collections.emptyList();
			}
			else {
				if(resultRows.isEmpty()) {
					for(Object v:result) {
						Object[] newRow = new Object[input.getOutput().length];
						newRow[oid]=v;
						resultRows.add(newRow);
					}
				}
				else {
					if(result.size()==1) {
						for(Object[] resultRow:resultRows) {
							resultRow[oid]=result.get(0);
						}
					}
					else {
						List<Object[]> newResultRows = new ArrayList<>(resultRows.size()*result.size());
						for(Object v:result) {
							for(Object[] resultRow:resultRows) {
								Object[] newResultRow = Arrays.copyOf(resultRow,resultRow.length);
								newResultRow[oid]=v;
								newResultRows.add(newResultRow);
							}
						}
						resultRows = newResultRows;
					}
				}
			}
			oid++;
		}
		return resultRows;
	}
}