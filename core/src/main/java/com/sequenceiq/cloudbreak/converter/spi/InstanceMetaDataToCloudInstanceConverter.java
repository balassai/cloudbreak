package com.sequenceiq.cloudbreak.converter.spi;

import static com.sequenceiq.cloudbreak.common.network.NetworkConstants.SUBNET_ID;
import static com.sequenceiq.cloudbreak.util.NullUtil.putIfPresent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.InstanceAuthentication;
import com.sequenceiq.cloudbreak.cloud.model.InstanceStatus;
import com.sequenceiq.cloudbreak.cloud.model.InstanceTemplate;
import com.sequenceiq.cloudbreak.common.exception.NotFoundException;
import com.sequenceiq.cloudbreak.common.mappable.CloudPlatform;
import com.sequenceiq.cloudbreak.converter.InstanceMetadataToImageIdConverter;
import com.sequenceiq.cloudbreak.domain.StackAuthentication;
import com.sequenceiq.cloudbreak.domain.Template;
import com.sequenceiq.cloudbreak.dto.InstanceGroupDto;
import com.sequenceiq.cloudbreak.service.environment.EnvironmentClientService;
import com.sequenceiq.cloudbreak.service.stack.InstanceGroupService;
import com.sequenceiq.cloudbreak.view.InstanceGroupView;
import com.sequenceiq.cloudbreak.view.InstanceMetadataView;
import com.sequenceiq.cloudbreak.view.StackView;
import com.sequenceiq.environment.api.v1.environment.model.response.DetailedEnvironmentResponse;

@Component
public class InstanceMetaDataToCloudInstanceConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceMetaDataToCloudInstanceConverter.class);

    @Inject
    private StackToCloudStackConverter stackToCloudStackConverter;

    @Inject
    private InstanceMetadataToImageIdConverter instanceMetadataToImageIdConverter;

    @Inject
    private EnvironmentClientService environmentClientService;

    @Inject
    private InstanceGroupService instanceGroupService;

    public List<CloudInstance> convert(List<InstanceGroupDto> instanceGroups, String envCrn, StackAuthentication stackAuthentication) {
        List<CloudInstance> cloudInstances = new ArrayList<>();
        DetailedEnvironmentResponse environment = environmentClientService.getByCrnAsInternal(envCrn);
        for (InstanceGroupDto instanceGroup : instanceGroups) {
            for (InstanceMetadataView metaDataEntity : instanceGroup.getNotDeletedAndNotZombieInstanceMetaData()) {
                cloudInstances.add(convert(metaDataEntity, instanceGroup.getInstanceGroup(), environment, stackAuthentication));
            }
        }
        return cloudInstances;
    }

    public List<CloudInstance> convert(Collection<InstanceMetadataView> instanceMetadataViews, StackView stack) {
        List<CloudInstance> cloudInstances = new ArrayList<>();
        DetailedEnvironmentResponse environment = environmentClientService.getByCrnAsInternal(stack.getEnvironmentCrn());
        logConvertDetails(environment.getCloudPlatform(), String.format("Converting to cloudInstances based on input param instanceDataViews: %s",
                Joiner.on(",").join(instanceMetadataViews.stream()
                                .map(imd -> String.format("[name: %s, instance group name: %s]", imd.getInstanceName(), imd.getInstanceGroupName()))
                                .collect(Collectors.toSet()))));
        Set<String> instanceGroupNames = instanceMetadataViews.stream().map(im -> im.getInstanceGroupName()).collect(Collectors.toSet());
        logConvertDetails(environment.getCloudPlatform(),
                String.format("InstanceGroup names has been collected based on input param instanceMetadataViews, result: %s",
                        Joiner.on(",").join(instanceGroupNames)));
        List<InstanceGroupView> instanceGroupViews = instanceGroupService
                .findAllInstanceGroupViewByStackIdAndGroupName(stack.getId(), instanceGroupNames);
        logConvertDetails(environment.getCloudPlatform(),
                String.format("InstanceGroupViews has been collected based on previously collected group names: %s",
                        Joiner.on(",").join(instanceGroupViews.stream()
                                .map(ig -> String.format("[name: %s, type: %s]", ig.getGroupName(), ig.getInstanceGroupType()))
                                .collect(Collectors.toSet()))));
        for (InstanceMetadataView instanceMetadataView : instanceMetadataViews) {
            InstanceGroupView instanceGroupView = instanceGroupViews.stream()
                    .filter(ig -> ig.getGroupName().equals(instanceMetadataView.getInstanceGroupName()))
                    .findFirst()
                    .orElseThrow(NotFoundException.notFound("InstanceGroup", instanceMetadataView.getInstanceGroupName()));
            cloudInstances.add(convert(instanceMetadataView, instanceGroupView, environment, stack.getStackAuthentication()));
        }
        return cloudInstances;
    }

    public List<CloudInstance> convert(List<InstanceMetadataView> metaDataEntities, InstanceGroupView group, String envCrn,
            StackAuthentication stackAuthentication) {
        List<CloudInstance> cloudInstances = new ArrayList<>();
        DetailedEnvironmentResponse environment = environmentClientService.getByCrnAsInternal(envCrn);
        for (InstanceMetadataView metaDataEntity : metaDataEntities) {
            cloudInstances.add(convert(metaDataEntity, group, environment, stackAuthentication));
        }
        return cloudInstances;
    }

    public CloudInstance convert(InstanceMetadataView metaDataEntity, InstanceGroupView group,
            DetailedEnvironmentResponse environment, StackAuthentication stackAuthentication) {
        Template template = group.getTemplate();
        InstanceStatus status = getInstanceStatus(metaDataEntity);
        String imageId = instanceMetadataToImageIdConverter.convert(metaDataEntity);
        InstanceTemplate instanceTemplate = stackToCloudStackConverter.buildInstanceTemplate(template, group.getGroupName(), metaDataEntity.getPrivateId(),
                status, imageId);
        InstanceAuthentication instanceAuthentication = new InstanceAuthentication(
                stackAuthentication.getPublicKey(),
                stackAuthentication.getPublicKeyId(),
                stackAuthentication.getLoginUserName());
        Map<String, Object> params = new HashMap<>();
        putIfPresent(params, SUBNET_ID, metaDataEntity.getSubnetId());
        putIfPresent(params, CloudInstance.INSTANCE_NAME, metaDataEntity.getInstanceName());
        Map<String, Object> cloudInstanceParameters = stackToCloudStackConverter.buildCloudInstanceParameters(
                environment,
                metaDataEntity,
                CloudPlatform.valueOf(template.getCloudPlatform()));
        params.putAll(cloudInstanceParameters);

        return new CloudInstance(
                metaDataEntity.getInstanceId(),
                instanceTemplate,
                instanceAuthentication,
                metaDataEntity.getSubnetId(),
                metaDataEntity.getAvailabilityZone(),
                params);
    }

    private InstanceStatus getInstanceStatus(InstanceMetadataView metaData) {
        switch (metaData.getInstanceStatus()) {
            case REQUESTED:
                return InstanceStatus.CREATE_REQUESTED;
            case CREATED:
                return InstanceStatus.CREATED;
            case SERVICES_RUNNING:
            case SERVICES_HEALTHY:
            case SERVICES_UNHEALTHY:
                return InstanceStatus.STARTED;
            // TODO ZZZ: Using DECOMMISSIONED is a bad idea based on this conversion. DECOMMISSIONED somehow means DELETE_REQUESTED, which
            //  is going to cause chaos on the user side. SERVICES_RUNNING is just an extremely STUPID state.
            case DECOMMISSIONED:
            case DELETE_REQUESTED:
                return InstanceStatus.DELETE_REQUESTED;
            case TERMINATED:
                return InstanceStatus.TERMINATED;
            case DELETED_BY_PROVIDER:
                return InstanceStatus.TERMINATED_BY_PROVIDER;
            default:
                return InstanceStatus.UNKNOWN;
        }
    }

    // we need logs to investigate CB-18662, which seems GCP related, so filtering for GCP environments to avoid overgeneration of logs
    private void logConvertDetails(String cloudPlatform, String message) {
        if (cloudPlatform != null && CloudPlatform.GCP.equals(CloudPlatform.valueOf(cloudPlatform))) {
            LOGGER.debug(message);
        }
    }

}
