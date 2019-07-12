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
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldRef;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JForEach;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructCodeGenerator {
  final String packageName = "com.github.jcustenborder.kafka.connect.wits";
  final JCodeModel codeModel;
  List<Record> records = new ArrayList<>();
  final AbstractJClass abstractStructMapperClass;
  final AbstractJClass schemaAndValueClass;
  final AbstractJClass recordBaseClass;
  final AbstractJClass recordWildcard;
  final AbstractJClass listClass;
  final AbstractJClass arrayListClass;

  public StructCodeGenerator(JCodeModel codeModel) {
    this.codeModel = codeModel;
    this.abstractStructMapperClass = this.codeModel.ref(this.packageName + ".AbstractStructMapper");
    this.schemaAndValueClass = this.codeModel.ref("org.apache.kafka.connect.data.SchemaAndValue");
    this.recordBaseClass = this.codeModel.ref("com.github.jcustenborder.netty.wits.Record");
    this.recordWildcard = this.codeModel.ref(Class.class).narrow(recordBaseClass.wildcardExtends());
    this.listClass = this.codeModel.ref(List.class);
    this.arrayListClass = this.codeModel.ref(ArrayList.class);
  }

  public void addRecord(Record record) {
    records.add(record);
  }

  public void addRecords(Collection<Record> records) {
    this.records.addAll(records);
  }

//  JDefinedClass recordKeyClass;
//
//  private void addRecordKey() throws JClassAlreadyExistsException {
//    JDefinedClass recordKeyClass = this.codeModel._class(
//        JMod.NONE,
//        this.packageName + ".RecordKey"
//    );
//    AbstractJClass recordBaseClass = this.codeModel.ref("com.github.jcustenborder.netty.wits.Record");
//    AbstractJClass recordClass = this.codeModel.ref(Class.class).narrow(recordBaseClass.wildcardExtends());
//    JFieldVar fieldClass = recordKeyClass.field(
//        JMod.PRIVATE | JMod.FINAL,
//        recordClass,
//        "recordClass"
//    );
//    JMethod constructor = recordKeyClass.constructor(JMod.PRIVATE);
//    JVar recordClassVar = constructor.param(recordClass, "recordClass");
//    constructor.body().assign(JExpr.refthis(fieldClass.name()), recordClassVar);
//
//    JMethod equalsMethod = recordKeyClass.method(JMod.PUBLIC, this.codeModel.BOOLEAN, "equals");
//    equalsMethod.annotate(Override.class);
//    JVar thatVar = equalsMethod.param(Object.class, "that");
//
//    JMethod hashCodeMethod = recordKeyClass.method(JMod.PUBLIC, this.codeModel.INT, "hashCode");
//    hashCodeMethod.annotate(Override.class);
//    hashCodeMethod.body()._return(fieldClass.invoke("hashCode"));
//
//
//    JMethod ofMethod = recordKeyClass.method(JMod.PUBLIC | JMod.STATIC, recordKeyClass, "of");
//    recordClassVar = ofMethod.param(recordClass, "recordClass");
//    JVar clsVar = ofMethod.body().decl(
//        JMod.FINAL,
//        recordClass,
//        "cls"
//    );
//  }

  private List<JDefinedClass> structMapperClasses;

  public void generate() throws JClassAlreadyExistsException {
    structMapperClasses = new ArrayList<>(records.size());
    for (Record record : records) {
      addStructMapper(record);
    }

    addRecordConverter();
  }

  private void addRecordConverter() throws JClassAlreadyExistsException {
    JDefinedClass recordConverterClass = this.codeModel._class(
        JMod.NONE,
        this.packageName + ".RecordConverter"
    );
    recordConverterClass._extends(this.codeModel.ref(this.packageName + ".AbstractRecordConverter"));
    AbstractJClass mapBaseClass = this.codeModel.ref(Map.class);
    AbstractJClass structMapperList = this.listClass.narrow(this.abstractStructMapperClass);


    AbstractJClass mapClass = mapBaseClass.narrow(recordWildcard, this.abstractStructMapperClass);
    AbstractJClass functionClass = this.codeModel.ref("java.util.function.Function").narrow(recordWildcard, this.abstractStructMapperClass);

    JFieldVar lookupVar = recordConverterClass.field(
        JMod.PRIVATE | JMod.FINAL,
        mapClass,
        "lookup"
    );
    lookupVar.init(JExpr._new(this.codeModel.ref(HashMap.class)));

    JDefinedClass abstractStructMapperClass = recordConverterClass._class(JMod.STATIC, "AbstractStructMapperFunction");
    abstractStructMapperClass._implements(functionClass);
    JFieldVar instanceVar = recordConverterClass.field(
        JMod.STATIC | JMod.FINAL,
        abstractStructMapperClass,
        "INSTANCE"
    );
    instanceVar.init(JExpr._new(abstractStructMapperClass));
    JFieldVar mappersVar = abstractStructMapperClass.field(
        JMod.FINAL | JMod.PRIVATE,
        structMapperList,
        "mappers"
    );

    JMethod applyMethod = abstractStructMapperClass.method(
        JMod.PUBLIC,
        this.abstractStructMapperClass,
        "apply"
    );
    applyMethod.annotate(Override.class);
    JVar recordClassVar = applyMethod.param(recordWildcard, "recordClass");
    JVar applyResultVar = applyMethod.body().decl(
        JMod.NONE,
        this.abstractStructMapperClass,
        "result"
    );
    applyResultVar.init(JExpr._null());
    JForEach forEach = applyMethod.body().forEach(this.abstractStructMapperClass, "mapper", mappersVar);
    JConditional ifMapper = forEach.body()._if(
        forEach.var().invoke("recordClass")
            .invoke("isAssignableFrom").arg(recordClassVar)
    );
    ifMapper._then().assign(applyResultVar, forEach.var());
    ifMapper._then()._break();

    JConditional ifMapperNull = applyMethod.body()._if(applyResultVar.eqNull());
    ifMapperNull._then()._throw(
        JExpr._new(this.codeModel.ref(UnsupportedOperationException.class))
    );

    applyMethod.body()._return(applyResultVar);

    JMethod convertMethod = recordConverterClass.method(
        JMod.PUBLIC,
        this.schemaAndValueClass,
        "convertValue"
    );
    convertMethod.annotate(Override.class);
    JVar convertResultVar = convertMethod.body().decl(
        JMod.FINAL,
        this.schemaAndValueClass,
        "result"
    );
    JVar recordVar = convertMethod.param(this.recordBaseClass, "record");
    JConditional ifRecordNull = convertMethod.body()._if(recordVar.eqNull());
    ifRecordNull._then().assign(convertResultVar, this.schemaAndValueClass.staticRef("NULL"));
    JVar mapperVar = ifRecordNull._else().decl(
        JMod.FINAL,
        this.abstractStructMapperClass,
        "mapper"
    );
    mapperVar.init(
        lookupVar.invoke("computeIfAbsent").arg(recordVar.invoke("getClass")).arg(instanceVar)
    );
    ifRecordNull._else().assign(convertResultVar, mapperVar.invoke("convert").arg(recordVar));

    convertMethod.body()._return(convertResultVar);

    JMethod constructor = abstractStructMapperClass.constructor(JMod.PRIVATE);
    JVar constructorMappers = constructor.body().decl(structMapperList, "mappers");
    constructorMappers.init(JExpr._new(this.arrayListClass));

    for (JDefinedClass structMapperClass : this.structMapperClasses) {
      constructor.body().add(
          constructorMappers.invoke("add").arg(JExpr._new(structMapperClass))
      );
    }
    constructor.body().assign(JExpr.refthis(mappersVar), constructorMappers);

//    applyMethod.

  }


  private void addStructMapper(Record record) throws JClassAlreadyExistsException {
    JDefinedClass structMapperClass = this.codeModel._class(
        JMod.NONE,
        this.packageName + "." + record.name() + "StructMapper"
    );
    structMapperClasses.add(structMapperClass);

    AbstractJClass structClass = this.codeModel.ref("org.apache.kafka.connect.data.Struct");
    AbstractJClass schemaClass = this.codeModel.ref("org.apache.kafka.connect.data.Schema");
    AbstractJClass schemaBuilderClass = this.codeModel.ref("org.apache.kafka.connect.data.SchemaBuilder");
    AbstractJClass recordClass = this.codeModel.ref("com.github.jcustenborder.netty.wits." + record.name());

    structMapperClass._extends(abstractStructMapperClass);

    JFieldRef schemaField = JExpr._super().ref("schema");

    JMethod schemaMethod = structMapperClass.method(
        JMod.PRIVATE | JMod.FINAL | JMod.STATIC,
        schemaClass,
        "schema"
    );

    JMethod constructor = structMapperClass.constructor(JMod.PUBLIC);
    constructor.body().add(
        JExpr.invokeSuper().arg(JExpr.invoke(schemaMethod.name()))
    );

    JMethod convertMethod = structMapperClass.method(
        JMod.PUBLIC,
        this.schemaAndValueClass,
        "convert"
    );
    convertMethod.annotate(Override.class);
    JVar inputVar = convertMethod.param(this.recordBaseClass, "input");
    JVar recordVar = convertMethod.body().decl(recordClass, "record");
    recordVar.init(inputVar.castTo(recordClass));
    JVar structVar = convertMethod.body().decl(structClass, "struct");
    structVar.init(JExpr._new(structClass).arg(schemaField));

    JMethod recordClassMethod = structMapperClass.method(
        JMod.PUBLIC,
        this.recordWildcard,
        "recordClass"
    );
    recordClassMethod.annotate(Override.class);
    recordClassMethod.body()._return(recordClass.dotclass());

    JVar builderVar = schemaMethod.body().decl(
        schemaBuilderClass,
        "builder",
        schemaBuilderClass.staticInvoke("struct")
    );
    schemaMethod.body().add(builderVar.invoke("name").arg(this.packageName + "." + record.name()));
    schemaMethod.body().add(builderVar.invoke("doc").arg(record.documentation()));
    schemaMethod.body().add(builderVar.invoke("parameter").arg("wits.record.id").arg(Integer.toString(record.recordId())));

    for (Record.Field field : record.fields()) {
      JInvocation addFieldMethod = schemaMethod(field)
          .arg(builderVar)
          .arg(field.fieldId())
          .arg(field.name())
          .arg(field.documentation());
      schemaMethod.body().add(addFieldMethod);
      JInvocation putMethod = putMethod(field, recordVar);


      convertMethod.body().add(
          structVar.invoke("put").arg(field.name()).arg(putMethod)
      );
    }

    schemaMethod.body().add(
        JExpr.invoke(
            "addDateTimeField"
        ).arg(builderVar)
    );
    schemaMethod.body()._return(
        builderVar.invoke("build")
    );
    convertMethod.body().add(
        structVar.invoke("put")
            .arg("dateTime")
            .arg(
                JExpr.invoke(
                    JExpr._super(),
                    "convertDateTime"
                ).arg(recordVar.invoke("dateTime"))
            )
    );
    convertMethod.body()._return(
        JExpr._new(schemaAndValueClass).arg(schemaField).arg(structVar)
    );
  }

  private JInvocation putMethod(Record.Field field, JVar recordVar) {
    JInvocation invokeField = recordVar.invoke(field.name());
    JInvocation result;
    if ("time".equals(field.name())) {
      result = JExpr.invoke(
          JExpr._super(),
          "convertTime"
      ).arg(invokeField);
    } else if ("date".equals(field.name())) {
      result = JExpr.invoke(
          JExpr._super(),
          "convertDate"
      ).arg(invokeField);
    } else {
      result = invokeField;
    }

    return result;
  }

  private JInvocation schemaMethod(Record.Field field) {
    JInvocation result;
    if ("time".equals(field.name())) {
      result = JExpr.invoke(
          "addTimeField"
      );
    } else if ("date".equals(field.name())) {
      result = JExpr.invoke(
          "addDateField"
      );
    } else {
      switch (field.type()) {
        case STRING:
          result = JExpr.invoke(
              "addStringField"
          );
          break;
        case LONG:
          result = JExpr.invoke(
              "addInt32Field"
          );
          break;
        case SHORT:
          result = JExpr.invoke(
              "addInt16Field"
          );
          break;
        case FLOAT:
          result = JExpr.invoke(
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
