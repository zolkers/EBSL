package fr.riege.ebsl.api.tasks;

import fr.riege.ebsl.api.annotation.EbslApiOperation;
import fr.riege.ebsl.api.annotation.EbslApiSurface;
import fr.riege.ebsl.general.registry.BotTaskRegistry;
import fr.riege.ebsl.general.storage.BotTaskSettingsStore;
import fr.riege.ebsl.general.task.BotTask;
import fr.riege.ebsl.settings.Setting;

import java.util.Collection;

@EbslApiSurface(EbslApiSurface.Domain.TASKS)
public final class TasksApi {
    public TasksApi() {
    }

    @EbslApiOperation("Read all registered bot tasks.")
    public Collection<BotTask> all() {
        return BotTaskRegistry.tasks();
    }

    @EbslApiOperation("Find a bot task by id.")
    public BotTask get(String id) {
        return BotTaskRegistry.get(id);
    }

    @EbslApiOperation("Persist task settings.")
    public void saveSettings() {
        BotTaskSettingsStore.save();
    }

    @EbslApiOperation("Notify task lifecycle after a setting change.")
    public void notifySettingChanged(BotTask task, Setting<?> setting) {
        BotTaskRegistry.onSettingChanged(task, setting);
    }

    @EbslApiOperation("Reset a task and notify lifecycle for every setting.")
    public void resetToDefaultsAndSave(BotTask task) {
        task.resetSettings();
        saveSettings();
        for (Setting<?> setting : task.settings()) {
            notifySettingChanged(task, setting);
        }
    }
}
