package com.sequenceiq.freeipa.flow.freeipa.provision.handler;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.common.event.Selectable;
import com.sequenceiq.flow.event.EventSelectorUtil;
import com.sequenceiq.flow.reactor.api.handler.EventHandler;
import com.sequenceiq.freeipa.flow.freeipa.provision.event.bootstrap.BootstrapMachinesFailed;
import com.sequenceiq.freeipa.flow.freeipa.provision.event.bootstrap.BootstrapMachinesRequest;
import com.sequenceiq.freeipa.flow.freeipa.provision.event.bootstrap.BootstrapMachinesSuccess;
import com.sequenceiq.freeipa.service.BootstrapService;

import reactor.bus.Event;
import reactor.bus.EventBus;

@Component
public class BootstrapMachineHandler implements EventHandler<BootstrapMachinesRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapMachineHandler.class);

    @Inject
    private EventBus eventBus;

    @Inject
    private BootstrapService bootstrapService;

    @Override
    public String selector() {
        return EventSelectorUtil.selector(BootstrapMachinesRequest.class);
    }

    @Override
    public void accept(Event<BootstrapMachinesRequest> event) {
        BootstrapMachinesRequest request = event.getData();
        Selectable response;
        try {
            bootstrapService.bootstrap(request.getResourceId());
            response = new BootstrapMachinesSuccess(request.getResourceId());
        } catch (Exception e) {
            LOGGER.error("Bootstrap failed", e);
            response = new BootstrapMachinesFailed(request.getResourceId(), e);
        }
        eventBus.notify(response.selector(), new Event<>(event.getHeaders(), response));
    }
}
