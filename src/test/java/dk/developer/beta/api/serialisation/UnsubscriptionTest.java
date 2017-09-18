package dk.developer.beta.api.serialisation;

import dk.developer.beta.api.Unsubscription;
import dk.developer.testing.JsonTool;
import dk.developer.utility.Converter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.google.common.truth.Truth.ASSERT;
import static dk.developer.utility.Converter.converter;

public class UnsubscriptionTest {
    private static final Converter converter = converter();
    private JsonTool tool;

    @BeforeClass
    public void setUp() throws Exception {
        tool = new JsonTool(UnsubscriptionTest.class);
    }

    @Test
    public void fromJson() throws Exception {
        String json = tool.readFilteredJsonFile("Unsubscription.json");
        Unsubscription unsubscription = converter.fromJson(json, Unsubscription.class);
        ASSERT.that(unsubscription.getDeviceToken()).isEqualTo("token");
    }

    @Test
    public void toJson() throws Exception {
        Unsubscription unsubscription = new Unsubscription("token");
        String json = converter.toJson(unsubscription);
        String expectedJson = tool.readFilteredJsonFile("Unsubscription.json");
        ASSERT.that(json).isEqualTo(expectedJson);
    }

}