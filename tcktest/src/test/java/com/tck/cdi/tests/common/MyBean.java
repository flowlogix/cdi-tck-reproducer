package com.tck.cdi.tests.common;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MyBean {
    public String getName() {
        return "MyBean";
    }
}
