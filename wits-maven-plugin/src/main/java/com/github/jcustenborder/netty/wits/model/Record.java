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
package com.github.jcustenborder.netty.wits.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.List;

@JsonSerialize(as = ImmutableRecord.class)
@JsonDeserialize(as = ImmutableRecord.class)
@Value.Immutable
@Value.Style(jdkOnly = true)
public interface Record {
  String name();

  @Nullable
  String documentation();

  int recordId();

  List<Field> fields();

  @Value.Immutable
  @JsonSerialize(as = ImmutableField.class)
  @JsonDeserialize(as = ImmutableField.class)
  interface Field {
    int fieldId();

    FieldType type();

    String name();

    @Nullable
    String documentation();

    @Nullable
    String longMnemonic();

    @Nullable
    String shortMnemonic();
  }

  enum FieldType {
    STRING,
    LONG,
    SHORT,
    FLOAT
  }
}
