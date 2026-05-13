/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.riege.ebsl.common.core.event;

import fr.riege.ebsl.common.core.registry.MapRegistry;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EventRegistry {

    public record Entry(String category, String name, List<String> fields) {
    }

    private static final MapRegistry<Class<?>, Class<?>> CLASSES = new MapRegistry<>(null);

    private EventRegistry() {
    }

    public static void register(Class<?> eventClass) {
        if (eventClass == null || CLASSES.contains(eventClass)) {
            return;
        }
        CLASSES.register(eventClass, eventClass);
    }

    public static List<Entry> all() {
        List<Entry> entries = new ArrayList<>(CLASSES.values().size());
        for (Class<?> eventClass : CLASSES.values()) {
            entries.add(entryOf(eventClass));
        }
        return Collections.unmodifiableList(entries);
    }

    private static Entry entryOf(Class<?> eventClass) {
        String category = lastPackageSegment(eventClass);
        String name = eventClass.getSimpleName().replaceFirst("Event$", "");
        List<String> fields = new ArrayList<>();

        Entry entry = new Entry(category, name, Collections.unmodifiableList(fields));
        if (eventClass.isRecord()) {
            for (RecordComponent component : eventClass.getRecordComponents()) {
                fields.add(component.getName() + ": " + component.getType().getSimpleName());
            }
            return entry;
        }

        for (Method method : eventClass.getDeclaredMethods()) {
            if (!method.getName().startsWith("get") || method.getParameterCount() != 0) {
                continue;
            }
            String fieldName = Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4);
            fields.add(fieldName + ": " + method.getReturnType().getSimpleName());
        }

        return entry;
    }

    private static String lastPackageSegment(Class<?> type) {
        String packageName = type.getPackageName();
        int lastDot = packageName.lastIndexOf('.');
        return lastDot < 0 ? packageName : packageName.substring(lastDot + 1);
    }
}
