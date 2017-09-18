package dk.developer.beta.api;

import dk.developer.database.DatabaseFront;
import dk.developer.database.DatabaseProvider;
import dk.developer.validation.single.Id;
import dk.developer.validation.single.NotEmpty;
import dk.developer.validation.single.NotStored;
import dk.developer.validation.single.Stored;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Calendar.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;

@Path("sale")
public class SaleService {
    private final DatabaseFront database;
    private Apple apple;

    public SaleService() {
        database = DatabaseProvider.databaseLayer();
    }

    Apple apple() {
        if ( apple == null ) apple = Apple.production();
        return apple;
    }

    @GET
    @Path("/get/all")
    @Produces(APPLICATION_JSON)
    public Response getAll() {
        List<Sale> sales = database.loadAll(Sale.class);
        return Response.status(OK).entity(sales).build();
    }

    @POST
    @Path("/create")
    @Consumes(APPLICATION_JSON)
    public Response create(
            @Valid
            @NotStored(message = "Eventet eksisterede allerede")
            Sale sale) {
        database.save(sale);
        apple().removeInactiveDevices(database);
        sendPushNotifications(sale);
        return Response.status(CREATED).build();
    }

    private void sendPushNotifications(Sale sale) {
        List<Subscription> subscriptions = database.loadAll(Subscription.class);
        if ( subscriptions.isEmpty() )
            return;

        apple().pushNotificationToMatchingDevices(subscriptions, sale);
    }

    @GET
    @Path("/get")
    @Produces(APPLICATION_JSON)
    public Response get() {
        List<Sale> sales = database.loadAll(Sale.class);

        List<Sale> filteredSales = sales.stream()
                .filter(this::saleIsStillActive)
                .collect(Collectors.toList());

        return Response.status(OK).entity(filteredSales).build();
    }

    private boolean saleIsStillActive(Sale sale) {
        Calendar currentDate = currentDate();

        Calendar toDate = Calendar.getInstance();
        toDate.setTime(new Date(sale.getToDate()));

        if ( currentDate.get(YEAR) < toDate.get(YEAR) )
            return true;

        boolean isSameYear = currentDate.get(YEAR) == toDate.get(YEAR);
        if ( isSameYear && currentDate.get(MONTH) < toDate.get(MONTH) )
            return true;

        boolean isSameMonth = currentDate.get(MONTH) == toDate.get(MONTH);
        if ( isSameYear && isSameMonth && currentDate.get(DAY_OF_MONTH) <= toDate.get(DAY_OF_MONTH) )
            return true;

        return false;
    }

    private Calendar currentDate() {
        return Calendar.getInstance();
    }

    @POST
    @Path("/update")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response update(
            @NotNull(message = "Der blev ikke angivet et event")
            @Valid
            @Stored(message = "Kunne ikke opdatere ikke-eksisterende event")
            Sale sale) {
        Sale existingSale = database.load(Sale.class).matching("_id").with(sale.getId());
        if ( sale.equals(existingSale) )
            throw new ValidationException("Der er ingen ændringer i eventet");

        boolean updated = database.update(sale);

        if ( updated ) {
            Sale updatedSale = database.load(Sale.class).matching("_id").with(sale.getId());
            return Response.status(OK).entity(updatedSale).build();
        }
        return Response.status(BAD_REQUEST).build();
    }

    @POST
    @Path("/delete")
    @Consumes(APPLICATION_JSON)
    public Response delete(
            @NotEmpty(message = "Der er ikke angivet et id")
            @Id(of = Sale.class, message = "Id'et pegede ikke på et event")
            String id) {
        boolean deleted = database.delete(Sale.class).matching("_id").with(id);
        if ( !deleted )
            throw new ValidationException("Kunne ikke slette event");

        return Response.status(OK).build();
    }
}
