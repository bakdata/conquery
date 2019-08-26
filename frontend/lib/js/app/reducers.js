// @flow

import { combineReducers } from "redux";

import conceptTrees, {
  type StateType as ConceptTreesStateType
} from "../concept-trees/reducer";

import {
  reducer as datasets,
  type StateType as DatasetsStateType
} from "../dataset";

import {
  reducer as tooltip,
  type StateType as TooltipStateType
} from "../tooltip";

import uploadConceptListModal, {
  type StateType as UploadConceptListModalStateType
} from "../upload-concept-list-modal/reducer";

import {
  reducer as previousQueries,
  type StateT as PreviousQueriesStateT
} from "../previous-queries/list";

import type { StateType as PanesStateType } from "../pane";
import type { TabT } from "../pane/types";

import { reducer as startup } from "../startup";
import { buildPanesReducer } from "../pane/reducer";
import { reducer as queryGroupModal } from "../query-group-modal";

import { reducer as previousQueriesSearch } from "../previous-queries/search";
import { reducer as previousQueriesFilter } from "../previous-queries/filter";
import { reducer as uploadQueryResults } from "../previous-queries/upload";
import { reducer as deletePreviousQueryModal } from "../previous-queries/delete-modal";
import { reducer as snackMessage } from "../snack-message";
import { reducer as preview } from "../preview";
import { reducer as queryUploadConceptListModal } from "../query-upload-concept-list-modal";

import { createQueryNodeEditorReducer } from "../query-node-editor";

// TODO: Introduce more StateTypes gradually
export type StateType = {
  conceptTrees: ConceptTreesStateType,
  datasets: DatasetsStateType,
  tooltip: TooltipStateType,
  panes: PanesStateType,
  uploadConceptListModal: UploadConceptListModalStateType,
  previousQueries: PreviousQueriesStateT
};

const buildAppReducer = (tabs: TabT[]) => {
  return combineReducers({
    startup,
    conceptTrees,
    uploadConceptListModal,
    queryNodeEditor: createQueryNodeEditorReducer("standard"),
    queryGroupModal,
    datasets,
    tooltip,
    panes: buildPanesReducer(tabs),
    previousQueries,
    previousQueriesSearch,
    previousQueriesFilter,
    uploadQueryResults,
    deletePreviousQueryModal,
    snackMessage,
    preview,
    queryUploadConceptListModal,
    ...tabs.reduce((all, tab) => {
      all[tab.key] = tab.reducer;
      return all;
    }, {})
  });
};

export default buildAppReducer;
