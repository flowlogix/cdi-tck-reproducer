package com.tck.cdi.earlib1;

import jakarta.enterprise.context.Dependent;

@Dependent
public class EarLibServiceImpl implements EarLibService {
    @Override
    public String getService() {
        return "EarLibServiceImpl";
    }
}
