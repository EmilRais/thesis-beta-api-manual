package dk.developer.beta.api;

import dk.developer.testing.DatabaseInitialiser;
import dk.developer.testing.Result;
import dk.developer.utility.Converter;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.ASSERT;
import static dk.developer.beta.api.Element.*;
import static dk.developer.testing.RestServiceTestHelper.from;
import static dk.developer.testing.RestServiceTestHelper.to;
import static dk.developer.utility.Converter.converter;
import static java.util.Arrays.asList;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;

public class ElementServiceAcceptanceTest extends DatabaseInitialiser {
    private static final Converter converter = converter();

    @Test
    public void shouldCreateElement() throws Exception {
        Brand element = new Brand("Nike");
        String json = converter.toJson(element);

        Result result = to(ElementService.class).with(json).post("/element/create");
        ASSERT.that(result.status()).isSameAs(CREATED);

        Brand databaseElement = database.load(Brand.class).matching("_id").with(element.getId());
        ASSERT.that(databaseElement).isEqualTo(element);
    }

    @Test
    public void shouldGetOneElement() throws Exception {
        Type element = new Type("Children");
        saveToDatabase(element);

        List<Type> elements = retrieveFromService(Type.class);
        ASSERT.that(elements).containsExactly(element);
    }

    @Test
    public void shouldGetSeveralElements() throws Exception {
        PaymentOption cash = new PaymentOption("Cash");
        PaymentOption loan = new PaymentOption("Loan");
        saveToDatabase(cash, loan);

        List<PaymentOption> elements = retrieveFromService(PaymentOption.class);
        ASSERT.that(elements).containsExactly(cash, loan);
    }

    private void saveToDatabase(Element... elements) {
        for (Element element: elements)
            database.save(element);
    }

    private <T extends Element> List<T> retrieveFromService(Class<T> typeClass) {
        String segment = urlSegment(typeClass);
        String url = String.format("/element/%s/get", segment);
        Result result = from(ElementService.class).get(url);
        ASSERT.that(result.status()).isSameAs(OK);

        List<T> resources = new ArrayList<>();
        List content = converter.fromJson(result.content(), List.class);
        for (Object object : content) {
            T item = converter.convert(object, typeClass);
            resources.add(item);
        }
        return resources;
    }

    private String urlSegment(Class<? extends Element> type) {
        if ( type == Brand.class ) return "brand";
        if ( type == Type.class ) return "type";
        if ( type == PaymentOption.class ) return "payment_option";
        throw new RuntimeException();
    }

    @Test
    public void shouldUpdateElement() throws Exception {
        Brand samsung = new Brand("Samsung");
        saveToDatabase(samsung);

        String id = samsung.getId();
        Brand updatedElement = new Brand(id, "Apple");
        String json = converter.toJson(updatedElement);

        Result result = to(ElementService.class).with(json).post("/element/update");
        ASSERT.that(result.status()).isSameAs(OK);

        Brand databaseElement = database.load(Brand.class).matching("_id").with(id);
        ASSERT.that(databaseElement).isEqualTo(updatedElement);
    }

    @Test
    public void shouldUpdateSaleWhenUpdatingElement() throws Exception {
        Brand oneBrand = new Brand("Nike");
        saveToDatabase(oneBrand);

        Location location = new Location(12.34, 56.78, "address", "city", "postalCode");
        List<PaymentOption> paymentOptions = asList(new PaymentOption("Dankort"));
        List<Type> types = asList(new Type("Men"), new Type("Women"), new Type("Other"));

        Sale oneSale = new Sale("name", location, "opening", 1234, 5678, paymentOptions, types, oneBrand, "description", "logo");
        database.save(oneSale);

        Brand anotherBrand = new Brand("Gucci");
        Sale anotherSale = new Sale("name", location, "opening", 1234, 5678, paymentOptions, types, anotherBrand, "description", "logo");
        database.save(anotherSale);

        Brand updatedBrand = new Brand(oneBrand.getId(), "Adidas");
        String json = converter.toJson(updatedBrand);
        Result result = to(ElementService.class).with(json).post("/element/update");
        ASSERT.that(result.status()).isSameAs(OK);

        Sale updatedSale = database.load(Sale.class).matching("_id").with(oneSale.getId());
        ASSERT.that(updatedSale.getBrand()).isEqualTo(updatedBrand);

        Sale notUpdatedSale = database.load(Sale.class).matching("_id").with(anotherSale.getId());
        ASSERT.that(notUpdatedSale.getBrand()).isEqualTo(anotherBrand);
    }
}
