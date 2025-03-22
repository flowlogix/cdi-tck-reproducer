package com.tck.cdi.ejbjar1;

import com.tck.cdi.earlib1.EarLibService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class EjbJarOneStartup {
    @Inject
    EarLibService earLibService;

    void init(@Observes Startup startup) {
        log.info("EjbJarOneStartup init");
        log.info("earLibService: {}", earLibService.getService());
    }
}
