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
