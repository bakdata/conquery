// @flow

import React from "react";
import { findDOMNode } from "react-dom";
import styled from "@emotion/styled";
import { css } from "@emotion/core";

import T from "i18n-react";
import { DragSource } from "react-dnd";
import { connect } from "react-redux";
import { parseISO, formatDistance } from "date-fns";

import { getDateLocale } from "../../localization";

import { ErrorMessage } from "../../error-message";
import { dndTypes } from "../../common/constants";
import { SelectableLabel } from "../../selectable-label";
import { isEmpty } from "../../common/helpers/commonHelper";

import DownloadButton from "../../button/DownloadButton";
import IconButton from "../../button/IconButton";
import FaIcon from "../../icon/FaIcon";
import WithTooltip from "../../tooltip/WithTooltip";

import { EditableText, EditableTags } from "../../form-components";

import { deletePreviousQueryModalOpen } from "../delete-modal/actions";

import { type DraggedQueryType } from "../../standard-query-editor/types";

import {
  toggleSharePreviousQuery,
  renamePreviousQuery,
  retagPreviousQuery,
  toggleEditPreviousQueryLabel,
  toggleEditPreviousQueryTags
} from "./actions";

import PreviousQueryTags from "./PreviousQueryTags";
import ReexecuteButton from "./ReexecuteButton";

const nodeSource = {
  beginDrag(props, monitor, component): DraggedQueryType {
    const { width, height } = findDOMNode(component).getBoundingClientRect();
    // Return the data describing the dragged item
    return {
      width,
      height,
      id: props.query.id,
      label: props.query.label,
      isPreviousQuery: true
    };
  }
};

// These props get injected into the component
function collect(connect, monitor) {
  return {
    connectDragSource: connect.dragSource(),
    isDragging: monitor.isDragging()
  };
}

const Root = styled("div")`
  margin: 0;
  padding: 5px 10px;
  cursor: pointer;
  border-radius: ${({ theme }) => theme.borderRadius};
  border: 1px solid ${({ theme }) => theme.col.grayLight};
  background-color: ${({ theme }) => theme.col.bg};
  box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.2);

  border-left: ${({ theme, own, system }) =>
    own
      ? `4px solid ${theme.col.orange}`
      : system
      ? `4px solid ${theme.col.blueGrayDark}`
      : `1px solid ${theme.col.grayLight}`};

  &:hover {
    ${({ theme, own, system }) =>
      !own &&
      !system &&
      css`
        border-left-color: ${theme.col.blueGray};
      `};
    border-top-color: ${({ theme }) => theme.col.blueGray};
    border-right-color: ${({ theme }) => theme.col.blueGray};
    border-bottom-color: ${({ theme }) => theme.col.blueGray};
  }
`;

const Gray = styled("div")`
  color: ${({ theme }) => theme.col.gray};
`;
const TopInfos = styled(Gray)`
  line-height: 24px;
`;

const TopRight = styled("div")`
  float: right;
`;
const SharedIndicator = styled("span")`
  margin-left: 10px;
  color: ${({ theme }) => theme.col.blueGray};
`;
const StyledSelectableLabel = styled(SelectableLabel)`
  margin: 0;
  font-weight: 400;
  word-break: break-word;
`;
const StyledEditableText = styled(EditableText)`
  margin: 0;
  font-weight: 400;
  word-break: break-word;
`;
const MiddleRow = styled("div")`
  display: flex;
  width: 100%;
  justify-content: space-between;
  line-height: 24px;
`;
const StyledErrorMessage = styled(ErrorMessage)`
  margin: 0;
`;

const StyledFaIcon = styled(FaIcon)`
  margin: 0 6px;
`;

const StyledWithTooltip = styled(WithTooltip)`
  margin-left: 10px;
`;

type PropsType = {
  query: {
    id: number | string,
    label: string,
    loading: boolean,
    numberOfResults: number,
    createdAt: string,
    tags: string[],
    own: boolean,
    shared: boolean
  },
  onRenamePreviousQuery: () => void,
  onToggleEditPreviousQueryLabel: () => void,
  onToggleEditPreviousQueryTags: () => void,
  onToggleSharePreviousQuery: () => void,
  onRetagPreviousQuery: () => void,
  onDeletePreviousQuery: () => void,
  connectDragSource: () => void,
  availableTags: string[]
};

// Has to be a class because of https://github.com/react-dnd/react-dnd/issues/530
class PreviousQuery extends React.Component {
  props: PropsType;

  render() {
    const {
      query,
      connectDragSource,
      availableTags,
      onRenamePreviousQuery,
      onDeletePreviousQuery,
      onToggleEditPreviousQueryTags,
      onToggleEditPreviousQueryLabel,
      onRetagPreviousQuery,
      onToggleSharePreviousQuery
    } = this.props;

    const peopleFound = isEmpty(query.numberOfResults)
      ? T.translate("previousQuery.notExecuted")
      : `${query.numberOfResults} ${T.translate("previousQueries.results")}`;
    const dateLocale = getDateLocale();
    const executedAt = formatDistance(parseISO(query.createdAt), new Date(), {
      locale: dateLocale,
      addSuffix: true
    });
    const label = query.label || query.id.toString();
    const mayEditQuery = query.own || query.shared;
    const isNotEditing = !(query.editingLabel || query.editingTags);

    return (
      <Root
        ref={instance => {
          if (isNotEditing) connectDragSource(instance);
        }}
        own={!!query.own}
        shared={!!query.shared}
        system={!!query.system || (!query.own && !query.shared)}
      >
        <TopInfos>
          <div>
            {query.resultUrl ? (
              <WithTooltip text={T.translate("previousQuery.downloadResults")}>
                <DownloadButton tight bare url={query.resultUrl}>
                  {peopleFound}
                </DownloadButton>
              </WithTooltip>
            ) : (
              <WithTooltip text={T.translate("previousQuery.reexecute")}>
                <ReexecuteButton id={query.id}>{peopleFound}</ReexecuteButton>
              </WithTooltip>
            )}
            {query.own && query.shared && (
              <SharedIndicator
                onClick={() => onToggleSharePreviousQuery(!query.shared)}
              >
                {T.translate("previousQuery.shared")}
              </SharedIndicator>
            )}
            <TopRight>
              {executedAt}
              {mayEditQuery &&
                !query.editingTags &&
                (!query.tags || query.tags.length === 0) && (
                  <StyledWithTooltip text={T.translate("previousQuery.addTag")}>
                    <IconButton
                      icon="tags"
                      bare
                      onClick={onToggleEditPreviousQueryTags}
                    />
                  </StyledWithTooltip>
                )}
              {query.own && !query.shared && (
                <StyledWithTooltip text={T.translate("previousQuery.share")}>
                  <IconButton
                    icon="upload"
                    bare
                    onClick={() => onToggleSharePreviousQuery(!query.shared)}
                  />
                </StyledWithTooltip>
              )}
              {query.loading ? (
                <StyledFaIcon icon="spinner" />
              ) : (
                query.own && (
                  <StyledWithTooltip text={T.translate("previousQuery.delete")}>
                    <IconButton
                      icon="times"
                      bare
                      onClick={onDeletePreviousQuery}
                    />
                  </StyledWithTooltip>
                )
              )}
            </TopRight>
          </div>
        </TopInfos>
        <MiddleRow>
          {mayEditQuery ? (
            <StyledEditableText
              loading={!!query.loading}
              text={label}
              selectTextOnMount={true}
              editing={!!query.editingLabel}
              onSubmit={onRenamePreviousQuery}
              onToggleEdit={onToggleEditPreviousQueryLabel}
            />
          ) : (
            <StyledSelectableLabel label={label} />
          )}
          <Gray>{query.ownerName}</Gray>
        </MiddleRow>
        {mayEditQuery ? (
          <EditableTags
            tags={query.tags}
            editing={!!query.editingTags}
            loading={!!query.loading}
            onSubmit={onRetagPreviousQuery}
            onToggleEdit={onToggleEditPreviousQueryTags}
            tagComponent={<PreviousQueryTags tags={query.tags} />}
            availableTags={availableTags}
          />
        ) : (
          <PreviousQueryTags tags={query.tags} />
        )}
        {!!query.error && <StyledErrorMessage message={query.error} />}
      </Root>
    );
  }
}

const mapStateToProps = state => ({
  availableTags: state.previousQueries.tags
});

const mapDispatchToProps = dispatch => ({
  onToggleSharePreviousQuery: (datasetId, queryId, shared) =>
    dispatch(toggleSharePreviousQuery(datasetId, queryId, shared)),

  onRenamePreviousQuery: (datasetId, queryId, label) =>
    dispatch(renamePreviousQuery(datasetId, queryId, label)),

  onRetagPreviousQuery: (datasetId, queryId, tags) =>
    dispatch(retagPreviousQuery(datasetId, queryId, tags)),

  onDeletePreviousQuery: queryId =>
    dispatch(deletePreviousQueryModalOpen(queryId)),

  onToggleEditPreviousQueryLabel: queryId =>
    dispatch(toggleEditPreviousQueryLabel(queryId)),

  onToggleEditPreviousQueryTags: queryId =>
    dispatch(toggleEditPreviousQueryTags(queryId))
});

const mapProps = (stateProps, dispatchProps, ownProps) => ({
  ...stateProps,
  ...dispatchProps,
  ...ownProps,
  onToggleSharePreviousQuery: shared =>
    dispatchProps.onToggleSharePreviousQuery(
      ownProps.datasetId,
      ownProps.query.id,
      shared
    ),
  onRenamePreviousQuery: label =>
    dispatchProps.onRenamePreviousQuery(
      ownProps.datasetId,
      ownProps.query.id,
      label
    ),
  onRetagPreviousQuery: tags =>
    dispatchProps.onRetagPreviousQuery(
      ownProps.datasetId,
      ownProps.query.id,
      tags
    ),
  onDeletePreviousQuery: () =>
    dispatchProps.onDeletePreviousQuery(ownProps.query.id),
  onToggleEditPreviousQueryLabel: () =>
    dispatchProps.onToggleEditPreviousQueryLabel(ownProps.query.id),
  onToggleEditPreviousQueryTags: () =>
    dispatchProps.onToggleEditPreviousQueryTags(ownProps.query.id)
});

export default connect(
  mapStateToProps,
  mapDispatchToProps,
  mapProps
)(DragSource(dndTypes.PREVIOUS_QUERY, nodeSource, collect)(PreviousQuery));
