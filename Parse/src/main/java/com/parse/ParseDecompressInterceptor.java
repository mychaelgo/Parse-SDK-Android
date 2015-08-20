/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.parse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/** package */ class ParseDecompressInterceptor implements ParseNetworkInterceptor {

  private static final String CONTENT_ENCODING_HEADER = "Content-Encoding";
  private static final String GZIP_ENCODING = "gzip";

  @Override
  public ParseHttpResponse intercept(Chain chain) throws IOException {
    ParseHttpRequest request = chain.getRequest();
    ParseHttpResponse response = chain.proceed(request);
    // If the response is gziped, we need to decompress the stream and remove the gzip header.
    if (GZIP_ENCODING.equalsIgnoreCase(response.getHeader(CONTENT_ENCODING_HEADER))) {
      Map<String, String > newHeaders = new HashMap<>(response.getAllHeaders());
      newHeaders.remove(CONTENT_ENCODING_HEADER);
      // TODO(mengyan): Add builder constructor based on an existing ParseHttpResponse
      response = new ParseHttpResponse.Builder()
          .setTotalSize(response.getTotalSize())
          .setContentType(response.getContentType())
          .setHeaders(newHeaders)
          .setReasonPhase(response.getReasonPhrase())
          .setStatusCode(response.getStatusCode())
          .setContent(new GZIPInputStream(response.getContent()))
          .build();
    }
    return response;
  }
}