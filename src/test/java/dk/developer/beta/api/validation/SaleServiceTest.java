package dk.developer.beta.api.validation;

import dk.developer.database.DatabaseFront;
import dk.developer.beta.api.Location;
import dk.developer.beta.api.Sale;
import dk.developer.beta.api.SaleService;
import dk.developer.testing.Result;
import dk.developer.testing.TestDatabaseProvider;
import dk.developer.utility.Converter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static dk.developer.beta.api.Element.*;
import static dk.developer.testing.RestServiceTestHelper.to;
import static dk.developer.utility.Converter.converter;
import static java.lang.Double.NaN;
import static java.util.Arrays.asList;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.truth0.Truth.ASSERT;

public class SaleServiceTest {
    private static final Converter converter = converter();
    private DatabaseFront database;

    @BeforeMethod
    public void setUp() throws Exception {
        database = TestDatabaseProvider.memoryTestDatabase();
    }

    @Test
    public void shouldFailCreatingAndUpdatingEmptyName() throws Exception {
        Sale sale = new Sale("", location(), openingHours(), fromDate(), toDate(), payments(), types(), brand(), description(), logo());
        assertInvalidSale(sale, "Navn er ikke valid");
    }

    @Test
    public void shouldFailCreatingAndUpdatingEmptyLatitude() throws Exception {
        Location location = new Location(NaN, longitude(), address(), city(), postalCode());
        Sale sale = new Sale(name(), location, openingHours(), fromDate(), toDate(), payments(), types(), brand(), description(), logo());
        assertInvalidSale(sale, "Breddegrad er ikke valid");
    }

    private String name() {
        return "name";
    }

    @Test
    public void shouldFailCreatingAndUpdatingEmptyLongitude() throws Exception {
        Location location = new Location(latitude(), NaN, address(), city(), postalCode());
        Sale sale = new Sale(name(), location, openingHours(), fromDate(), toDate(), payments(), types(), brand(), description(), logo());
        assertInvalidSale(sale, "Længdegrad er ikke valid");
    }

    private double latitude() {
        return 12.34;
    }

    @Test
    public void shouldFailCreatingAndUpdatingEmptyAddress() throws Exception {
        Location location = new Location(latitude(), longitude(), "", city(), postalCode());
        Sale sale = new Sale(name(), location, openingHours(), fromDate(), toDate(), payments(), types(), brand(), description(), logo());
        assertInvalidSale(sale, "Adresse er ikke valid");
    }

    private double longitude() {
        return 56.78;
    }

    @Test
    public void shouldFailCreatingAndUpdatingEmptyCity() throws Exception {
        Location location = new Location(latitude(), longitude(), address(), "", postalCode());
        Sale sale = new Sale(name(), location, openingHours(), fromDate(), toDate(), payments(), types(), brand(), description(), logo());
        assertInvalidSale(sale, "By er ikke valid");
    }

    private String address() {
        return "address";
    }

    @Test
    public void shouldFailCreatingAndUpdatingEmptyPostalCode() throws Exception {
        Location location = new Location(latitude(), longitude(), address(), city(), "");
        Sale sale = new Sale(name(), location, openingHours(), fromDate(), toDate(), payments(), types(), brand(), description(), logo());
        assertInvalidSale(sale, "Postnummer er ikke valid");
    }

    private String city() {
        return "city";
    }

    @Test
    public void shouldFailCreatingAndUpdatingInvalidOpeningHours() throws Exception {
        Sale sale = new Sale(name(), location(), "10#11#12#13#14#1516", fromDate(), toDate(), payments(), types(), brand(), description(), logo());
        assertInvalidSale(sale, "Åbningstider er ikke valid");
    }

    private String postalCode() {
        return "postalCode";
    }

    @Test
    public void shouldFailCreatingAndUpdatingToDateBeforeFromDate() throws Exception {
        Sale sale = new Sale(name(), location(), openingHours(), 2, 1, payments(), types(), brand(), description(), logo());
        assertInvalidSale(sale, "Fra dato er efter til dato");
    }

    private String openingHours() {
        return "10#11#12#13#14#15#16";
    }

    @Test
    public void shouldFailCreatingAndUpdatingEmptyPaymentOptions() throws Exception {
        Sale sale = new Sale(name(), location(), openingHours(), fromDate(), toDate(), asList(), types(), brand(), description(), logo());
        assertInvalidSale(sale, "Der var ikke angivet nogen betalingsmuligheder");
    }

    private int toDate() {
        return 123;
    }

    private int fromDate() {
        return 123;
    }

    @Test
    public void shouldFailCreatingAndUpdatingNonExistingPaymentOptions() throws Exception {
        PaymentOption paymentOption = new PaymentOption("Dankort");
        Sale sale = new Sale(name(), location(), openingHours(), fromDate(), toDate(), asList(paymentOption), types(), brand(), description(), logo());
        assertInvalidSale(sale, "Der var betalingsmuligheder, som ikke eksisterede");
    }

    @Test
    public void shouldFailCreatingAndUpdatingEmptyTypes() throws Exception {
        Sale sale = new Sale(name(), location(), openingHours(), fromDate(), toDate(), payments(), asList(), brand(), description(), logo());
        assertInvalidSale(sale, "Der var ikke angivet nogen filtre");
    }

    private List<PaymentOption> payments() {
        PaymentOption paymentOption = new PaymentOption("Dankort");
        database.save(paymentOption);
        return asList(paymentOption);
    }

    @Test
    public void shouldFailCreatingAndUpdatingNonExistingTypes() throws Exception {
        Type type = new Type("Men");

        Sale sale = new Sale(name(), location(), openingHours(), fromDate(), toDate(), payments(), asList(type), brand(), description(), logo());
        assertInvalidSale(sale, "Der var filtre, som ikke eksisterede");
    }

    @Test
    public void shouldFailCreatingAndUpdatingEmptyBrand() throws Exception {
        Sale sale = new Sale(name(), location(), openingHours(), fromDate(), toDate(), payments(), types(), null, description(), logo());
        assertInvalidSale(sale, "Brand er ikke valid");
    }

    @Test
    public void shouldFailCreatingAndUpdatingNonExitingBrand() throws Exception {
        Brand brand = new Brand("Nike");

        Sale sale = new Sale(name(), location(), openingHours(), fromDate(), toDate(), payments(), types(), brand, description(), logo());
        assertInvalidSale(sale, "Brand eksisterede ikke");
    }

    @Test
    public void shouldFailCreatingAndUpdatingEmptyDescription() throws Exception {
        Sale sale = new Sale(name(), location(), openingHours(), fromDate(), toDate(), payments(), types(), brand(), "", logo());
        assertInvalidSale(sale, "Beskrivelse er ikke valid");
    }

    private String logo() {
        return "logo";
    }

    private Brand brand() {
        Brand brand = new Brand("Nike");
        database.save(brand);
        return brand;
    }

    private List<Type> types() {
        Type type = new Type("Men");
        database.save(type);
        return asList(type);
    }

    @Test
    public void shouldFailCreatingAndUpdatingEmptyLogo() throws Exception {
        Sale sale = new Sale(name(), location(), openingHours(), fromDate(), toDate(), payments(), types(), brand(), description(), "");
        assertInvalidSale(sale, "Logo er ikke valid");
    }

    private String description() {
        return "description";
    }

    @Test(enabled = false)
    public void shouldFailCreatingExistingSale() throws Exception {
        String json = converter.toJson(sale());
        to(SaleService.class).with(json).post("/sale/create");
        Result errorResult = to(SaleService.class).with(json).post("/sale/create");

        ASSERT.that(errorResult.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(errorResult.content()).isEqualTo("Eventet eksisterede allerede");
    }

    @Test
    public void shouldFailUpdatingNonExistingSale() throws Exception { 
        String json = converter.toJson(sale());
        Result result = to(SaleService.class).with(json).post("/sale/update");
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Kunne ikke opdatere ikke-eksisterende event");
    }

    @Test
    public void shouldFailUpdatingNotChangedSale() throws Exception {
        Sale sale = sale();
        database.save(sale);
        String json = converter.toJson(sale);
        Result result = to(SaleService.class).with(json).post("/sale/update");
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Der er ingen ændringer i eventet");
    }

    @Test
    public void shouldFailDeletingWithEmptyId() throws Exception {
        Result result = to(SaleService.class).with("").post("/sale/delete");
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Der er ikke angivet et id");
    }

    @Test
    public void shouldFailDeletingNonExistingSale() throws Exception {
        Result result = to(SaleService.class).with("1").post("/sale/delete");
        ASSERT.that(result.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(result.content()).isEqualTo("Id'et pegede ikke på et event");
    }

    private Sale sale() {
        return new Sale(name(), location(), openingHours(), fromDate(), toDate(), payments(), types(), brand(), description(), "l");
    }

    private Location location() {
        return new Location(12.34, 56.78, address(), city(), postalCode());
    }

    private Result create(String json) {
        return to(SaleService.class).with(json).post("/sale/create");
    }

    private Result update(String json) {
        return to(SaleService.class).with(json).post("/sale/update");
    }

    private void assertInvalidSale(Sale sale, String errorMessage) {
        String json = converter.toJson(sale);
        Result create = create(json);

        database.save(sale);
        Result update = update(json);
        assertError(create, update, errorMessage);
    }

    private void assertError(Result create, Result update, String message) {
        ASSERT.that(create.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(create.content()).isEqualTo(message);

        ASSERT.that(update.status()).isSameAs(BAD_REQUEST);
        ASSERT.that(update.content()).isEqualTo(message);
    }

}
