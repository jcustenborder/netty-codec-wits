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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.jcustenborder.netty.wits.model.Record;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JCase;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JForEach;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JSwitch;
import com.helger.jcodemodel.JVar;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class RecordGenerator {
  final String packageName = "com.github.jcustenborder.netty.wits";
  final JCodeModel codeModel;
  final AbstractJClass stringType;
  final AbstractJClass recordReaderClass;
  final AbstractJClass recordWriterClass;
  final AbstractJClass recordBaseInterface;
  final AbstractJClass loggerClass;
  final AbstractJClass loggerFactoryClass;


  RecordGenerator(JCodeModel codeModel) {
    this.codeModel = codeModel;
    this.stringType = this.codeModel.ref(String.class);
    this.recordReaderClass = this.codeModel.ref("com.github.jcustenborder.netty.wits.RecordReader");
    this.recordWriterClass = this.codeModel.ref("com.github.jcustenborder.netty.wits.RecordWriter");
    this.recordBaseInterface = this.codeModel.ref("com.github.jcustenborder.netty.wits.Record");
    this.loggerClass = this.codeModel.ref("org.slf4j.Logger");
    this.loggerFactoryClass = this.codeModel.ref("org.slf4j.LoggerFactory");
  }

  private AbstractJClass recordBuilderClass;
  private AbstractJClass recordImmutableClass;
  private JDefinedClass recordInterface;

  void addGenerated(JDefinedClass definedClass) {
    AbstractJClass generatedAnnotation = this.codeModel.ref("javax.annotation.Generated");
    definedClass.annotate(generatedAnnotation).param(this.getClass().getName());
  }

  JFieldVar addLogger(JDefinedClass definedClass) {
    JFieldVar result = definedClass.field(
        JMod.PRIVATE | JMod.STATIC | JMod.FINAL,
        this.loggerClass,
        "log"
    );
    result.init(
        this.loggerFactoryClass.staticInvoke("getLogger")
            .arg(definedClass.dotclass())
    );
    return result;
  }

  AbstractJType typeForField(Record.Field field) {
    AbstractJType result;

    if (field.name().equals("date")) {
      result = codeModel.ref(LocalDate.class);
    } else if (field.name().equals("time")) {
      result = codeModel.ref(LocalTime.class);
    } else {
      switch (field.type()) {
        case STRING:
          result = codeModel.ref(String.class);
          break;
        case LONG:
          result = codeModel.ref(Integer.class);
          break;
        case FLOAT:
          result = codeModel.ref(Float.class);
          break;
        case SHORT:
          result = codeModel.ref(Short.class);
          break;
        default:
          throw new UnsupportedOperationException(
              String.format("%s(%s) is not supported", field.name(), field.type())
          );
      }
    }


    return result;
  }

  JInvocation readerMethodForField(Record.Field field) {
    JInvocation result;

    if (field.name().equals("date")) {
      result = JExpr._super().invoke("readDate");
    } else if (field.name().equals("time")) {
      result = JExpr._super().invoke("readTime");
    } else {
      switch (field.type()) {
        case STRING:
          result = JExpr._super().invoke("readString");
          break;
        case LONG:
          result = JExpr._super().invoke("readLong");
          break;
        case FLOAT:
          result = JExpr._super().invoke("readFloat");
          break;
        case SHORT:
          result = JExpr._super().invoke("readShort");
          break;
        default:
          throw new UnsupportedOperationException(
              String.format("%s(%s) is not supported", field.name(), field.type())
          );
      }
    }


    return result;
  }

  JInvocation writerMethodForField(Record.Field field) {
    JInvocation result;

    if (field.name().equals("date")) {
      result = JExpr._super().invoke("writeDate");
    } else if (field.name().equals("time")) {
      result = JExpr._super().invoke("writeTime");
    } else {
      switch (field.type()) {
        case STRING:
          result = JExpr._super().invoke("writeString");
          break;
        case LONG:
          result = JExpr._super().invoke("writeLong");
          break;
        case FLOAT:
          result = JExpr._super().invoke("writeFloat");
          break;
        case SHORT:
          result = JExpr._super().invoke("writeShort");
          break;
        default:
          throw new UnsupportedOperationException(
              String.format("%s(%s) is not supported", field.name(), field.type())
          );
      }
    }


    return result;
  }


  void addRecordClass(Record record) throws JClassAlreadyExistsException {
    this.recordInterface = this.codeModel._class(
        JMod.PUBLIC,
        this.packageName + "." + record.name(),
        EClassType.INTERFACE
    )._implements(this.recordBaseInterface);
    addGenerated(this.recordInterface);
    this.recordImmutableClass = this.codeModel.ref(this.packageName + ".Immutable" + record.name());
    this.recordBuilderClass = this.codeModel.ref(this.packageName + ".Immutable" + record.name() + ".Builder");

    this.recordInterface.annotate(codeModel.ref("org.immutables.value.Value.Immutable"));
    this.recordInterface.annotate(codeModel.ref("org.immutables.value.Value.Style"))
        .param("jdkOnly", true);
    this.recordInterface.annotate(JsonSerialize.class)
        .param("as", this.codeModel.ref("Immutable" + record.name()).dotclass());
    this.recordInterface.annotate(JsonDeserialize.class)
        .param("as", this.codeModel.ref("Immutable" + record.name()).dotclass());
    if (null != record.documentation()) {
      this.recordInterface.javadoc().add(record.documentation());
    }
    List<IJExpression> expressions = new ArrayList<>();

    record.fields().stream().sorted(Comparator.comparingInt(Record.Field::fieldId)).forEach(field -> {
      AbstractJType fieldType = typeForField(field);
      JMethod method = this.recordInterface.method(JMod.NONE, fieldType, field.name());
      method.annotate(Nullable.class);
      method.javadoc().addReturn().add(field.documentation());
      method.annotate(JsonProperty.class)
          .param("value", field.name());
      method.annotate(JsonPropertyDescription.class)
          .param(field.documentation());
      expressions.add(JExpr.lit(method.name()));
    });

    if (record.fields().stream().anyMatch(f -> "date".equals(f.name())) &&
        record.fields().stream().anyMatch(f -> "time".equals(f.name()))) {
      AbstractJClass localDateTimeType = this.codeModel.ref(LocalDateTime.class);
      AbstractJClass derived = this.codeModel.ref("org.immutables.value.Value.Derived");
      JMethod method = this.recordInterface.method(JMod.DEFAULT, localDateTimeType, "dateTime");
      method.annotate(derived);
      method.annotate(Nullable.class);
      JInvocation invokeTime = JExpr._this().invoke("time");
      JInvocation invokeDate = JExpr._this().invoke("date");
      method.body()._if(invokeDate.eqNull())._then()._return(JExpr._null());
      method.body()._if(invokeTime.eqNull())._then()._return(JExpr._null());

      method.body()._return(localDateTimeType.staticInvoke("of").arg(invokeDate).arg(invokeTime));
      expressions.add(JExpr.lit(method.name()));
      method.annotate(JsonProperty.class)
          .param("value", method.name());
      final String doc = "DateTime the event occurred.";
      method.javadoc().addReturn().add(doc);
      method.annotate(JsonPropertyDescription.class)
          .param(doc);
    }

    this.recordInterface.annotate(JsonPropertyOrder.class)
        .paramArray("value", expressions.toArray(new IJExpression[0]));
  }

  List<Record> records = new ArrayList<>();

  public void addRecord(Record record) {
    records.add(record);
  }

  public void addRecords(Collection<Record> records) {
    this.records.addAll(records);
  }

  void addRecordReaderFactoryFunction() throws JClassAlreadyExistsException {
    AbstractJClass integerClass = this.codeModel.ref(Integer.class);
    AbstractJClass functionClass = this.codeModel.ref("java.util.function.Function")
        .narrow(integerClass, this.recordReaderClass);
    JDefinedClass readerFunctionClass = this.codeModel._class(JMod.NONE, "com.github.jcustenborder.netty.wits.RecordReaderFunction")
        ._implements(functionClass);
    addGenerated(readerFunctionClass);
    JFieldVar logVar = addLogger(readerFunctionClass);
    JMethod methodApply = readerFunctionClass.method(JMod.PUBLIC, this.recordReaderClass, "apply");
    methodApply.annotate(Override.class);
    JVar recordIdParm = methodApply.param(integerClass, "recordId");
    methodApply.body().add(
        JExpr.invoke(logVar, "trace")
            .arg("apply() - Creating reader. RecordId = '{}'")
            .arg(recordIdParm)
    );
    JVar result = methodApply.body().decl(JMod.FINAL, this.recordReaderClass, "result");

    readerFunctionClass.field(JMod.STATIC | JMod.FINAL | JMod.PUBLIC, functionClass, "INSTANCE")
        .init(JExpr._new(readerFunctionClass));

    JSwitch switchRecordId = methodApply.body()._switch(recordIdParm);

    for (Record record : this.records) {
      AbstractJClass recordReaderClass = this.codeModel.ref(this.packageName + "." + record.name() + "Reader");
      JCase readerCase = switchRecordId._case(JExpr.lit(record.recordId()));
      readerCase.body().assign(result, JExpr._new(recordReaderClass));
      readerCase.body()._break();
    }

    switchRecordId._default().body()._throw(
        JExpr._new(
            this.codeModel.ref(UnsupportedOperationException.class)
        )
    );

    methodApply.body()._return(result);


  }

  public void generate() throws JClassAlreadyExistsException {
    for (Record record : this.records) {
      addRecordClass(record);
      addReaderClass(record);
      addWriterClass(record);
    }
    addRecordReaderFactoryFunction();
    addRecordReaderFactory();
    addRecordWriterFactory();
  }

  private void addRecordWriterFactory() throws JClassAlreadyExistsException {
    JDefinedClass writerFactoryClass = this.codeModel._class(
        JMod.PUBLIC,
        "com.github.jcustenborder.netty.wits.RecordWriterFactory"
    );
    addGenerated(writerFactoryClass);
    JFieldVar logVar = addLogger(writerFactoryClass);
    AbstractJClass mapClass = this.codeModel.ref(Map.class);
    AbstractJClass classOfRecord = this.codeModel.ref(Class.class).narrow(this.recordBaseInterface.wildcardExtends());

    AbstractJClass narrowedClass = mapClass.narrow(
        classOfRecord,
        this.recordWriterClass
    );
    JFieldVar writerLookup = writerFactoryClass.field(
        JMod.PRIVATE | JMod.FINAL,
        narrowedClass,
        "writerLookup"
    );
    JMethod constructor = writerFactoryClass.constructor(JMod.PUBLIC);
    constructor.body().addSingleLineComment("This is a little lame but for now look for immutable classes and perform the lookup this way.");
    JVar map = constructor.body().decl(JMod.NONE, narrowedClass, "map");
    map.init(
        JExpr._new(this.codeModel.ref(HashMap.class))
    );
    JVar recordWriterVar = constructor.body().decl(JMod.NONE, this.recordWriterClass, "recordWriter");

    for (Record record : this.records) {
      AbstractJClass immutableClass = this.codeModel.ref(this.packageName + ".Immutable" + record.name());
      AbstractJClass recordClass = this.codeModel.ref(this.packageName + "." + record.name());
      AbstractJClass recordWriterClass = this.codeModel.ref(this.packageName + "." + record.name() + "Writer");
      constructor.body().assign(recordWriterVar, JExpr._new(recordWriterClass));
      constructor.body().add(map.invoke("put").arg(immutableClass.dotclass()).arg(recordWriterVar));
      constructor.body().add(map.invoke("put").arg(recordClass.dotclass()).arg(recordWriterVar));
    }

    constructor.body().assign(writerLookup, map);

    JMethod getMethod = writerFactoryClass.method(JMod.PUBLIC, this.recordWriterClass, "get");
    JVar recordClassVar = getMethod.param(classOfRecord, "recordClass");
    getMethod.body().add(logVar.invoke("trace").arg("get() - recordClass = '{}'").arg(recordClassVar));
    getMethod.body()._return(writerLookup.invoke("get").arg(recordClassVar));
  }

  private void addRecordReaderFactory() throws JClassAlreadyExistsException {
    JDefinedClass readerFactoryClass = this.codeModel._class(
        JMod.PUBLIC,
        "com.github.jcustenborder.netty.wits.RecordReaderFactory"
    );
    addGenerated(readerFactoryClass);
    JFieldVar logVar = addLogger(readerFactoryClass);
    AbstractJClass mapClass = this.codeModel.ref(Map.class);
    AbstractJClass narrowedClass = mapClass.narrow(
        this.codeModel.ref(Integer.class),
        this.recordReaderClass
    );
    JFieldVar readerLookup = readerFactoryClass.field(
        JMod.PRIVATE | JMod.FINAL,
        narrowedClass,
        "readerLookup"
    );
    readerLookup.init(
        JExpr._new(this.codeModel.ref(LinkedHashMap.class))
    );

    JMethod getMethod = readerFactoryClass.method(
        JMod.PUBLIC,
        this.recordReaderClass,
        "get"
    );
    JVar recordId = getMethod.param(
        this.codeModel.INT,
        "recordId"
    );
    getMethod.body().add(
        JExpr.invoke(logVar, "trace")
            .arg("get() - recordId = {}")
            .arg(recordId)
    );
    AbstractJClass readerFunctionClass = this.codeModel.ref("com.github.jcustenborder.netty.wits.RecordReaderFunction");
    getMethod.body()._return(
        readerLookup.invoke("computeIfAbsent")
            .arg(recordId)
            .arg(readerFunctionClass.staticRef("INSTANCE"))
    );
    AbstractJClass listClass = this.codeModel.ref(List.class);
    AbstractJClass narrowedList = listClass.narrow(Object.class);

    JMethod writeMethod = readerFactoryClass.method(
        JMod.PUBLIC,
        this.codeModel.VOID,
        "write"
    );
    JVar list = writeMethod.param(narrowedList, "list");
    JForEach forEachBuilder = writeMethod.body().forEach(
        JMod.NONE,
        this.recordReaderClass,
        "reader",
        readerLookup.invoke("values")

    );
    JVar record = forEachBuilder.body().decl(
        this.recordBaseInterface,
        "record",
        forEachBuilder.var().invoke("build")
    );
    forEachBuilder.body().add(
        list.invoke("add")
            .arg(record)
    );
  }

  private void addWriterClass(Record record) throws JClassAlreadyExistsException {
    JDefinedClass writerClass = this.codeModel._class(
        JMod.NONE,
        this.packageName + "." + record.name() + "Writer"
    )._extends(this.recordWriterClass.narrow(this.recordInterface));
    addGenerated(writerClass);
    JMethod methodRecordNumber = writerClass.method(JMod.PROTECTED, this.codeModel.SHORT, "recordNumber");
    methodRecordNumber.annotate(Override.class);
    methodRecordNumber.body()._return(JExpr.lit(record.recordId()));

    JMethod writeMethod = writerClass.method(JMod.PUBLIC, this.codeModel.VOID, "write");
    writeMethod._throws(IOException.class);
    writeMethod.annotate(Override.class);
    JVar writerVar = writeMethod.param(BufferedWriter.class, "writer");
    JVar recordVar = writeMethod.param(this.recordInterface, "record");

    for (Record.Field field : record.fields()) {
      JConditional ifFieldNotNull = writeMethod.body()._if(
          JExpr._null().ne(recordVar.invoke(field.name()))
      );
      JInvocation writerInvoke = writerMethodForField(field);
      writerInvoke.arg(writerVar);
      writerInvoke.arg(JExpr.cast(this.codeModel.SHORT, JExpr.lit(field.fieldId())));
      writerInvoke.arg(JExpr.invoke(recordVar, field.name()));

      ifFieldNotNull._then().add(writerInvoke);


    }
  }

  private void addReaderClass(Record record) throws JClassAlreadyExistsException {
    JDefinedClass readerClass = this.codeModel._class(
        JMod.NONE,
        this.packageName + "." + record.name() + "Reader"
    )._extends(this.recordReaderClass);
    addGenerated(readerClass);
    JFieldVar logVar = addLogger(readerClass);
    JMethod methodRecordId = readerClass.method(JMod.PUBLIC, this.codeModel.SHORT, "recordId");
    methodRecordId.annotate(Override.class);
    methodRecordId.body()._return(JExpr.lit(record.recordId()));

    JFieldVar builderField = readerClass.field(
        JMod.PRIVATE_FINAL,
        this.recordBuilderClass,
        "builder",
        this.recordImmutableClass.staticInvoke("builder")
    );
    JMethod buildMethod = readerClass.method(JMod.PUBLIC, this.recordBaseInterface, "build");
    buildMethod.annotate(Override.class);
    buildMethod.body()._return(
        JExpr.invoke(builderField, "build")
    );

    JMethod applyMethod = readerClass.method(JMod.PUBLIC, this.codeModel.VOID, "apply");
    applyMethod.annotate(Override.class);

    JVar lineVar = applyMethod.param(String.class, "line");
    JVar fieldNumber = applyMethod.body().decl(JMod.FINAL, this.codeModel.SHORT, "fieldNumber");
    fieldNumber.init(JExpr.invoke(JExpr._super(), "fieldNumber").arg(lineVar));
    JSwitch switchFieldNumber = applyMethod.body()._switch(fieldNumber);

    switchFieldNumber._default().body()._throw(
        JExpr._new(this.codeModel.ref(IllegalStateException.class))
            .arg(
                this.stringType.staticInvoke("format")
                    .arg("Field %s is not supported for record %s.")
                    .arg(fieldNumber)
                    .arg(record.recordId())
            )
    );

    for (Record.Field field : record.fields()) {
      JCase caseField = switchFieldNumber._case(JExpr.lit(field.fieldId()));
      AbstractJType fieldType = typeForField(field);
      JInvocation readMethod = readerMethodForField(field);
      readMethod.arg(lineVar);
      JVar fieldVar = caseField.body().decl(JMod.FINAL, fieldType, field.name());

      fieldVar.init(readMethod);
      caseField.body().add(
          JExpr.invoke(logVar, "trace")
              .arg("apply() - " + field.name() + " = '{}'")
              .arg(fieldVar)
      );
      caseField.body().add(builderField.invoke(field.name()).arg(fieldVar));
      caseField.body()._break();
    }

  }

}
