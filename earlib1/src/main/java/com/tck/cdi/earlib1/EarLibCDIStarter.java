package com.tck.cdi.earlib1;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class EarLibCDIStarter {
    @Inject
    EarLibService earLibService;

    void init(@Observes Startup startup) {
        System.out.println("EarLibCDIStarter.init() called");
        log.info("Referencing EarLibService from ear-lib: {}", earLibService.getService());
    }

    public String getService() {
        return earLibService.getService();
    }
}
