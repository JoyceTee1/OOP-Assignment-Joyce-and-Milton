package smartlib.patterns;

public final class DecoratorCompositionExample {
    private DecoratorCompositionExample() {
    }

    public static NotificationService notificationService() {
        return SmartLibPatternComposition.decoratedNotificationService();
    }
}
