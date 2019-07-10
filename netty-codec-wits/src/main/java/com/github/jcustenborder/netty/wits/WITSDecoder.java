/**
 * Copyright © 2019 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jcustenborder.netty.wits;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;

@ChannelHandler.Sharable
public class WITSDecoder extends MessageToMessageDecoder<ByteBuf> {

  static final Charset ASCII = Charset.forName("ASCII");
  private static final Logger log = LoggerFactory.getLogger(WITSDecoder.class);

  static int recordType(String line) {
    String recordNumber = line.substring(0, 2);
    return Integer.parseInt(recordNumber);
  }

  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
    RecordReaderFactory recordReaderFactory = new RecordReaderFactory();

    try (InputStream inputStream = new ByteBufInputStream(byteBuf)) {
      try (Reader inputStreamReader = new InputStreamReader(inputStream, ASCII)) {
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
          String line;
          while ((line = reader.readLine()) != null) {
            log.trace("decode() - line='{}'", line);
            int recordId = recordType(line);
            log.trace("decode() - recordId = {}", recordId);
            RecordReader recordReader = recordReaderFactory.get(recordId);
            recordReader.apply(line);
          }
        }
      }
    }

    recordReaderFactory.write(list);
  }
}
