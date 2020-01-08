package com.bakdata.conquery.models.identifiable;

import com.bakdata.conquery.io.jackson.Injectable;
import com.bakdata.conquery.io.jackson.MutableInjectableValues;
import com.bakdata.conquery.models.identifiable.ids.IId;
import com.bakdata.conquery.models.identifiable.ids.specific.DatasetId;
import com.bakdata.conquery.models.worker.NamespaceCollection;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.NoArgsConstructor;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

@SuppressWarnings({"rawtypes", "unchecked"}) @NoArgsConstructor
public class CentralRegistry implements Injectable {
	
	private final IdMap map = new IdMap<>();
	private final ConcurrentMap<IId<?>, Supplier<Identifiable<?>>> cacheables = new ConcurrentHashMap<>();
	
	public synchronized void register(Identifiable<?> ident) {
		map.add(ident);
	}
	
	public synchronized void registerCacheable(IId<?> id, Supplier<Identifiable<?>> supplier) {
		cacheables.put(id, supplier);
	}
	
	public <T extends Identifiable<?>> T resolve(IId<T> name) {
		Object res = map.get(name);
		if(res!=null) {
			return (T)res;
		}
		Supplier<Identifiable<?>> supplier = cacheables.get(name);
		if(supplier == null) {
			throw new NoSuchElementException("Could not find an "+name.getClass().getSimpleName()+" element called '"+name+"'");
		}
		return (T)supplier.get();
	}

	public <T extends Identifiable<?>> Optional<T> getOptional(IId<T> name) {
		Object res = map.get(name);
		if(res!=null) {
			return Optional.of((T)res);
		}
		Supplier<Identifiable<?>> supplier = cacheables.get(name);
		if(supplier == null) {
			return Optional.empty();
		}
		return Optional.of((T)supplier.get());
	}

	public synchronized void remove(IId<?> id) {
		map.remove(id);
		cacheables.remove(id);
	}
	
	public void remove(Identifiable<?> ident) {
		remove(ident.getId());
	}

	@Override
	public MutableInjectableValues inject(MutableInjectableValues values) {
		return values.add(CentralRegistry.class, this);
	}

	public static CentralRegistry get(DeserializationContext ctxt) throws JsonMappingException {
		CentralRegistry result = (CentralRegistry) ctxt.findInjectableValue(CentralRegistry.class.getName(), null, null);
		if(result == null) {
			NamespaceCollection alternative = (NamespaceCollection)ctxt.findInjectableValue(NamespaceCollection.class.getName(), null, null);
			if(alternative == null) {
				return null;
			}
			else {
				return alternative.getMetaRegistry();
			}
		}
		else {
			return result;
		}
	}

	public static CentralRegistry getForDataset(DeserializationContext ctxt, DatasetId datasetId) throws JsonMappingException {
		NamespaceCollection alternative = (NamespaceCollection)ctxt.findInjectableValue(NamespaceCollection.class.getName(), null, null);

		if(alternative == null)
			return null;

		return alternative.findRegistry(datasetId);
	}
}
