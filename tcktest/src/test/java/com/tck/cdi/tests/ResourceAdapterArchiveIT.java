/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.tck.cdi.tests;

import com.flowlogix.testcontainers.PayaraServerLifecycleExtension;
import com.flowlogix.util.ShrinkWrapManipulator;
import com.tck.cdi.tests.rar.TestResourceAdapter;
import com.tck.cdi.tests.rar.Translator;
import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.CDI;
import lombok.extern.slf4j.Slf4j;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.beans11.BeanDiscoveryMode;
import org.jboss.shrinkwrap.descriptor.api.beans11.BeansDescriptor;
import org.jboss.shrinkwrap.descriptor.api.connector16.ConnectorDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import static com.flowlogix.util.ShrinkWrapManipulator.logArchiveContents;
import static com.tck.cdi.tests.common.TestUtil.addSlf4jLibraries;
import static com.tck.cdi.tests.common.TestUtil.deleteAssertJ;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@ExtendWith(PayaraServerLifecycleExtension.class)
@ExtendWith(ArquillianExtension.class)
class ResourceAdapterArchiveIT {
    @Test
    void testInjection() {
        Translator translator = CDI.current().select(Translator.class).get();
        assertNotNull(translator);
        assertEquals(1, translator.ping());
    }

    @Test
    void resolution() {
        getUniqueBean(Translator.class);
    }

    protected <T> Bean<T> getUniqueBean(Class<T> type, Annotation... bindings) {
        Set<Bean<T>> beans = getBeans(type, bindings);
        return resolveUniqueBean(type, beans);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected <T> Set<Bean<T>> getBeans(Class<T> type, Annotation... bindings) {
        return (Set) CDI.current().getBeanManager().getBeans(type, bindings);
    }


    private <T> Bean<T> resolveUniqueBean(Type type, Set<Bean<T>> beans) {
        if (beans.isEmpty()) {
            throw new UnsatisfiedResolutionException("Unable to resolve any beans of " + type);
        } else if (beans.size() > 1) {
            throw new AmbiguousResolutionException("More than one bean available (" + beans + ")");
        }
        return beans.iterator().next();
    }

    @Deployment
    @SuppressWarnings("unused")
    static EnterpriseArchive deploy() throws IOException {
        var archive = ShrinkWrap.create(EnterpriseArchive.class)
                .addAsLibrary(logArchiveContents(deleteAssertJ(ShrinkWrapManipulator
                        .createDeployment(JavaArchive.class, "test-classes.jar")
                        .addClass(ResourceAdapterArchiveIT.class)
                        .addPackage(TestResourceAdapter.class.getPackage())), System.out::println));
        ResourceAdapterArchive rar = ShrinkWrap.create(ResourceAdapterArchive.class);
        // JCA spec 20.2.0.1 Resource Adapter Archive
        rar.addAsLibrary(ShrinkWrap
                .create(JavaArchive.class)
                .addClasses(Translator.class, TestResourceAdapter.class)
                .addAsManifestResource(new StringAsset(Descriptors.create(BeansDescriptor.class)
                        .beanDiscoveryMode(BeanDiscoveryMode._ALL.toString()).version("1.1")
                        .exportAsString()), "beans.xml"));
        rar.addAsManifestResource(
                new StringAsset(Descriptors.create(ConnectorDescriptor.class).version("1.6").displayName("Test RA")
                        .vendorName("Red Hat Middleware LLC").eisType("Test RA").resourceadapterVersion("0.1")
                        .getOrCreateResourceadapter().resourceadapterClass(TestResourceAdapter.class.getName())
                        .getOrCreateOutboundResourceadapter().transactionSupport("NoTransaction")
                        .reauthenticationSupport(false).up().up().exportAsString()), "ra.xml");
        logArchiveContents(rar, System.out::println);
        archive.addAsModule(rar);
        addSlf4jLibraries(archive);
        return logArchiveContents(archive, System.out::println);
    }
}
