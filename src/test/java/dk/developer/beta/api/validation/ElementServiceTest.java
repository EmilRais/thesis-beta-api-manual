package dk.developer.beta.api.validation;

import dk.developer.beta.api.Element;
import dk.developer.beta.api.Sale;
import dk.developer.database.DatabaseFront;
import dk.developer.beta.api.ElementService;
import dk.developer.testing.Result;
import dk.developer.testing.TestDatabaseProvider;
import dk.developer.utility.Converter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.google.common.truth.Truth.ASSERT;
import static dk.developer.testing.RestServiceTestHelper.from;
import static dk.developer.testing.RestServiceTestHelper.to;
import static dk.developer.utility.Converter.converter;
import static java.util.Arrays.asList;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

public class ElementServiceTest {
    private static final Converter converter = converter();
    private DatabaseFront database;

    @BeforeMethod
    public void setUp() throws Exception {
        database = TestDatabaseProvider.memoryTestDatabase();
    }

    @Test
    public void shouldFailWhenCreatingElementWithNoValue() throws Exception {
        Element.Brand element = new Element.Brand("");
        String json = converter.toJson(element);
        Result result = to(ElementService.class).with(json).post("/element/create");

        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Elementet havde ikke en værdi");
    }

    @Test
    public void shouldFailWhenTryingToCreateExistingElement() throws Exception {
        Element.Brand element = new Element.Brand("Nike");
        saveToDatabase(element);

        Element.Brand newElement = new Element.Brand(element.getId(), "Adidas");
        String json = converter.toJson(newElement);
        Result result = to(ElementService.class).with(json).post("/element/create");

        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Elementet eksisterer allerede");
    }

    @Test
    public void shouldFailWhenCreatingElementWithAValueThatAlreadyExistsForTheType() throws Exception {
        Element.Brand element = new Element.Brand("Nike");
        saveToDatabase(element);

        Element.Brand otherElement = new Element.Brand("Nike");
        String json = converter.toJson(otherElement);
        Result result = to(ElementService.class).with(json).post("/element/create");

        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Et element af typen Brand med værdien Nike eksisterer allerede");
    }

    @Test
    public void shouldFailWhenGettingElementsWithNonExistingType() throws Exception {
        Result result = from(ElementService.class).get("/element/fake/get");
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Elementets type kunne ikke genkendes");
    }

    private void saveToDatabase(Element... elements) {
        for (Element element: elements)
            database.save(element);
    }

    @Test
    public void shouldFailDeletingIfNoId() throws Exception {
        Element.PaymentOption element = new Element.PaymentOption("Cash");
        saveToDatabase(element);

        Result result = to(ElementService.class).with("").post("/element/delete");
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Der er ikke angivet et id");
    }

    @Test
    public void shouldFailDeletingNonExisting() throws Exception {
        Result result = to(ElementService.class).with("fake").post("/element/delete");
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Kunne ikke slette ikke-eksisterende element");
    }

    @Test
    public void shouldFailDeletingElementWhenASaleUsesItForBrand() throws Exception {
        Element.Brand brand = new Element.Brand("Apple");
        saveToDatabase(brand);

        Sale sale = new Sale(null, null, null, 0, 0, asList(), asList(), brand, null, null);
        database.save(sale);

        Result result = to(ElementService.class).with(brand.getId()).post("/element/delete");
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Brand er i brug hos et udsalg og kan ikke slettes");

        Sale saleStillContainingBrand = database.load(Sale.class).matching("_id").with(sale.getId());
        ASSERT.that(saleStillContainingBrand.getBrand()).isEqualTo(brand);

        Element.Brand elementStillExists = database.load(Element.Brand.class).matching("_id").with(brand.getId());
        ASSERT.that(elementStillExists).isNotNull();
    }

    @Test
    public void shouldFailDeletingElementWhenASaleUsesItAsPaymentOption() throws Exception {
        Element.PaymentOption paymentOption = new Element.PaymentOption("Dankort");
        saveToDatabase(paymentOption);

        Element.Brand brand = new Element.Brand("Nike");
        Sale sale = new Sale(null, null, null, 0, 0, asList(paymentOption), asList(), brand, null, null);
        database.save(sale);

        Result result = to(ElementService.class).with(paymentOption.getId()).post("/element/delete");
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Betalingsmulighed er i brug hos et udsalg og kan ikke slettes");

        Sale saleStillContainingPaymentOption = database.load(Sale.class).matching("_id").with(sale.getId());
        ASSERT.that(saleStillContainingPaymentOption.getPaymentOptions()).containsExactly(paymentOption);

        Element.PaymentOption elementStillExists = database.load(Element.PaymentOption.class).matching("_id").with(paymentOption.getId());
        ASSERT.that(elementStillExists).isNotNull();
    }

    @Test
    public void shouldFailDeletingElementWhenASaleUsesItAsType() throws Exception {
        Element.Type type = new Element.Type("Men");
        saveToDatabase(type);

        Element.Brand brand = new Element.Brand("Nike");
        Sale sale = new Sale(null, null, null, 0, 0, asList(), asList(type), brand, null, null);
        database.save(sale);

        Result result = to(ElementService.class).with(type.getId()).post("/element/delete");
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Filter er i brug hos et udsalg og kan ikke slettes");

        Sale saleStillContainingType = database.load(Sale.class).matching("_id").with(sale.getId());
        ASSERT.that(saleStillContainingType.getTypes()).containsExactly(type);

        Element.Type elementStillExists = database.load(Element.Type.class).matching("_id").with(type.getId());
        ASSERT.that(elementStillExists).isNotNull();
    }

    @Test
    public void shouldFailUpdatingElementWithNoValue() throws Exception {
        Element.Brand element = new Element.Brand("Apple");
        saveToDatabase(element);

        String id = element.getId();
        Element.Brand updatedElement = new Element.Brand(id, "");
        String json = converter.toJson(updatedElement);

        Result result = to(ElementService.class).with(json).post("/element/update");
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Elementet havde ikke en værdi");
    }

    @Test
    public void shouldFailUpdatingNonExistingElement() throws Exception {
        Element.Brand element = new Element.Brand("Apple");
        String json = converter.toJson(element);

        Result result = to(ElementService.class).with(json).post("/element/update");
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Elementet eksisterer ikke");
    }

    @Test
    public void shouldFailUpdatingElementWithValueThatAlreadyExistsForType() throws Exception {
        Element.Brand apple = new Element.Brand("Apple");
        Element.Brand samsung = new Element.Brand("Samsung");
        saveToDatabase(apple, samsung);

        Element.Brand updatedElement = new Element.Brand(samsung.getId(), "Apple");
        String json = converter.toJson(updatedElement);
        Result result = to(ElementService.class).with(json).post("/element/update");
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Et element af typen Brand med værdien Apple eksisterer allerede");
    }
}
