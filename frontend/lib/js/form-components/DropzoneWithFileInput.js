// @flow

import * as React from "react";
import styled from "@emotion/styled";
import T from "i18n-react";
import { NativeTypes } from "react-dnd-html5-backend";

import Dropzone from "./Dropzone";

const FileInput = styled("input")`
  display: none;
`;

const SxDropzone = styled(Dropzone)`
  cursor: ${({ isInitial }) => (isInitial ? "initial" : "pointer")};
  transition: box-shadow ${({ theme }) => theme.transitionTime};
  position: relative;

  &:hover {
    box-shadow: 0 0 5px 0 rgba(0, 0, 0, 0.2);
  }
`;

const TopRight = styled("p")`
  margin: 0;
  font-size: ${({ theme }) => theme.font.tiny};
  color: ${({ theme }) => theme.font.gray};
  position: absolute;
  top: 5px;
  right: 10px;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
`;

type PropsT = {
  children: React.Node,
  acceptedDropTypes?: string[],
  onSelectFile: File => void,
  disableClick: boolean,
  showFileSelectButton: boolean
};

/*
  Augments a dropzone with file drop support

  - opens file dialog on dropzone click
  - adds NativeTypes.FILE

  => The "onDrop"-prop needs to handle the file drop itself, though!
*/
export default ({
  children,
  onSelectFile,
  acceptedDropTypes,
  disableClick,
  showFileSelectButton,
  ...props
}: PropsT) => {
  const fileInputRef = React.useRef(null);

  const dropTypes = [...(acceptedDropTypes || []), NativeTypes.FILE];

  function onOpenFileDialog() {
    fileInputRef.current.click();
  }

  return (
    <SxDropzone
      acceptedDropTypes={dropTypes}
      onClick={() => {
        if (disableClick) return;

        onOpenFileDialog();
      }}
      {...props}
    >
      {showFileSelectButton && (
        <TopRight onClick={onOpenFileDialog}>
          {T.translate("inputMultiSelect.openFileDialog")}
        </TopRight>
      )}
      <FileInput
        ref={fileInputRef}
        type="file"
        onChange={e => {
          onSelectFile(e.target.files[0]);

          fileInputRef.current.value = null;
        }}
      />
      {children}
    </SxDropzone>
  );
};
