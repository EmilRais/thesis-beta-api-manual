package dk.developer.beta.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.developer.beta.api.validation.ConsistentDates;
import dk.developer.beta.api.validation.OpeningHours;
import dk.developer.database.Collection;
import dk.developer.database.DatabaseObject;
import dk.developer.validation.plural.AllAreStored;
import dk.developer.validation.single.NotEmpty;
import dk.developer.validation.single.Stored;
import org.bson.types.ObjectId;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@Collection("Sales")
@ConsistentDates(message = "Fra dato er efter til dato")
public class Sale extends DatabaseObject {
    @JsonProperty("_id")
    private final String id;

    @NotEmpty(message = "Navn er ikke valid")
    private final String name;

    @NotNull(message = "Lokation er ikke valid")
    @Valid
    private final Location location;

    @NotEmpty(message = "Der var ikke angivet åbningstider")
    @OpeningHours(message = "Åbningstider er ikke valid")
    private final String openingHours;

    private final long fromDate;
    private final long toDate;

    @NotEmpty(message = "Der var ikke angivet nogen betalingsmuligheder")
    @Valid
    @AllAreStored(message = "Der var betalingsmuligheder, som ikke eksisterede")
    private final List<Element.PaymentOption> paymentOptions;

    @NotEmpty(message = "Der var ikke angivet nogen filtre")
    @Valid
    @AllAreStored(message = "Der var filtre, som ikke eksisterede")
    private final List<Element.Type> types;

    @NotNull(message = "Brand er ikke valid")
    @Valid
    @Stored(message = "Brand eksisterede ikke")
    private Element.Brand brand;

    @NotEmpty(message = "Beskrivelse er ikke valid")
    private final String description;

    @NotEmpty(message = "Logo er ikke valid")
    private final String logo;

    public Sale(String id, String name, Location location, String openingHours, long fromDate, long toDate,
                List<Element.PaymentOption> paymentOptions, List<Element.Type> types, Element.Brand brand, String description, String logo) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.openingHours = openingHours;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.paymentOptions = paymentOptions;
        this.types = types;
        this.brand = brand;
        this.description = description;
        this.logo = logo;
    }

    @JsonCreator
    public Sale(@JsonProperty("name") String name,
                @JsonProperty("location") Location location,
                @JsonProperty("openingHours") String openingHours,
                @JsonProperty("fromDate") long fromDate,
                @JsonProperty("toDate") long toDate,
                @JsonProperty("paymentOptions") List<Element.PaymentOption> paymentOptions,
                @JsonProperty("types") List<Element.Type> types,
                @JsonProperty("brand") Element.Brand brand,
                @JsonProperty("description") String description,
                @JsonProperty("logo") String logo) {
        id = ObjectId.get().toString();
        this.name = name;
        this.location = location;
        this.openingHours = openingHours;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.paymentOptions = paymentOptions;
        this.types = types;
        this.brand = brand;
        this.description = description;
        this.logo = logo;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public long getFromDate() {
        return fromDate;
    }

    public long getToDate() {
        return toDate;
    }

    public List<Element.PaymentOption> getPaymentOptions() {
        return paymentOptions;
    }

    public List<Element.Type> getTypes() {
        return types;
    }

    public Element.Brand getBrand() {
        return brand;
    }

    public void setBrand(Element.Brand brand) {
        this.brand = brand;
    }

    public String getDescription() {
        return description;
    }

    public String getLogo() {
        return logo;
    }

    @Override
    public String toString() {
        return "Sale{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location=" + location +
                ", openingHours='" + openingHours + '\'' +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                ", paymentOptions=" + paymentOptions +
                ", types=" + types +
                ", brand=" + brand +
                ", description='" + description + '\'' +
                ", logo='" + logo + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Sale sale = (Sale) o;
        return Objects.equals(fromDate, sale.fromDate) &&
                Objects.equals(toDate, sale.toDate) &&
                Objects.equals(id, sale.id) &&
                Objects.equals(name, sale.name) &&
                Objects.equals(location, sale.location) &&
                Objects.equals(openingHours, sale.openingHours) &&
                Objects.equals(paymentOptions, sale.paymentOptions) &&
                Objects.equals(types, sale.types) &&
                Objects.equals(brand, sale.brand) &&
                Objects.equals(description, sale.description) &&
                Objects.equals(logo, sale.logo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, location, openingHours, fromDate, toDate, paymentOptions, types, brand, description, logo);
    }
}
