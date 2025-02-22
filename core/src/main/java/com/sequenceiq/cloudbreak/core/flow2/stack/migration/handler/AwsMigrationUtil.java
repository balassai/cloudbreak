package com.sequenceiq.cloudbreak.core.flow2.stack.migration.handler;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesResult;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.sequenceiq.cloudbreak.cloud.aws.AwsCloudFormationClient;
import com.sequenceiq.cloudbreak.cloud.aws.CloudFormationStackUtil;
import com.sequenceiq.cloudbreak.cloud.aws.common.view.AwsCredentialView;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;

@Component
public class AwsMigrationUtil {

    private static final Logger LOGGER = getLogger(AwsMigrationUtil.class);

    @Inject
    private CloudFormationStackUtil cfStackUtil;

    @Inject
    private AwsCloudFormationClient awsClient;

    public boolean allInstancesDeletedFromCloudFormation(AuthenticatedContext ac, CloudResource cloudResource) {
        String regionName = ac.getCloudContext().getLocation().getRegion().value();
        AwsCredentialView awsCredential = new AwsCredentialView(ac.getCloudCredential());
        List<StackResource> asGroups = getStackResourceIfCfExists(ac, cloudResource);
        LOGGER.debug("AutoScalingGroup fetched: {}", asGroups);
        boolean empty = true;
        int i = 0;
        while (empty && i < asGroups.size()) {
            StackResource asGroup = asGroups.get(i);
            List<String> result = cfStackUtil.getInstanceIds(awsClient.createAutoScalingClient(awsCredential, regionName), asGroup.getPhysicalResourceId());
            LOGGER.debug("{} autoScalingGroup has {} instance(s): {}", asGroup.getPhysicalResourceId(), result.size(), result);
            empty = result.isEmpty();
            i++;
        }
        return empty;
    }

    private List<StackResource> getStackResourceIfCfExists(AuthenticatedContext ac, CloudResource cloudResource) {
        String regionName = ac.getCloudContext().getLocation().getRegion().value();
        AwsCredentialView awsCredential = new AwsCredentialView(ac.getCloudCredential());
        List<StackResource> asGroups = new ArrayList<>();
        String stackName = cloudResource.getName();
        try {
            DescribeStackResourcesResult describeStackResourcesResult = awsClient.createCloudFormationClient(awsCredential, regionName)
                    .describeStackResources(new DescribeStackResourcesRequest().withStackName(stackName));
            asGroups = describeStackResourcesResult.getStackResources().stream()
                    .filter(it -> "AWS::AutoScaling::AutoScalingGroup".equals(it.getResourceType()))
                    .collect(Collectors.toList());
        } catch (AmazonCloudFormationException e) {
            if (e.getErrorMessage().contains(stackName + " does not exist")) {
                LOGGER.info("CloudFormation resource does not found: {}", e.getMessage());
            } else {
                LOGGER.error("Cannot describe stack resources: {}", e.getMessage(), e);
                throw e;
            }
        }
        return asGroups;
    }
}
