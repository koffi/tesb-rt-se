/*
 * #%L
 * Service Locator Client for CXF
 * %%
 * Copyright (C) 2011 Talend Inc.
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
package org.talend.esb.servicelocator.client.internal;

import org.junit.Before;
import org.junit.Test;
import org.talend.esb.servicelocator.client.SLProperties;
import org.talend.esb.servicelocator.client.SLPropertiesImpl;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.esb.servicelocator.TestContent.*;
import static org.talend.esb.servicelocator.TestValues.*;

public class SLEndpointImplTest {

    private  byte[] content;

    private SLPropertiesImpl props;

    private SLEndpointImpl slEndpoint;

    @Before
    public void setUp() throws Exception {
        props = new SLPropertiesImpl();
        props.addProperty(NAME_1, VALUE_1, VALUE_2);
        
        content = createContent(ENDPOINT_1, LAST_TIME_STARTED, LAST_TIME_STOPPED, props);
        slEndpoint = new SLEndpointImpl(SERVICE_QNAME_1, content, false);
    }
    
    @Test
    public void forService() {
        assertEquals(SERVICE_QNAME_1, slEndpoint.forService());
    }

    @Test
    public void getAddress() {
        assertEquals(ENDPOINT_1, slEndpoint.getAddress());
    }

    @Test
    public void getLastTimeStarted() {
        assertEquals(LAST_TIME_STARTED, slEndpoint.getLastTimeStarted());
    }
    
    @Test
    public void getLastTimeStopped() {
        assertEquals(LAST_TIME_STOPPED, slEndpoint.getLastTimeStopped());
    }

    @Test
    public void getProperties() {
        SLProperties props = slEndpoint.getProperties();
        assertTrue(props.hasProperty(NAME_1));
        assertThat(props.getValues(NAME_1), containsInAnyOrder(VALUE_1, VALUE_2));
    }
}
