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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.jcustenborder.netty.wits.model.Record;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.JCase;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JSwitch;
import com.helger.jcodemodel.JVar;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;

class RecordGenerator {
  final String packageName;
  final JCodeModel codeModel;
  final AbstractJClass stringType;
  final AbstractJClass recordReaderClass;
  final AbstractJClass writerReaderClass;
  final AbstractJClass recordBaseInterface;

  RecordGenerator(String packageName, JCodeModel codeModel) {
    this.packageName = packageName;
    this.codeModel = codeModel;
    this.stringType = this.codeModel.ref(String.class);
    this.recordReaderClass = this.codeModel.ref("com.github.jcustenborder.netty.wits.RecordReader");
    this.writerReaderClass = this.codeModel.ref("com.github.jcustenborder.netty.wits.RecordWriter");
    this.recordBaseInterface = this.codeModel.ref("com.github.jcustenborder.netty.wits.Record");
  }

  private AbstractJClass recordBuilderClass;
  private AbstractJClass recordImmutableClass;
  private JDefinedClass recordInterface;


  AbstractJType typeForField(Record.Field field) {
    AbstractJType result;

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


    return result;
  }

  JInvocation readerMethodForField(Record.Field field) {
    JInvocation result;

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


    return result;
  }

  JInvocation writerMethodForField(Record.Field field) {
    JInvocation result;

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


    return result;
  }


  void addRecordClass(Record record) throws JClassAlreadyExistsException {
    this.recordInterface = this.codeModel._class(
        JMod.PUBLIC,
        this.packageName + "." + record.name(),
        EClassType.INTERFACE
    )._implements(this.recordBaseInterface);
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

    for (Record.Field field : record.fields()) {
      AbstractJType fieldType = typeForField(field);
      JMethod method = this.recordInterface.method(JMod.NONE, fieldType, field.name());
      method.annotate(Nullable.class);
      method.javadoc().addReturn().add(field.documentation());
    }
  }


  public void generate(Record record) throws JClassAlreadyExistsException {
    addRecordClass(record);
    addReaderClass(record);
    addWriterClass(record);
  }

  private void addWriterClass(Record record) throws JClassAlreadyExistsException {
    JDefinedClass writerClass = this.codeModel._class(
        JMod.NONE,
        this.packageName + "." + record.name() + "Writer"
    )._extends(this.writerReaderClass.narrow(this.recordInterface));
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

    JMethod methodRecordId = readerClass.method(JMod.PUBLIC, this.codeModel.SHORT, "recordId");
    methodRecordId.annotate(Override.class);
    methodRecordId.body()._return(JExpr.lit(record.recordId()));

    JFieldVar builderField = readerClass.field(
        JMod.PRIVATE_FINAL,
        this.recordBuilderClass,
        "builder",
        this.recordImmutableClass.staticInvoke("builder")
    );
    JMethod buildMethod = readerClass.method(JMod.PUBLIC, this.recordInterface, "build");
    buildMethod.body()._return(
        JExpr.invoke(builderField, "build")
    );

    JMethod applyMethod = readerClass.method(JMod.PUBLIC, this.codeModel.VOID, "apply");
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
      caseField.body().add(builderField.invoke(field.name()).arg(fieldVar));


      caseField.body()._break();
    }

  }

}
