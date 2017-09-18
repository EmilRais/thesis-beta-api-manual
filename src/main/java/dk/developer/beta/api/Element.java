package dk.developer.beta.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dk.developer.database.Collection;
import dk.developer.database.DatabaseObject;
import dk.developer.validation.single.NotEmpty;
import org.bson.types.ObjectId;

import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = Element.Brand.class, name = "BRAND"),
                @JsonSubTypes.Type(value = Element.Type.class, name = "TYPE"),
                @JsonSubTypes.Type(value = Element.PaymentOption.class, name = "PAYMENT_OPTION"),
        }
)
public abstract class Element extends DatabaseObject {
    @JsonProperty("_id")
    protected final String id;

    @NotEmpty(message = "Elementet havde ikke en værdi")
    protected final String value;

    protected Element(String value) {
        this.value = value;
        this.id = ObjectId.get().toString();
    }

    protected Element(String id, String value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Element element = (Element) o;
        return Objects.equals(id, element.id) &&
                Objects.equals(value, element.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }

    @Override
    public String toString() {
        return "Element{" +
                "id='" + id + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    @Collection("Brands")
    public static class Brand extends Element {
        @JsonCreator
        public Brand(@JsonProperty("value") String value) {
            super(value);
        }

        public Brand(String id, String value) {
            super(id, value);
        }
    }

    @Collection("Types")
    public static class Type extends Element {
        @JsonCreator
        public Type(@JsonProperty("value") String value) {
            super(value);
        }

        public Type(String id, String value) {
            super(id, value);
        }
    }

    @Collection("PaymentOptions")
    public static class PaymentOption extends Element {
        @JsonCreator
        public PaymentOption(@JsonProperty("value") String value) {
            super(value);
        }

        public PaymentOption(String id, String value) {
            super(id, value);
        }
    }
}
