// @flow

import { connect }     from 'react-redux';
import type { Dispatch } from 'redux-thunk';

import { QueryRunner } from '../query-runner';
import actions         from '../app/actions';

const {
  startStandardQuery,
  stopStandardQuery,
} = actions;

function isButtonEnabled(state, ownProps) {
  return !!( // Return true or false even if all are undefined / null
    ownProps.datasetId !== null &&
    !state.panes.right.tabs.queryEditor.queryRunner.startQuery.loading &&
    !state.panes.right.tabs.queryEditor.queryRunner.stopQuery.loading &&
    state.panes.right.tabs.queryEditor.query.length > 0
  );
}


const mapStateToProps = (state, ownProps) => ({
  queryRunner: state.panes.right.tabs.queryEditor.queryRunner,
  isButtonEnabled: isButtonEnabled(state, ownProps),
  isQueryRunning: !!state.panes.right.tabs.queryEditor.queryRunner.runningQuery,
  // Following ones only needed in dispatch functions
  queryId: state.panes.right.tabs.queryEditor.queryRunner.runningQuery,
  version: state.categoryTrees.version,
  query: state.panes.right.tabs.queryEditor.query,
});

const mapDispatchToProps = (dispatch: Dispatch) => ({
  startQuery: (datasetId, query, version) =>
    dispatch(startStandardQuery(datasetId, query, version)),
  stopQuery: (datasetId, queryId) =>
    dispatch(stopStandardQuery(datasetId, queryId)),
});

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  startQuery: () => dispatchProps.startQuery(
    ownProps.datasetId,
    stateProps.query,
    stateProps.version,
  ),
  stopQuery: () => dispatchProps.stopQuery(
    ownProps.datasetId,
    stateProps.queryId,
  ),
});

export const StandardQueryRunner =
  connect(mapStateToProps, mapDispatchToProps, mergeProps)(QueryRunner);
