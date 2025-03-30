package com.tck.cdi.ejbjar2;

import com.tck.cdi.earlib1.EjbJarTwoRemote;
import com.tck.cdi.earlib1.SimpleApplicationBean;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

@Stateless
public class EjbJarTwo implements EjbJarTwoRemote {
    @Inject
    BeanManager beanManager;

    @Inject
    SimpleApplicationBean simpleApplicationBean;

    @Override
    public Integer getNumber() {
        Context applicationContext = beanManager.getContext(ApplicationScoped.class);
        if (applicationContext == null || !applicationContext.isActive() || simpleApplicationBean == null) {
            return null;
        }
        return simpleApplicationBean.getNumber();
    }
}
