package dk.developer.beta.api;

import dk.developer.database.DatabaseFront;
import dk.developer.database.DatabaseProvider;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;

@Path("push")
public class PushNotificationService {
    private final DatabaseFront database;

    public PushNotificationService() {
        database = DatabaseProvider.databaseLayer();
    }

    @POST
    @Path("/register")
    @Consumes(APPLICATION_JSON)
    public Response register(@Valid Subscription subscription) {
        String deviceToken = subscription.getDeviceToken();
        if ( deviceTokenAlreadyExists(deviceToken) ) {
            updateSubscription(subscription, deviceToken);
            return Response.status(OK).build();
        }

        database.save(subscription);
        return Response.status(OK).build();
    }

    private void updateSubscription(Subscription subscription, String deviceToken) {
        String id = getExistingId(subscription);
        List<String> exclusionFilters = subscription.getExclusionFilters();
        List<String> favoriteBrands = subscription.getFavoriteBrands();
        Subscription updatedSubscription = new Subscription(id, deviceToken, exclusionFilters, favoriteBrands);
        database.update(updatedSubscription);
    }

    private String getExistingId(Subscription subscription) {
        Subscription fromDatabase = database.load(Subscription.class).matching("deviceToken").with(subscription.getDeviceToken());
        if ( fromDatabase != null )
            return fromDatabase.getId();
        return "";
    }

    private boolean deviceTokenAlreadyExists(String token) {
        return database.load(Subscription.class).matching("deviceToken").with(token) != null;
    }

    @POST
    @Path("/deregister")
    @Consumes(APPLICATION_JSON)
    public Response deregister(@Valid Unsubscription unsubscription) {
        database.delete(Subscription.class).matching("deviceToken").with(unsubscription.getDeviceToken());
        return Response.status(OK).build();
    }
}
