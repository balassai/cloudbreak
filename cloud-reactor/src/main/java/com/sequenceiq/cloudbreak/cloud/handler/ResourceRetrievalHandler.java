package com.sequenceiq.cloudbreak.cloud.handler;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.notification.model.ResourceRetrievalNotification;
import com.sequenceiq.cloudbreak.cloud.service.ResourceRetriever;

import reactor.bus.Event;
import reactor.fn.Consumer;

@Component
public class ResourceRetrievalHandler implements Consumer<Event<ResourceRetrievalNotification>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceRetrievalHandler.class);

    @Inject
    private ResourceRetriever cloudResourceRetrieverService;

    @Override
    public void accept(Event<ResourceRetrievalNotification> event) {
        LOGGER.debug("Resource retrieval notification event received: {}", event);
        ResourceRetrievalNotification data = event.getData();
        Optional<CloudResource> resources = retrieveResource(data);
        data.getPromise().onNext(resources);
    }

    private Optional<CloudResource> retrieveResource(ResourceRetrievalNotification data) {
        Optional<CloudResource> result;
        if (data.getStackId() != null && StringUtils.isNotEmpty(data.getResourceReference())) {
            result = cloudResourceRetrieverService.findByResourceReferenceAndStatusAndTypeAndStack(data.getResourceReference(),
                    data.getStatus(), data.getResourceType(), data.getStackId());
        } else if (data.getStackId() != null && StringUtils.isEmpty(data.getResourceReference())) {
            result = cloudResourceRetrieverService.findByStatusAndTypeAndStack(data.getStatus(), data.getResourceType(), data.getStackId());
        } else {
            result = cloudResourceRetrieverService.findByResourceReferenceAndStatusAndType(data.getResourceReference(), data.getStatus(),
                    data.getResourceType());
        }
        return result;
    }
}
