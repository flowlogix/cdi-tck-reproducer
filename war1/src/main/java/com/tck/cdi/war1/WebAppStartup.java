package com.tck.cdi.war1;

import com.tck.cdi.earlib1.EarLibService;
import com.tck.cdi.warlib.WarLibPojo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class WebAppStartup {
    @Inject
    WarLibPojo warLibPojo;
    @Inject
    EarLibService earLibService;

    void init(@Observes Startup startup) {
        log.info("WebAppStartup.init() called");
        log.info("warLibPojo: {}", warLibPojo);

        log.info("earLibService instance: {}", earLibService);
        if (earLibService == null) {
            log.error("*** earLibService is null");
        } else {
            log.info("earLibService: {}", earLibService.getService());
        }
    }
}
