package com.tck.cdi.tests;

import com.flowlogix.testcontainers.PayaraServerLifecycleExtension;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.ext.ContextResolver;
import lombok.extern.slf4j.Slf4j;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import static com.flowlogix.util.ShrinkWrapManipulator.createDeployment;
import static com.flowlogix.util.ShrinkWrapManipulator.packageSlf4j;
import static jakarta.ws.rs.RuntimeType.CLIENT;
import static jakarta.ws.rs.RuntimeType.SERVER;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(PayaraServerLifecycleExtension.class)
@ExtendWith(ArquillianExtension.class)
class JsonbContextProviderIT {
    @ArquillianResource
    URL baseUrl;

    /**
     * Verifies that an implementation will use the {@link Jsonb} instance
     * offered by an application-provided context resolver.
     * @throws URISyntaxException if the baseUrl cannot be converted to a URI
     */
    @Test
    final void shouldUseApplicationProvidedJsonbInstance() throws URISyntaxException {

        try (Client client = ClientBuilder.newBuilder().register(new CustomJsonbProvider(CLIENT)).build()) {
            // when
            final String origin = String.format("Origin(%d)", mockInt());
            final POJO requestPojo = new POJO();
            requestPojo.setSeenBy(origin);

            final URI effectiveUri = UriBuilder.fromUri(baseUrl.toURI()).path("echo").build();
            final POJO responsePojo = client.target(effectiveUri)
                    .request(APPLICATION_JSON_TYPE)
                    .buildPost(Entity.entity(requestPojo, APPLICATION_JSON_TYPE))
                    .invoke(POJO.class);

            // then
            final String expectedWaypoints = String.join(",", origin,
                    "CustomSerializer(CLIENT)",
                    "CustomDeserializer(SERVER)",
                    "EchoResource",
                    "CustomSerializer(SERVER)",
                    "CustomDeserializer(CLIENT)");
            assertThat(responsePojo.getSeenBy()).isEqualTo(expectedWaypoints);
        }
    }

    public static final class CustomJsonbProvider implements ContextResolver<Jsonb> {
        private final RuntimeType runtimeType;

        private CustomJsonbProvider(final RuntimeType runtimeType) {
            this.runtimeType = runtimeType;
        }

        public Jsonb getContext(final Class<?> type) {
            if (!POJO.class.isAssignableFrom(type)) {
                return null;
            }

            return JsonbBuilder.create(new JsonbConfig().withSerializers(new CustomSerializer())
                    .withDeserializers(new CustomDeserializer()));
        }

        private final class CustomSerializer implements JsonbSerializer<POJO> {
            @Override
            public void serialize(final POJO pojo, final JsonGenerator generator, final SerializationContext ctx) {
                generator.writeStartObject();
                generator.write("seenBy", String.format("%s,CustomSerializer(%s)", pojo.getSeenBy(),
                        CustomJsonbProvider.this.runtimeType));
                generator.writeEnd();
            }
        }

        private final class CustomDeserializer implements JsonbDeserializer<POJO> {
            @Override
            public POJO deserialize(final JsonParser parser, final DeserializationContext ctx, final Type rtType) {
                final POJO pojo = new POJO();
                pojo.setSeenBy(String.format("%s,CustomDeserializer(%s)", parser.getObject().getString("seenBy"),
                        CustomJsonbProvider.this.runtimeType));
                return pojo;
            }
        }
    }

    public static final class POJO {
        private String seenBy;

        public String getSeenBy() {
            return this.seenBy;
        }

        public void setSeenBy(final String seenBy) {
            this.seenBy = seenBy;
        }
    }

    @ApplicationPath("")
    public static class EchoApplication extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            return Collections.singleton(EchoResource.class);
        }

        @Override
        public Set<Object> getSingletons() {
            return Collections.singleton(new CustomJsonbProvider(SERVER));
        }

        @Path("echo")
        public static class EchoResource {
            @POST
            @Consumes(APPLICATION_JSON)
            @Produces(APPLICATION_JSON)
            public POJO echo(final POJO pojo) {
                pojo.setSeenBy(String.join(",", pojo.getSeenBy(), "EchoResource"));
                return pojo;
            }
        }
    }

    private static int mockInt() {
        return (int) Math.round(Integer.MAX_VALUE * Math.random());
    }

    @Deployment
    @SuppressWarnings("unused")
    static WebArchive deploy() {
        return packageSlf4j(createDeployment(WebArchive.class, name -> "jsonbcontext-" + name));
    }
}
