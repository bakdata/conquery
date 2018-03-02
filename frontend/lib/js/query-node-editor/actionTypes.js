// @flow

import { toUpperCaseUnderscore } from '../common/helpers';

export const createActionTypes = (type: string) => {
  const uppercasedType = toUpperCaseUnderscore(type);

  return {
    SET_DETAILS_VIEW_ACTIVE: `query-node-editor/SET_${uppercasedType}_DETAILS_VIEW_ACTIVE`,
    SET_INPUT_TABLE_VIEW_ACTIVE: `query-node-editor/SET_${uppercasedType}_INPUT_TABLE_VIEW_ACTIVE`,
    SET_FOCUSED_INPUT: `query-node-editor/SET_${uppercasedType}_FOCUSED_INPUT`,
    TOGGLE_EDIT_NAME: `query-node-editor/TOGGLE_${uppercasedType}_EDIT_NAME`,
    UPDATE_NAME: `query-node-editor/UPDATE_${uppercasedType}_NAME`,
  };
};
