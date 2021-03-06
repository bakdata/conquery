import { tabDescription } from ".";
import { StateT } from "app-types";
import React, { useEffect } from "react";
import { useSelector, useStore } from "react-redux";

import { useGetForms } from "../api/api";
import { DatasetIdT } from "../api/types";
import StandardQueryEditorTab from "../standard-query-editor";
import { updateReducers } from "../store";
import TimebasedQueryEditorTab from "../timebased-query-editor";

import FormsContainer from "./FormsContainer";
import FormsNavigation from "./FormsNavigation";
import FormsQueryRunner from "./FormsQueryRunner";
import buildExternalFormsReducer from "./reducer";

const FormsTab = () => {
  const store = useStore();
  const getForms = useGetForms();
  const datasetId = useSelector<StateT, DatasetIdT | null>(
    (state) => state.datasets.selectedDatasetId,
  );

  useEffect(() => {
    async function loadForms() {
      if (!datasetId) {
        return;
      }

      const configuredForms = await getForms(datasetId);

      const forms = configuredForms.reduce((all, form) => {
        all[form.type] = form;

        return all;
      }, {});

      const externalFormsReducer = buildExternalFormsReducer(forms);

      const tabs = [
        StandardQueryEditorTab,
        TimebasedQueryEditorTab,
        {
          ...tabDescription,
          reducer: externalFormsReducer,
        },
      ];

      updateReducers(store, tabs);
    }

    loadForms();
  }, [store, datasetId]);

  return (
    <>
      <FormsNavigation />
      <FormsContainer />
      <FormsQueryRunner />
    </>
  );
};

export default FormsTab;
