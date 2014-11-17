package org.talend.esb.mep.requestcallback.feature;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.DispatchImpl;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.jaxws.JaxWsClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.service.factory.AbstractServiceConfiguration;
import org.apache.cxf.service.factory.DefaultServiceConfiguration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.talend.esb.mep.requestcallback.impl.wsdl.CallbackDefaultServiceConfiguration;

public class CallContext implements Serializable {

	private static final Logger LOGGER = LogUtils.getL7dLogger(CallContext.class);
	private static final String NULL_MEANS_ONEWAY = "jaxws.provider.interpretNullAsOneway";
	private static final String CLASSPATH_URL_PREFIX = "classpath:";
	private static final long serialVersionUID = -5024912330689208965L;
	private static final int CLASSPATH_URL_PREFIX_LENGTH = CLASSPATH_URL_PREFIX.length();

	private QName portTypeName;
	private QName serviceName;
	private QName operationName;
	private String requestId;
	private String callId;
	private String correlationId;
	private String callbackId;
	private String replyToAddress;
	private String bindingId;
	private String flowId;  // Service Activity Monitoring flowId
	private URL wsdlLocationURL;
	private Map<String, String> userData;
	private transient CallbackInfo callbackInfo = null;
	private static boolean logging = false;
	private static boolean serviceActivityMonitoring = false;

	private static ClassPathXmlApplicationContext samContext = null;

	public QName getPortTypeName() {
		return portTypeName;
	}

	public void setPortTypeName(final QName portTypeName) {
		this.portTypeName = portTypeName;
		this.callbackInfo = null;
	}

	public QName getServiceName() {
		return serviceName;
	}

	public void setServiceName(final QName serviceName) {
		this.serviceName = serviceName;
		this.callbackInfo = null;
	}

	public QName getOperationName() {
		return operationName;
	}

	public void setOperationName(final QName operationName) {
		this.operationName = operationName;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(final String requestId) {
		this.requestId = requestId;
	}

	public String getCallId() {
		return callId;
	}

	public void setCallId(final String callId) {
		this.callId = callId;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(final String correlationId){
		this.correlationId = correlationId;
	}

	public String getCallbackId() {
		return callbackId;
	}

	public void setCallbackId(final String callbackId) {
		this.callbackId = callbackId;
	}

	public String getReplyToAddress() {
		return replyToAddress;
	}

	public void setReplyToAddress(final String replyToAddress) {
		this.replyToAddress = replyToAddress;
	}

	public String getBindingId() {
		return bindingId;
	}

	public void setBindingId(final String bindingId) {
		this.bindingId = bindingId;
	}

	public String getWsdlLocation() {
		return wsdlLocationURL == null ? null : wsdlLocationURL.toExternalForm();
	}

	public void setWsdlLocation(final String wsdlLocation) throws MalformedURLException {
		this.wsdlLocationURL = toWsdlUrl(wsdlLocation);
		this.callbackInfo = null;
	}

	public void setWsdlLocation(final File wsdlLocation) throws MalformedURLException {
		this.wsdlLocationURL = wsdlLocation == null ? null : wsdlLocation.toURI().toURL();
		this.callbackInfo = null;
	}

	public void setWsdlLocation(final URL wsdlLocation) {
		setWsdlLocationURL(wsdlLocation);
	}

	public URL getWsdlLocationURL() {
		return wsdlLocationURL;
	}

	public void setWsdlLocationURL(final URL wsdlLocationURL) {
		this.wsdlLocationURL = wsdlLocationURL;
		this.callbackInfo = null;
	}

	public CallbackInfo getCallbackInfo() {
		if (callbackInfo == null && wsdlLocationURL != null) {
			callbackInfo = new CallbackInfo(wsdlLocationURL);
		}
		return callbackInfo;
	}

	public Map<String, String> getUserData() {
		if (userData == null) {
			userData = new HashMap<String, String>();
		}
		return userData;
	}

	public boolean hasUserData() {
		return userData != null && !userData.isEmpty();
	}

	public static boolean isLogging() {
		return logging;
	}

	public static void setLogging(final boolean logging) {
		CallContext.logging = logging;
	}

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(final String flowId) {
        this.flowId = flowId;
    }

    public static boolean isServiceActivityMonitoring() {
        return serviceActivityMonitoring;
    }

    public static void setServiceActivityMonitoring(final boolean value) {
        CallContext.serviceActivityMonitoring = value;
        if (CallContext.serviceActivityMonitoring && samContext == null) {
            samContext = new ClassPathXmlApplicationContext(
                    new String[] {"/META-INF/tesb/agent-context.xml"});
        }
    }

	public <T> T createCallbackProxy(final Class<T> proxyInterface) {
        final JaxWsProxyFactoryBean callback = new JaxWsProxyFactoryBean();
        callback.setServiceName(serviceName);
        callback.setEndpointName(new QName(serviceName.getNamespaceURI(), serviceName.getLocalPart() + "Port"));
        callback.setAddress(replyToAddress);
        callback.setServiceClass(proxyInterface);
        final List<Feature> features = callback.getFeatures();
        features.add(new RequestCallbackFeature());
        if (logging) {
        	features.add(new LoggingFeature());
        }
        if (serviceActivityMonitoring) {
            features.add(getEventFeature());
        }

        final Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(RequestCallbackFeature.CALLCONTEXT_PROPERTY_NAME, this);
        callback.setProperties(properties);

        return callback.create(proxyInterface);
	}

	public <T extends Source> Dispatch<T> createCallbackDispatch(
			final Class<T> sourceClass, final Service.Mode mode,
			final QName operation, final String soapAction, final URL wsdlLocationURL) {
		final QName callbackPortTypeName = new QName(
				portTypeName.getNamespaceURI(), portTypeName.getLocalPart() + "Consumer");
		final QName callbackServiceName = new QName(
				callbackPortTypeName.getNamespaceURI(), callbackPortTypeName.getLocalPart() + "Service");
		final QName callbackPortName = new QName(
				callbackPortTypeName.getNamespaceURI(), callbackPortTypeName.getLocalPart() + "Port");

		Service service = null;
		final URL wsdlURL = wsdlLocationURL == null ? this.wsdlLocationURL : wsdlLocationURL;
		if (wsdlURL != null) {
			try {
				service = Service.create(wsdlURL, callbackServiceName);
			} catch (WebServiceException e) {
				if (LOGGER.isLoggable(Level.INFO)) {
					LOGGER.info("Service " + callbackServiceName +
							" is not defined in WSDL (old-style callback definition");
					LOGGER.info("Proceeding without callback service model");
					if (LOGGER.isLoggable(Level.FINER)) {
						LOGGER.log(Level.FINER, "Exception caught: ", e);
					}
				}
			}
		}
		final Dispatch<T> dispatch;
		if (service != null) {
			if (!service.getPorts().hasNext()) {
				service.addPort(callbackPortName, bindingId, replyToAddress);
			}
			dispatch = service.createDispatch(
	        		callbackPortName, sourceClass, mode);
	        dispatch.getRequestContext().put(
	        		BindingProvider.ENDPOINT_ADDRESS_PROPERTY, replyToAddress);
		} else {
			service = Service.create(callbackServiceName);
			service.addPort(callbackPortName, bindingId, replyToAddress);
			dispatch = service.createDispatch(
	        		callbackPortName, sourceClass, mode);
		}

        setupDispatch(dispatch);
        final Map<String, Object> requestContext = dispatch.getRequestContext();
        requestContext.put(RequestCallbackFeature.CALLCONTEXT_PROPERTY_NAME, this);
        // The current request context is still not thread local, but subsequent
        // calls to dispatch.getRequestContext() return a thread local one.
        requestContext.put(JaxWsClientProxy.THREAD_LOCAL_REQUEST_CONTEXT, Boolean.TRUE);
        if (operation != null) {
            requestContext.put(MessageContext.WSDL_OPERATION, operation);
            requestContext.put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
            final String action = soapAction == null || soapAction.length() == 0
            		? operation.getLocalPart() : soapAction;
            requestContext.put(BindingProvider.SOAPACTION_URI_PROPERTY, action);
        } else if (soapAction != null && soapAction.length() > 0) {
            requestContext.put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
            requestContext.put(BindingProvider.SOAPACTION_URI_PROPERTY, soapAction);
        }
		return dispatch;
	}

	public <T extends Source> Dispatch<T> createCallbackDispatch(
			final Class<T> sourceClass, final Service.Mode mode, final QName operation) {
		return createCallbackDispatch(sourceClass, mode, operation, null, null);
	}

	public <T extends Source> Dispatch<T> createCallbackDispatch(
			final Class<T> sourceClass, final Service.Mode mode, final QName operation, final String soapAction) {
		return createCallbackDispatch(sourceClass, mode, operation, soapAction, null);
	}

	public <T extends Source> Dispatch<T> createCallbackDispatch(
			final Class<T> sourceClass, final QName operation, final URL wsdlLocation) {
		return createCallbackDispatch(
				sourceClass, Service.Mode.PAYLOAD, operation, null, wsdlLocation);
	}

	public <T extends Source> Dispatch<T> createCallbackDispatch(
			final Class<T> sourceClass, final QName operation, final String soapAction, final URL wsdlLocation) {
		return createCallbackDispatch(
				sourceClass, Service.Mode.PAYLOAD, operation, soapAction, wsdlLocation);
	}

	public <T extends Source> Dispatch<T> createCallbackDispatch(
			final Class<T> sourceClass, final QName operation) {
		return createCallbackDispatch(sourceClass, Service.Mode.PAYLOAD, operation, null, null);
	}

	public <T extends Source> Dispatch<T> createCallbackDispatch(
			final Class<T> sourceClass, final QName operation, final String soapAction) {
		return createCallbackDispatch(sourceClass, Service.Mode.PAYLOAD, operation, soapAction, null);
	}

	public <T extends Source> Dispatch<T> createCallbackDispatch(final Class<T> sourceClass) {
		return createCallbackDispatch(sourceClass, Service.Mode.PAYLOAD, null, null, null);
	}

	public Dispatch<StreamSource> createCallbackDispatch(final QName operation) {
		return createCallbackDispatch(
				StreamSource.class, Service.Mode.PAYLOAD, operation, null, null);
	}

	public Dispatch<StreamSource> createCallbackDispatch(final QName operation, final String soapAction) {
		return createCallbackDispatch(
				StreamSource.class, Service.Mode.PAYLOAD, operation, soapAction, null);
	}

	public Dispatch<StreamSource> createCallbackDispatch() {
		return createCallbackDispatch(StreamSource.class, Service.Mode.PAYLOAD, null, null, null);
	}

	public <T extends Source> Dispatch<T> createCallbackDispatch(
			final Class<T> sourceClass, final Service.Mode mode,
			final QName operation, final URL wsdlLocationURL) {
		return createCallbackDispatch(sourceClass, mode, operation, null, wsdlLocationURL);
	}

	public static CallContext getCallContext(final WebServiceContext wsContext) {
		return getCallContext(wsContext.getMessageContext());
	}

	public static CallContext getCallContext(final Map<?, ?> contextHolder) {
		try {
			return (CallContext) contextHolder.get(
					RequestCallbackFeature.CALLCONTEXT_PROPERTY_NAME);
		} catch (ClassCastException e) {
			LOGGER.warning("Ignoring CallContext value of invalid type in contextual property");
			if (LOGGER.isLoggable(Level.FINER)) {
				LOGGER.log(Level.FINER, "Exception caught: ", e);
			}
			return null;
		}
	}

	public static Endpoint createCallbackEndpoint(
			final Object implementor, final String wsdlLocation) {
		return createCallbackEndpoint(implementor, new CallbackInfo(wsdlLocation));
	}

	public static Endpoint createCallbackEndpoint(
			final Object implementor, final URL wsdlLocation) {
		return createCallbackEndpoint(implementor, new CallbackInfo(wsdlLocation));
	}

	public static void setCallbackEndpoint(
			final Dispatch<?> dispatch, final Object callbackEndpoint) {
		dispatch.getRequestContext().put(
				RequestCallbackFeature.CALLBACK_ENDPOINT_PROPERTY_NAME, callbackEndpoint);
	}

	public static void setCallbackEndpoint(
			final Map<String, Object> context, final Object callbackEndpoint) {
		context.put(RequestCallbackFeature.CALLBACK_ENDPOINT_PROPERTY_NAME, callbackEndpoint);
	}

	public static void setupEndpoint(final Endpoint endpoint) {
		if (!(endpoint instanceof EndpointImpl)) {
			throw new IllegalArgumentException("Only CXF JAX-WS endpoints supported. ");
		}
		final EndpointImpl ep = (EndpointImpl) endpoint;
        final List<Feature> features = new ArrayList<Feature>();
        features.add(new RequestCallbackFeature());
        if (logging) {
        	features.add(new LoggingFeature());
        }
        if (serviceActivityMonitoring) {
            features.add(getEventFeature());
        }
        if (ep.getFeatures() != null) {
            features.addAll(ep.getFeatures());
        }
        ep.setFeatures(features);
        ep.getProperties().put(NULL_MEANS_ONEWAY, Boolean.TRUE);
	}

	public static void setupDispatch(final Dispatch<?> dispatch) {
		if (!(dispatch instanceof DispatchImpl)) {
			throw new IllegalArgumentException("Only CXF JAX-WS Dispatch supported. ");
		}
		final DispatchImpl<?> dsp = (DispatchImpl<?>) dispatch;
        final Client dispatchClient = dsp.getClient();
        final Bus bus = dispatchClient.getBus();
        (new RequestCallbackFeature()).initialize(dispatchClient, bus);
        if (logging) {
	        (new LoggingFeature()).initialize(dispatchClient, bus);
        }
        if (serviceActivityMonitoring) {
            getEventFeature().initialize(dispatchClient, bus);
        }
	}

	public static void setupDispatch(final Dispatch<?> dispatch, final Object callbackEndpoint) {
		setupDispatch(dispatch);
		setCallbackEndpoint(dispatch, callbackEndpoint);
	}

	public static void setupServerFactory(final JaxWsServerFactoryBean serverFactory) {
		final List<Feature> features = serverFactory.getFeatures();
        features.add(new RequestCallbackFeature());
        if (logging) {
	        features.add(new LoggingFeature());
        }
        if (serviceActivityMonitoring) {
            features.add(getEventFeature());
        }
        serverFactory.getProperties(true).put(NULL_MEANS_ONEWAY, Boolean.TRUE);
	}

	public <T> void setupCallbackProxy(final T proxy) {
		final Client client = ClientProxy.getClient(proxy);
		final Bus bus = client.getBus();
        (new RequestCallbackFeature()).initialize(client, bus);
        if (logging) {
	        (new LoggingFeature()).initialize(client, bus);
        }
        if (serviceActivityMonitoring) {
        	getEventFeature().initialize(client, bus);
        }
        final BindingProvider bp = (BindingProvider) proxy;
		bp.getRequestContext().put(
				JaxWsClientProxy.THREAD_LOCAL_REQUEST_CONTEXT, Boolean.TRUE);
		bp.getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY, replyToAddress);
		bp.getRequestContext().put(
				RequestCallbackFeature.CALLCONTEXT_PROPERTY_NAME, this);
	}

	public static CallbackInfo createCallbackInfo(final String wsdlLocation) {
		return new CallbackInfo(wsdlLocation);
	}

	public static CallbackInfo createCallbackInfo(final URL wsdlLocationURL) {
		return new CallbackInfo(wsdlLocationURL);
	}

	public static void enforceOperation(
			final QName operationName, final String soapAction, final Dispatch<?> dispatch) {
		enforceOperation(operationName, soapAction, dispatch.getRequestContext());
	}

	public static void enforceOperation(final QName operationName, final Dispatch<?> dispatch) {
		enforceOperation(operationName, null, dispatch.getRequestContext());
	}

	public static void enforceOperation(
			final QName operationName, final Map<String, Object> requestContext) {
		enforceOperation(operationName, null, requestContext);
	}

	public static void enforceOperation(
			final QName operationName, final String soapAction,
			final Map<String, Object> requestContext) {
		if (operationName == null) {
			throw new IllegalArgumentException("Invalid operation name: (null) ");
		}
        requestContext.put(MessageContext.WSDL_OPERATION, operationName);
        requestContext.put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        final String action = soapAction == null || soapAction.length() == 0
        		? operationName.getLocalPart() : soapAction;
        requestContext.put(BindingProvider.SOAPACTION_URI_PROPERTY, action);
	}

	public static Configuration resolveConfiguration(final QName serviceName) {
		return ConfigurationInitializer.resolveConfiguration(serviceName);
	}

	public static Endpoint createCallbackEndpoint(
			final Object implementor, final CallbackInfo cbInfo) {
		final Bus bus = BusFactory.getThreadDefaultBus();
		final JaxWsServerFactoryBean serverFactory = new JaxWsServerFactoryBean();
        final List<Feature> features = new ArrayList<Feature>();
        features.add(new RequestCallbackFeature());
        if (logging) {
        	features.add(new LoggingFeature());
        }
        if (serviceActivityMonitoring) {
            features.add(getEventFeature());
        }

		serverFactory.setFeatures(features);
		final QName cbInterfaceName = cbInfo == null ? null : cbInfo.getCallbackPortTypeName();
		final String wsdlLocation = cbInfo == null ? null : cbInfo.getWsdlLocation();
		final boolean useWsdlLocation = wsdlLocation != null &&
				cbInfo.getCallbackServiceName() != null &&
				cbInfo.getCallbackPortName() != null;
		if (cbInterfaceName != null) {
			final QName cbServiceName = cbInfo.getCallbackServiceName() == null
					? new QName(cbInterfaceName.getNamespaceURI(),
							cbInterfaceName.getLocalPart() + "Service")
					: cbInfo.getCallbackServiceName();
			final QName cbEndpointName = cbInfo.getCallbackServiceName() == null
					? new QName(cbInterfaceName.getNamespaceURI(),
							cbInterfaceName.getLocalPart() + "ServicePort")
					: new QName(cbServiceName.getNamespaceURI(),
							cbInfo.getCallbackPortName() == null
									? cbServiceName.getLocalPart() + "Port"
									: cbInfo.getCallbackPortName());
			serverFactory.setServiceName(cbServiceName);
			serverFactory.setEndpointName(cbEndpointName);
			final List<AbstractServiceConfiguration> svcConfigs =
					serverFactory.getServiceFactory().getServiceConfigurations();
			for (ListIterator<AbstractServiceConfiguration> it = svcConfigs.listIterator();
					it.hasNext(); ) {
				final AbstractServiceConfiguration cfg = it.next();
				if (cfg instanceof DefaultServiceConfiguration) {
					final AbstractServiceConfiguration ncfg =
							new CallbackDefaultServiceConfiguration(cbInfo);
					it.set(ncfg);
				}
			}
			if (useWsdlLocation) {
				serverFactory.setWsdlLocation(wsdlLocation);
			}
		}
		final EndpointImpl endpoint = new EndpointImpl(bus, implementor, serverFactory);
		endpoint.setFeatures(features);
        endpoint.getProperties().put(NULL_MEANS_ONEWAY, Boolean.TRUE);
        if (cbInterfaceName != null) {
        	endpoint.setEndpointName(serverFactory.getEndpointName());
        	endpoint.setServiceName(serverFactory.getServiceName());
        	if (useWsdlLocation) {
        		endpoint.setWsdlLocation(wsdlLocation);
        	}
        }
		return endpoint;
	}

	private static Feature getEventFeature() {
	    return (Feature) samContext.getBean("eventFeature");
	}

	private static URL toWsdlUrl(final String wsdlLocation) throws MalformedURLException {
		if (wsdlLocation == null || wsdlLocation.length() == 0) {
			return null;
		}
        if (isWsdlUrlString(wsdlLocation)) {
        	if (wsdlLocation.startsWith(CLASSPATH_URL_PREFIX)) {
        		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        		if (cl == null) {
        			return null;
        		}
        		final int len = wsdlLocation.length();
        		for (int ndx = CLASSPATH_URL_PREFIX_LENGTH; ndx < len; ndx++) {
        			if (wsdlLocation.charAt(ndx) != '/') {
        				return cl.getResource(wsdlLocation.substring(ndx));
        			}
        		}
        		return null;
        	}
            return new URL(wsdlLocation);
        }
        return (new File(wsdlLocation)).toURI().toURL();
    }

    private static boolean isWsdlUrlString(final String wsdlLocation) {
        if (wsdlLocation == null || wsdlLocation.length() == 0) {
            return false;
        }
        return wsdlLocation.startsWith("file:/") || wsdlLocation.startsWith("http://")
                || wsdlLocation.startsWith("https://")
                || wsdlLocation.startsWith(CLASSPATH_URL_PREFIX);
    }
}
