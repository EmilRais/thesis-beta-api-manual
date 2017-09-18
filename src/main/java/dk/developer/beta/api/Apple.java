package dk.developer.beta.api;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.EnhancedApnsNotification;
import dk.developer.database.DatabaseFront;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Apple {
    public static final String PASSWORD = "21312adfcdcdd23143";
    private final ApnsService service;

    public static Apple production() {
        InputStream inputStream = Apple.class.getResourceAsStream("production.p12");
        return new Apple(APNS.newService()
                .withCert(inputStream, PASSWORD)
                .withFeedbackDestination("feedback.sandbox.push.apple.com", 2196)
                .withProductionDestination()
                .build());
    }

    public static Apple development() {
        InputStream inputStream = Apple.class.getResourceAsStream("development.p12");
        return new Apple(APNS.newService()
                .withCert(inputStream, PASSWORD)
                .withSandboxDestination()
                .build());
    }

    private Apple(ApnsService service) {
        this.service = service;
    }

    public void pushNotificationToMatchingDevices(List<Subscription> subscriptions, Sale sale) {
        List<Subscription> filteredSubscriptions = filterSubscriptions(subscriptions, sale);
        filteredSubscriptions.stream().forEach(sub -> pushNotification(sub.getDeviceToken(), sale));
    }

    List<Subscription> filterSubscriptions(List<Subscription> subscriptions, Sale sale) {
        Set<String> filterIds = sale.getTypes().stream()
                .map(filter -> filter.getId())
                .collect(Collectors.toSet());

        return subscriptions.stream()
                .filter(sub -> sub.getFavoriteBrands().contains(sale.getBrand().getId()))
                .filter(sub -> !sub.getExclusionFilters().containsAll(filterIds))
                .collect(Collectors.toList());
    }

    public void pushNotification(String token, Sale sale) {
        String alert = alertMessage(sale);
        String payload = APNS.newPayload()
                .alertBody(alert)
                .sound("default")
                .build();

        EnhancedApnsNotification notification = new EnhancedApnsNotification(
                EnhancedApnsNotification.INCREMENT_ID(),
                currentTime(),
                token,
                payload);

        service.push(notification);
    }

    private String alertMessage(Sale sale) {
        String name = sale.getName();
        String city = sale.getLocation().getCity();
        Date date = new Date(sale.getFromDate());
        String dateString = new SimpleDateFormat("dd-MM-yyyy").format(date);
        return "Nyt event " + name + " i " + city + " d. " + dateString + ".";
    }

    public void removeInactiveDevices(DatabaseFront database) {
        Map<String, Date> inactiveDevices = service.getInactiveDevices();
        for (String deviceToken : inactiveDevices.keySet()) {
            Date inactiveAsOf = inactiveDevices.get(deviceToken);
            boolean isInactive = inactiveAsOf.before(currentDate());
            if ( isInactive ) {
                database.delete(Subscription.class).matching("deviceToken").with(deviceToken);
            }
        }
    }

    public String inactiveTest() {
        Map<String, Date> devices = service.getInactiveDevices();
        return devices.toString();
    }

    private Date currentDate() {
        return new Date();
    }

    private int currentTime() {
        return (int) currentDate().getTime() / 1000 + 60 * 60;
    }
}
