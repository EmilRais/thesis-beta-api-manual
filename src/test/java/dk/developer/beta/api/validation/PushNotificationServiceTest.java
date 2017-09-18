package dk.developer.beta.api.validation;

import dk.developer.beta.api.PushNotificationService;
import dk.developer.beta.api.Subscription;
import dk.developer.beta.api.Unsubscription;
import dk.developer.testing.Result;
import dk.developer.utility.Converter;
import org.testng.annotations.Test;

import static com.google.common.truth.Truth.ASSERT;
import static dk.developer.testing.RestServiceTestHelper.to;
import static dk.developer.utility.Converter.converter;
import static java.util.Arrays.asList;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

public class PushNotificationServiceTest {
    private static final Converter converter = converter();

    @Test
    public void shouldFailRegisterEmptyToken() throws Exception {
        Result result = registerSubscription(new Subscription("", asList(), asList()));
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Ugyldig token");
    }

    @Test
    public void shouldFailDeregisterEmptySubscription() throws Exception {
        Result result = deregisterSubscription("");
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Ugyldig token");
    }

    @Test
    public void shouldFailRegisterNullFilters() throws Exception {
        Result result = registerSubscription(new Subscription("abc", asList("123", null), asList()));
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Der må ikke være nogen ugyldige eksklusions-filtre");
    }

    @Test
    public void shouldFailRegisterEmptyFilters() throws Exception {
        Result result = registerSubscription(new Subscription("id", asList("abc", ""), asList()));
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Der må ikke være nogen ugyldige eksklusions-filtre");
    }

    @Test
    public void shouldFailRegisterWithNullForFilters() throws Exception {
        Result result = registerSubscription(new Subscription("id", null, asList()));
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Eksklusions-filtre må ikke angives som null");
    }

    @Test
    public void shouldFailRegisterNullBrands() throws Exception {
        Result result = registerSubscription(new Subscription("id", asList(), asList("abc", null)));
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Der må ikke være nogen ugyldige favorit-brands");
    }

    @Test
    public void shouldFailRegisterEmptyBrands() throws Exception {
        Result result = registerSubscription(new Subscription("id", asList(), asList("abc", "")));
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Der må ikke være nogen ugyldige favorit-brands");
    }

    @Test
    public void shouldFailRegisterWithNullForBrands() throws Exception {
        Result result = registerSubscription(new Subscription("id", asList(), null));
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Favorit-brands må ikke angives som null");
    }

    private Result registerSubscription(Subscription subscription) {
        String json = converter.toJson(subscription);
        return to(PushNotificationService.class).with(json).post("/push/register");
    }

    private Result deregisterSubscription(String deviceToken) {
        String json = converter.toJson(new Unsubscription(deviceToken));
        return to(PushNotificationService.class).with(json).post("/push/deregister");
    }
}
