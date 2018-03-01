// @flow

import React                      from 'react';
import { connect }                from 'react-redux';
import type { Dispatch }          from 'redux';
import T                          from 'i18n-react';
import { replace }                from 'react-router-redux';


import { toQuery }                from '../routes'


import {
  queryGroupModalSetNode,
}                                 from '../query-group-modal/actions';

import {
  loadPreviousQuery,
  loadAllPreviousQueriesInGroups,
}                                 from '../previous-queries/list/actions';

import {
  dropAndNode,
  dropConceptListFile,
  dropOrConceptListFile,
  dropOrNode,
  deleteNode,
  deleteGroup,
  toggleExcludeGroup,
  expandPreviousQuery,
  selectNodeForEditing,
  showConceptListDetails,
  hideConceptListDetails,
}                                 from './actions'
import type { StandardQueryType } from './types';

import QueryEditorDropzone        from './QueryEditorDropzone';
import QueryGroup                 from './QueryGroup';
import ConceptListDetailsModal    from './ConceptListDetailsModal';


type PropsType = {
  query: StandardQueryType,
  isEmptyQuery: boolean,
  dropAndNode: Function,
  dropConceptListFile: Function,
  dropOrNode: Function,
  dropOrConceptListFile: Function,
  deleteNode: Function,
  deleteGroup: Function,
  toggleExcludeGroup: Function,
  expandPreviousQuery: Function,
  showConceptListDetails: Function,
  hideConceptListDetails: Function,
  loadPreviousQuery: Function,
  selectNodeForEditing: Function,
  queryGroupModalSetNode: Function,
  dateRange: Object,
  selectedConceptListDetails: Object,
};

const Query = (props: PropsType) => {
  return (
    <div className="query-editor__query-container">
      {
        props.isEmptyQuery &&
        // Render a large Dropzone
        <QueryEditorDropzone
          isInitial
          onDropNode={props.dropAndNode}
          onDropFiles={props.dropConceptListFile}
          onLoadPreviousQuery={props.loadPreviousQuery}
        />
      }
      <div className="query-editor__query">
        {
          !props.isEmptyQuery &&
          // Render all query groups plus individual AND / OR dropzones
          props.query.map((group, andIdx) => ([
              <QueryGroup
                key={andIdx}
                group={group}
                andIdx={andIdx}
                onDropNode={item => props.dropOrNode(item, andIdx)}
                onDropFiles={item => props.dropOrConceptListFile(item, andIdx)}
                onDeleteNode={orIdx => props.deleteNode(andIdx, orIdx)}
                onDeleteGroup={() => props.deleteGroup(andIdx)}
                onFilterClick={orIdx => props.selectNodeForEditing(andIdx, orIdx)}
                onExpandClick={props.expandPreviousQuery}
                onDetailsClick={props.showConceptListDetails}
                onExcludeClick={() => props.toggleExcludeGroup(andIdx)}
                onDateClick={() => props.queryGroupModalSetNode(andIdx)}
                onLoadPreviousQuery={props.loadPreviousQuery}
              />,
              <p className="query-group-connector">{T.translate('common.and')}</p>
            ])).concat(
              <div
                className="dropzone-wrap"
                key={props.query.length + 1}
              >
                <QueryEditorDropzone
                  isAnd
                  onDropNode={item => props.dropAndNode(item, props.dateRange)}
                  onDropFiles={item => props.dropConceptListFile(item, props.dateRange)}
                  onLoadPreviousQuery={props.loadPreviousQuery}
                />
              </div>
            )
        }
      </div>
      {
        props.selectedConceptListDetails &&
        <ConceptListDetailsModal
          onCloseModal={props.hideConceptListDetails}
          headline={props.selectedConceptListDetails.label}
          conceptTreeRoot={props.selectedConceptListDetails.conceptListMetadata.root}
          items={
            props.selectedConceptListDetails.conceptListMetadata.concepts
              .map(({ label }) => label)
          }
        />
      }
    </div>
  );
};

function mapStateToProps(state) {
  return {
    query: state.query,
    isEmptyQuery: state.query.length === 0,
    selectedConceptListDetails: state.query.map(x => x.elements)
      .reduce((a, b) => a.concat(b), [])
      .find(x => x.showDetails),

    // only used by other actions
    rootConcepts: state.categoryTrees.trees,
  };
}

function mapDispatchToProps(dispatch: Dispatch<*>) {
  return {
    dropAndNode: (item, dateRange) => dispatch(dropAndNode(item, dateRange)),
    dropConceptListFile: (item, dateRange) => dispatch(dropConceptListFile(item, { dateRange })),
    dropOrConceptListFile: (item, andIdx) => dispatch(dropOrConceptListFile(item, andIdx)),
    dropOrNode: (item, andIdx) => dispatch(dropOrNode(item, andIdx)),
    deleteNode: (andIdx, orIdx) => dispatch(deleteNode(andIdx, orIdx)),
    deleteGroup: (andIdx) => dispatch(deleteGroup(andIdx)),
    toggleExcludeGroup: (andIdx) => dispatch(toggleExcludeGroup(andIdx)),
    selectNodeForEditing: (andIdx, orIdx) =>
      dispatch(selectNodeForEditing(andIdx, orIdx)),
    queryGroupModalSetNode: (andIdx) =>
      dispatch(queryGroupModalSetNode(andIdx)),
    expandPreviousQuery: (datasetId, rootConcepts, groups, queryId) => {
      dispatch(expandPreviousQuery(rootConcepts, groups));

      dispatch(loadAllPreviousQueriesInGroups(groups, datasetId));

      dispatch(replace(toQuery(datasetId, queryId)));
    },
    showConceptListDetails: (andIdx, orIdx) => dispatch(showConceptListDetails(andIdx, orIdx)),
    hideConceptListDetails: () => dispatch(hideConceptListDetails()),
    loadPreviousQuery: (datasetId, queryId) =>
      dispatch(loadPreviousQuery(datasetId, queryId)),
  };
}

const mergeProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  loadPreviousQuery: (queryId) =>
    dispatchProps.loadPreviousQuery(
      ownProps.selectedDatasetId,
      queryId
    ),
  expandPreviousQuery: (groups, queryId) =>
    dispatchProps.expandPreviousQuery(
      ownProps.selectedDatasetId,
      stateProps.rootConcepts,
      groups,
      queryId
    ),
});

export default connect(mapStateToProps, mapDispatchToProps, mergeProps)(Query);
