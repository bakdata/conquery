// @flow

import { type NodeType }  from '../common/types/backend';

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

import { setTree, getConceptById }        from './globalTreeStoreHelper';

export type TreesType = { [treeId: string]: NodeType }

export type SearchType = {
  searching: boolean,
  searchStr: string,
  words: Array<string>,
  result: TreesType
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
  search: { searching: false, words: [], result: {} }
};

const searchTreesEnd = (state: StateType, action: Object): StateType => {
  return {
    ...state
  }
}

const searchTreesStart = (state: StateType, action: Object): StateType => {
  const { searchStr } = action.payload;
  const searching = searchStr.length > 2;

return {
    ...state,
    search: {
      searching: searching,
      searchStr: searchStr,
      words: searchStr ? searchStr.split(' ') : [],
      result: searching ? searchingTrees(state.trees, searchStr) : {}
    }
  }
}

const searchingTrees = (trees: TreesType, searchStr) => {
  return Object.assign({}, ...Object.entries(trees).map(([treeId, treeNode]) => ({
    [treeId]: findTreeNodes(treeId, treeNode, searchStr)
  })).filter(r => { return Object.keys(r).some(k => r[k].length > 0) }));
}

const findTreeNodes = (treeId: string, treeNode: NodeType, searchStr: string) => {
  if (treeNode === null)
    treeNode = getConceptById(treeId);

  const children = treeNode.children || [];
  const result = children.map(child => findTreeNodes(child, null, searchStr))
    .reduce((agg, cur) => [...agg, ...cur], []);

  const label = treeNode.label || '';
  const description = treeNode.description || '';
  const additionalInfos = treeNode.additionalInfos
    ? treeNode.additionalInfos.map(t => { return t.key + " " + t.value   }).join('')
    : '';

  if (result.length ||
      label.toLowerCase().includes(searchStr.toLowerCase()) ||
      description.toLowerCase().includes(searchStr.toLowerCase()) ||
      additionalInfos.toLowerCase().includes(searchStr.toLowerCase()))
      // (fuzzyMatch(label, searchStr).length > 0) ||
      // (fuzzyMatch(description, searchStr).length > 0) ||
      // (fuzzyMatch(additionalInfos, searchStr).length > 0))
        return [treeId, ...result];

  return [];
}

const fuzzyMatch = (text: string, searchStr: string) => {
    if (!text) return '';

    var search = searchStr.replace(/ /g, '').toLowerCase();
    var tokens = [];
    var searchPosition = 0;

    // Go through each character in the text
    for (var n = 0; n < text.length; n++) {
        var textChar = text[n];
        if (searchPosition < search.length &&
          textChar && textChar.toLowerCase() === search[searchPosition])
            searchPosition += 1;

        tokens.push(textChar);
    }
    // If are characters remaining in the search text,
    // return an empty string to indicate no match
    if (searchPosition !== search.length)
      return '';

    return tokens.join('');
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
