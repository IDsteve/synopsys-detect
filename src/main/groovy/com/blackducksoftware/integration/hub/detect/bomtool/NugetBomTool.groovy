/*
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.detect.bomtool

import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.detect.bomtool.nuget.NugetInspectorPackager
import com.blackducksoftware.integration.hub.detect.model.BomToolType
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocation
import com.blackducksoftware.integration.hub.detect.type.ExecutableType
import com.blackducksoftware.integration.hub.detect.util.executable.Executable
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableOutput

@Component
class NugetBomTool extends BomTool {
    private final Logger logger = LoggerFactory.getLogger(NugetBomTool.class)

    static final String SOLUTION_PATTERN = '*.sln'
    static final String PROJECT_PATTERN = '*.*proj'
    static final String INSPECTOR_OUTPUT_PATTERN ='*_inspection.json'

    @Autowired
    NugetInspectorPackager nugetInspectorPackager

    String nugetExecutable

    BomToolType getBomToolType() {
        return BomToolType.NUGET
    }

    @Override
    public boolean isBomToolApplicable() {
        def containsSolutionFile = detectFileManager.containsAllFiles(sourcePath, SOLUTION_PATTERN)
        def containsProjectFile = detectFileManager.containsAllFiles(sourcePath, PROJECT_PATTERN)

        if (containsSolutionFile || containsProjectFile) {
            nugetExecutable = findExecutablePath(ExecutableType.NUGET, true, detectConfiguration.getNugetPath())
            if (!nugetExecutable) {
                logger.warn("Could not find a ${executableManager.getExecutableName(ExecutableType.NUGET)} executable")
            }
        }

        nugetExecutable && (containsSolutionFile || containsProjectFile)
    }

    List<DetectCodeLocation> extractDetectCodeLocations() {
        def outputDirectory = new File(detectConfiguration.outputDirectory, 'nuget')
        def sourceDirectory = new File(sourcePath)
        String inspectorExePath = getInspectorExePath(sourceDirectory, outputDirectory, new File(nugetExecutable))

        if (!inspectorExePath) {
            return null
        }

        def options =  [
            "--target_path=${sourcePath}",
            "--output_directory=${outputDirectory.getAbsolutePath()}",
            "--ignore_failure=${detectConfiguration.getNugetInspectorIgnoreFailure()}"
        ]
        if (detectConfiguration.getNugetInspectorExcludedModules()) {
            options.add("--excluded_modules=${detectConfiguration.getNugetInspectorExcludedModules()}")
        }
        if (logger.traceEnabled) {
            options.add("-v")
        }

        def hubNugetInspectorExecutable = new Executable(sourceDirectory, inspectorExePath, options)
        ExecutableOutput executableOutput = executableRunner.execute(hubNugetInspectorExecutable)

        def dependencyNodeFiles = detectFileManager.findFiles(outputDirectory, INSPECTOR_OUTPUT_PATTERN)
        if (!dependencyNodeFiles) {
            return null
        }
        List<DetectCodeLocation> codeLocations = dependencyNodeFiles.collect { nugetInspectorPackager.createDetectCodeLocation(it) }
        FileUtils.deleteDirectory(outputDirectory)

        if (!codeLocations) {
            logger.warn('Unable to extract any dependencies from nuget')
            return []
        }
        codeLocations
    }

    private String getInspectorExePath(File sourceDirectory, File outputDirectory, File nugetExecutable) {
        File inspectorVersionDirectory = new File(outputDirectory, "${detectConfiguration.getNugetInspectorPackageName()}.${detectConfiguration.getNugetInspectorPackageVersion()}")
        File toolsDirectory = new File(inspectorVersionDirectory, 'tools')
        File inspectorExe = new File(toolsDirectory, "${detectConfiguration.getNugetInspectorPackageName()}.exe")

        //if we can't find the inspector where we expect to, attempt to install it from nuget.org
        if (inspectorExe == null || !inspectorExe.exists()) {
            installInspectorFromNugetDotOrg(sourceDirectory, outputDirectory, nugetExecutable)
            inspectorExe = new File(toolsDirectory, "${detectConfiguration.getNugetInspectorPackageName()}.exe")
        }

        if (inspectorExe == null || !inspectorExe.exists()) {
            logger.error("Could not find the ${detectConfiguration.getNugetInspectorPackageName()} version:${detectConfiguration.getNugetInspectorPackageVersion()} even after an install attempt.")
            return null
        }

        return inspectorExe.absolutePath
    }

    private ExecutableOutput installInspectorFromNugetDotOrg(File sourceDirectory, File outputDirectory, File nugetExecutable) {
        def options =  [
            'install',
            detectConfiguration.getNugetInspectorPackageName(),
            '-Version',
            detectConfiguration.getNugetInspectorPackageVersion(),
            '-OutputDirectory',
            outputDirectory.absolutePath
        ]

        Executable installExecutable = new Executable(sourceDirectory, nugetExecutable.absolutePath, options)
        executableRunner.execute(installExecutable)
    }
}