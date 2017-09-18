package dk.developer.beta.api.serialisation;

import dk.developer.beta.api.Location;
import dk.developer.beta.api.Sale;
import dk.developer.testing.JsonTool;
import dk.developer.utility.Converter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.truth.Truth.ASSERT;
import static dk.developer.beta.api.Element.*;
import static dk.developer.utility.Converter.converter;
import static java.util.Arrays.asList;

public class SaleTest {
    private static final Converter converter = converter();
    private JsonTool tool;

    @BeforeClass
    public void setUp() throws Exception {
        tool = new JsonTool(SaleTest.class);
    }

    @Test
    public void fromJson() throws Exception {
        String json = tool.readFilteredJsonFile("SaleInput.json");
        Sale sale = converter.fromJson(json, Sale.class);

        ASSERT.that(sale.getName()).isEqualTo("name");
        ASSERT.that(sale.getLocation()).isEqualTo(new Location(12.34, 56.78, "address", "city", "postalCode"));
        ASSERT.that(sale.getOpeningHours()).isEqualTo("openingHours");
        ASSERT.that(sale.getFromDate()).isEqualTo((long) 1234);
        ASSERT.that(sale.getToDate()).isEqualTo((long) 5678);
        ASSERT.that(sale.getPaymentOptions()).containsExactly(new PaymentOption("1", "Dankort"), new PaymentOption("2", "Kontant"));
        ASSERT.that(sale.getTypes()).containsExactly(new Type("3", "Home"), new Type("4", "Other"));
        ASSERT.that(sale.getBrand()).isEqualTo(new Brand("5", "brand"));
        ASSERT.that(sale.getDescription()).isEqualTo("description");
        ASSERT.that(sale.getLogo()).isEqualTo("logo");
    }

    @Test
    public void toJson() throws Exception {
        List<PaymentOption> payment = asList(new PaymentOption("1", "Dankort"), new PaymentOption("2", "Kontant"));
        List<Type> type = asList(new Type("3", "Home"), new Type("4", "Other"));
        Location location = new Location(12.34, 56.78, "address", "city", "postalCode");
        Brand brand = new Brand("5", "brand");
        Sale sale = new Sale("abc123", "name", location, "openingHours", 1234, 5678, payment, type, brand, "description", "logo");
        String json = converter.toJson(sale);
        String expectedJson = tool.readFilteredJsonFile("SaleOutput.json");

        ASSERT.that(json).isEqualTo(expectedJson);
    }
}