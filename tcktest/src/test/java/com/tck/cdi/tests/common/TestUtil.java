package com.tck.cdi.tests.common;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class TestUtil {
    public static void addSlf4jLibraries(EnterpriseArchive archive) throws IOException {
        Files.walk(Paths.get("../ear1/target"))
                .filter(path -> path.getFileName().toString().startsWith("org.slf4j-slf4j")
                        && path.getFileName().toString().endsWith(".jar"))
                .map(Path::toFile).forEach(jar -> archive.addAsLibrary(ShrinkWrap.createFromZipFile(JavaArchive.class, jar)));
    }

    public static <TT extends Archive<TT>> TT deleteAssertJ(TT archive) {
        archive.delete("org/assertj");
        return archive;
    }
}
