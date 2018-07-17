// @flow

import { connect }          from 'react-redux';
import { type Dispatch }    from 'redux-thunk';

import type {
  DateRangeType,
  InfoType
}                           from '../common/types/backend';

import { isEmpty }          from '../common/helpers';
import * as actions         from './actions';
import HoverableBase        from './HoverableBase';

export type AdditionalInfoHoverableNodeType = {
  label: string,
  description: string,
  matchingEntries: number,
  dateRange: DateRangeType,
  additionalInfos: Array<InfoType>
}

// Whitelist the data we pass (especially: don't pass all children)
const additionalInfos = (node: AdditionalInfoHoverableNodeType) => ({
  label: node.label,
  description: node.description,
  matchingEntries: node.matchingEntries,
  dateRange: node.dateRange,
  additionalInfos: node.additionalInfos,
});

// Decorates a component with a hoverable node.
// On mouse enter, additional infos about the component are saved in the state
// The Tooltip (and potential other components) might then update their view.
// On mouse leave, the infos are cleared from the state again
const AdditionalInfoHoverable = (Component: any) => {
  const mapStateToProps = () => ({});

  const mapDispatchToProps = (
    dispatch: Dispatch,
    ownProps: { node: AdditionalInfoHoverableNodeType }
  ) => ({
    onDisplayAdditionalInfos: () => {
      const node = ownProps.node;

      if (!node.additionalInfos && isEmpty(node.matchingEntries)) return;

      dispatch(actions.displayAdditionalInfos(additionalInfos(node)))
    },
    onToggleAdditionalInfos: () => {
      const node = ownProps.node;

      if (!node.additionalInfos && isEmpty(node.matchingEntries)) return;

      dispatch([
        actions.toggleAdditionalInfos(additionalInfos(node)),
        actions.displayAdditionalInfos(additionalInfos(node))
      ])
    },
  });

  return connect(mapStateToProps, mapDispatchToProps)(HoverableBase(Component));
};

export default AdditionalInfoHoverable;
