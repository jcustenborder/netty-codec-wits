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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jcustenborder.netty.wits.model.ImmutableField;
import com.github.jcustenborder.netty.wits.model.ImmutableRecord;
import com.github.jcustenborder.netty.wits.model.Record;
import com.google.common.base.CaseFormat;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import org.immutables.value.Value;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class RecordGeneratorTest {

  @Value.Immutable
  interface State {
    int recordId();

    File metadataFile();

    File fieldFile();

    File outputFile();
  }

//
////  @Disabled
//  @TestFactory
//  public Stream<DynamicTest> cleanup() throws IOException {
//    File inputRoot = new File("/Users/jeremy/source/opensource/netty/netty-codec-wits/wits-maven-plugin/src/main/dirty");
//    File outputRoot = new File("/Users/jeremy/source/opensource/netty/netty-codec-wits/netty-codec-wits/src/main/wits-records");
//    List<State> states = new ArrayList<>();
//
//
//    Map<Integer, String> recordNames = new LinkedHashMap<>();
//    recordNames.put(1, "GeneralTimeBasedRecord");
//    recordNames.put(2, "DrillingDepthBasedRecord");
//    recordNames.put(3, "DrillingConnectionsRecord");
//    recordNames.put(4, "HydraulicsRecord");
//    recordNames.put(5, "TrippingCasingTimeBasedRecord");
//    recordNames.put(6, "TrippingCasingConnectionBasedRecord");
//    recordNames.put(7, "SurveyDirectionalRecord");
//    recordNames.put(8, "MWDFormationEvaluationRecord");
//    recordNames.put(9, "MWDMechanicalRecord");
//    recordNames.put(10, "PressureEvaluationRecord");
//    recordNames.put(11, "MudTankVolumeRecord");
//    recordNames.put(12, "ChromatographGasesCycleBasedRecord");
//    recordNames.put(13, "ChromatographGasesDepthBasedRecord");
//    recordNames.put(14, "LaggedContinuousMudPropertiesRecord");
//    recordNames.put(15, "CuttingsLithologyRecord");
//    recordNames.put(16, "HydrocarbonShowRecord");
//    recordNames.put(17, "CementingRecord");
//    recordNames.put(18, "DrillStemTestingRecord");
//    recordNames.put(19, "ConfigurationRecord");
//    recordNames.put(20, "MudReportRecord");
//    recordNames.put(21, "BitReportRecord");
//    recordNames.put(22, "RemarkRecord");
//    recordNames.put(23, "WellIdentificationRecord");
//    recordNames.put(24, "VesselMotionMooringStatusRecord");
//    recordNames.put(25, "WeatherSeaStateRecord");
//
//
//    for (Integer i : recordNames.keySet()) {
//      State state = ImmutableState.builder()
//          .recordId(i)
//          .metadataFile(new File(inputRoot, String.format("%s.metadata.json", i)))
//          .fieldFile(new File(inputRoot, String.format("%s.fields.json", i)))
//          .outputFile(new File(outputRoot, String.format("%s.json", i)))
//          .build();
//      states.add(state);
//    }
//
//    ObjectMapper mapper = new ObjectMapper();
//    mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
//
//
//    Map<String, Record.FieldType> typeMapping = new LinkedHashMap<>();
//    typeMapping.put("A", Record.FieldType.STRING);
//    typeMapping.put("L", Record.FieldType.LONG);
//    typeMapping.put("S", Record.FieldType.SHORT);
//    typeMapping.put("F", Record.FieldType.FLOAT);
//
//    return states.stream().map(state -> DynamicTest.dynamicTest(state.toString(), () -> {
//      ArrayNode metadata = mapper.readValue(state.metadataFile(), ArrayNode.class);
//      ArrayNode fieldData = mapper.readValue(state.fieldFile(), ArrayNode.class);
//
//      ImmutableRecord.Builder recordBuilder = ImmutableRecord.builder();
//      recordBuilder.name(recordNames.get(state.recordId()));
//
//      for (Iterator<JsonNode> it = metadata.elements(); it.hasNext(); ) {
//        ObjectNode element = (ObjectNode) it.next();
//
//        if (null != element.get("FIELD1") && element.get("FIELD1").asText().startsWith("WITS Record ID")) {
//          recordBuilder.recordId(
//              Integer.parseInt(element.get("FIELD2").asText())
//          );
//        } else if (null != element.get("FIELD1") && element.get("FIELD1").asText().startsWith("Data Source")) {
//          recordBuilder.documentation(
//              element.get("FIELD2").asText()
//          );
//        }
//      }
//
//      for (Iterator<JsonNode> it = fieldData.elements(); it.hasNext(); ) {
//        ObjectNode element = (ObjectNode) it.next();
//        ImmutableField.Builder fieldBuilder = ImmutableField.builder();
//
//        int fieldNumber = element.get("Item").asInt();
//        final String description = element.get("Description").asText()
//            .replaceAll("[<>]", "")
//            .trim();
//        final String type = element.get("Type").asText();
//        final String longMnemonic = element.get("Long\nMnemonic").asText();
//        final String shortMnemonic = element.get("Short\nMnemonic").asText();
//        String name = description.replaceAll("[^a-zA-Z\\d]", "_");
//        name = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name);
//
//        if (name.equals("showMudSmpleNPentane") && fieldNumber == 38) {
//          name = "showMudSmpleEPentane";
//        }
//        fieldBuilder.name(name);
//        fieldBuilder.fieldId(fieldNumber);
//        fieldBuilder.longMnemonic(longMnemonic);
//        fieldBuilder.shortMnemonic(shortMnemonic);
//        Record.FieldType fieldType = typeMapping.get(type);
//        fieldBuilder.type(fieldType);
//        fieldBuilder.documentation(description);
//        recordBuilder.addFields(fieldBuilder.build());
//      }
//
//
//      Record record = recordBuilder.build();
//      mapper.writeValue(state.outputFile(), record);
//    }));
//  }
//
//  @Test
//  public void foo() throws JClassAlreadyExistsException, IOException {
//
//    JCodeModel codeModel = new JCodeModel();
//    RecordGenerator recordGenerator = new RecordGenerator("com.github.jcustenborder.netty.wits", codeModel);
//
//    Record record = ImmutableRecord.builder()
//        .recordId(1)
//        .name("GeneralTimeBasedRecord")
//        .documentation("Data acquired in real-time and computed over the trigger interval; record transmitted and computation reset when triggering interval occurs")
//        .addFields(
//            ImmutableField.builder()
//                .name("wellIdentifier")
//                .fieldId(1)
//                .documentation("Well Identifier")
//                .type(Record.FieldType.STRING)
//                .build(),
//            ImmutableField.builder()
//                .name("sideTrackNumber")
//                .fieldId(2)
//                .documentation("Sidetrack/Hole Sect No.")
//                .type(Record.FieldType.SHORT)
//                .build()
//        )
//        .build();
//
//    ObjectMapper mapper = new ObjectMapper();
//    mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
//    mapper.writeValue(new File("/Users/jeremy/source/opensource/netty/netty-codec-wits/netty-codec-wits/src/main/wits-records/1.json"), record);
//
//    recordGenerator.generate(record);
//
//    File outputPath = new File("target/test-code-generator");
//    if (!outputPath.exists()) {
//      outputPath.mkdirs();
//    }
//
//    codeModel.build(outputPath);
//
//  }
}

