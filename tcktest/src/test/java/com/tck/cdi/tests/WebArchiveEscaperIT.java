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
import java.nio.file.Path;
import static com.flowlogix.util.ShrinkWrapManipulator.logArchiveContents;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(PayaraServerLifecycleExtension.class)
@ExtendWith(ArquillianExtension.class)
@ArquillianSuiteDeployment
class WebArchiveEscaperIT {
    @Test
    void sanityCheck() {
        assertThat(true).isFalse();
    }

    @Deployment
    @SuppressWarnings("unused")
    static EnterpriseArchive deploy() {
        var depl = logArchiveContents(
                ShrinkWrap.create(EnterpriseArchive.class)
                        .addAsLibrary(ShrinkWrapManipulator.createDeployment(JavaArchive.class, "test-classes.jar")
                                .addPackage(WebArchiveEscaperIT.class.getPackage()))
                        .addAsLibrary(
                                logArchiveContents(ShrinkWrapManipulator.createDeployment(JavaArchive.class,
                                        "earlib1.jar", Path.of("../earlib1/pom.xml")), System.out::println))
                        .addAsModule(ShrinkWrapManipulator.createDeployment(WebArchive.class, "com.tck.cdi-war1-1.x-SNAPSHOT.war",
                                Path.of("../war1/pom.xml")))
                        .addAsModule(
                                logArchiveContents(ShrinkWrapManipulator.createDeployment(JavaArchive.class,
                                        "com.tck.cdi-ejbjar1-1.x-SNAPSHOT.jar",
                                        Path.of("../ejbjar1/pom.xml")), System.out::println))
                        .addAsLibrary(ShrinkWrap.createFromZipFile(JavaArchive.class,
                                Path.of("../ear1/target/ear1-1.x-SNAPSHOT/lib/org.slf4j-slf4j-api-2.0.17.jar").toFile()))
                        .addAsLibrary(ShrinkWrap.createFromZipFile(JavaArchive.class,
                                Path.of("../ear1/target/ear1-1.x-SNAPSHOT/lib/org.slf4j-slf4j-jdk14-2.0.17.jar").toFile()))
                        .addAsManifestResource(Path.of("../ear1/target/ear1-1.x-SNAPSHOT/META-INF/application.xml").toFile(),
                                "application.xml"),
                System.out::println);
        return depl;
    }
}
