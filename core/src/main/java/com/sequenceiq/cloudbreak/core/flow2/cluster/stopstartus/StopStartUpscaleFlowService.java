package com.sequenceiq.cloudbreak.core.flow2.cluster.stopstartus;

import static com.sequenceiq.cloudbreak.api.endpoint.v4.common.Status.UPDATE_FAILED;
import static com.sequenceiq.cloudbreak.api.endpoint.v4.common.Status.UPDATE_IN_PROGRESS;
import static com.sequenceiq.cloudbreak.event.ResourceEvent.CLUSTER_SCALING_STOPSTART_UPSCALE_COMMISSIONING;
import static com.sequenceiq.cloudbreak.event.ResourceEvent.CLUSTER_SCALING_STOPSTART_UPSCALE_COMMISSIONING2;
import static com.sequenceiq.cloudbreak.event.ResourceEvent.CLUSTER_SCALING_STOPSTART_UPSCALE_COMMISSION_FAILED;
import static com.sequenceiq.cloudbreak.event.ResourceEvent.CLUSTER_SCALING_STOPSTART_UPSCALE_COULDNOTCOMMISSION;
import static com.sequenceiq.cloudbreak.event.ResourceEvent.CLUSTER_SCALING_STOPSTART_UPSCALE_FAILED;
import static com.sequenceiq.cloudbreak.event.ResourceEvent.CLUSTER_SCALING_STOPSTART_UPSCALE_FINISHED;
import static com.sequenceiq.cloudbreak.event.ResourceEvent.CLUSTER_SCALING_STOPSTART_UPSCALE_INADEQUATE_NODES;
import static com.sequenceiq.cloudbreak.event.ResourceEvent.CLUSTER_SCALING_STOPSTART_UPSCALE_INIT;
import static com.sequenceiq.cloudbreak.event.ResourceEvent.CLUSTER_SCALING_STOPSTART_UPSCALE_NODES_NOT_STARTED;
import static com.sequenceiq.cloudbreak.event.ResourceEvent.CLUSTER_SCALING_STOPSTART_UPSCALE_NODES_STARTED;
import static com.sequenceiq.cloudbreak.event.ResourceEvent.CLUSTER_SCALING_STOPSTART_UPSCALE_IDENTIFIEDRECOVERYCANDIDATES;
import static com.sequenceiq.cloudbreak.event.ResourceEvent.CLUSTER_SCALING_STOPSTART_UPSCALE_NODES_STARTED_WITH_RECOVERY_CANDIDATES;
import static com.sequenceiq.cloudbreak.event.ResourceEvent.CLUSTER_SCALING_STOPSTART_UPSCALE_STARTING;
import static com.sequenceiq.cloudbreak.event.ResourceEvent.CLUSTER_SCALING_STOPSTART_UPSCALE_START_FAILED;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v4.common.DetailedStackStatus;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base.InstanceStatus;
import com.sequenceiq.cloudbreak.cloud.model.CloudInstance;
import com.sequenceiq.cloudbreak.cloud.model.CloudVmInstanceStatus;
import com.sequenceiq.cloudbreak.core.flow2.stack.CloudbreakFlowMessageService;
import com.sequenceiq.cloudbreak.dto.StackDtoDelegate;
import com.sequenceiq.cloudbreak.service.StackUpdater;
import com.sequenceiq.cloudbreak.service.stack.InstanceMetaDataService;
import com.sequenceiq.cloudbreak.view.InstanceMetadataView;

@Component
class StopStartUpscaleFlowService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StopStartUpscaleFlowService.class);

    @Inject
    private CloudbreakFlowMessageService flowMessageService;

    @Inject
    private StackUpdater stackUpdater;

    @Inject
    private InstanceMetaDataService instanceMetaDataService;

    void initScaleUp(long stackId, String hostGroupName) {
        stackUpdater.updateStackStatus(stackId, DetailedStackStatus.UPSCALE_BY_START_IN_PROGRESS,
                String.format("Scaling up (stopstart) host  group: %s", hostGroupName));
        flowMessageService.fireEventAndLog(stackId, UPDATE_IN_PROGRESS.name(), CLUSTER_SCALING_STOPSTART_UPSCALE_INIT, hostGroupName);
    }

    void startingInstances(long stackId, String hostGroup, int instancesToStart, List<CloudInstance> recoveryCandidates) {
        if (!recoveryCandidates.isEmpty()) {
            flowMessageService.fireEventAndLog(stackId, UPDATE_IN_PROGRESS.name(), CLUSTER_SCALING_STOPSTART_UPSCALE_IDENTIFIEDRECOVERYCANDIDATES,
                    String.valueOf(instancesToStart),
                    hostGroup,
                    String.valueOf(recoveryCandidates.size()),
                    recoveryCandidates.stream().map(CloudInstance::getInstanceId).collect(Collectors.joining(", ")));
        } else {
            flowMessageService.fireEventAndLog(stackId, UPDATE_IN_PROGRESS.name(), CLUSTER_SCALING_STOPSTART_UPSCALE_STARTING,
                    String.valueOf(instancesToStart),
                    hostGroup);
        }
    }

    void instancesStarted(long stackId, List<InstanceMetadataView> instancesStarted, List<InstanceMetadataView> recoveryCandidates) {
        instancesStarted.stream().forEach(x -> instanceMetaDataService.updateInstanceStatus(x, InstanceStatus.SERVICES_RUNNING));
        stackUpdater.updateStackStatus(stackId, DetailedStackStatus.STARTING_CLUSTER_MANAGER_SERVICES,
                "Instances: " + instancesStarted.size() + " started successfully.");
        if (recoveryCandidates.isEmpty()) {
            flowMessageService.fireEventAndLog(stackId, UPDATE_IN_PROGRESS.name(), CLUSTER_SCALING_STOPSTART_UPSCALE_NODES_STARTED,
                    String.valueOf(instancesStarted.size()), instancesStarted.stream().map(InstanceMetadataView::getInstanceId)
                            .collect(Collectors.joining(", ")));
        } else {
            flowMessageService.fireEventAndLog(stackId, UPDATE_IN_PROGRESS.name(), CLUSTER_SCALING_STOPSTART_UPSCALE_NODES_STARTED_WITH_RECOVERY_CANDIDATES,
                    String.valueOf(instancesStarted.size()), instancesStarted.stream().map(InstanceMetadataView::getInstanceId)
                            .collect(Collectors.joining(", ")),
                    String.valueOf(recoveryCandidates.size()), recoveryCandidates.stream().map(InstanceMetadataView::getInstanceId)
                            .collect(Collectors.joining(", ")));
        }
    }

    void logInstancesFailedToStart(long stackId, List<CloudVmInstanceStatus> notStartedIntances) {
        // TODO CB-14929: CB-15418 This needs to be an orange message (i.e. not success, not failure). Need to figure out how the UI
        //  processes these and applies icons.
        flowMessageService.fireEventAndLog(stackId, UPDATE_FAILED.name(), CLUSTER_SCALING_STOPSTART_UPSCALE_NODES_NOT_STARTED,
                String.valueOf(notStartedIntances.size()),
                        notStartedIntances.stream()
                                .map(x -> String.format("{%s : %s}", x.getCloudInstance().getInstanceId(), x.getStatus()))
                                .collect(Collectors.joining(", ", "{", "}")));
    }

    void warnNotEnoughInstances(long stackId, String hostGroupName, int desiredCount, int addedCount) {
        // TODO CB-14929: CB-15418 This needs to be an orange message (i.e. not success, not failure). Need to figure out how the UI
        //  processes these and applies icons.
        flowMessageService.fireEventAndLog(stackId, UPDATE_FAILED.name(), CLUSTER_SCALING_STOPSTART_UPSCALE_INADEQUATE_NODES,
                hostGroupName,
                String.valueOf(desiredCount), String.valueOf(addedCount));
    }

    void upscaleCommissioningNodes(long stackId, String hostGroupName, List<InstanceMetadataView> startedInstances,
            List<InstanceMetadataView> instancesWithServicesNotRunning) {
        stackUpdater.updateStackStatus(stackId, DetailedStackStatus.STARTING_CLUSTER_MANAGER_SERVICES,
                String.format("Commissioning via CM in hostGroup: %s", hostGroupName));
        if (instancesWithServicesNotRunning.size() != 0) {
            flowMessageService.fireEventAndLog(stackId, UPDATE_IN_PROGRESS.name(), CLUSTER_SCALING_STOPSTART_UPSCALE_COMMISSIONING2,
                    hostGroupName,
                    String.valueOf(startedInstances.size()),
                    startedInstances.stream().map(InstanceMetadataView::getDiscoveryFQDN).collect(Collectors.joining(", ")),
                    String.valueOf(instancesWithServicesNotRunning.size()),
                    instancesWithServicesNotRunning.stream().map(InstanceMetadataView::getDiscoveryFQDN).collect(Collectors.joining(", ")));
        } else {
            flowMessageService.fireEventAndLog(stackId, UPDATE_IN_PROGRESS.name(), CLUSTER_SCALING_STOPSTART_UPSCALE_COMMISSIONING,
                    hostGroupName,
                    String.valueOf(startedInstances.size()),
                    startedInstances.stream().map(InstanceMetadataView::getDiscoveryFQDN).collect(Collectors.joining(", ")));
        }
    }

    void logInstancesFailedToCommission(long stackId, List<String> notCommissionedFqdns) {
        // TODO CB-14929: CB-15418 This needs to be an orange message (i.e. not success, not failure). Need to figure out how the UI
        //  processes these and applies icons.
        flowMessageService.fireEventAndLog(stackId, UPDATE_FAILED.name(), CLUSTER_SCALING_STOPSTART_UPSCALE_COULDNOTCOMMISSION,
                String.valueOf(notCommissionedFqdns.size()), String.join(", ", notCommissionedFqdns));
    }

    void clusterUpscaleFinished(StackDtoDelegate stack, String hostgroupName, List<InstanceMetadataView> commissioned, DetailedStackStatus finalStackStatus) {
        LOGGER.debug("StopStart upscale finished successfully. CommissionedCount={}", commissioned.size());
        commissioned.stream().forEach(x -> instanceMetaDataService.updateInstanceStatus(x, InstanceStatus.SERVICES_HEALTHY));
        stackUpdater.updateStackStatus(stack.getId(), finalStackStatus, String.format("finished starting nodes"));
        flowMessageService.fireEventAndLog(stack.getId(), finalStackStatus.getStatus().name(), CLUSTER_SCALING_STOPSTART_UPSCALE_FINISHED,
                hostgroupName, String.valueOf(commissioned.size()),
                commissioned.stream().map(InstanceMetadataView::getDiscoveryFQDN).collect(Collectors.joining(", ")));
    }

    void startInstancesFailed(long stackId, List<CloudInstance> instancesRequested) {
        // TODO CB-15132. This message can be improved to include a precise list of nodes acted upon, depending on
        //  where the failure occurred.
        flowMessageService.fireEventAndLog(stackId, UPDATE_FAILED.name(), CLUSTER_SCALING_STOPSTART_UPSCALE_START_FAILED,
                String.valueOf(instancesRequested.size()),
                instancesRequested.stream().map(CloudInstance::getInstanceId).collect(Collectors.joining(", ")));
    }

    void commissionViaCmFailed(long stackId, List<InstanceMetadataView> startedInstancesToCommission) {
        flowMessageService.fireEventAndLog(stackId, UPDATE_FAILED.name(), CLUSTER_SCALING_STOPSTART_UPSCALE_COMMISSION_FAILED,
                String.valueOf(startedInstancesToCommission.size()),
                getHostNames(startedInstancesToCommission).stream().collect(Collectors.joining(", ")));
    }

    void clusterUpscaleFailed(long stackId, Exception errorDetails) {
        LOGGER.info("Error during stopstart upscale flow: " + errorDetails.getMessage(), errorDetails);
        stackUpdater.updateStackStatus(stackId, DetailedStackStatus.UPSCALE_BY_START_FAILED,
                String.format("Node(s) could not be upscaled via startstop: %s", errorDetails));
        flowMessageService.fireEventAndLog(stackId, UPDATE_FAILED.name(), CLUSTER_SCALING_STOPSTART_UPSCALE_FAILED, errorDetails.getMessage());
    }

    private List<String> getHostNames(Collection<InstanceMetadataView> instanceMetaData) {
        return instanceMetaData.stream().map(InstanceMetadataView::getDiscoveryFQDN).collect(Collectors.toList());
    }
}
