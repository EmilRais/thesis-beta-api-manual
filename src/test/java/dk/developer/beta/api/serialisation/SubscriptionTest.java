package dk.developer.beta.api.serialisation;

import dk.developer.beta.api.Subscription;
import dk.developer.testing.JsonTool;
import dk.developer.utility.Converter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.google.common.truth.Truth.ASSERT;
import static dk.developer.utility.Converter.converter;
import static java.util.Arrays.asList;

public class SubscriptionTest {
    private static final Converter converter = converter();
    private JsonTool tool;

    @BeforeClass
    public void setUp() throws Exception {
        tool = new JsonTool(SubscriptionTest.class);
    }

    @Test
    public void fromJson() throws Exception {
        String json = tool.readFilteredJsonFile("SubscriptionInput.json");
        Subscription token = converter.fromJson(json, Subscription.class);
        ASSERT.that(token.getDeviceToken()).isEqualTo("token");
    }

    @Test
    public void toJson() throws Exception {
        Subscription token = new Subscription("abc123", "token", asList("filter1", "filter2"), asList("brand1", "brand2"));
        String json = converter.toJson(token);
        String expectedJson = tool.readFilteredJsonFile("SubscriptionOutput.json");
        ASSERT.that(json).isEqualTo(expectedJson);
    }
}