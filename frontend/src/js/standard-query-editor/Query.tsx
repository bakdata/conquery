import styled from "@emotion/styled";
import { StateT } from "app-types";
import QueryGroupModal from "js/query-group-modal/QueryGroupModal";
import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { useDispatch, useSelector } from "react-redux";

import type { DatasetIdT } from "../api/types";
import { exists } from "../common/helpers/exists";
import { TreesT } from "../concept-trees/reducer";
import { useLoadPreviousQuery } from "../previous-queries/list/actions";
import { PreviousQueryIdT } from "../previous-queries/list/reducer";
import { openQueryUploadConceptListModal } from "../query-upload-concept-list-modal/actions";

import ExpandPreviousQueryModal from "./ExpandPreviousQueryModal";
import QueryEditorDropzone from "./QueryEditorDropzone";
import QueryFooter from "./QueryFooter";
import QueryGroup from "./QueryGroup";
import QueryHeader from "./QueryHeader";
import {
  dropAndNode,
  dropOrNode,
  deleteNode,
  deleteGroup,
  toggleExcludeGroup,
  useExpandPreviousQuery,
  selectNodeForEditing,
  toggleTimestamps,
  toggleSecondaryIdExclude,
} from "./actions";
import type { StandardQueryStateT } from "./queryReducer";
import type {
  DragItemConceptTreeNode,
  DragItemNode,
  DragItemQuery,
  PreviousQueryQueryNodeType,
} from "./types";

const Container = styled("div")`
  height: 100%;
  display: flex;
  flex-direction: column;
`;

const Groups = styled("div")`
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  padding: 0 0 20px;
  flex-grow: 1;
`;

const QueryGroupConnector = styled("p")`
  padding: 110px 6px 0;
  margin: 0;
  font-size: ${({ theme }) => theme.font.sm};
  color: ${({ theme }) => theme.col.gray};
  text-align: center;
`;

const Query = () => {
  const { t } = useTranslation();
  const datasetId = useSelector<StateT, DatasetIdT | null>(
    (state) => state.datasets.selectedDatasetId,
  );
  const query = useSelector<StateT, StandardQueryStateT>(
    (state) => state.queryEditor.query,
  );
  const isEmptyQuery = query.length === 0;
  const isQueryWithSingleElement =
    query.length === 1 && query[0].elements.length === 1;

  // only used by other actions
  const rootConcepts = useSelector<StateT, TreesT>(
    (state) => state.conceptTrees.trees,
  );

  const dispatch = useDispatch();
  const loadPreviousQuery = useLoadPreviousQuery();
  const expandPreviousQuery = useExpandPreviousQuery();

  const onDropAndNode = (
    item: DragItemNode | DragItemQuery | DragItemConceptTreeNode,
  ) => dispatch(dropAndNode(item));
  const onDropConceptListFile = (file: File, andIdx: number | null) =>
    dispatch(openQueryUploadConceptListModal(andIdx, file));
  const onDropOrNode = (
    item: DragItemNode | DragItemQuery | DragItemConceptTreeNode,
    andIdx: number,
  ) => dispatch(dropOrNode(item, andIdx));
  const onDeleteNode = (andIdx: number, orIdx: number) =>
    dispatch(deleteNode(andIdx, orIdx));
  const onDeleteGroup = (andIdx: number) => dispatch(deleteGroup(andIdx));
  const onToggleExcludeGroup = (andIdx: number) =>
    dispatch(toggleExcludeGroup(andIdx));
  const onToggleTimestamps = (andIdx: number, orIdx: number) =>
    dispatch(toggleTimestamps(andIdx, orIdx));
  const onToggleSecondaryIdExclude = (andIdx: number, orIdx: number) =>
    dispatch(toggleSecondaryIdExclude(andIdx, orIdx));
  const onSelectNodeForEditing = (andIdx: number, orIdx: number) =>
    dispatch(selectNodeForEditing(andIdx, orIdx));
  const onLoadPreviousQuery = (queryId: PreviousQueryIdT) => {
    if (datasetId) {
      loadPreviousQuery(datasetId, queryId);
    }
  };

  const [
    queryToExpand,
    setQueryToExpand,
  ] = useState<PreviousQueryQueryNodeType | null>(null);

  const [queryGroupModalAndIx, setQueryGroupModalAndIdx] = useState<
    number | null
  >(null);

  if (!datasetId) {
    return null;
  }

  const onExpandPreviousQuery = (q: PreviousQueryQueryNodeType) => {
    if (isQueryWithSingleElement) {
      expandPreviousQuery(datasetId, rootConcepts, q);
    } else {
      setQueryToExpand(q);
    }
  };

  return (
    <Container>
      {exists(queryGroupModalAndIx) && (
        <QueryGroupModal
          andIdx={queryGroupModalAndIx}
          onClose={() => setQueryGroupModalAndIdx(null)}
        />
      )}
      {exists(queryToExpand) && (
        <ExpandPreviousQueryModal
          onClose={() => setQueryToExpand(null)}
          onAccept={() => {
            if (datasetId) {
              expandPreviousQuery(datasetId, rootConcepts, queryToExpand);
              setQueryToExpand(null);
            }
          }}
        />
      )}
      {isEmptyQuery ? (
        <QueryEditorDropzone
          isInitial
          onDropNode={onDropAndNode}
          onDropFile={(file) => onDropConceptListFile(file, null)}
          onLoadPreviousQuery={onLoadPreviousQuery}
        />
      ) : (
        <>
          <QueryHeader />
          <Groups>
            {query.map((group, andIdx) => [
              <QueryGroup
                key={andIdx}
                group={group}
                andIdx={andIdx}
                onDropNode={(item) => onDropOrNode(item, andIdx)}
                onDropFile={(file: File) => onDropConceptListFile(file, andIdx)}
                onDeleteNode={(orIdx: number) => onDeleteNode(andIdx, orIdx)}
                onDeleteGroup={() => onDeleteGroup(andIdx)}
                onEditClick={(orIdx: number) =>
                  onSelectNodeForEditing(andIdx, orIdx)
                }
                onExpandClick={onExpandPreviousQuery}
                onExcludeClick={() => onToggleExcludeGroup(andIdx)}
                onDateClick={() => setQueryGroupModalAndIdx(andIdx)}
                onLoadPreviousQuery={onLoadPreviousQuery}
                onToggleTimestamps={(orIdx: number) =>
                  onToggleTimestamps(andIdx, orIdx)
                }
                onToggleSecondaryIdExclude={(orIdx: number) =>
                  onToggleSecondaryIdExclude(andIdx, orIdx)
                }
              />,
              <QueryGroupConnector key={`${andIdx}.and`}>
                {t("common.and")}
              </QueryGroupConnector>,
            ])}
            <QueryEditorDropzone
              isAnd
              tooltip={t("help.editorDropzoneAnd")}
              onDropNode={onDropAndNode}
              onDropFile={(file) => onDropConceptListFile(file, null)}
              onLoadPreviousQuery={onLoadPreviousQuery}
            />
          </Groups>
          <QueryFooter />
        </>
      )}
    </Container>
  );
};

export default Query;
