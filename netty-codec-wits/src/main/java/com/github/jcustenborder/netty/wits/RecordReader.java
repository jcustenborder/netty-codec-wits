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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class RecordReader {
  private static final Logger log = LoggerFactory.getLogger(RecordReader.class);

  public abstract short recordId();

  public abstract void apply(String line);

  public abstract Record build();

  protected short fieldNumber(String line) {
    String fieldNumber = line.substring(2, 4);
    return Short.parseShort(fieldNumber);
  }

  protected String readString(String line) {
    return line.substring(4).trim();
  }

  protected Short readShort(String line) {
    String value = readString(line);
    return Short.parseShort(value);
  }

  protected Float readFloat(String line) {
    String value = readString(line);
    return Float.parseFloat(value);
  }

  protected Integer readLong(String line) {
    String value = readString(line);
    return Integer.parseInt(value);
  }
}
