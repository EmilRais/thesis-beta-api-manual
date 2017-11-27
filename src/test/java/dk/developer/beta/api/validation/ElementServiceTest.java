package dk.developer.beta.api.validation;

import dk.developer.beta.api.Element;
import dk.developer.beta.api.ElementService;
import dk.developer.database.DatabaseFront;
import dk.developer.testing.Result;
import dk.developer.testing.TestDatabaseProvider;
import dk.developer.utility.Converter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.google.common.truth.Truth.ASSERT;
import static dk.developer.testing.RestServiceTestHelper.from;
import static dk.developer.testing.RestServiceTestHelper.to;
import static dk.developer.utility.Converter.converter;
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
