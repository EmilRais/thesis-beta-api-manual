package dk.developer.beta.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.developer.validation.single.NotEmpty;

import java.util.Objects;

public class Unsubscription {
    @NotEmpty(message = "Ugyldig token")
    private final String deviceToken;

    @JsonCreator
    public Unsubscription(@JsonProperty("deviceToken") String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    @Override
    public String toString() {
        return "Unsubscription{" +
                "deviceToken='" + deviceToken + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Unsubscription that = (Unsubscription) o;
        return Objects.equals(deviceToken, that.deviceToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceToken);
    }
}
