// @flow

import { T } from "conquery/lib/js/localization";
import { isEmpty } from "conquery/lib/js/common/helpers";

export const validateRequired = (value: any): ?string => {
  return isEmpty(value)
    ? T.translate("externalForms.formValidation.isRequired")
    : null;
};

export const validatePositive = (value: any) => {
  return value > 0
    ? null
    : T.translate("externalForms.formValidation.mustBePositiveNumber");
};

export const validateDateRange = (value: {
  min: string,
  max: string
}): ?string => {
  if (!value) return T.translate("externalForms.formValidation.isRequired");

  if (!value.min || !value.max)
    return T.translate("externalForms.formValidation.isRequired");
  if (value.max < value.min)
    return T.translate("externalForms.formValidation.invalidDateRange");

  return null;
};

export const validateConceptGroupFilled = (
  group: { concepts: [] }[]
): ?string => {
  if (!group || group.length === 0)
    return T.translate("externalForms.formValidation.isRequired");

  return group.some(e => e.concepts.length === 0 || e.concepts.some(c => !c))
    ? T.translate("externalForms.formValidation.isRequired")
    : null;
};
