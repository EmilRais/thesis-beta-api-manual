package dk.developer.beta.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.developer.validation.single.NotEmpty;
import dk.developer.validation.single.NotNaN;

import java.util.Objects;

public class Location {
    @NotNaN(message = "Breddegrad er ikke valid")
    private final double latitude;

    @NotNaN(message = "Længdegrad er ikke valid")
    private final double longitude;

    @NotEmpty(message = "Adresse er ikke valid")
    private final String address;

    @NotEmpty(message = "By er ikke valid")
    private final String city;

    @NotEmpty(message = "Postnummer er ikke valid")
    private final String postalCode;

    @JsonCreator
    public Location(@JsonProperty("latitude") double latitude,
                    @JsonProperty("longitude") double longitude,
                    @JsonProperty("address") String address,
                    @JsonProperty("city") String city,
                    @JsonProperty("postalCode") String postalCode) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Location location = (Location) o;
        return Objects.equals(latitude, location.latitude) &&
                Objects.equals(longitude, location.longitude) &&
                Objects.equals(address, location.address) &&
                Objects.equals(city, location.city) &&
                Objects.equals(postalCode, location.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, address, city, postalCode);
    }

    @Override
    public String toString() {
        return "Location{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", postalCode='" + postalCode + '\'' +
                '}';
    }
}
