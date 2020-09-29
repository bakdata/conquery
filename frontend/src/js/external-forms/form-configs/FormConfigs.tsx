import React, { useState } from "react";
import styled from "@emotion/styled";
import ReactList from "react-list";

import { FormConfigT } from "./reducer";
import FormConfig from "./FormConfig";
import DeleteFormConfigModal from "./DeleteFormConfigModal";
import { useDispatch } from "react-redux";
import { deleteFormConfigSuccess } from "./actions";
import ShareFormConfigModal from "./ShareFormConfigModal";

interface PropsT {
  datasetId: string;
  formConfigs: FormConfigT[];
}

const Root = styled("div")`
  flex: 1;
  overflow-y: auto;
  font-size: ${({ theme }) => theme.font.sm};
  padding: 0 10px;
`;
const Container = styled("div")`
  margin: 4px 0;
`;

const FormConfigs: React.FC<PropsT> = ({ datasetId, formConfigs }) => {
  const [formConfigToDelete, setFormConfigToDelete] = useState<string | null>(
    null
  );
  const [formConfigToShare, setFormConfigToShare] = useState<string | null>(
    null
  );

  const dispatch = useDispatch();
  const closeDeleteModal = () => setFormConfigToDelete(null);
  const onCloseShareModal = () => setFormConfigToShare(null);

  function onDeleteSuccess() {
    if (formConfigToDelete) {
      dispatch(deleteFormConfigSuccess(formConfigToDelete));
    }

    closeDeleteModal();
  }

  function onShareSuccess() {
    onCloseShareModal();
  }

  function renderConfig(index: number, key: string | number) {
    return (
      <Container key={key}>
        <FormConfig
          datasetId={datasetId}
          config={formConfigs[index]}
          onIndicateDeletion={() =>
            setFormConfigToDelete(formConfigs[index].id)
          }
          onIndicateShare={() => setFormConfigToShare(formConfigs[index].id)}
        />
      </Container>
    );
  }

  return (
    <Root>
      {!!formConfigToShare && (
        <ShareFormConfigModal
          formConfigId={formConfigToShare}
          onClose={onCloseShareModal}
          onShareSuccess={onShareSuccess}
        />
      )}
      {!!formConfigToDelete && (
        <DeleteFormConfigModal
          formConfigId={formConfigToDelete}
          onClose={closeDeleteModal}
          onDeleteSuccess={onDeleteSuccess}
        />
      )}
      <ReactList
        itemRenderer={renderConfig}
        length={formConfigs.length}
        type="variable"
      />
    </Root>
  );
};

export default FormConfigs;
