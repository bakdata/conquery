import React, { FC } from "react";
import { useSelector } from "react-redux";
import { StateT } from "app-types";

import { DatasetIdT, QueryIdT } from "../api/types";
import { QueryRunnerStateT } from "../query-runner/reducer";
import { useStartQuery, useStopQuery } from "../query-runner/actions";
import QueryRunner from "../query-runner/QueryRunner";

import { allConditionsFilled } from "./helpers";
import { TimebasedQueryStateT } from "./reducer";

const selectIsButtonEnabled = (
  datasetId: DatasetIdT,
  queryRunner: QueryRunnerStateT | null
) => (state: StateT) => {
  if (!queryRunner) return false;

  return !!(
    datasetId !== null &&
    !queryRunner.startQuery.loading &&
    !queryRunner.stopQuery.loading &&
    allConditionsFilled(state.timebasedQueryEditor.timebasedQuery)
  );
};

interface PropsT {
  datasetId: DatasetIdT;
}

const TimebasedQueryRunner: FC<PropsT> = ({ datasetId }) => {
  const queryRunner = useSelector<StateT, QueryRunnerStateT>(
    (state) => state.timebasedQueryEditor.timebasedQueryRunner
  );
  const isButtonEnabled = useSelector<StateT, boolean>(
    selectIsButtonEnabled(datasetId, queryRunner)
  );
  const isQueryRunning = !!queryRunner.runningQuery;
  // Following ones only needed in dispatch functions
  const queryId = useSelector<StateT, QueryIdT | null>(
    (state) => state.timebasedQueryEditor.timebasedQueryRunner.runningQuery
  );
  const query = useSelector<StateT, TimebasedQueryStateT>(
    (state) => state.timebasedQueryEditor.timebasedQuery
  );

  const startTimebasedQuery = useStartQuery("timebased");
  const stopTimebasedQuery = useStopQuery("timebased");

  const startQuery = () => startTimebasedQuery(datasetId, query);
  const stopQuery = () => {
    if (queryId) {
      stopTimebasedQuery(datasetId, queryId);
    }
  };

  return (
    <QueryRunner
      queryRunner={queryRunner}
      isButtonEnabled={isButtonEnabled}
      isQueryRunning={isQueryRunning}
      startQuery={startQuery}
      stopQuery={stopQuery}
    />
  );
};

export default TimebasedQueryRunner;
