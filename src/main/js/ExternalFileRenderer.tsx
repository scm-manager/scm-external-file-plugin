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

import { ErrorNotification, Icon, Loading } from "@scm-manager/ui-components";
import { extensionPoints } from "@scm-manager/ui-extensions";
import React, { FC } from "react";
import { useExternalFileUrl } from "./useExternalFile";
import { useTranslation } from "react-i18next";

const ExternalFileRenderer: FC<extensionPoints.RepositorySourcesView["props"]> = ({ file }) => {
  const { data: url, isLoading, error } = useExternalFileUrl(file);
  const [t] = useTranslation("plugins");

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading) {
    return <Loading />;
  }

  return (
    <a href={url} target="_blank" rel="noopener noreferrer" className="is-flex button is-link m-4">
      <span className="icon is-medium">
        <Icon name="external-link-square-alt" color="inherit" />
      </span>
      <span>{t("scm-external-file-plugin.openUrl")}</span>
    </a>
  );
};

export default ExternalFileRenderer;
