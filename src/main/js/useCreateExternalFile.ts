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

import { File, Link, Repository } from "@scm-manager/ui-types";
import { useMutation, useQueryClient } from "react-query";
import { apiClient } from "@scm-manager/ui-components";
import { useHistory } from "react-router-dom";

export type CreateExternalFileRequest = {
  commitMessage: string;
  url: string;
  branch: string;
  path: string;
};

export const useCreateExternalFile = (repository: Repository, sources: File) => {
  const queryClient = useQueryClient();
  const history = useHistory();

  const { mutate, isLoading, error } = useMutation<unknown, Error, CreateExternalFileRequest>(
    request =>
      apiClient
        .post((sources._links.createExternalFile as Link).href, request, "application/vnd.scmm-externalFile+json;v=2")
        .then(() => {
          let path = request.path;
          if (path.startsWith("/")) {
            path = path.substring(1);
          }
          if (!path.endsWith(".URL")) {
            path += ".URL";
          }
          history.push(`/repo/${repository.namespace}/${repository.name}/code/sources/${request.branch}/${path}`);
        }),
    {
      onSuccess: () => queryClient.invalidateQueries(["repository", repository.namespace, repository.name, "sources"])
    }
  );
  return {
    create: (request: CreateExternalFileRequest) => mutate(request),
    isLoading,
    error
  };
};
