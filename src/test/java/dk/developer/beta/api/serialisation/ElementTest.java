package dk.developer.beta.api.serialisation;

import dk.developer.testing.JsonTool;
import dk.developer.utility.Converter;
import org.testng.annotations.Test;

import static com.google.common.truth.Truth.ASSERT;
import static dk.developer.beta.api.Element.*;
import static dk.developer.utility.Converter.converter;

public class ElementTest {
    private static final Converter converter = converter();
    private static final JsonTool tool = new JsonTool(ElementTest.class);

    @Test
    public void brandFromJson() throws Exception {
        String json = tool.readFilteredJsonFile("BrandInput.json");
        Brand brand = converter.fromJson(json, Brand.class);

        ASSERT.that(brand.getValue()).isEqualTo("value");
    }

    @Test
    public void brandToJson() throws Exception {
        Brand brand = new Brand("abc123", "value");
        String json = converter.toJson(brand);
        String expectedJson = tool.readFilteredJsonFile("BrandOutput.json");

        ASSERT.that(json).isEqualTo(expectedJson);
    }

    @Test
    public void typeFromJson() throws Exception {
        String json = tool.readFilteredJsonFile("TypeInput.json");
        Type type = converter.fromJson(json, Type.class);

        ASSERT.that(type.getValue()).isEqualTo("value");
    }

    @Test
    public void typeToJson() throws Exception {
        Type type = new Type("abc123", "value");
        String json = converter.toJson(type);
        String expectedJson = tool.readFilteredJsonFile("TypeOutput.json");

        ASSERT.that(json).isEqualTo(expectedJson);
    }

    @Test
    public void paymentOptionFromJson() throws Exception {
        String json = tool.readFilteredJsonFile("PaymentOptionInput.json");
        PaymentOption paymentOption = converter.fromJson(json, PaymentOption.class);

        ASSERT.that(paymentOption.getValue()).isEqualTo("value");
    }

    @Test
    public void paymentOptionToJson() throws Exception {
        PaymentOption paymentOption = new PaymentOption("abc123", "value");
        String json = converter.toJson(paymentOption);
        String expectedJson = tool.readFilteredJsonFile("PaymentOptionOutput.json");

        ASSERT.that(json).isEqualTo(expectedJson);
    }
}
