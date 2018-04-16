// @flow

import type { NodeType, TreeNodeIdType }  from '../common/types/backend';

import {
  LOAD_TREES_START,
  LOAD_TREES_SUCCESS,
  LOAD_TREES_ERROR,
  LOAD_TREE_START,
  LOAD_TREE_SUCCESS,
  LOAD_TREE_ERROR,
  CLEAR_TREES,
  SEARCH_TREES_START,
  SEARCH_TREES_END,
}                                         from './actionTypes';

import {
    setTree,
    createTreeSearchIndex
}                                         from './globalTreeStoreHelper';

export type TreesType = { [treeId: string]: NodeType }

export type SearchType = {
  searching: boolean,
  query: string,
  words: Array<string>,
  result: Array<TreeNodeIdType>
}

export type StateType = {
  loading: boolean,
  version: any,
  trees: TreesType,
  search?: SearchType,
};

const initialState: StateType = {
  loading: false,
  version: null,
  trees: {},
  search: { searching: false, query: '', words: [], result: [] }
};

const searchTreesEnd = (state: StateType, action: Object): StateType => {
  const { query, result } = action.payload;
  const limit = [];

  for (var i = 0; i < result.length; i++) {
    limit.push(result[i]);
    if (i >= (parseInt(process.env.SEARCH_RESULT_LIMIT) || 50)) break;
  }

  return {
    ...state,
    search: {
      searching: query && query.length > 0,
      query: query,
      words: query ? query.split(' ') : [],
      result: limit
    }
  }
}

const searchTreesStart = (state: StateType, action: Object): StateType => {
  const { query } = action.payload;

  return {
    ...state,
    search: {
      searching: false,
      query: query,
      words: query ? query.split(' ') : [],
      result: []
    }
  }
}


const updateTree = (state: StateType, action: Object, attributes: Object): StateType => {
  return {
    ...state,
    trees: {
      ...state.trees,
      [action.payload.treeId]: {
        ...state.trees[action.payload.treeId],
        ...attributes
      }
    }
  };
};

const setTreeLoading = (state: StateType, action: Object): StateType => {
  return updateTree(state, action, { loading: true });
};

const setTreeSuccess = (state: StateType, action: Object): StateType => {
  // Side effect in a reducer.
  // Globally store the huge (1-5 MB) trees for read only
  // - keeps the redux store free from huge data
  const { treeId, data } = action.payload;
  const newState = updateTree(state, action, { loading: false });

  const rootConcept = newState.trees[treeId];

  setTree(rootConcept, treeId, data);
  createTreeSearchIndex(data);

  return newState;
};

const setTreeError = (state: StateType, action: Object): StateType => {
  return updateTree(state, action, { loading: false, error: action.payload.message });
};

const categoryTrees = (
  state: StateType = initialState,
  action: Object
): StateType => {
  switch (action.type) {
    // All trees
    case LOAD_TREES_START:
      return { ...state, loading: true };
    case LOAD_TREES_SUCCESS:
      return {
        ...state,
        loading: false,
        version: action.payload.data.version,
        trees: action.payload.data.concepts
      };
    case LOAD_TREES_ERROR:
      return { ...state, loading: false, error: action.payload.message };

    // Individual tree:
    case LOAD_TREE_START:
      return setTreeLoading(state, action);
    case LOAD_TREE_SUCCESS:
      return setTreeSuccess(state, action);
    case LOAD_TREE_ERROR:
      return setTreeError(state, action);
    case SEARCH_TREES_START:
      return searchTreesStart(state, action);
    case SEARCH_TREES_END:
      return searchTreesEnd(state, action);
    case CLEAR_TREES:
      return initialState;
    default:
      return state;
  }
};

export default categoryTrees;
