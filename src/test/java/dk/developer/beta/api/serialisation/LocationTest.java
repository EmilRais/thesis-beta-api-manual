package dk.developer.beta.api.serialisation;

import dk.developer.beta.api.Location;
import dk.developer.testing.JsonTool;
import dk.developer.utility.Converter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.google.common.truth.Truth.ASSERT;
import static dk.developer.utility.Converter.converter;

public class LocationTest {
    private static final Converter converter = converter();
    private JsonTool tool;

    @BeforeClass
    public void setUp() throws Exception {
        tool = new JsonTool(SaleTest.class);
    }

    @Test
    public void fromJson() throws Exception {
        String json = tool.readFilteredJsonFile("LocationInput.json");
        Location location = converter.fromJson(json, Location.class);

        ASSERT.that(location.getLatitude()).isEqualTo(12.34);
        ASSERT.that(location.getLongitude()).isEqualTo(56.78);
        ASSERT.that(location.getAddress()).isEqualTo("address");
        ASSERT.that(location.getCity()).isEqualTo("city");
        ASSERT.that(location.getPostalCode()).isEqualTo("postalCode");
    }

    @Test
    public void toJson() throws Exception {
        Location location = new Location(12.34, 56.78, "address", "city", "postalCode");
        String json = converter.toJson(location);
        String expectedJson = tool.readFilteredJsonFile("LocationOutput.json");

        ASSERT.that(json).isEqualTo(expectedJson);
    }
}