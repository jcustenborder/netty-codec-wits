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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface Record {
  /**
   * @return
   *     Well Identifier
   */
  @Nullable
  @JsonProperty("wellId")
  @JsonPropertyDescription("Well Identifier")
  String wellId();

  /**
   * @return
   *     Sidetrack/Hole Sect No.
   */
  @Nullable
  @JsonProperty("sidetrackHoleSectNo")
  @JsonPropertyDescription("Sidetrack/Hole Sect No.")
  Short sidetrackHoleSectNo();

  /**
   * @return
   *     Record Identifier
   */
  @Nullable
  @JsonProperty("recordId")
  @JsonPropertyDescription("Record Identifier")
  Short recordId();

  /**
   * @return
   *     Sequence Identifier
   */
  @Nullable
  @JsonProperty("sequenceId")
  @JsonPropertyDescription("Sequence Identifier")
  Integer sequenceId();

  /**
   * @return
   *     Date
   */
  @Nullable
  @JsonProperty("date")
  @JsonPropertyDescription("Date")
  LocalDate date();

  /**
   * @return
   *     Time
   */
  @Nullable
  @JsonProperty("time")
  @JsonPropertyDescription("Time")
  LocalTime time();

  /**
   * @return
   *     DateTime the event occurred.
   */
  @Value.Derived
  @Nullable
  @JsonProperty("dateTime")
  @JsonPropertyDescription("DateTime the event occurred.")
  default LocalDateTime dateTime() {
    if (this.date() == null) {
      return null;
    }
    if (this.time() == null) {
      return null;
    }
    return LocalDateTime.of(this.date(), this.time());
  }
}
