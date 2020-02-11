/**
 * configuration
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
package com.synopsys.integration.configuration.property.base

import com.synopsys.integration.configuration.parse.ValueParser

/**
 * A property that returns null when it is not present in a Configuration.
 *
 * @param T the type this property returns when it is retrieved from a Configuration.
 * @property key a normalized lowercase dot separated key such as "example.key".
 * @property parser the parser that converts string values to the given type T.
 */
abstract class ValuedProperty<T>(key: String, parser: ValueParser<T>, val default: T) : TypedProperty<T>(key, parser) {}