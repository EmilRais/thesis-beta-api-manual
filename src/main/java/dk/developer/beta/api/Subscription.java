package dk.developer.beta.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.developer.database.Collection;
import dk.developer.database.DatabaseObject;
import dk.developer.validation.plural.NoneAreEmpty;
import dk.developer.validation.single.NotEmpty;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@Collection("Subscriptions")
public class Subscription extends DatabaseObject {
    @JsonProperty("_id")
    private final String id;

    @NotEmpty(message = "Ugyldig token")
    private final String deviceToken;

    @NotNull(message = "Eksklusions-filtre må ikke angives som null")
    @NoneAreEmpty(message = "Der må ikke være nogen ugyldige eksklusions-filtre")
    private final List<String> exclusionFilters;

    @NotNull(message = "Favorit-brands må ikke angives som null")
    @NoneAreEmpty(message = "Der må ikke være nogen ugyldige favorit-brands")
    private final List<String> favoriteBrands;

    @JsonCreator
    public Subscription(@JsonProperty("deviceToken") String deviceToken,
                        @JsonProperty("exclusionFilters") List<String> exclusionFilters,
                        @JsonProperty("favoriteBrands") List<String> favoriteBrands) {
        this.deviceToken = deviceToken;
        this.exclusionFilters = exclusionFilters;
        this.favoriteBrands = favoriteBrands;
        id = ObjectId.get().toString();
    }

    public Subscription(String id, String deviceToken, List<String> exclusionFilters, List<String> favoriteBrands) {
        this.id = id;
        this.deviceToken = deviceToken;
        this.exclusionFilters = exclusionFilters;
        this.favoriteBrands = favoriteBrands;
    }

    public String getId() {
        return id;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public List<String> getExclusionFilters() {
        return exclusionFilters;
    }

    public List<String> getFavoriteBrands() {
        return favoriteBrands;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "id='" + id + '\'' +
                ", deviceToken='" + deviceToken + '\'' +
                ", exclusionFilters=" + exclusionFilters +
                ", favoriteBrands=" + favoriteBrands +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Subscription that = (Subscription) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(deviceToken, that.deviceToken) &&
                Objects.equals(exclusionFilters, that.exclusionFilters) &&
                Objects.equals(favoriteBrands, that.favoriteBrands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deviceToken, exclusionFilters, favoriteBrands);
    }
}
