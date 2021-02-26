import { StandardQueryStateT } from "../standard-query-editor/queryReducer";
import { TimebasedQueryStateT } from "../timebased-query-editor/reducer";
import {
  useGetQuery,
  useDeleteQuery,
  usePostFormQueries,
  usePostQueries,
} from "../api/api";
import type {
  DatasetIdT,
  GetQueryErrorResponseT,
  GetQueryResponseDoneT,
  QueryIdT,
} from "../api/types";
import { defaultError, defaultSuccess, ErrorObject } from "../common/actions";
import { useLoadPreviousQueries } from "../previous-queries/list/actions";
import { QUERY_AGAIN_TIMEOUT } from "./constants";
import {
  START_QUERY_START,
  START_QUERY_SUCCESS,
  START_QUERY_ERROR,
  STOP_QUERY_SUCCESS,
  STOP_QUERY_ERROR,
  STOP_QUERY_START,
  QUERY_RESULT_START,
  QUERY_RESULT_RESET,
  QUERY_RESULT_ERROR,
  QUERY_RESULT_SUCCESS,
} from "./actionTypes";
import { useDispatch } from "react-redux";

/*
  This implements a polling mechanism,
  because queries are running sometimes longer than 10s.
  (we're using "long polling", where the backend delays the response)

  The polling works like:
  - Start the query (POST request)
    - From the response, get an ID when query was successfully started
  - Continuously poll (GET request) for the query results using that ID
  - Stop polling once the status is DONE, CANCELED or FAILED

  Also, there's a possibility to stop a query while it's running,
  by sending a DELETE request for that query ID
*/

export type QueryTypeT =
  | "standard"
  | "external"
  | "timebased"
  | "externalForms";

const startQueryStart = (queryType: QueryTypeT) => ({
  type: START_QUERY_START,
  payload: { queryType },
});
const startQueryError = (queryType: QueryTypeT, err: ErrorObject) =>
  defaultError(START_QUERY_ERROR, err, { queryType });
const startQuerySuccess = (queryType: QueryTypeT, res: any) =>
  defaultSuccess(START_QUERY_SUCCESS, res, { queryType });

export const useStartQuery = (queryType: QueryTypeT) => {
  const dispatch = useDispatch();
  const queryResult = useQueryResult(queryType);
  const postQueries = usePostQueries();
  const postFormQueries = usePostFormQueries();

  return (
    datasetId: DatasetIdT,
    query: StandardQueryStateT | TimebasedQueryStateT,
    {
      formQueryTransformation,
      selectedSecondaryId,
    }: {
      formQueryTransformation?: Function;
      selectedSecondaryId?: string | null;
    } = {}
  ) => {
    dispatch(startQueryStart(queryType));

    const apiMethod = formQueryTransformation
      ? () => postFormQueries(datasetId, query, { formQueryTransformation })
      : () =>
          postQueries(datasetId, query, {
            queryType,
            selectedSecondaryId,
          });

    return apiMethod().then(
      (r) => {
        dispatch(startQuerySuccess(queryType, r));

        const queryId = r.id;

        return queryResult(datasetId, queryId);
      },
      (e) => dispatch(startQueryError(queryType, e))
    );
  };
};

const stopQueryStart = (queryType: QueryTypeT) => ({
  type: STOP_QUERY_START,
  payload: { queryType },
});
const stopQueryError = (queryType: QueryTypeT, err: ErrorObject) =>
  defaultError(STOP_QUERY_ERROR, err, { queryType });
const stopQuerySuccess = (queryType: QueryTypeT, res: any) =>
  defaultSuccess(STOP_QUERY_SUCCESS, res, { queryType });

export const useStopQuery = (queryType: QueryTypeT) => {
  const dispatch = useDispatch();
  const deleteQuery = useDeleteQuery();

  return (datasetId: DatasetIdT, queryId: QueryIdT) => {
    dispatch(stopQueryStart(queryType));

    return deleteQuery(datasetId, queryId).then(
      (r) => dispatch(stopQuerySuccess(queryType, r)),
      (e) => dispatch(stopQueryError(queryType, e))
    );
  };
};

const queryResultStart = (queryType: QueryTypeT) => ({
  type: QUERY_RESULT_START,
  payload: { queryType },
});

export const queryResultReset = (queryType: QueryTypeT) => ({
  type: QUERY_RESULT_RESET,
  payload: { queryType },
});

const queryResultError = (
  queryType: QueryTypeT,
  e: GetQueryErrorResponseT | Error
) => {
  if (e instanceof Error)
    return defaultError(QUERY_RESULT_ERROR, e, { queryType });

  // TODO: Refactor and get rid of defaultError, it's too generic
  return defaultError(QUERY_RESULT_ERROR, e, {
    error: e.error,
    queryType,
  });
};
const queryResultSuccess = (
  queryType: QueryTypeT,
  res: GetQueryResponseDoneT,
  datasetId: DatasetIdT
) => defaultSuccess(QUERY_RESULT_SUCCESS, res, { datasetId, queryType });

const useQueryResult = (queryType: QueryTypeT) => {
  const dispatch = useDispatch();
  const getQuery = useGetQuery();
  const loadPreviousQueries = useLoadPreviousQueries();

  const queryResult = (datasetId: DatasetIdT, queryId: QueryIdT) => {
    dispatch(queryResultStart(queryType));

    return getQuery(datasetId, queryId).then(
      (r) => {
        // Indicate that looking for the result has stopped,
        // but not necessarily succeeded
        dispatch(queryResultReset(queryType));

        if (r.status === "DONE") {
          dispatch(queryResultSuccess(queryType, r, datasetId));

          // Now there should be a new result that can be queried
          loadPreviousQueries(datasetId);
        } else if (r.status === "CANCELED") {
        } else if (r.status === "FAILED") {
          dispatch(queryResultError(queryType, r));
        } else {
          // Try again after a short time:
          //   Use the "long polling" strategy, where we assume that the
          //   backend blocks the request for a couple of seconds and waits
          //   for the query comes back.
          //   If it doesn't come back the request resolves and
          //   we - the frontend - try again almost instantly.
          setTimeout(
            () => queryResult(datasetId, queryId),
            QUERY_AGAIN_TIMEOUT
          );
        }
      },
      (e: Error) => dispatch(queryResultError(queryType, e))
    );
  };

  return queryResult;
};
