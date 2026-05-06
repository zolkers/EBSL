package fr.riege.ebsl.common.settings;

abstract class AbstractSetting<T> extends Settingable implements Setting<T> {
    private final String id;
    private final String displayName;
    private final T defaultValue;
    private T value;

    AbstractSetting(String id, String displayName, T defaultValue) {
        this.id = id;
        this.displayName = displayName;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public T defaultValue() {
        return defaultValue;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
        onSettingChanged(this);
    }

    @Override
    public void resetToDefault() {
        setValue(defaultValue);
    }
}
