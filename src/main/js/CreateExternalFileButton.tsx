/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
