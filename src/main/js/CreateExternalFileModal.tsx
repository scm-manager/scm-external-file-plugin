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

import { File, Repository } from "@scm-manager/ui-types";
import React, { FC, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import {
  Button,
  ButtonGroup,
  CommitAuthor,
  ErrorNotification,
  InputField,
  Modal,
  Textarea,
  validation
} from "@scm-manager/ui-components";
import { useCreateExternalFile } from "./useCreateExternalFile";

type Props = {
  repository: Repository;
  revision?: string;
  close: () => void;
  sources: File;
};

const CreateExternalFileModal: FC<Props> = ({ repository, close, revision, sources }) => {
  const originalPath = sources.path === "/" ? "/" : "/" + sources.path;
  const [t] = useTranslation("plugins");
  const [path, setPath] = useState(originalPath);
  const [filename, setFilename] = useState("");
  const [url, setUrl] = useState("");
  const [pathValid, setPathValid] = useState(true);
  const [urlValid, setUrlValid] = useState(true);
  const [filenameValid, setFilenameValid] = useState(true);
  const [commitMessage, setCommitMessage] = useState("");
  const initialFocusRef = useRef<HTMLInputElement>(null);
  const { create, isLoading, error } = useCreateExternalFile(repository, sources);

  const commitDisabled = !commitMessage || !filename || !url;

  const submit = () => {
    if (commitDisabled) {
      return;
    }

    let resultingPath = path;
    if (resultingPath.length !== 0 && !resultingPath.endsWith("/")) {
      resultingPath = resultingPath + "/";
    }
    resultingPath = resultingPath + filename;
    create({
      commitMessage,
      url,
      branch: revision || "",
      path: resultingPath
    });
  };

  const body = (
    <>
      {error ? <ErrorNotification error={error} /> : null}
      <InputField
        label={t("scm-external-file-plugin.create.path")}
        value={path}
        onChange={event => {
          setPathValid(validation.isPathValid(event.target.value));
          setPath(event.target.value);
        }}
        disabled={isLoading}
        validationError={!pathValid}
        errorMessage={t("scm-external-file-plugin.create.invalidPath")}
        helpText={t("scm-external-file-plugin.create.pathHelpText")}
        onReturnPressed={submit}
        ref={initialFocusRef}
        className="mb-4"
      />
      <InputField
        label={t("scm-external-file-plugin.create.filename")}
        value={filename}
        onChange={value => {
          setFilenameValid(validation.isFilenameValid(value));
          setFilename(value);
        }}
        validationError={!filenameValid}
        errorMessage={t("scm-external-file-plugin.create.invalidFilename")}
        helpText={t("scm-external-file-plugin.create.filenameHelpText")}
        disabled={isLoading}
        onReturnPressed={submit}
      />
      <hr />
      <InputField
        label={t("scm-external-file-plugin.create.url")}
        value={url}
        onChange={value => {
          console.log("url validation", validation.isUrlValid(value));
          setUrlValid(validation.isUrlValid(value));
          setUrl(value);
        }}
        validationError={!urlValid}
        errorMessage={t("scm-external-file-plugin.create.invalidUrl")}
        helpText={t("scm-external-file-plugin.create.urlHelpText")}
        disabled={isLoading}
        onReturnPressed={submit}
      />
      <div className="mb-2 mt-5">
        <CommitAuthor />
      </div>
      <Textarea
        placeholder={t("scm-external-file-plugin.create.commitPlaceholder")}
        onChange={message => setCommitMessage(message)}
        value={commitMessage}
        disabled={isLoading}
        onSubmit={submit}
      />
    </>
  );

  const footer = (
    <ButtonGroup>
      <Button action={close} disabled={isLoading}>
        {t("scm-external-file-plugin.modal.cancel")}
      </Button>
      <Button action={submit} disabled={commitDisabled} loading={isLoading} color="primary">
        {t("scm-external-file-plugin.modal.submit")}
      </Button>
    </ButtonGroup>
  );

  return (
    <Modal
      body={body}
      footer={footer}
      title={t("scm-external-file-plugin.modal.title")}
      closeFunction={close}
      active={true}
      initialFocusRef={initialFocusRef}
    />
  );
};

export default CreateExternalFileModal;
