package dk.developer.beta.api;

import dk.developer.testing.DatabaseInitialiser;
import dk.developer.testing.Result;
import dk.developer.utility.Converter;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.truth.Truth.ASSERT;
import static dk.developer.testing.RestServiceTestHelper.to;
import static dk.developer.utility.Converter.converter;
import static java.util.Arrays.asList;
import static javax.ws.rs.core.Response.Status.OK;

public class PushNotificationServiceAcceptanceTest extends DatabaseInitialiser {
    private static final Converter converter = converter();

    @Test
    public void shouldUpdateFilters() throws Exception {
        registerSubscription(new Subscription("123", filterIds(), brandIds()));
        Element.Type newFilter = new Element.Type("newFilter");
        database.save(newFilter);
        List<String> filterIds = asList(newFilter.getId());
        Result result = registerSubscription(new Subscription("123", filterIds, brandIds()));
        ASSERT.that(result.status()).isSameAs(OK);

        Subscription subscription = database.load(Subscription.class).matching("deviceToken").with("123");
        ASSERT.that(subscription.getExclusionFilters()).containsExactly(newFilter.getId());
    }

    @Test
    public void shouldUpdateBrands() throws Exception {
        registerSubscription(new Subscription("123", filterIds(), brandIds()));
        Element.Brand newBrand = new Element.Brand("newBrand");
        database.save(newBrand);
        List<String> brandIds = asList(newBrand.getId());
        Result result = registerSubscription(new Subscription("123", filterIds(), brandIds));
        ASSERT.that(result.status()).isSameAs(OK);

        Subscription subscription = database.load(Subscription.class).matching("deviceToken").with("123");
        ASSERT.that(subscription.getFavoriteBrands()).containsExactly(newBrand.getId());
    }

    @Test
    public void shouldRegisterSubscription() throws Exception {
        List<String> filters = filterIds();
        List<String> brands = brandIds();

        Subscription subscription = new Subscription("123", filters, brands);
        Result result = registerSubscription(subscription);
        ASSERT.that(result.status()).isSameAs(OK);

        Subscription databaseSubscription = database.load(Subscription.class).matching("_id").with(subscription.getId());
        ASSERT.that(databaseSubscription).isEqualTo(subscription);
    }

    @Test
    public void shouldDeregisterNonExistingSubscription() throws Exception {
        Result result = deregisterSubscription("123");
        ASSERT.that(result.status()).isSameAs(OK);
    }

    @Test
    public void shouldDeregisterSubscription() throws Exception {
        registerSubscription(new Subscription("123", filterIds(), brandIds()));
        Result result = deregisterSubscription("123");
        ASSERT.that(result.status()).isSameAs(OK);

        Subscription databaseSubscription = database.load(Subscription.class).matching("deviceToken").with("123");
        ASSERT.that(databaseSubscription).isNull();
    }

    private Result registerSubscription(Subscription subscription) {
        String json = converter.toJson(subscription);
        return to(PushNotificationService.class).with(json).post("/push/register");
    }

    private Result deregisterSubscription(String deviceToken) {
        String json = converter.toJson(new Unsubscription(deviceToken));
        return to(PushNotificationService.class).with(json).post("/push/deregister");
    }


    private List<String> filterIds() {
        Element.Type filter = new Element.Type("filter");
        database.save(filter);
        return asList(filter.getId());
    }

    private List<String> brandIds() {
        Element.Brand brand = new Element.Brand("brand");
        database.save(brand);
        return asList(brand.getId());
    }
}