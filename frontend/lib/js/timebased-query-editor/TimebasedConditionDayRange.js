// @flow

import React from 'react';
import T                            from 'i18n-react';

import {
  InputWithLabel,
} from '../form';


type PropsType = {
  minDays: ?(number | string),
  maxDays: ?(number | string),
  onSetTimebasedConditionMinDays: Function,
  onSetTimebasedConditionMaxDays: Function,
};

const TimebasedConditionDayRange = (props: PropsType) => (
  <div className="timebased-condition__day-range-container">
    <div className="timebased-condition__day-range">
      <InputWithLabel
        inputType="number"
        input={{
          value: props.minDays,
          onChange: (value) => props.onSetTimebasedConditionMinDays(value),
        }}
        className="input-range__input-with-label"
        placeholder={T.translate('common.timeUnitDays')}
        label={T.translate('timebasedQueryEditor.minDaysLabel')}
        tinyLabel
      />
      <InputWithLabel
        inputType="number"
        input={{
          value: props.maxDays,
          onChange: (value) => props.onSetTimebasedConditionMaxDays(value),
        }}
        className="input-range__input-with-label"
        placeholder={T.translate('common.timeUnitDays')}
        label={T.translate('timebasedQueryEditor.maxDaysLabel')}
        tinyLabel
      />
    </div>
  </div>
);

export default TimebasedConditionDayRange;
