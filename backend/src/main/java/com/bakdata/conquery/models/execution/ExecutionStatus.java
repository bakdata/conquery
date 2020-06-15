package com.bakdata.conquery.models.execution;

import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;

import com.bakdata.conquery.apiv1.QueryDescription;
import com.bakdata.conquery.models.identifiable.ids.specific.ManagedExecutionId;
import com.bakdata.conquery.models.identifiable.ids.specific.UserId;
import com.bakdata.conquery.models.query.ColumnDescriptor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

@NoArgsConstructor
@ToString
@AllArgsConstructor
@Data
@FieldNameConstants
public class ExecutionStatus {

	private String[] tags;
	private String label;
	private ZonedDateTime createdAt;
	private ZonedDateTime lastUsed;
	private UserId owner;
	private String ownerName;
	private boolean shared;
	private boolean own;
	private boolean system;

	private ManagedExecutionId id;
	private ExecutionState status;
	private Long numberOfResults;
	private Long requiredTime;
	private URL resultUrl;

	@Data
	@NoArgsConstructor
	@EqualsAndHashCode(callSuper = true)
	public static class WithSingleQuery extends ExecutionStatus {
		/**
		 * Indicates if the concepts that are included in the query description can be accesed by the user.
		 */
		boolean canExpand;
		/**
		 * Is set to the query description if the user can expand all included concepts.
		 */
		private QueryDescription query;
		
		/**
		 * Holds a description for each column, present in the result.
		 */
		private List<ColumnDescriptor> columnDescriptions;
		
	}
}
