package com.bakdata.eva.models.auth;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Builder;
import lombok.Getter;

@Builder
public class IngefCredentials {

	@NotEmpty @Getter
	private String email;
	@NotEmpty @Getter
	private String name;
	@NotNull
	private LocalDateTime validUntil;
	@NotNull @Getter
	private String company;

	public boolean isValid() {
		return validUntil.isAfter(LocalDateTime.now());
	}

}
