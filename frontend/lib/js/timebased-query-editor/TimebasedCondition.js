// @flow

import React                        from 'react';
import T                            from 'i18n-react';

import {
  BEFORE,
  BEFORE_OR_SAME,
  DAYS_BEFORE,
  SAME,
  DAYS_OR_NO_EVENT_BEFORE,
}                                   from '../common/constants/timebasedQueryOperatorTypes'
import { isEmpty }                  from '../common/helpers';

import {
  VerticalToggleButton,
} from '../form';

import TimebasedQueryEditorDropzone from './TimebasedQueryEditorDropzone';
import TimebasedConditionDayRange   from './TimebasedConditionDayRange';
import TimebasedNode                from './TimebasedNode';

type PropsType = {
  condition: Object,
  conditionIdx: number,
  indexResult: ?(number | string),
  removable: boolean,
  onRemove: Function,
  onSetOperator: Function,
  onDropTimebasedNode: Function,
  onSetTimebasedNodeTimestamp: Function,
  onRemoveTimebasedNode: Function,
  onSetTimebasedIndexResult: Function,
  onSetTimebasedConditionMinDays: Function,
  onSetTimebasedConditionMaxDays: Function,
  onSetTimebasedConditionMinDaysOrNoEvent: Function,
  onSetTimebasedConditionMaxDaysOrNoEvent: Function,
};

const TimebasedCondition = (props: PropsType) => {
  const minDays = !isEmpty(props.condition.minDays) ? props.condition.minDays : '';
  const maxDays = !isEmpty(props.condition.maxDays) ? props.condition.maxDays : '';
  const minDaysOrNoEvent =
    !isEmpty(props.condition.minDaysOrNoEvent) ? props.condition.minDaysOrNoEvent : '';
  const maxDaysOrNoEvent =
    !isEmpty(props.condition.maxDaysOrNoEvent) ? props.condition.maxDaysOrNoEvent : '';

  const createTimebasedResult = (idx) => {
    return props.condition[`result${idx}`]
      ? <TimebasedNode
          node={props.condition[`result${idx}`]}
          conditionIdx={props.conditionIdx}
          resultIdx={idx}
          isIndexResult={props.condition[`result${idx}`].id === props.indexResult}
          position={idx === 0 ? "left" : "right"}
          onRemove={() => props.onRemoveTimebasedNode(idx, false)}
          onSetTimebasedNodeTimestamp={(timestamp) => {
            props.onSetTimebasedNodeTimestamp(idx, timestamp);
          }}
          onSetTimebasedIndexResult={() => {
            props.onSetTimebasedIndexResult(props.condition[`result${idx}`].id);
          }}
          isIndexResultDisabled={idx === 0 && props.condition.operator === DAYS_OR_NO_EVENT_BEFORE}
        />
      : <TimebasedQueryEditorDropzone
          onDropNode={(node, moved) => props.onDropTimebasedNode(idx, node, moved)}
        />;
  };

  const result0 = createTimebasedResult(0);
  const result1 = createTimebasedResult(1);

  return (
    <div className="timebased-condition">
      <div className="timebased-condition__background" />
      {
        props.removable &&
        <span
          onClick={props.onRemove}
          className="timebased-condition__remove-btn btn btn--icon"
        >
          <i className="fa fa-close" />
        </span>
      }
      <div className="timebased-condition__nodes-container">
        <div className="timebased-condition__horizontal-line" />
        <div className="timebased-condition__nodes">
          { result0 }
          <div className="timebased-condition__operator">
            <VerticalToggleButton
              onToggle={props.onSetOperator}
              activeValue={props.condition.operator}
              options={[
                {
                  label: T.translate('timebasedQueryEditor.opBefore'),
                  value: BEFORE
                }, {
                  label: T.translate('timebasedQueryEditor.opBeforeOrSame'),
                  value: BEFORE_OR_SAME
                }, {
                  label: T.translate('timebasedQueryEditor.opDays'),
                  value: DAYS_BEFORE
                }, {
                  label: T.translate('timebasedQueryEditor.opSame'),
                  value: SAME
                }, {
                  label: T.translate('timebasedQueryEditor.opDaysOrNoEventBefore'),
                  value: DAYS_OR_NO_EVENT_BEFORE
                },
              ]}
            />
            </div>
          { result1 }
        </div>
      </div>
      {
        props.condition.operator === DAYS_BEFORE &&
        <TimebasedConditionDayRange
          minDays={minDays}
          maxDays={maxDays}
          onSetTimebasedConditionMinDays={props.onSetTimebasedConditionMinDays}
          onSetTimebasedConditionMaxDays={props.onSetTimebasedConditionMaxDays}
        />
      }
      {
        props.condition.operator === DAYS_OR_NO_EVENT_BEFORE &&
        <TimebasedConditionDayRange
          minDays={minDaysOrNoEvent}
          maxDays={maxDaysOrNoEvent}
          onSetTimebasedConditionMinDays={props.onSetTimebasedConditionMinDaysOrNoEvent}
          onSetTimebasedConditionMaxDays={props.onSetTimebasedConditionMaxDaysOrNoEvent}
        />
      }
    </div>
  );
};

export default TimebasedCondition;
