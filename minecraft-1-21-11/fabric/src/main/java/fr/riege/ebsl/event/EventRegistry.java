package fr.riege.ebsl.event;

import fr.riege.ebsl.registry.MapRegistry;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EventRegistry {

    public record Entry(String category, String name, List<String> fields) {
    }

    private static final MapRegistry<Class<? extends Event>, Class<? extends Event>> CLASSES = new MapRegistry<>(null);

    private EventRegistry() {
    }

    public static void register(Class<? extends Event> eventClass) {
        if (eventClass == null || CLASSES.contains(eventClass)) {
            return;
        }
        CLASSES.register(eventClass, eventClass);
    }

    public static List<Entry> all() {
        List<Entry> entries = new ArrayList<>(CLASSES.values().size());
        for (Class<? extends Event> eventClass : CLASSES.values()) {
            entries.add(entryOf(eventClass));
        }
        return Collections.unmodifiableList(entries);
    }

    private static Entry entryOf(Class<? extends Event> eventClass) {
        String category = lastPackageSegment(eventClass);
        String name = eventClass.getSimpleName().replaceFirst("Event$", "");
        List<String> fields = new ArrayList<>();

        for (Method method : eventClass.getDeclaredMethods()) {
            if (!method.getName().startsWith("get") || method.getParameterCount() != 0) {
                continue;
            }
            String fieldName = Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4);
            fields.add(fieldName + ": " + method.getReturnType().getSimpleName());
        }

        return new Entry(category, name, Collections.unmodifiableList(fields));
    }

    private static String lastPackageSegment(Class<?> type) {
        String packageName = type.getPackageName();
        int lastDot = packageName.lastIndexOf('.');
        return lastDot < 0 ? packageName : packageName.substring(lastDot + 1);
    }
}
