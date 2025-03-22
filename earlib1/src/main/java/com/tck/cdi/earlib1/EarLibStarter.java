package com.tck.cdi.earlib1;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class EarLibStarter {
//    @Inject
//    WarLibPojo warLibPojo;
    @Inject
    EarLibService earLibService;

    @PostConstruct
    void init() {
        log.info("EarLibStarter.init() called");
//        log.info("Referencing WarLibPojo from ear-lib: {}", warLibPojo);
        log.info("Referencing EarLibService from ear-lib: {}", earLibService.getService());
    }
}
