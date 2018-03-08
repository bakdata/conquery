// @flow

import { type Dispatch }         from 'redux-thunk';

import api                       from '../../api';

import { createActionTypes }     from './actionTypes';

export const createFormSuggestionActions = (formType: string, fieldName: string): Object => {
  const actionTypes = createActionTypes(formType, fieldName);

  const loadFormFilterSuggestionsStart =
    (formName, fieldName, andIdx, orIdx, tableIdx, conceptId, filterIdx, prefix) => ({
      type: actionTypes.LOAD_FILTER_SUGGESTIONS_START,
      payload: { formName, fieldName, andIdx, orIdx, tableIdx, conceptId, filterIdx, prefix }
  });

  const loadFormFilterSuggestionsSuccess =
    (suggestions, formName, fieldName, andIdx, orIdx, tableIdx, filterIdx) => ({
      type: actionTypes.LOAD_FILTER_SUGGESTIONS_SUCCESS,
      payload: {
        suggestions,
        formName,
        fieldName,
        andIdx,
        orIdx,
        tableIdx,
        filterIdx
      }
  });

  const loadFormFilterSuggestionsError =
    (error, formName, fieldName, andIdx, orIdx, tableIdx, filterIdx) => ({
      type: actionTypes.LOAD_FILTER_SUGGESTIONS_ERROR,
      payload: {
        ...error,
        formName,
        fieldName,
        andIdx,
        orIdx,
        tableIdx,
        filterIdx
      }
  });

  const loadFormFilterSuggestions =
    (formName, fieldName, datasetId, andIdx, orIdx, tableIdx,
      tableId, conceptId, filterIdx, filterId, prefix) => {
      return (dispatch: Dispatch) => {
        dispatch(loadFormFilterSuggestionsStart(
          formName, fieldName, andIdx, orIdx, tableIdx, conceptId, filterIdx, prefix
        ));

        return api.postPrefixForSuggestions(datasetId, conceptId, tableId, filterId, prefix)
          .then(
            r => dispatch(
              loadFormFilterSuggestionsSuccess(r, formName, fieldName, andIdx,
                orIdx, tableIdx, filterIdx)
            ),
            e => dispatch(
              loadFormFilterSuggestionsError(e, formName, fieldName, andIdx,
                orIdx, tableIdx, filterIdx)
            )
          );
      }
  }

  return {
    loadFormFilterSuggestions
  };
};
