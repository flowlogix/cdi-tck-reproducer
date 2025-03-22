package com.tck.cdi.war1;

import com.tck.cdi.earlib1.EarLibService;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;

@Dependent
@Alternative
@Priority(1000)
public class War1ServiceImpl implements EarLibService {
    @Override
    public String getService() {
        return "War1ServiceImpl";
    }
}
