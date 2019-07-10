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

import com.github.jcustenborder.netty.wits.model.Record;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StructCodeGenerator {
  final String packageName = "com.github.jcustenborder.netty.wits";
  final JCodeModel codeModel;
  List<Record> records = new ArrayList<>();

  public StructCodeGenerator(JCodeModel codeModel) {
    this.codeModel = codeModel;
  }

  public void addRecord(Record record) {
    records.add(record);
  }

  public void addRecords(Collection<Record> records) {
    this.records.addAll(records);
  }

  public void generate() throws JClassAlreadyExistsException {
    for (Record record : records) {
      addStructMapper(record);
    }
  }

  private void addStructMapper(Record record) throws JClassAlreadyExistsException {
    JDefinedClass structMapperClass = this.codeModel._class(
        JMod.NONE,
        this.packageName + "." + record.name() + "StructMapper"
    );
    AbstractJClass schemaAndValueClass = this.codeModel.ref("org.apache.kafka.connect.data.SchemaAndValue");
    AbstractJClass structClass = this.codeModel.ref("org.apache.kafka.connect.data.Struct");
    AbstractJClass schemaClass = this.codeModel.ref("org.apache.kafka.connect.data.Schema");
    AbstractJClass schemaBuilderClass = this.codeModel.ref("org.apache.kafka.connect.data.SchemaBuilder");
    AbstractJClass recordClass = this.codeModel.ref("com.github.jcustenborder.netty.wits." + record.name());
    AbstractJClass abstractStructMapperClass = this.codeModel.ref(this.packageName + ".AbstractStructMapper");
    structMapperClass._extends(abstractStructMapperClass.narrow(recordClass));
    JFieldVar schemaField = structMapperClass.field(
        JMod.FINAL,
        schemaClass,
        "schema"
    );

    JMethod schemaMethod = structMapperClass.method(
        JMod.PRIVATE | JMod.FINAL,
        schemaClass,
        "schema"
    );

    JMethod constructor = structMapperClass.constructor(JMod.PUBLIC);
    constructor.body().add(
        JExpr.invokeSuper().arg(JExpr.invoke(JExpr._this(), schemaMethod.name()))
    );

    JMethod convertMethod = structMapperClass.method(
        JMod.PUBLIC,
        schemaAndValueClass,
        "convert"
    );
    convertMethod.annotate(Override.class);
    JVar recordVar = convertMethod.param(recordClass, "record");
    JVar structVar = convertMethod.body().decl(structClass, "struct");
    structVar.init(JExpr._new(structClass).arg(schemaField));


    JVar builderVar = schemaMethod.body().decl(
        schemaBuilderClass,
        "builder",
        schemaBuilderClass.staticInvoke("struct")
    );
    schemaMethod.body().add(builderVar.invoke("name").arg(this.packageName + "." + record.name()));

    for (Record.Field field : record.fields()) {
      JInvocation addFieldMethod = schemaMethod(field)
          .arg(builderVar)
          .arg(field.fieldId())
          .arg(field.name())
          .arg(field.documentation());
      schemaMethod.body().add(addFieldMethod);

      convertMethod.body().add(
          structVar.invoke("put").arg(field.name()).arg(recordVar.invoke(field.name()))
      );
    }

    schemaMethod.body().add(
        JExpr.invoke(
            JExpr._super(),
            "addDateTimeField"
        ).arg(builderVar)
    );

    schemaMethod.body()._return(
        builderVar.invoke("build")
    );
    convertMethod.body()._return(
        JExpr._new(schemaAndValueClass).arg(schemaField).arg(structVar)
    );
  }

  private JInvocation schemaMethod(Record.Field field) {
    JInvocation result;
    if ("time".equals(field.name())) {
      result = JExpr.invoke(
          JExpr._super(),
          "addTimeField"
      );
    } else if ("date".equals(field.name())) {
      result = JExpr.invoke(
          JExpr._super(),
          "addDateField"
      );
    } else {
      switch (field.type()) {
        case STRING:
          result = JExpr.invoke(
              JExpr._super(),
              "addStringField"
          );
          break;
        case LONG:
          result = JExpr.invoke(
              JExpr._super(),
              "addInt32Field"
          );
          break;
        case SHORT:
          result = JExpr.invoke(
              JExpr._super(),
              "addInt16Field"
          );
          break;
        case FLOAT:
          result = JExpr.invoke(
              JExpr._super(),
              "addFloat32Field"
          );
          break;
        default:
          throw new UnsupportedOperationException(
              "Field '" + field.name() + "' has an unsupported type of " + field.type()
          );

      }
    }

    return result;
  }
}
