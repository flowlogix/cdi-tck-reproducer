package com.tck.cdi.earlib1;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Random;

@ApplicationScoped
public class SimpleApplicationBean {
    private final Random random = new Random();

    public Integer getNumber() {
        return random.nextInt();
    }
}
