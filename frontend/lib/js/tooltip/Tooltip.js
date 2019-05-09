// @flow

import React from "react";
import styled from "@emotion/styled";
import type { Dispatch } from "redux-thunk";
import T from "i18n-react";
import { connect } from "react-redux";
import Markdown from "react-markdown";
import Highlighter from "react-highlight-words";

import IconButton from "../button/IconButton";
import FaIcon from "../icon/FaIcon";
import type { SearchType } from "../category-trees/reducer";

import ActivateTooltip from "./ActivateTooltip";
import { toggleDisplayTooltip } from "./actions";
import type { AdditionalInfosType } from "./reducer";
import TooltipEntries from "./TooltipEntries";

type PropsType = {
  additionalInfos: AdditionalInfosType,
  displayTooltip: boolean,
  toggleAdditionalInfos: boolean,
  toggleDisplayTooltip: Function,
  search: SearchType
};

const Root = styled("div")`
  width: 100%;
  height: 100%;
  padding: 50px 0 10px;
  display: flex;
  flex-direction: column;
  background-color: ${({ theme }) => theme.col.bgAlt};
`;

const Header = styled("h2")`
  background-color: white;
  height: 47px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #ccc;
  margin: 0 0 5px;
  padding: 0 20px;
  font-size: ${({ theme }) => theme.font.sm};
  letter-spacing: 1px;
  line-height: 38px;
  text-transform: uppercase;
  color: ${({ theme }) => theme.col.blueGrayDark};
`;
const Content = styled("div")`
  padding: 10px 20px;
  width: 100%;
  height: 100%;
  overflow: auto;
`;
const Head = styled("div")`
  padding: 10px;
  background-color: white;
  border-radius: ${({ theme }) => theme.borderRadius};
  border: 1px solid ${({ theme }) => theme.col.gray};
  margin: 10px 0 15px;
`;

const StyledFaIcon = styled(FaIcon)`
  margin-right: 12px;
`;
const PinnedLabel = styled("p")`
  display: flex;
  flex-direction: row;
  align-items: center;
  margin: 0;
  line-height: 1.2;
  font-size: ${({ theme }) => theme.font.sm};
`;

const Description = styled("p")`
  margin: 5px 0 2px;
  font-size: ${({ theme }) => theme.font.xs};
  line-height: 1;
  text-transform: uppercase;
`;

const PieceOfInfo = styled("div")`
  margin-bottom: 15px;

  /* Markdown */
  font-size: ${({ theme }) => theme.font.xs};

  table {
    border-collapse: collapse;
  }
  td {
    border: 1px solid ${({ theme }) => theme.col.gray};
    padding: 5px;
  }
`;

const InfoHeadline = styled("h4")`
  margin: 0;
  font-size: ${({ theme }) => theme.font.xs};
  font-weight: 700;
  line-height: 1.3;
`;

const StyledIconButton = styled(IconButton)`
  position: absolute;
  top: 0;
  right: 20px;
  border-top: 0;
  border-top-left-radius: 0;
  border-top-right-radius: 0;
`;

const Tooltip = (props: PropsType) => {
  if (!props.displayTooltip) return <ActivateTooltip />;

  const {
    additionalInfos,
    toggleDisplayTooltip,
    toggleAdditionalInfos
  } = props;
  const {
    label,
    description,
    infos,
    matchingEntries,
    dateRange
  } = additionalInfos;

  const searchHighlight = text => {
    return (
      <Highlighter
        searchWords={props.search.words || []}
        autoEscape={true}
        textToHighlight={text || ""}
      />
    );
  };

  return (
    <Root>
      <Header>{T.translate("tooltip.headline")}</Header>
      <Content>
        <TooltipEntries
          matchingEntries={matchingEntries}
          dateRange={dateRange}
        />
        {label && (
          <Head>
            <PinnedLabel>
              {toggleAdditionalInfos && <StyledFaIcon icon="thumbtack" />}
              {searchHighlight(label)}
            </PinnedLabel>
            {description && (
              <Description>{searchHighlight(description)}</Description>
            )}
          </Head>
        )}
        {infos &&
          infos.map((info, i) => (
            <PieceOfInfo key={info.key}>
              <InfoHeadline>{searchHighlight(info.key)}</InfoHeadline>
              <Markdown source={info.value} escapeHtml={true} />
            </PieceOfInfo>
          ))}
      </Content>
    </Root>
  );
};
// <StyledIconButton
//   small
//   frame
//   onClick={toggleDisplayTooltip}
//   icon="angle-down"
// />

const mapStateToProps = state => {
  return {
    additionalInfos: state.tooltip.additionalInfos,
    displayTooltip: state.tooltip.displayTooltip,
    toggleAdditionalInfos: state.tooltip.toggleAdditionalInfos,
    search: state.categoryTrees.search
  };
};

const mapDispatchToProps = (dispatch: Dispatch) => ({
  toggleDisplayTooltip: () => dispatch(toggleDisplayTooltip())
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Tooltip);
