package com.tck.cdi.earlib1;

import jakarta.ejb.Remote;

@Remote
public interface EjbJarOneRemote {
    String getService();
}
