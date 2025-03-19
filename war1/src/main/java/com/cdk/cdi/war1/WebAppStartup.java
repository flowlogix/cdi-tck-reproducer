package com.cdk.cdi.war1;

import com.cdk.cdi.warlib.WarLibPojo;
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

    void init(@Observes Startup startup) {
        log.info("WebAppStartup.init() called");
        log.info("warLibPojo: {}", warLibPojo);
    }
}
