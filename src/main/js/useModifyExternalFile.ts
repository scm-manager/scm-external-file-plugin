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

export type ModifyExternalFileRequest = {
  commitMessage: string;
  url: string;
  branch: string;
};

export const useModifyExternalFile = (repository: Repository, file: File) => {
  const queryClient = useQueryClient();

  const link = (file._links.modifyExternalFile as Link).href;
  const { mutateAsync, isLoading, error } = useMutation<unknown, Error, ModifyExternalFileRequest>(
    request => apiClient.put(link, request, "application/vnd.scmm-externalFile+json;v=2"),
    {
      onSuccess: () => queryClient.invalidateQueries(["externalFileUrl", link])
    }
  );
  return {
    modify: (request: ModifyExternalFileRequest) => mutateAsync(request),
    isLoading,
    error
  };
};
