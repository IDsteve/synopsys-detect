/**
 * synopsys-detect
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.detect.workflow.nameversion.decision;

import org.slf4j.Logger;

import com.synopsys.integration.detect.workflow.nameversion.DetectorProjectInfo;

public class PreferredDetectorDecision extends NameVersionDecision {
    private final DetectorProjectInfo detectorProjectInfo;

    public PreferredDetectorDecision(final DetectorProjectInfo detectorProjectInfo) {
        super(detectorProjectInfo.getNameVersion());
        this.detectorProjectInfo = detectorProjectInfo;
    }

    public DetectorProjectInfo getDetectorType() {
        return detectorProjectInfo;
    }

    @Override
    public void printDescription(final Logger logger) {
        logger.debug(String.format("Using preferred bom tool project info from %s found at depth %d as project info.", detectorProjectInfo.getDetectorType().name(), detectorProjectInfo.getDepth()));
    }
}
