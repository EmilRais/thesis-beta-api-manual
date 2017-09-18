package dk.developer.beta.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.truth.Truth;
import dk.developer.testing.DatabaseInitialiser;
import dk.developer.testing.Result;
import dk.developer.utility.Converter;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.List;

import static dk.developer.testing.RestServiceTestHelper.from;
import static dk.developer.testing.RestServiceTestHelper.to;
import static dk.developer.utility.Converter.converter;
import static java.util.Arrays.asList;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;
import static org.truth0.Truth.ASSERT;

public class SaleServiceAcceptanceTest extends DatabaseInitialiser {
    private static final Converter converter = converter();

    @Test(enabled = false)
    public void shouldCreateValidSale() throws Exception {
        Sale sale = sale();
        String json = converter.toJson(sale);

        Result create = create(json);
        ASSERT.that(create.status()).isSameAs(CREATED);
    }

    private Sale sale() {
        return new Sale("name", location(), "10#11#12#13#14#15#16", 123, 123, payments(), types(), brand(), "description", "l");
    }

    private List<Element.PaymentOption> payments() {
        Element.PaymentOption paymentOption = new Element.PaymentOption("Dankort");
        database.save(paymentOption);
        return asList(paymentOption);
    }

    private Element.Brand brand() {
        Element.Brand brand = new Element.Brand("Nike");
        database.save(brand);
        return brand;
    }

    private List<Element.Type> types() {
        Element.Type type = new Element.Type("Men");
        database.save(type);
        return asList(type);
    }

    private Location location() {
        return new Location(12.34, 56.78, "address", "city", "postalCode");
    }

    private Result create(String json) {
        return to(SaleService.class).with(json).post("/sale/create");
    }

    @Test
    public void shouldUpdateValidSale() throws Exception {
        Sale sale = sale();
        database.save(sale);

        Element.Brand adidas = new Element.Brand("Adidas");
        database.save(adidas);
        sale.setBrand(adidas);
        String json = converter.toJson(sale);
        Result update = update(json);
        ASSERT.that(update.status()).isSameAs(OK);
    }

    private Result update(String json) {
        return to(SaleService.class).with(json).post("/sale/update");
    }

    @Test
    public void shouldDeleteWithValidId() throws Exception {
        Sale sale = sale();
        database.save(sale);

        Result result = to(SaleService.class).with(sale.getId().toString()).post("/sale/delete");
        ASSERT.that(result.status()).isSameAs(OK);
    }

    @Test
    public void shouldGetOneActiveSale() throws Exception {
        List<Element.PaymentOption> payment = asList(new Element.PaymentOption("Dankort"), new Element.PaymentOption("Kontant"));
        List<Element.Type> type = asList(new Element.Type("Men"), new Element.Type("WOMEN"));
        Element.Brand brand = new Element.Brand("Nike");

        Sale activeSale = new Sale("Bilka", location(), "10#11#12#13#14#15#16", 123l, timeAfterNow(), payment, type, brand,
                "beskrivelse", "logo");
        Sale inactiveSale = new Sale("Bilka", location(), "10#11#12#13#14#15#16", 123l, timeBeforeNow(), payment, type, brand,
                "beskrivelse", "logo");
        List<Sale> sales = getHelper("/sale/get", activeSale, inactiveSale);
        ASSERT.that(sales).containsExactly(activeSale);
    }

    private long timeBeforeNow() {
        return new Date().getTime() - 1000 * 60 * 60 * 24;
    }

    private long timeAfterNow() {
        return new Date().getTime() + 100000;
    }

    private List<Sale> getHelper(String url, Sale... sales) throws java.io.IOException {
        for (Sale sale : sales) {
            database.save(sale);
        }

        Result result = from(SaleService.class).get(url);
        Truth.ASSERT.that(result.status()).isSameAs(OK);

        return converter.fromJson(result.content(), new TypeReference<List<Sale>>() {
        });
    }

    @Test
    public void shouldGetSeveralActiveSales() throws Exception {
        List<Element.PaymentOption> onePayment = asList(new Element.PaymentOption("Dankort"));
        List<Element.Type> oneType = asList(new Element.Type("Men"), new Element.Type("WOMEN"), new Element.Type("Other"));

        List<Element.PaymentOption> anotherPayment = asList(new Element.PaymentOption("Dankort"), new Element.PaymentOption("Kontant"));
        List<Element.Type> anotherType = asList(new Element.Type("Men"), new Element.Type("Women"));

        Element.Brand brand = new Element.Brand("Nike");
        Sale oneActiveSale = new Sale("Føtex", location(), "10#11#12#13#14#15#16", 123l, timeAfterNow(), onePayment, oneType, brand,
                "beskrivelse", "logo");

        Sale anotherActiveSale = new Sale("Bilka", location(), "10#11#12#13#14#15#16", 123l, timeAfterNow(), anotherPayment, anotherType,
                brand, "beskrivelse", "logo");
        Sale inactiveSale = new Sale("Bilka", location(), "10#11#12#13#14#15#16", 123l, timeBeforeNow(), anotherPayment, anotherType,
                brand, "beskrivelse", "logo");

        List<Sale> sales = getHelper("/sale/get", oneActiveSale, anotherActiveSale, inactiveSale);
        ASSERT.that(sales).containsExactly(oneActiveSale, anotherActiveSale);
    }

    @Test
    public void shouldGetOneSale() throws Exception {
        List<Element.PaymentOption> payment = asList(new Element.PaymentOption("Dankort"));
        List<Element.Type> type = asList(new Element.Type("Men"), new Element.Type("WOMEN"), new Element.Type("Other"));
        Element.Brand brand = new Element.Brand("Nike");
        Sale inactiveSale = new Sale("Bilka", location(), "Fra 10 til 22", 123l, timeBeforeNow(), payment, type, brand, "beskrivelse",
                "logo");

        List<Sale> sales = getHelper("/sale/get/all", inactiveSale);
        ASSERT.that(sales).containsExactly(inactiveSale);
    }

    @Test
    public void shouldGetSeveralSales() throws Exception {
        Element.Brand brand = new Element.Brand("Nike");
        List<Element.PaymentOption> payment = asList(new Element.PaymentOption("Dankort"));
        List<Element.Type> type = asList(new Element.Type("Men"), new Element.Type("WOMEN"), new Element.Type("Other"));
        Sale activeSale = new Sale("Føtex", location(), "Fra 11 til 23", 123l, timeAfterNow(), payment, type, brand, "beskrivelse", "logo");
        Sale inactiveSale = new Sale("Bilka", location(), "Fra 10 til 22", 123l, timeBeforeNow(), payment, type, brand, "beskrivelse",
                "logo");

        List<Sale> sales = getHelper("/sale/get/all", activeSale, inactiveSale);
        ASSERT.that(sales).containsExactly(activeSale, inactiveSale);
    }
}
