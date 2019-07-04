package com.bakdata.conquery.models.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bakdata.conquery.io.jackson.Jackson;
import com.bakdata.conquery.models.identifiable.ids.NamespacedId;
import com.bakdata.conquery.models.identifiable.ids.specific.DatasetId;
import com.bakdata.conquery.models.query.concept.CQElement;
import com.bakdata.conquery.models.query.concept.ConceptQuery;
import com.bakdata.conquery.models.worker.Namespaces;

import lombok.experimental.UtilityClass;

@UtilityClass
public class QueryTranslator {

	public <T extends IQuery> T translate(Namespaces namespaces, T element, DatasetId target) {
		if(element instanceof ConceptQuery) {
			CQElement root = translate(namespaces, ((ConceptQuery) element).getRoot(), target);
			ConceptQuery translated = new ConceptQuery();
			translated.setRoot(root);
			return (T)translated;
		}
		else {
			throw new IllegalStateException("Can't translate non ConceptQuery IQueries");
		}
	}
	
	public <T extends CQElement> T translate(Namespaces namespaces, T element, DatasetId target) {
		try {
			String value = Jackson.MAPPER.writeValueAsString(element);
	
			Pattern[] patterns = element
				.collectNamespacedIds()
				.stream()
				.map(NamespacedId::getDataset)
				.map(DatasetId::toString)
				.map(n -> Pattern.compile("(?<=(\"))" + Pattern.quote(n) + "(?=(\\.|\"))"))
				.toArray(Pattern[]::new);
	
			String replacement = Matcher.quoteReplacement(target.toString());
			for (Pattern pattern : patterns) {
				value = pattern.matcher(value).replaceAll(replacement);
			}
	
			return (T)namespaces
				.injectInto(Jackson.MAPPER.copy())
				.readValue(value, CQElement.class);
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to translate element "+element+" to dataset "+target);
		}
	}
}
