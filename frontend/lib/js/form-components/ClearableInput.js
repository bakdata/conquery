// @flow

import React                from 'react';
import T                    from 'i18n-react';
import NumberFormat         from 'react-number-format';

import { isEmpty }          from '../common/helpers';
import { MONEY_RANGE }      from './filterTypes';

type PropsType = {
  inputType: string,
  valueType?: string,
  placeholder?: string,
  value: ?(number | string),
  formattedValue?: string,
  inputProps?: Object,
  onChange: Function,
};

type NumberFormatValueType = {
  floatValue:number,
  formattedValue: string,
  value: string
};

const ClearableInput = (props: PropsType) => {
  return (
    <span className="clearable-input">
      {
        props.valueType === MONEY_RANGE
        ? <NumberFormat
            prefix={T.translate('moneyRange.prefix')}
            thousandSeparator={T.translate('moneyRange.thousandSeparator')}
            decimalSeparator={T.translate('moneyRange.decimalSeparator')}
            decimalScale={parseInt(T.translate('moneyRange.decimalScale'))}
            className="clearable-input__input"
            placeholder={props.placeholder}
            type={props.inputType}
            onValueChange={(values: NumberFormatValueType) => {
              const { formattedValue, floatValue } = values;
              const parsed = Math.round(floatValue * (T.translate('moneyRange.factor') || 1))

              props.onChange(parsed, formattedValue);
            }}
            value={props.formattedValue}
            {...props.inputProps}
          />
        : <input
            className="clearable-input__input"
            placeholder={props.placeholder}
            type={props.inputType}
            onChange={(e) => props.onChange(e.target.value)}
            value={props.value}
            {...props.inputProps}
          />
      }
      {
        !isEmpty(props.value) &&
        <span
          className="clearable-input__clear-zone"
          title={T.translate('common.clearValue')}
          aria-label={T.translate('common.clearValue')}
          onClick={() => props.onChange('')}
        >
          ×
        </span>
      }
    </span>
  );
};

export default ClearableInput;
