package org.talend.esb.policy.transformation;

import java.util.Arrays;
import org.apache.cxf.ws.policy.AbstractPolicyInterceptorProvider;

public class TransformationInterceptorProvider extends AbstractPolicyInterceptorProvider {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 5697743589425687362L;
   
    public TransformationInterceptorProvider() {
        super(Arrays.asList(TransformationPolicyBuilder.TRANSFORMATION));
        
        XSLTOutPolicyInterceptor out = new XSLTOutPolicyInterceptor();
        XSLTInPolicyInterceptor in = new XSLTInPolicyInterceptor();

        this.getOutInterceptors().add(out);
        this.getOutFaultInterceptors().add(out);        
        this.getInInterceptors().add(in);
   }        
} 
