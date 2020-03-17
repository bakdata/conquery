package com.bakdata.conquery.models.config;

import java.io.File;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.dropwizard.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class StorageConfig {
	@NotNull @Valid
	private File directory = new File("storage");

	private boolean validateOnWrite = false;
	@NotNull @Valid
	private XodusConfig xodus = new XodusConfig();
	private boolean useWeakDictionaryCaching = true;
	@NotNull
	private Duration weakCacheDuration = Duration.hours(48);
}
