import { useSelector } from "react-redux";
import { StateT } from "app-types";
import { TreesT } from "./reducer";
import { useMemo } from "react";

const isRootTreeId = (trees: TreesT) => (treeId: string) => {
  // Those that don't have a parent, must be root
  // If they don't have a label, they're loading, or in any other broken state
  return !trees[treeId].parent && trees[treeId].label;
};

export const useRootConceptIds = () => {
  const trees = useSelector<StateT, TreesT>(
    (state) => state.conceptTrees.trees
  );

  return useMemo(() => Object.keys(trees).filter(isRootTreeId(trees)), [trees]);
};
