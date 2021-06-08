import styled from "@emotion/styled";
import React from "react";

import type {
  CurrencyConfigT,
  DatasetIdT,
  ConceptIdT,
  TableIdT,
  FilterIdT,
} from "../api/types";
import InputPlain from "../form-components/InputPlain";
import InputRange, { ModeT } from "../form-components/InputRange";
import InputSelect from "../form-components/InputSelect";
import type { FilterWithValueType } from "../standard-query-editor/types";

import ResolvableMultiSelect from "./ResolvableMultiSelect";

export interface FiltersContextT {
  datasetId: DatasetIdT;
  treeId: ConceptIdT;
  tableId: TableIdT;
}

interface PropsT {
  className?: string;
  context: FiltersContextT;
  filters: FilterWithValueType[] | null;
  excludeTable?: boolean;
  onSwitchFilterMode: (filterIdx: number, mode: ModeT) => void;
  onSetFilterValue: Function;
  onLoadFilterSuggestions: (
    tableIdx: number,
    filterId: FilterIdT,
    prefix: string,
  ) => void;
  onShowDescription: (filterIdx: number) => void;
  currencyConfig: CurrencyConfigT;
}

const Row = styled("div")`
  margin-bottom: 10px;
`;

const TableFilters = (props: PropsT) => {
  if (!props.filters || props.filters.length === 0) return null;

  return (
    <div>
      {props.filters
        .map((filter, filterIdx) => {
          switch (filter.type) {
            case "SELECT":
              return (
                <InputSelect
                  input={{
                    clearable: filter.value !== filter.defaultValue,
                    defaultValue: filter.defaultValue,
                    value: filter.value,
                    onChange: (value) =>
                      props.onSetFilterValue(filterIdx, value),
                  }}
                  label={filter.label}
                  options={filter.options}
                  disabled={props.excludeTable}
                />
              );
            case "MULTI_SELECT":
              return (
                <ResolvableMultiSelect
                  context={{ ...props.context, filterId: filter.id }}
                  input={{
                    value: filter.value,
                    defaultValue: filter.defaultValue,
                    onChange: (value: string[]) =>
                      props.onSetFilterValue(filterIdx, value),
                  }}
                  label={filter.label}
                  options={filter.options}
                  disabled={props.excludeTable}
                  allowDropFile={!!filter.allowDropFile}
                />
              );
            case "BIG_MULTI_SELECT":
              return (
                <ResolvableMultiSelect
                  context={{ ...props.context, filterId: filter.id }}
                  input={{
                    value: filter.value || [],
                    defaultValue: filter.defaultValue || [],
                    onChange: (value) =>
                      props.onSetFilterValue(filterIdx, value),
                  }}
                  label={filter.label}
                  options={filter.options}
                  disabled={!!props.excludeTable}
                  allowDropFile={!!filter.allowDropFile}
                  isLoading={filter.isLoading}
                  startLoadingThreshold={filter.threshold || 1}
                  onLoad={(prefix: string) =>
                    props.onLoadFilterSuggestions(filterIdx, filter.id, prefix)
                  }
                />
              );
            case "INTEGER_RANGE":
              return (
                <InputRange
                  input={{
                    value: filter.value,
                    defaultValue: filter.defaultValue,
                    onChange: (value) =>
                      props.onSetFilterValue(filterIdx, value),
                  }}
                  limits={{ min: filter.min, max: filter.max }}
                  unit={filter.unit}
                  label={filter.label}
                  mode={filter.mode || "range"}
                  disabled={!!props.excludeTable}
                  onSwitchMode={(mode) =>
                    props.onSwitchFilterMode(filterIdx, mode)
                  }
                  placeholder="-"
                  pattern={filter.pattern}
                />
              );
            case "REAL_RANGE":
              return (
                <InputRange
                  input={{
                    value: filter.value,
                    defaultValue: filter.defaultValue,
                    onChange: (value) =>
                      props.onSetFilterValue(filterIdx, value),
                  }}
                  limits={{ min: filter.min, max: filter.max }}
                  unit={filter.unit}
                  label={filter.label}
                  mode={filter.mode || "range"}
                  stepSize={filter.precision || 0.1}
                  disabled={!!props.excludeTable}
                  onSwitchMode={(mode) =>
                    props.onSwitchFilterMode(filterIdx, mode)
                  }
                  placeholder="-"
                  pattern={filter.pattern}
                />
              );
            case "MONEY_RANGE":
              return (
                <InputRange
                  moneyRange
                  input={{
                    value: filter.value,
                    defaultValue: filter.defaultValue,
                    onChange: (value) =>
                      props.onSetFilterValue(filterIdx, value),
                  }}
                  unit={filter.unit}
                  label={filter.label}
                  mode={filter.mode || "range"}
                  disabled={!!props.excludeTable}
                  onSwitchMode={(mode) =>
                    props.onSwitchFilterMode(filterIdx, mode)
                  }
                  placeholder="-"
                  currencyConfig={props.currencyConfig}
                />
              );
            case "STRING":
              return (
                <InputPlain
                  input={{
                    value: filter.value || "",
                    defaultValue: filter.defaultValue,
                    onChange: (value) =>
                      props.onSetFilterValue(filterIdx, value),
                  }}
                  placeholder="-"
                  label={filter.label}
                />
              );
            default:
              // In the future, there might be other filter types supported
              return null;
          }
        })
        .filter((input) => !!input)
        .map((input, filterIdx) => (
          <Row
            key={filterIdx}
            onFocusCapture={() => props.onShowDescription(filterIdx)}
          >
            {input}
          </Row>
        ))}
    </div>
  );
};

export default TableFilters;
