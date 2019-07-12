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
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class WITSPacketDecoder extends ByteToMessageDecoder {
  public WITSPacketDecoder() {
//    setSingleDecode(true);
    setCumulator(ByteToMessageDecoder.COMPOSITE_CUMULATOR);
  }

  static final ByteBuf HEADER = Unpooled.wrappedBuffer(
      ByteBufUtil.decodeHexDump("26260d0a")
  );
  static final int HEADER_SIZE = HEADER.capacity();
  static final ByteBuf FOOTER = Unpooled.wrappedBuffer(
      ByteBufUtil.decodeHexDump("21210d0a")
  );
  static final int FOOTER_SIZE = FOOTER.capacity();
  private static final Logger log = LoggerFactory.getLogger(WITSPacketDecoder.class);

  private static int indexOf(ByteBuf haystack, ByteBuf needle) {
    for (int i = haystack.readerIndex(); i < haystack.writerIndex(); i++) {
      int haystackIndex = i;
      int needleIndex;
      for (needleIndex = 0; needleIndex < needle.capacity(); needleIndex++) {
        if (haystack.getByte(haystackIndex) != needle.getByte(needleIndex)) {
          break;
        } else {
          haystackIndex++;
          if (haystackIndex == haystack.writerIndex() &&
              needleIndex != needle.capacity() - 1) {
            return -1;
          }
        }
      }

      if (needleIndex == needle.capacity()) {
        // Found the needle from the haystack!
        return i - haystack.readerIndex();
      }
    }
    return -1;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> output) throws Exception {
    int start = indexOf(input, HEADER);
    int end = indexOf(input, FOOTER);

    log.trace("decode() - start = {} end = {}", start, end);
    if (start > -1 && end > -1) {
      input.skipBytes(HEADER_SIZE);
      int count = (end - start) - HEADER_SIZE;
      log.trace("decode() - count = {}", count);
      output.add(
          input.readRetainedSlice(count)
      );
      input.skipBytes(FOOTER_SIZE);
    }

    log.trace("decode() - isReadable = {} readerIndex = {}", input.isReadable(), input.readerIndex());
  }
}
