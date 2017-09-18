package dk.developer.beta.api;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.truth0.Truth.ASSERT;

public class AppleTest {
    Apple apple;

    @BeforeMethod(enabled = false)
    public void setUp() throws Exception {
        apple = Apple.development();
    }

    @Test(enabled = false)
    public void shouldFilterSubscriptionWhenNotFavorite() throws Exception {
        Element.Brand saleBrand = new Element.Brand("brand");
        Sale sale = sale(asList(), saleBrand);

        Subscription subscription = subscription(asList(), asList());

        List<Subscription> filterSubscriptions = apple.filterSubscriptions(asList(subscription), sale);
        ASSERT.that(filterSubscriptions).isEmpty();
    }

    @Test(enabled = false)
    public void shouldFilterSubscriptionWhenAllFiltersMatch() throws Exception {
        Element.Brand brand = new Element.Brand("brand");
        List<Element.Type> filters = asList(new Element.Type("type1"), new Element.Type("type2"));
        Sale sale = sale(filters, brand);

        List<String> filterIds = filters.stream().map(filter -> filter.getId()).collect(toList());
        Subscription subscription = subscription(filterIds, asList(brand.getId()));
        List<Subscription> filteredSubscriptions = apple.filterSubscriptions(asList(subscription), sale);
        ASSERT.that(filteredSubscriptions).isEmpty();
    }

    @Test(enabled = false)
    public void shouldNotFilterWhenValid() throws Exception {
        Element.Brand saleBrand = new Element.Brand("brand");
        List<Element.Type> filters = asList(new Element.Type("type1"), new Element.Type("type2"));
        Sale sale = sale(filters, saleBrand);

        Subscription subscription = subscription(asList(), asList(saleBrand.getId()));

        List<Subscription> filterSubscriptions = apple.filterSubscriptions(asList(subscription), sale);
        ASSERT.that(filterSubscriptions).containsExactly(subscription);
    }

    @Test(enabled = false)
    public void developmentPushNotificationTest() throws Exception {
        Apple apple = Apple.development();

        Location location = new Location(123, 123, "address", "city", "postalCode");
        Element.Brand brand = new Element.Brand("brand");
        Sale sale = new Sale("Development Test", location, null, 123, 123, null, asList(new Element.Type("type")), brand, "", "");

        String deviceToken = "9d2e03d7717ed1c68b277946b6a3ca31a45c985eea91b16615f212cfbb919b16";
        apple.pushNotification(deviceToken, sale);

        System.out.println("Devices: " + apple.inactiveTest());
    }

    @Test(enabled = false)
    public void productionPushNotificationTest() throws Exception {
        Apple apple = Apple.production();
        Location location = new Location(123, 123, "address", "city", "postalCode");
        Element.Brand brand = new Element.Brand("brand");
        Sale sale = new Sale("Production Test", location, null, 123, 123, null, asList(new Element.Type("type")), brand, "", "");

        String deviceToken = "f0a2204f4233a6a0ca04652ead4edd1a46e5e508dfc649b0bb3c28bcbf99c37f";
        apple.pushNotification(deviceToken, sale);

        System.out.println("Devices: " + apple.inactiveTest());
    }

    private Sale sale(List<Element.Type> filters, Element.Brand brand) {
        return new Sale(null, null, null, 123, 123, null, filters, brand, null, null);
    }

    private Subscription subscription(List<String> filters, List<String> brands) {
        return new Subscription("token", filters, brands);
    }
}