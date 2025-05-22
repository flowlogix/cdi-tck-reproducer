package com.tck.cdi.tests;

import com.flowlogix.testcontainers.PayaraServerLifecycleExtension;
import com.tck.cdi.tests.common.exclude.Charlie;
import com.tck.cdi.tests.common.exclude.Delta;
import com.tck.cdi.tests.common.exclude.VerifyingExtension;
import com.tck.cdi.tests.common.exclude.food.Meat;
import com.tck.cdi.tests.common.exclude.haircut.Chonmage;
import com.tck.cdi.tests.common.exclude.mustache.Mustache;
import com.tck.cdi.tests.common.exclude.mustache.beard.Beard;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static com.flowlogix.util.ShrinkWrapManipulator.createDeployment;
import static com.flowlogix.util.ShrinkWrapManipulator.logArchiveContents;
import static com.flowlogix.util.ShrinkWrapManipulator.packageSlf4j;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(PayaraServerLifecycleExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ExcludeFiltersIT {
    @Inject
    VerifyingExtension extension;

    @Inject
    BeanManager beanManager;

    @Test
    public void testExcludeSystemPropertyActivator() {
        assertTypeIsExcluded(Charlie.class);
        assertTypeIsExcluded(Delta.class);
    }

    private void assertTypeIsExcluded(Class<?> type) {
        assertTrue(beanManager.getBeans(type).isEmpty());
        assertFalse(extension.getObservedAnnotatedTypes().contains(type));
    }

    private void assertTypeIsNotExcluded(Class<?> type) {
        assertFalse(beanManager.getBeans(type).isEmpty());
        assertTrue(extension.getObservedAnnotatedTypes().contains(type));
    }

    @Deployment
    @SuppressWarnings("unused")
    static WebArchive deploy() {
        return packageSlf4j(logArchiveContents(createDeployment(WebArchive.class, name -> "excludefilters-" + name)
                .addClass(VerifyingExtension.class)
                .addPackage(Mustache.class.getPackage())
                .addPackage(Beard.class.getPackage())
                .addPackage(Chonmage.class.getPackage())
                .addPackage(Meat.class.getPackage())
                .addPackage(Charlie.class.getPackage())
                .addAsWebInfResource(new StringAsset(getWebXml()), "beans.xml")
                        .addAsResource(new StringAsset("com.tck.cdi.tests.common.exclude.VerifyingExtension"),
                "META-INF/services/jakarta.enterprise.inject.spi.Extension")
                ,
        System.out::println));
    }

    private static String getWebXml() {
        return
                """
                        <beans version="3.0" bean-discovery-mode="all">
                        <scan>
                        <exclude name="com.tck.cdi.tests.common.exclude.haircut.*" />
                        <exclude name="com.tck.cdi.tests.common.exclude.mustache.**" />
                        <exclude name="com.tck.cdi.tests.common.exclude.food.*">
                        <if-class-available name="com.some.unreal.class.Name" />
                        </exclude>
                        <exclude name="com.tck.cdi.tests.common.exclude.food.*">
                        <if-class-not-available name="org.jboss.cdi.tck.test.ExcludeFiltersTest" />
                        </exclude>
                        <exclude name="com.tck.cdi.tests.common.exclude.Alpha">
                        <if-class-available name="com.tck.cdi.tests.common.exclude.Stubble" />
                        </exclude>
                        <exclude name="com.tck.cdi.tests.common.exclude.Stubble" />
                        <exclude name="com.tck.cdi.tests.common.exclude.Foxtrot">
                        <if-class-available name="com.some.unreal.class.Name" />
                        </exclude>
                        <exclude name="com.tck.cdi.tests.common.exclude.Bravo">
                        <if-class-not-available name="com.some.unreal.class.Name" />
                        </exclude>
                        <exclude name="com.tck.cdi.tests.common.exclude.Echo">
                        <if-class-not-available name="org.jboss.cdi.tck.test.ExcludeFiltersTest" />
                        </exclude>
                        <exclude name="com.tck.cdi.tests.common.exclude.Charlie">
                        <if-system-property name="cdiTckExcludeDummy" />
                        </exclude>
                        <exclude name="com.tck.cdi.tests.common.exclude.Delta">
                        <if-system-property name="cdiTckExcludeDummy" value="true" />
                        </exclude>
                        </scan>
                        </beans>
                        """;
    }
}
