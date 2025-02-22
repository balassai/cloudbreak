package com.sequenceiq.cloudbreak.reactor.api.event.cluster;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sequenceiq.cloudbreak.common.event.FlowPayload;
import com.sequenceiq.cloudbreak.reactor.api.ClusterPlatformResult;

public class ClusterStartPollingResult extends ClusterPlatformResult<ClusterStartPollingRequest> implements FlowPayload {

    public ClusterStartPollingResult(ClusterStartPollingRequest request) {
        super(request);
    }

    @JsonCreator
    public ClusterStartPollingResult(
            @JsonProperty("statusReason") String statusReason,
            @JsonProperty("errorDetails") Exception errorDetails,
            @JsonProperty("request") ClusterStartPollingRequest request) {
        super(statusReason, errorDetails, request);
    }
}
