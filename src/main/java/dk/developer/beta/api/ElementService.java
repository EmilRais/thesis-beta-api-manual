package dk.developer.beta.api;

import dk.developer.database.DatabaseFront;
import dk.developer.database.DatabaseProvider;
import dk.developer.validation.single.NotEmpty;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.*;

@Path("element")
public class ElementService {
    private final DatabaseFront database = DatabaseProvider.databaseLayer();

    @POST
    @Path("/create")
    @Consumes(APPLICATION_JSON)
    public Response create(@Valid Element element) {
        if ( elementAlreadyExists(element) )
            throw new ValidationException("Elementet eksisterer allerede");

        if ( typeAlreadyExists(element) )
            throw new ValidationException(typeAlreadyExistsMessage(element));

        database.save(element);
        return Response.status(CREATED).build();
    }

    private String typeAlreadyExistsMessage(Element element) {
        String type = element.getClass().getSimpleName();
        String value = element.getValue();
        return format("Et element af typen %s med værdien %s eksisterer allerede", type, value);
    }

    private boolean typeAlreadyExists(Element element) {
        List<? extends Element> elements = database.loadAll(element.getClass());

        String elementValue = element.getValue();
        boolean alreadyExists = elements.stream()
                .map(Element::getValue)
                .anyMatch(value -> value.equals(elementValue));

        return alreadyExists;
    }

    private boolean elementAlreadyExists(Element element) {
        Object id = element.getId();
        return database.load(element.getClass()).matching("_id").with(id) != null;
    }

    @GET
    @Path("/{type}/get")
    @Produces(APPLICATION_JSON)
    public Response get(
            @PathParam("type")
            @NotEmpty(message = "Der er ikke angivet en type")
            String typeName) {
        Class<? extends Element> type = extractType(typeName);
        List<? extends Element> elements = database.loadAll(type);
        return Response.status(OK).entity(hack(elements)).build();
    }

    private List<Element> hack(List<? extends Element> elements) {
        System.out.println("Jackson polymorphic serialisation hack is in effect");

        class HackedStaticTypeList extends ArrayList<Element> {}
        HackedStaticTypeList result = new HackedStaticTypeList();
        elements.stream().forEach(result::add);
        return result;
    }

    private Class<? extends Element> extractType(String typeName) {
        if ( typeName.equalsIgnoreCase("brand") ) return Element.Brand.class;
        if ( typeName.equalsIgnoreCase("type") ) return Element.Type.class;
        if ( typeName.equalsIgnoreCase("payment_option") ) return Element.PaymentOption.class;
        throw new ValidationException("Elementets type kunne ikke genkendes");
    }

    @POST
    @Path("/delete")
    @Consumes(APPLICATION_JSON)
    public Response delete(
            @NotEmpty(message = "Der er ikke angivet et id")
            String id) {
        if ( elementIsUsedByASaleAsBrand(id) )
            throw new ValidationException("Brand er i brug hos et udsalg og kan ikke slettes");

        if ( elementIsUsedByASaleAsPaymentOption(id) )
            throw new ValidationException("Betalingsmulighed er i brug hos et udsalg og kan ikke slettes");

        if ( elementIsUsedByASaleAsType(id) )
            throw new ValidationException("Filter er i brug hos et udsalg og kan ikke slettes");

        boolean didUpdateAllSales = deleteElementFromAllSales(id);
        if ( !didUpdateAllSales )
            throw new ValidationException("Der skete en fejl, da elementet skulle slettes fra et udsalg");

        boolean didDelete = asList(Element.Brand.class, Element.Type.class, Element.PaymentOption.class).stream()
                .anyMatch(type -> database.delete(type).matching("_id").with(id));

        if ( !didDelete )
            throw new ValidationException("Kunne ikke slette ikke-eksisterende element");

        return Response.status(OK).build();
    }

    @POST
    @Path("/delete/brand/{id}")
    @Produces(TEXT_PLAIN)
    public Response deleteBrand(@PathParam("id") @NotEmpty(message = "Der er ikke angivet et id") String id) {
        if ( brandIsNeededByAnySale(id) )
            return Response.status(BAD_REQUEST).entity("Brand er i brug hos et udsalg og kan ikke slettes").build();

        boolean didDelete = database.delete(Element.Brand.class).matching("_id").with(id);
        if ( !didDelete )
            return Response.status(BAD_REQUEST).entity("Kunne ikke slette brand").build();

        return Response.status(OK).build();
    }

    @POST
    @Path("/delete/payment-option/{id}")
    @Produces(TEXT_PLAIN)
    public Response deletePaymentOption(@PathParam("id") @NotEmpty(message = "Der er ikke angivet et id") String id) {
        if ( paymentOptionIsNeededByAnySale(id) )
            return Response.status(BAD_REQUEST).entity("Betalingsmulighed er i brug hos et udsalg og kan ikke slettes").build();

        boolean didUpdateAllSales = removePaymentOptionFromAllSales(id);
        if ( !didUpdateAllSales )
            return Response.status(BAD_REQUEST).entity("Der skete en fejl, da elementet skulle slettes fra et udsalg").build();

        boolean didDelete = database.delete(Element.PaymentOption.class).matching("_id").with(id);
        if ( !didDelete )
            return Response.status(BAD_REQUEST).entity("Kunne ikke slette betalingsmulighed").build();

        return Response.status(OK).build();
    }

    @POST
    @Path("/delete/type/{id}")
    @Produces(TEXT_PLAIN)
    public Response deleteType(@PathParam("id") @NotEmpty(message = "Der er ikke angivet et id") String id) {
        if ( typeIsNeededByAnySale(id) )
            return Response.status(BAD_REQUEST).entity("Type er i brug hos et udsalg og kan ikke slettes").build();

        boolean didUpdateAllSales = removeTypeFromAllSales(id);
        if ( !didUpdateAllSales )
            return Response.status(BAD_REQUEST).entity("Der skete en fejl, da elementet skulle slettes fra et udsalg").build();

        boolean didDelete = database.delete(Element.Type.class).matching("_id").with(id);
        if ( !didDelete )
            return Response.status(BAD_REQUEST).entity("Kunne ikke slette type").build();

        return Response.status(OK).build();
    }

    private boolean removePaymentOptionFromAllSales(String id) {
        List<Sale> sales = database.loadAll(Sale.class);
        for (Sale sale : sales) {
            boolean didRemovePaymentOption = removeIfExists(id, sale.getPaymentOptions());
            if ( !didRemovePaymentOption )
                continue;

            boolean didUpdateSale = database.update(sale);
            if ( !didUpdateSale )
                return false;
        }
        return true;
    }

    private boolean removeTypeFromAllSales(String id) {
        List<Sale> sales = database.loadAll(Sale.class);
        for (Sale sale : sales) {
            boolean didRemoveType = removeIfExists(id, sale.getTypes());
            if ( !didRemoveType )
                continue;

            boolean didUpdateSale = database.update(sale);
            if ( !didUpdateSale )
                return false;
        }
        return true;
    }

    private boolean deleteElementFromAllSales(String id) {
        List<Sale> sales = database.loadAll(Sale.class);
        for (Sale sale : sales) {
            List<Element.PaymentOption> paymentOptions = sale.getPaymentOptions();
            boolean didRemovePaymentOption = removeIfExists(id, paymentOptions);

            List<Element.Type> types = sale.getTypes();
            boolean didRemoveType = removeIfExists(id, types);

            if ( !didRemovePaymentOption && !didRemoveType )
                continue;

            boolean didUpdateSale = database.update(sale);
            if ( !didUpdateSale )
                return false;
        }
        return true;
    }

    private boolean removeIfExists(String id, List<? extends Element> elements) {
        boolean elementContainsId = elements.stream()
                .anyMatch(paymentOption -> paymentOption.getId().equals(id));

        if ( !elementContainsId )
            return false;

        Iterator<? extends Element> iterator = elements.iterator();
        while ( iterator.hasNext() ) {
            Element element = iterator.next();
            if ( element.getId().equals(id) )
                iterator.remove();
        }
        return true;
    }

    private boolean brandIsNeededByAnySale(String id) {
        List<Sale> sales = database.loadAll(Sale.class);
        return sales.stream()
                .map(Sale::getBrand)
                .anyMatch(brand -> brand.getId().equals(id));
    }

    private boolean paymentOptionIsNeededByAnySale(String id) {
        List<Sale> sales = database.loadAll(Sale.class);
        return sales.stream()
                .map(Sale::getPaymentOptions)
                .anyMatch(paymentOptions -> paymentOptions.stream()
                        .anyMatch(paymentOption -> paymentOption.getId().equals(id)) && paymentOptions.size() < 2);
    }

    private boolean typeIsNeededByAnySale(String id) {
        List<Sale> sales = database.loadAll(Sale.class);
        return sales.stream()
                .map(Sale::getTypes)
                .anyMatch(types -> types.stream()
                        .anyMatch(type -> type.getId().equals(id)) && types.size() < 2);
    }

    private boolean elementIsUsedByASaleAsBrand(String id) {
        List<Sale> sales = database.loadAll(Sale.class);
        return sales.stream()
                .map(Sale::getBrand)
                .anyMatch(brand -> brand.getId().equals(id));
    }

    private boolean elementIsUsedByASaleAsPaymentOption(String id) {
        List<Sale> sales = database.loadAll(Sale.class);
        return sales.stream()
                .map(Sale::getPaymentOptions)
                .anyMatch(paymentOptions -> paymentOptions.stream()
                        .anyMatch(paymentOption -> paymentOption.getId().equals(id)) && paymentOptions.size() < 2);
    }

    private boolean elementIsUsedByASaleAsType(String id) {
        List<Sale> sales = database.loadAll(Sale.class);
        return sales.stream()
                .map(Sale::getTypes)
                .anyMatch(types -> types.stream()
                        .anyMatch(type -> type.getId().equals(id)) && types.size() < 2);
    }

    @POST
    @Path("/update")
    @Consumes(APPLICATION_JSON)
    public Response update(@Valid Element element) {
        if ( !elementAlreadyExists(element) )
            throw new ValidationException("Elementet eksisterer ikke");

        if ( typeAlreadyExists(element) )
            throw new ValidationException(typeAlreadyExistsMessage(element));

        boolean didUpdate = database.update(element);
        if ( !didUpdate )
            throw new ValidationException("Kunne ikke opdatere elementet");

        List<Sale> sales = database.loadAll(Sale.class);
        boolean didUpdateSales = sales.stream()
                .allMatch(sale -> updateSaleIfContainsElement(sale, element));
        if ( !didUpdateSales )
            throw new ValidationException("Kunne ikke opdatere alle udsalg indeholdende elementet");

        return Response.status(OK).build();
    }

    private boolean updateSaleIfContainsElement(Sale sale, Element element) {
        boolean didContainElement = didExchange(sale, element);
        if ( !didContainElement )
            return true;

        return database.update(sale);
    }

    private boolean didExchange(Sale sale, Element element) {
        if ( element instanceof Element.Brand)
            return exchangeBrandInSaleIfMatches(sale, (Element.Brand) element);

        if ( element instanceof Element.Type)
            return exchangeTypeInSaleIfMatches(sale, (Element.Type) element);

        if ( element instanceof Element.PaymentOption)
            return exchangePaymentOptionInSaleIfMatches(sale, (Element.PaymentOption) element);

        throw new RuntimeException("Type was not supposed to be null");
    }

    private boolean exchangePaymentOptionInSaleIfMatches(Sale sale, Element.PaymentOption paymentOption) {
        List<Element.PaymentOption> paymentOptions = sale.getPaymentOptions();
        ListIterator<Element.PaymentOption> iterator = paymentOptions.listIterator();
        return exchangeElementIfIdMatches(iterator, paymentOption);
    }

    private boolean exchangeTypeInSaleIfMatches(Sale sale, Element.Type type) {
        List<Element.Type> types = sale.getTypes();
        ListIterator<Element.Type> iterator = types.listIterator();
        return exchangeElementIfIdMatches(iterator, type);
    }

    private <T extends Element> boolean exchangeElementIfIdMatches(ListIterator<T> iterator, T element) {
        boolean didExchange = false;
        String id = element.getId();
        while ( iterator.hasNext() ) {
            T currentElement = iterator.next();
            if ( !currentElement.getId().equals(id) )
                continue;
            iterator.set(element);
            didExchange = true;

        }
        return didExchange;
    }

    private boolean exchangeBrandInSaleIfMatches(Sale sale, Element.Brand brand) {
        Element.Brand currentBrand = sale.getBrand();
        if ( !currentBrand.getId().equals(brand.getId()) )
            return false;

        sale.setBrand(brand);
        return true;
    }
}
