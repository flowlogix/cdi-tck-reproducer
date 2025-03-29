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
import com.tck.cdi.earlib1.EarLibCDIStarter;
import com.tck.cdi.earlib1.EjbJarOneRemote;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import static com.flowlogix.util.ShrinkWrapManipulator.logArchiveContents;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(PayaraServerLifecycleExtension.class)
@ExtendWith(ArquillianExtension.class)
@ArquillianSuiteDeployment
class WebArchiveEscaperIT {
    @Inject
    EarLibCDIStarter earLibCDIStarter;
    @EJB
    EjbJarOneRemote ejbJarOneStartup;

    @Test
    void checkEarLibIsolation() {
        assertThat(earLibCDIStarter).isNotNull();
        assertThat(earLibCDIStarter.getService()).isEqualTo("EarLibServiceImpl");
    }

    @Test
    void checkEjbJarIsolation() {
        assertThat(ejbJarOneStartup).isNotNull();
        assertThat(ejbJarOneStartup.getService()).isEqualTo("EarLibServiceImpl");
    }

    @Deployment
    @SuppressWarnings("unused")
    static EnterpriseArchive deploy() throws IOException {
        var archive = ShrinkWrap.create(EnterpriseArchive.class)
                .addAsLibrary(ShrinkWrapManipulator.createDeployment(JavaArchive.class, "test-classes.jar")
                        .addPackage(WebArchiveEscaperIT.class.getPackage()))
                .addAsLibrary(logArchiveContents(ShrinkWrapManipulator.createDeployment(JavaArchive.class,
                                "earlib1.jar", Path.of("../earlib1/pom.xml")), System.out::println))
                .addAsModule(logArchiveContents(ShrinkWrapManipulator.createDeployment(WebArchive.class,
                                                UUID.randomUUID() + "-cdi-war1.war",
                                                Path.of("../war1/pom.xml"))
                                        .filter(path -> !path.get().contains("earlib")),
                                System.out::println))
                .addAsModule(
                        logArchiveContents(ShrinkWrapManipulator.createDeployment(JavaArchive.class,
                                UUID.randomUUID() + "-cdi-ejbjar1.jar",
                                Path.of("../ejbjar1/pom.xml")), System.out::println));

        Files.walk(Paths.get("../ear1/target"))
                .filter(path -> path.getFileName().toString().startsWith("org.slf4j-slf4j")
                        && path.getFileName().toString().endsWith(".jar"))
                .map(Path::toFile).forEach(jar -> archive.addAsLibrary(ShrinkWrap.createFromZipFile(JavaArchive.class, jar)));

        return logArchiveContents(archive, System.out::println);
    }
}
