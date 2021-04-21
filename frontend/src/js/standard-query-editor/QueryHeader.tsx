import styled from "@emotion/styled";
import React, { FC } from "react";

import QueryClearButton from "./QueryClearButton";

const Container = styled("div")`
  display: flex;
  align-items: center;
  justify-content: flex-end;
`;

const QueryHeader: FC = () => {
  return (
    <Container>
      <QueryClearButton />
    </Container>
  );
};

export default QueryHeader;
