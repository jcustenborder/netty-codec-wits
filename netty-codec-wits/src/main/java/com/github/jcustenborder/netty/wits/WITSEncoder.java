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

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

@ChannelHandler.Sharable
public class WITSEncoder extends MessageToMessageEncoder<Record> {
  RecordWriterFactory recordWriterFactory = new RecordWriterFactory();

  @Override
  protected void encode(ChannelHandlerContext channelHandlerContext, Record record, List<Object> list) throws Exception {
    RecordWriter writer = recordWriterFactory.get(record.getClass());

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      try (OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream)) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(streamWriter)) {
          writer.write(bufferedWriter, record);
          bufferedWriter.flush();
          list.add(
              Unpooled.wrappedBuffer(outputStream.toByteArray())
          );
        }
      }
    }
  }
}
