package fr.riege.ebsl.common.feature.scripting.runtime;

import fr.riege.ebsl.common.feature.scripting.parser.EbslProgram;
import fr.riege.ebsl.common.core.settings.BooleanSetting;
import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.core.settings.Settingable;
import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.task.BotTask;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.service.EbslServices;

import java.util.List;

public final class EbslScriptTask extends Settingable implements BotTask {
    public static final EbslScriptTask INSTANCE = new EbslScriptTask();

    private static final String DEFAULT_SCRIPT = """
        # EBSL example
        start
        message "EBSL ready"
        """;

    private final BooleanSetting enabled = registerSetting(new BooleanSetting("enabled", "Enabled", false));
    private final BooleanSetting restartOnChange = registerSetting(new BooleanSetting("restart_on_change", "Restart on change", true));
    private final StringSetting scriptFile = registerSetting(new StringSetting("script_file", "Script file", "main.ebsl"));
    private final StringSetting inlineScript = registerSetting(new StringSetting("inline_script", "Inline script", DEFAULT_SCRIPT));

    private EbslProgram program;
    private EbslRunner runner;
    private String loadedSource = "";
    private String status = "idle";

    private EbslScriptTask() {
    }

    @Override public String id() { return "ebsl_script"; }
    @Override public String displayName() { return "EBSL Script"; }
    @Override public String description() { return "Runs .ebsl task scripts inspired by Pathmind node workflows."; }
    @Override public boolean isEnabled() { return enabled.value(); }
    @Override public void setEnabled(boolean enabled) { this.enabled.setValue(enabled); }

    @Override
    public void tick(EbslPlatform platform) {
        if (!platform.player().isAlive()) {
            stopRunner();
            return;
        }
        String source = source(platform);
        if (program == null || !source.equals(loadedSource)) {
            compile(source);
            if (restartOnChange.value()) {
                start(platform);
            }
        }
        if (runner == null) {
            start(platform);
        }
        runner.tick();
        status = runner.status();
        if (runner.done()) {
            setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
        stopRunner();
    }

    public void runInline(String source) {
        inlineScript.setValue(source == null ? "" : source);
        scriptFile.setValue("");
        setEnabled(true);
        compile(inlineScript.value());
        start(EbslServices.platform());
    }

    public void runFile(String file) {
        scriptFile.setValue(file == null ? "" : file);
        setEnabled(true);
        program = null;
        start(EbslServices.platform());
    }

    public String status() {
        return status;
    }

    private void compile(String source) {
        try {
            loadedSource = source;
            program = EbslScriptEngine.compile(source);
            runner = null;
            status = "compiled";
        } catch (RuntimeException exception) {
            status = "compile error: " + exception.getMessage();
            setEnabled(false);
        }
    }

    private void start(EbslPlatform platform) {
        if (program == null) {
            compile(source(platform));
        }
        if (program == null) {
            return;
        }
        runner = EbslScriptEngine.runner(program, platform);
        runner.start();
        status = "running";
    }

    private void stopRunner() {
        if (runner != null) {
            runner.stop();
            runner = null;
        }
        status = "stopped";
    }

    private String source(EbslPlatform platform) {
        String file = scriptFile.value().trim();
        if (!file.isEmpty()) {
            String normalized = file.endsWith(".ebsl") ? file : file + ".ebsl";
            return platform.storage().loadText("scripts/" + normalized).orElse(inlineScript.value());
        }
        return inlineScript.value();
    }
}
