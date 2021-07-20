package ro.cloudify;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.types.Facing;
import org.openstack4j.model.compute.RebootType;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.identity.v3.Token;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.openstack.identity.v3.domain.KeystoneToken;
import org.openstack4j.openstack.internal.OSClientSession;
import ro.cloudify.model.ApplicationCredentials;
import ro.cloudify.model.ApplicationCredentialsRequest;
import ro.cloudify.model.Auth;
import ro.cloudify.model.Identity;


import java.util.List;
import java.util.Map;

@Slf4j
public class Main {
    public static String endpoint = "https://id.cloud.acvile.com/v3";
    public static final String ID = "";
    public static final String SECRET = "";


    public static void main(String[] args) {
        OSClient.OSClientV3 client = getClient();

        //List images
        client.imagesV2().list();

        // List flavors
        client.compute().flavors().list();

        // Create a Server Model Object
        ServerCreate sc = Builders.server()
                .name("ubuntu-dev-01")
                .flavor("flavorId")
                .image("imageId")
                .build();

        // BOOT the Server
        Server server = client.compute().servers().boot(sc);

        // REBOOT the Server
        client.compute().servers().reboot(server.getId(), RebootType.SOFT);
    }

    private static OSClientSession.OSClientSessionV3 getClient() {
        Token token = getToken();

        OSClientSession.OSClientSessionV3 sessionV3 = (OSClientSession.OSClientSessionV3) OSFactory.clientFromToken(token, Facing.PUBLIC);

        Map<String, String> headers = Map.of("X-OpenStack-Nova-API-Version", "2.65");
        sessionV3.headers(headers);

        return sessionV3;
    }

    private static Token getToken() {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        ObjectMapper objectMapper = new ObjectMapper();
        ApplicationCredentialsRequest applicationCredentials = ApplicationCredentialsRequest.builder()
                .auth(Auth.builder()
                        .identity(Identity.builder()
                                .methods(List.of("application_credential"))
                                .applicationCredentials(ApplicationCredentials.builder()
                                        .id(ID)
                                        .secret(SECRET)
                                        .build())
                                .build())
                        .build())
                .build();

        Token token = null;
        try {
            StringEntity postingString = new StringEntity(objectMapper.writeValueAsString(applicationCredentials));
            HttpPost post = new HttpPost(String.format("%s/auth/tokens", endpoint));
            post.setEntity(postingString);
            post.setHeader("Content-type", "application/json");

            CloseableHttpResponse response = httpClient.execute(post);
            String tokenResponse = EntityUtils.toString(response.getEntity());

            objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
            objectMapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
            token = objectMapper.readValue(tokenResponse, KeystoneToken.class);
        } catch (Exception e) {
            log.error("An error occurred while getting the token.", e);
        }
        return token;
    }

}
