/**
 * Copyright © 2019 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WITSDecoderTest {
  private static final Logger log = LoggerFactory.getLogger(WITSDecoderTest.class);

  @TestFactory
  public Stream<DynamicTest> recordType() {
    Map<String, Integer> tests = new LinkedHashMap<>();
    tests.put("30313035313930363235", 1);
    return tests.entrySet().stream()
        .map(e -> DynamicTest.dynamicTest(e.toString(), () -> {

        }));

  }

  @Test
  public void foo() throws Exception {
    final String expected = "303130353139303632350d0a303130363139323134370d0a313938344572646f73204d696c6c65720d0a3031323038370d0a303132333131360d0a3031323438310d0a30313231313331322e38350d0a3031313232372e36350d0a303130383336332e34330d0a303131303336332e34330d0a3031313434342e30350d0a3031313534342e30350d0a30313136352e36380d0a30313137352e36380d0a3031313332392e35320d0a30313431302e3030303030300d0a30313432302e3030303030300d0a30313433302e3030303030300d0a30313434302e3030303030300d0a30313435302e3030303030300d0a";
    ByteBuf buf = TestUtils.byteBuf(expected);
    WITSDecoder decoder = new WITSDecoder();
    List<Object> list = new ArrayList<>();
    decoder.decode(null, buf, list);
    log.info("{}", list);
    log.info(ObjectMapperFactory.INSTANCE.writeValueAsString(list));

    WITSEncoder encoder = new WITSEncoder();

    List<Record> records = list.stream().map(o -> (Record) o).collect(Collectors.toList());

    for (Record record : records) {
      list.clear();
      encoder.encode(null, record, list);
      ByteBuf byteBuf = (ByteBuf) list.get(0);
      log.info(ByteBufUtil.hexDump(byteBuf));
    }


  }
}
