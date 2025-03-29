package com.tck.cdi.ejbjar1;

import com.tck.cdi.earlib1.EarLibService;
import com.tck.cdi.earlib1.EjbJarOneRemote;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Startup
public class EjbJarOneStartup implements EjbJarOneRemote {
    @Inject
    EarLibService earLibService;

    @PostConstruct
    void init() {
        log.info("EjbJarOneStartup init");
        log.info("earLibService: {}", earLibService.getService());
    }

    @Override
    public String getService() {
        return earLibService.getService();
    }
}
