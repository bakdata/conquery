import styled from "@emotion/styled";
import React, { FC } from "react";
import { useTranslation } from "react-i18next";

import type { ConceptIdT } from "../api/types";
import { getConceptById } from "../concept-trees/globalTreeStoreHelper";
import { Heading3, Heading4 } from "../headings/Headings";
import type {
  DragItemConceptTreeNode,
  StandardQueryNodeT,
} from "../standard-query-editor/types";

import ConceptDropzone from "./ConceptDropzone";
import ConceptEntry from "./ConceptEntry";
import MenuColumnItem from "./MenuColumnItem";
import { QueryNodeEditorStateT } from "./reducer";

const FixedColumn = styled("div")<{ isEmpty?: boolean }>`
  display: flex;
  flex-direction: column;
  width: ${({ isEmpty }) => (isEmpty ? "200px" : "270px")};
  overflow: hidden;
  flex-shrink: 0;
  flex-grow: 1;

  &:first-of-type {
    border-right: 1px solid ${({ theme }) => theme.col.grayLight};
  }
`;

const Padded = styled("div")`
  padding: 0 15px 15px;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
`;

const HeadingBetween = styled(Heading4)`
  margin: 15px 15px 0;
`;
const Heading4Highlighted = styled(Heading4)`
  color: ${({ theme }) => theme.col.blueGrayDark};
  font-weight: 700;
  margin: 10px 0 5px;
`;

const DimmedNote = styled(Heading3)`
  color: ${({ theme }) => theme.col.grayLight};
  padding: 15px;
  font-weight: 400;
`;

interface PropsT {
  className?: string;

  node: StandardQueryNodeT;
  editorState: QueryNodeEditorStateT;
  showTables: boolean;
  allowlistedTables?: string[];
  blocklistedTables?: string[];

  onDropConcept: (node: DragItemConceptTreeNode) => void;
  onRemoveConcept: (conceptId: ConceptIdT) => void;
  onToggleTable: (tableIdx: number, isExcluded: boolean) => void;
  onSelectInputTableView: (tableIdx: number) => void;
  onResetTable: (tableIdx: number) => void;
}

const MenuColumn: FC<PropsT> = ({
  className,
  node,
  editorState,
  showTables,
  blocklistedTables,
  allowlistedTables,
  onDropConcept,
  onRemoveConcept,
  onToggleTable,
  onSelectInputTableView,
  onResetTable,
}) => {
  const { t } = useTranslation();
  const isOnlyOneTableIncluded =
    !node.isPreviousQuery &&
    node.tables.filter((table) => !table.exclude).length === 1;

  const rootConcept = !node.isPreviousQuery ? getConceptById(node.tree) : null;

  const isEmpty =
    node.isPreviousQuery ||
    (!showTables &&
      (!rootConcept ||
        !rootConcept.children ||
        rootConcept.children.length === 0));

  return (
    <FixedColumn className={className} isEmpty={isEmpty}>
      {isEmpty && (
        <DimmedNote>{t("queryNodeEditor.emptyMenuColumn")}</DimmedNote>
      )}
      {!node.isPreviousQuery && showTables && (
        <div>
          <HeadingBetween>
            {t("queryNodeEditor.conceptNodeTables")}
          </HeadingBetween>
          {node.tables.map((table, tableIdx) => (
            <MenuColumnItem
              key={tableIdx}
              table={table}
              isActive={editorState.selectedInputTableIdx === tableIdx}
              isOnlyOneTableIncluded={isOnlyOneTableIncluded}
              blocklistedTables={blocklistedTables}
              allowlistedTables={allowlistedTables}
              onClick={() => onSelectInputTableView(tableIdx)}
              onToggleTable={(value) => onToggleTable(tableIdx, value)}
              onResetTable={() => onResetTable(tableIdx)}
            />
          ))}
        </div>
      )}
      {!node.isPreviousQuery &&
        rootConcept &&
        rootConcept.children &&
        rootConcept.children.length > 0 && (
          <>
            <HeadingBetween>
              {t("queryNodeEditor.dropMoreConcepts")}
            </HeadingBetween>
            <Padded>
              <Heading4Highlighted>{rootConcept.label}</Heading4Highlighted>
              <div>
                <ConceptDropzone node={node} onDropConcept={onDropConcept} />
              </div>
              <div>
                {node.ids.map((conceptId) => (
                  <ConceptEntry
                    key={conceptId}
                    node={getConceptById(conceptId)}
                    conceptId={conceptId}
                    canRemoveConcepts={node.ids.length > 1}
                    onRemoveConcept={onRemoveConcept}
                  />
                ))}
              </div>
            </Padded>
          </>
        )}
    </FixedColumn>
  );
};

export default MenuColumn;
