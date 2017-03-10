package org.osc.controller.nuage.api;

import static org.osc.sdk.controller.Constants.PLUGIN_NAME;
import static org.osc.sdk.controller.Constants.QUERY_PORT_INFO;
import static org.osc.sdk.controller.Constants.SUPPORT_FAILURE_POLICY;
import static org.osc.sdk.controller.Constants.SUPPORT_OFFBOX_REDIRECTION;
import static org.osc.sdk.controller.Constants.SUPPORT_PORT_GROUP;
import static org.osc.sdk.controller.Constants.SUPPORT_SFC;
import static org.osc.sdk.controller.Constants.USE_PROVIDER_CREDS;

import java.util.HashMap;
import java.util.List;

import org.osc.sdk.controller.FailurePolicyType;
import org.osc.sdk.controller.FlowInfo;
import org.osc.sdk.controller.FlowPortInfo;
import org.osc.sdk.controller.Status;
import org.osc.sdk.controller.TagEncapsulationType;
import org.osc.sdk.controller.api.SdnControllerApi;
import org.osc.sdk.controller.element.InspectionHookElement;
import org.osc.sdk.controller.element.InspectionPortElement;
import org.osc.sdk.controller.element.NetworkElement;
import org.osc.sdk.controller.element.VirtualizationConnectorElement;
import org.osc.sdk.controller.exception.NetworkPortNotFoundException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.springframework.util.CollectionUtils;

@Component(scope=ServiceScope.PROTOTYPE,
property={
        PLUGIN_NAME + "=Nuage",
        SUPPORT_OFFBOX_REDIRECTION + ":Boolean=true",
        SUPPORT_SFC + ":Boolean=false",
        SUPPORT_FAILURE_POLICY + ":Boolean=false",
        USE_PROVIDER_CREDS + ":Boolean=false",
        QUERY_PORT_INFO + ":Boolean=false",
        SUPPORT_PORT_GROUP + ":Boolean=true"})
public class NuageSdnControllerApi implements SdnControllerApi {

    private VirtualizationConnectorElement vc;
    @SuppressWarnings("unused")
    private String region;

    public NuageSdnControllerApi() {
    }

    public NuageSdnControllerApi(VirtualizationConnectorElement vc, String region) throws Exception {
        this.vc = vc;
        this.region = region;
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public Status getStatus() throws Exception {
        Status status = null;
        try(NuageSecurityControllerApi nuageSecApi = new NuageSecurityControllerApi(this.vc); ) {
            nuageSecApi.test();
        }
        status = new Status("Nuage", "0.1", true);
        return status;
    }

    @Override
    public void installInspectionHook(NetworkElement policyGroup, InspectionPortElement inspectionPort, Long tag,
            TagEncapsulationType encType, Long order, FailurePolicyType failurePolicyType)
            throws NetworkPortNotFoundException, Exception {
        try (NuageSecurityControllerApi nuageSecApi = new NuageSecurityControllerApi(this.vc)){
            nuageSecApi.installInspectionHook(policyGroup, inspectionPort);
        }
    }

    @Override
    public void removeInspectionHook(NetworkElement policyGroup, InspectionPortElement inspectionPort)
            throws Exception {
        try (NuageSecurityControllerApi nuageSecApi = new NuageSecurityControllerApi(this.vc)){
            nuageSecApi.removeInspectionHook(policyGroup, inspectionPort);
        }
    }

    @Override
    public void removeAllInspectionHooks(NetworkElement inspectedPort) throws Exception {

    }

    @Override
    public void setInspectionHookTag(NetworkElement inspectedPort, InspectionPortElement inspectionPort, Long tag)
            throws NetworkPortNotFoundException, Exception {
    }

    @Override
    public Long getInspectionHookTag(NetworkElement inspectedPort, InspectionPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
        return null;
    }

    @Override
    public void setInspectionHookFailurePolicy(NetworkElement inspectedPort, InspectionPortElement inspectionPort,
            FailurePolicyType failurePolicyType) throws NetworkPortNotFoundException, Exception {

    }

    @Override
    public FailurePolicyType getInspectionHookFailurePolicy(NetworkElement inspectedPort,
            InspectionPortElement inspectionPort) throws NetworkPortNotFoundException, Exception {
        return null;
    }

    @Override
    public InspectionHookElement getInspectionHook(NetworkElement inspectedPort,
            InspectionPortElement inspectionPort) throws NetworkPortNotFoundException, Exception {
        return null;
    }

    @Override
    public void updateInspectionHook(InspectionHookElement inspectionHook)
            throws NetworkPortNotFoundException, Exception {

    }

    @Override
    public void updateInspectionHook(NetworkElement inspectedPort, InspectionPortElement inspectionPort, Long tag,
            TagEncapsulationType encType, Long order, FailurePolicyType failurePolicyType)
            throws NetworkPortNotFoundException, Exception {

    }

    @Override
    public InspectionPortElement getInspectionPort(InspectionPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
        String domainId = null;
        if (inspectionPort !=null && inspectionPort.getIngressPort() !=null){
            domainId = inspectionPort.getIngressPort().getParentId();
            try (NuageSecurityControllerApi nuageSecApi = new NuageSecurityControllerApi(this.vc)){
                return nuageSecApi.getRedirectionTarget(inspectionPort, domainId);
            }
        }
        return null;
    }

    @Override
    public void setInspectionHookOrder(NetworkElement inspectedPort, InspectionPortElement inspectionPort,
            Long order) throws NetworkPortNotFoundException, Exception {
    }

    @Override
    public Long getInspectionHookOrder(NetworkElement inspectedPort, InspectionPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
        return null;
    }

    @Override
    public void setVirtualizationConnector(VirtualizationConnectorElement vc) {
        this.vc = vc;

    }

    @Override
    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public HashMap<String, FlowPortInfo> queryPortInfo(HashMap<String, FlowInfo> portsQuery) throws Exception {
        throw new UnsupportedOperationException("Nuage SDN Controller does not support flow based query");
    }

    @Override
    public NetworkElement registerNetworkElement(List<NetworkElement> elements) throws Exception {
        String domainId = CollectionUtils.isEmpty(elements) ? null : elements.get(0).getParentId();
        NetworkElement policyGroup = null;
        try (NuageSecurityControllerApi nuageSecApi = new NuageSecurityControllerApi(this.vc)){
            policyGroup = nuageSecApi.createPolicyGroup(elements, domainId);
        }
        return policyGroup;
    }

    @Override
    public void deleteNetworkElement(NetworkElement policyGroup) throws Exception {
        try (NuageSecurityControllerApi nuageSecApi = new NuageSecurityControllerApi(this.vc)){
            nuageSecApi.deletePolicyGroup(policyGroup );
        }
    }

    @Override
    public List<NetworkElement> getNetworkElements(NetworkElement element) throws Exception {
        return null;
    }

    @Override
    public NetworkElement updateNetworkElement(NetworkElement policyGroup, List<NetworkElement> protectedPorts)
            throws Exception {
        String domainId = CollectionUtils.isEmpty(protectedPorts) ? null : protectedPorts.get(0).getParentId();
        try (NuageSecurityControllerApi nuageSecApi = new NuageSecurityControllerApi(this.vc)){
            return nuageSecApi.updatePolicyGroup(policyGroup, protectedPorts, domainId);
        }
    }

    @Override
    public void registerInspectionPort(InspectionPortElement inspectionPort)
            throws NetworkPortNotFoundException, Exception {
        String domainId = null;
        if (inspectionPort !=null && inspectionPort.getIngressPort() !=null){
            domainId = inspectionPort.getIngressPort().getParentId();
            try (NuageSecurityControllerApi nuageSecApi = new NuageSecurityControllerApi(this.vc)){
                nuageSecApi.createRedirectionTarget(inspectionPort, domainId);
            }
        }
    }

}