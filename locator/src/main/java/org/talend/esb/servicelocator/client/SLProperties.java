/*
 * #%L
 * Service Locator Client for CXF
 * %%
 * Copyright (C) 2011-2019 Talend Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.talend.esb.servicelocator.client;

import java.io.Serializable;
import java.util.Collection;

public interface SLProperties extends Serializable {

    Collection<String> getPropertyNames();

    boolean hasProperty(String name);

    Collection<String> getValues(String name);

    boolean includesValues(String name, String... values);

    boolean includesValues(String name, Collection<String> values);
}
