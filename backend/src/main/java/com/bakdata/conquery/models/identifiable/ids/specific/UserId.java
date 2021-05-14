package com.bakdata.conquery.models.identifiable.ids.specific;

import java.util.List;

import com.bakdata.conquery.io.storage.MetaStorage;
import com.bakdata.conquery.models.auth.entities.User;
import com.bakdata.conquery.models.identifiable.ids.Id;
import com.bakdata.conquery.models.identifiable.ids.IdIterator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper=false)
public class UserId extends PermissionOwnerId<User> {
	public static final String TYPE = "user";

	@Getter
	private final String email;

	public UserId(String email) {
		super();
		this.email = email;
	}
	
	@Override
	public void collectComponents(List<Object> components) {
		components.add(TYPE);
		components.add(email);
	}

	public enum Parser implements Id.Parser<UserId> {
		INSTANCE;
		
		@Override
		public UserId parseInternally(IdIterator parts) {
			return (UserId) PermissionOwnerId.Parser.INSTANCE.parse(parts);
		}
	}

	@Override
	public User getPermissionOwner(MetaStorage storage) {
		return storage.getUser(this);
	}
}
