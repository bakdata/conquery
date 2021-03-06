import React from "react";
import { TFunction, useTranslation } from "react-i18next";
import { connect } from "react-redux";
import { reduxForm, formValueSelector } from "redux-form";

import type { SelectOptionT } from "../../api/types";
import { useActiveLang } from "../../localization/useActiveLang";
import FormsHeader from "../FormsHeader";
import type {
  Form as FormType,
  FormField as FormFieldType,
} from "../config-types";
import { collectAllFormFields, isFormField } from "../helper";
import { selectReduxFormState } from "../stateSelectors";
import {
  validateRequired,
  validateDateRange,
  validatePositive,
  validateConceptGroupFilled,
  validateDateRangeRequired,
} from "../validators";

import Field from "./Field";

const DEFAULT_VALUE_BY_TYPE = {
  STRING: "",
  NUMBER: null,
  CHECKBOX: false,
  CONCEPT_LIST: [],
  RESULT_GROUP: null,
  MULTI_RESULT_GROUP: [],
  SELECT: null,
  TABS: null,
  DATASET_SELECT: null,
  MULTI_SELECT: null,
  DATE_RANGE: {
    min: null,
    max: null,
  },
};

const DEFAULT_VALIDATION_BY_TYPE = {
  STRING: null,
  NUMBER: null,
  CHECKBOX: null,
  CONCEPT_LIST: null,
  RESULT_GROUP: null,
  MULTI_RESULT_GROUP: null,
  SELECT: null,
  TABS: null,
  DATASET_SELECT: null,
  MULTI_SELECT: null,
  DATE_RANGE: validateDateRange,
};

function getNotEmptyValidation(fieldType: string) {
  switch (fieldType) {
    case "CONCEPT_LIST":
      return validateConceptGroupFilled;
    case "DATE_RANGE":
      return validateDateRangeRequired;
    default:
      return validateRequired;
  }
}

function getPossibleValidations(fieldType: string) {
  const notEmptyValidation = {
    NOT_EMPTY: getNotEmptyValidation(fieldType),
  };

  return {
    ...notEmptyValidation,
    GREATER_THAN_ZERO: validatePositive,
  };
}

function getInitialValue(field: FormFieldType) {
  return field.defaultValue || DEFAULT_VALUE_BY_TYPE[field.type];
}

function getErrorForField(t: TFunction, field: FormFieldType, value: any) {
  const defaultValidation = DEFAULT_VALIDATION_BY_TYPE[field.type];

  let error = defaultValidation ? defaultValidation(t, value) : null;

  if (!!field.validations && field.validations.length > 0) {
    for (let validation of field.validations) {
      const validateFn = getPossibleValidations(field.type)[validation];

      if (validateFn) {
        // If not, someone must have configured an unsupported validation
        error = error || validateFn(t, value);
      }
    }
  }

  return error;
}

interface ConfiguredFormPropsType {
  config: FormType;
}

interface PropsType {
  onSubmit: Function;
  getFieldValue: (fieldName: string) => any;
  availableDatasets: SelectOptionT[];
}

// This is the generic form component that receives a form config
// and builds all fields from there.
//
// Note: The config contains the fields in a hierarchical structure,
//       because one of the fields is a "TAB", which contains subfields
//       depending on the tab, that is selected
//
// The form works with `redux-form``
const ConfiguredForm = ({ config, ...props }: ConfiguredFormPropsType) => {
  const { t } = useTranslation();

  const Form = ({ onSubmit, getFieldValue, availableDatasets }: PropsType) => {
    const activeLang = useActiveLang();

    return (
      <form>
        <FormsHeader headline={config.headline[activeLang]} />
        {config.fields.map((field, i) => {
          const key = isFormField(field) ? field.name : field.type + i;

          return (
            <Field
              key={key}
              formType={config.type}
              getFieldValue={getFieldValue}
              field={field}
              availableDatasets={availableDatasets}
              locale={activeLang}
            />
          );
        })}
      </form>
    );
  };

  const allFields = collectAllFormFields(config.fields);
  const fieldValueSelector = formValueSelector(
    config.type,
    selectReduxFormState,
  );

  const ReduxFormConnectedForm = reduxForm({
    form: config.type,
    getFormState: selectReduxFormState,
    initialValues: allFields.reduce((allValues, field) => {
      allValues[field.name] = getInitialValue(field);

      return allValues;
    }, {}),
    destroyOnUnmount: false,
    validate: (values) =>
      Object.keys(values).reduce((errors, name) => {
        const field = allFields.find((field) => field.name === name);

        // Note: For some reason, redux form understands, that:
        //       EVEN IF we add errors for ALL fields –
        //       including those fields that are not shown,
        //       because their tab is hidden – as long as those
        //       fields are not "rendered", the form seems to be valid
        //
        // => Otherwise, we'd have to check which tab is selected here,
        //    and which errors to add
        const error = getErrorForField(t, field, values[name]);

        if (error) {
          errors[name] = error;
        }

        return errors;
      }, {}),
  })(Form);

  const mapStateToProps = (state) => ({
    getFieldValue: (field) => fieldValueSelector(state, field),
    availableDatasets: state.datasets.data.map((dataset) => ({
      label: dataset.label,
      value: dataset.id,
    })),
  });

  const ReduxConnectedForm = connect(mapStateToProps)(ReduxFormConnectedForm);

  return <ReduxConnectedForm {...props} />;
};

export default ConfiguredForm;
