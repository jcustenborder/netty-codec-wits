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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jcustenborder.netty.wits.model.Record;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mojo(name = "generate-struct-mappers", requiresDirectInvocation = true, requiresOnline = false)
public class StructGeneratorPlugin extends AbstractMojo {

  @Parameter(property = "outputPath", defaultValue = "${project.build.directory}/generated-sources/struct-code-generator")
  private File outputPath;

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Parameter(property = "includeFiles", required = true)
  private List<String> includeFiles;

  @Parameter(property = "excludeFiles")
  private List<String> excludeFiles = new ArrayList<>();


  int errors;

  ObjectMapper mapper = new ObjectMapper();

  Record loadRecord(File f) {
    getLog().info("Loading " + f);

    try {
      return mapper.readValue(f, Record.class);
    } catch (IOException e) {
      getLog().error("Exception while loading " + f, e);
      return null;
    }
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!outputPath.exists()) {
      getLog().debug("Creating output directory " + outputPath);
      outputPath.mkdirs();
    }

    final FileSetManager fileSetManager = new FileSetManager(getLog(), true);
    getLog().info("Searching for input files.");

    FileSet fileSet = new FileSet();
    fileSet.setDirectory(project.getBasedir().getAbsolutePath());
    fileSet.setIncludes(this.includeFiles);

    if (!this.excludeFiles.isEmpty()) {
      fileSet.setExcludes(this.excludeFiles);
    }

    String[] fileNames = fileSetManager.getIncludedFiles(fileSet);
    List<Record> records = Arrays.stream(fileNames)
        .map(s -> new File(project.getBasedir(), s))
        .map(this::loadRecord)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    if (errors > 0) {
      throw new MojoFailureException("Exception while loading record(s).");
    }

    if (records.isEmpty()) {
      getLog().warn("Found no input files.");
      return;
    }

    getLog().info(
        String.format("Found %s input file(s).", records.size())
    );

    JCodeModel codeModel = new JCodeModel();
    StructCodeGenerator generator = new StructCodeGenerator(codeModel);
    generator.addRecords(records);

    try {
      generator.generate();
    } catch (JClassAlreadyExistsException e) {
      throw new MojoFailureException("Duplicate class defined", e);
    }

    try {
      codeModel.build(this.outputPath);
    } catch (IOException e) {
      throw new MojoFailureException("Exception thrown", e);
    }

    this.project.addCompileSourceRoot(this.outputPath.getAbsolutePath());
  }
}