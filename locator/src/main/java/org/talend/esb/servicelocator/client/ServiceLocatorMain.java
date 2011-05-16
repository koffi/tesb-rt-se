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
package org.talend.esb.servicelocator.client;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import org.talend.esb.servicelocator.client.internal.ServiceLocatorException;
import org.talend.esb.servicelocator.client.internal.ServiceLocatorImpl;

public class ServiceLocatorMain {

	private ServiceLocatorImpl sl = new ServiceLocatorImpl();

	private PrintStream out;

	public ServiceLocatorMain() {
	}
	
	public void setLocatorEndpoints(String locatorEndpoints) {
		sl.setLocatorEndpoints(locatorEndpoints);
	}
	
	public void exec(OutputStream out) throws InterruptedException,
	ServiceLocatorException {
		this.out = new PrintStream(out);
	
		sl.connect();
		printServices();
		sl.disconnect();
	}

	private void printServices() throws InterruptedException,
			ServiceLocatorException {
		List<QName> services = sl.getServices();
		
		for(QName service : services) {
			out.println(service);
			printEndpoints(service);
		}
	}

	private void printEndpoints(QName service) throws InterruptedException,
	ServiceLocatorException {
		List<String> endpoints = sl.getEndpoints(service);
		
		for (String endpoint : endpoints) {
			out.println(" |--" + endpoint);
		}
	}
	
	public static void main(String[] args) {
		
		ServiceLocatorMain main = new ServiceLocatorMain();
		if (! parseOptions(args, main)) {
			usage();
			return;
		}
		try {
			main.exec(System.out);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}

	}
	
	private static boolean parseOptions(String[] args, ServiceLocatorMain main) {
        List<String> argList = Arrays.asList(args);
        Iterator<String> argIter = argList.iterator();
        while (argIter.hasNext()) {
             String opt = argIter.next();
             try {
                 if (opt.equals("-endpoints")) {
                	 main.setLocatorEndpoints(argIter.next());
                 } else {
                     System.err.println("Error: unknown option " + opt);
                     return false;
                 }
             } catch (NoSuchElementException e){
                 System.err.println("Error: no argument found for option "
                         + opt);
                 return false;
             }
        }
        return true;
	}

    static void usage() {
        System.err.println("ServiceLocatorMain -endpoints host:port[,host:port]*");
    }
}

