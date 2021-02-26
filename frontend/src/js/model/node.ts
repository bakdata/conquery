import type { ConceptQueryNodeType } from "../standard-query-editor/types";

import { tablesHaveActiveFilter } from "./table";
import { objectHasSelectedSelects } from "./select";

export const nodeHasActiveFilters = (node: ConceptQueryNodeType) =>
  node.excludeTimestamps ||
  node.includeSubnodes ||
  objectHasSelectedSelects(node) ||
  nodeHasActiveTableFilters(node) ||
  nodeHasExludedTable(node);

export const nodeHasActiveTableFilters = (node: ConceptQueryNodeType) => {
  if (!node.tables) return false;

  return tablesHaveActiveFilter(node.tables);
};

export const nodeHasExludedTable = (node: ConceptQueryNodeType) => {
  if (!node.tables) return false;

  return node.tables.some((table) => table.exclude);
};

export function nodeIsInvalid(
  node: ConceptQueryNodeType,
  blocklistedConceptIds?: string[],
  allowlistedConceptIds?: string[]
) {
  return (
    (!!allowlistedConceptIds &&
      !nodeIsAllowlisted(node, allowlistedConceptIds)) ||
    (!!blocklistedConceptIds && nodeIsBlocklisted(node, blocklistedConceptIds))
  );
}

export function nodeIsBlocklisted(
  node: ConceptQueryNodeType,
  blocklistedConceptIds: string[]
) {
  return (
    !!node.ids &&
    blocklistedConceptIds.some((id) =>
      node.ids.some((conceptId) => conceptId.indexOf(id.toLowerCase()) !== -1)
    )
  );
}

export function nodeIsAllowlisted(
  node: ConceptQueryNodeType,
  allowlistedConceptIds: string[]
) {
  return (
    !!node.ids &&
    allowlistedConceptIds.some((id) =>
      node.ids.every((conceptId) => conceptId.indexOf(id.toLowerCase()) !== -1)
    )
  );
}
