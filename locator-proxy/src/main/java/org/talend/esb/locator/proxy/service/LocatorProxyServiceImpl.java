/*
 * #%L
 * Service Locator :: Proxy
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
package org.talend.esb.locator.proxy.service;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.talend.esb.locator.proxy.service.types.EndpointReferenceListType;

public class LocatorProxyServiceImpl implements LocatorProxyService {

	@Override
	public void registerEndpoint(QName serviceName, String endpointURL) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public W3CEndpointReference lookupEndpoint(QName serviceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EndpointReferenceListType lookupEndpoints(QName serviceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean unregisterEnpoint(QName serviceName, String endpointURL) {
		// TODO Auto-generated method stub
		return false;
	}

}
