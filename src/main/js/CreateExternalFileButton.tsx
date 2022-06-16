/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import { Button, Icon } from "@scm-manager/ui-components";
import styled from "styled-components";
import ExternalFileModal from "./ExternalFileModal";
import { extensionPoints } from "@scm-manager/ui-extensions";
import { useCreateExternalFile } from "./useCreateExternalFile";

const ButtonLink = styled(Button)`
  width: 50px;

  &:hover {
    color: #33b2e8;
  }
`;

const CreateExternalFileButton: FC<extensionPoints.ReposSourcesActionbar["props"]> = ({
  repository,
  revision,
  sources
}) => {
  const [t] = useTranslation("plugins");
  const [showModal, setShowModal] = useState(false);
  const { create, isLoading, error } = useCreateExternalFile(repository, sources);

  return (
    <>
      <ButtonLink
        className="button"
        title={t("scm-external-file-plugin.create.tooltip")}
        action={() => setShowModal(true)}
      >
        <Icon name="external-link-square-alt" color="inherit" />
      </ButtonLink>
      {showModal ? (
        <ExternalFileModal
          submitButtonLabel={t("scm-external-file-plugin.modal.submit")}
          title={t("scm-external-file-plugin.modal.title")}
          onSubmit={form => create({ ...form, branch: revision || "" })}
          repository={repository}
          revision={revision}
          initialPath={sources.path}
          isLoading={isLoading}
          error={error}
          close={() => setShowModal(false)}
        />
      ) : null}
    </>
  );
};

export default CreateExternalFileButton;
