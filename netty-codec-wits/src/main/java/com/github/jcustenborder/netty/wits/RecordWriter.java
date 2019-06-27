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

import java.io.BufferedWriter;
import java.io.IOException;

abstract class RecordWriter<T extends Record> {

  protected abstract short recordNumber();

  public abstract void write(BufferedWriter writer, T record) throws IOException;

  private void writeRecordFieldPrefix(BufferedWriter writer, short fieldNumber) throws IOException {
    writer.write(String.format("%02d", recordNumber()));
    writer.write(String.format("%02d", fieldNumber));
  }

  protected void writeString(BufferedWriter writer, short fieldNumber, String value) throws IOException {
    writeRecordFieldPrefix(writer, fieldNumber);
    writer.write(value); //TODO: Validate the string doesn't contain newlines
    writer.write(value);
    writer.newLine();
  }

  protected void writeShort(BufferedWriter writer, short fieldNumber, Short value) throws IOException {
    writeRecordFieldPrefix(writer, fieldNumber);
    writer.write(value);
    writer.newLine();
  }

  protected void writeLong(BufferedWriter writer, short fieldNumber, Integer value) throws IOException {
    writeRecordFieldPrefix(writer, fieldNumber);
    writer.write(value);
    writer.newLine();
  }

  protected void writeFloat(BufferedWriter writer, short fieldNumber, Float value) throws IOException {
    writeRecordFieldPrefix(writer, fieldNumber);
    writer.write(Float.toString(value)); //TODO: Comeback and make sure that we validate the number of decimals that can be handled.
    writer.newLine();
  }
}
