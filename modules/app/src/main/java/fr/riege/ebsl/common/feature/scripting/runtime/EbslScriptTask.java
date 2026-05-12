package fr.riege.ebsl.common.feature.scripting.runtime;

import fr.riege.ebsl.common.core.settings.BooleanSetting;
import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.core.settings.Settingable;
import fr.riege.ebsl.common.feature.scripting.manager.EbslScriptManager;
import fr.riege.ebsl.common.feature.scripting.parser.EbslProgram;
import fr.riege.ebsl.common.feature.task.BotTask;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.service.EbslServices;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public final class EbslScriptTask extends Settingable implements BotTask {
    public static final EbslScriptTask INSTANCE = new EbslScriptTask();

    private static final String INLINE_LABEL = "inline";

    private final BooleanSetting enabled = registerSetting(new BooleanSetting("enabled", "Enabled", false));
    private final BooleanSetting restartOnChange = registerSetting(new BooleanSetting("restart_on_change", "Restart on change", true));
    private final StringSetting scriptFile = registerSetting(new StringSetting("script_file", "Script file", EbslScriptManager.DEFAULT_FILE));
    private final StringSetting inlineScript = registerSetting(new StringSetting("inline_script", "Inline script", EbslScriptManager.DEFAULT_SOURCE));

    private final List<ScriptRun> runs = new ArrayList<>();
    private int nextRunId = 1;
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
            stopAll();
            return;
        }
        String lastFinished = null;
        Iterator<ScriptRun> iterator = runs.iterator();
        while (iterator.hasNext()) {
            ScriptRun run = iterator.next();
            run.tick(platform, restartOnChange.value());
            if (run.done()) {
                lastFinished = run.summary();
                iterator.remove();
            }
        }
        if (runs.isEmpty()) {
            setEnabled(false);
            status = lastFinished == null ? "idle" : lastFinished;
            return;
        }
        status = activeStatus();
    }

    @Override
    public void onDisable() {
        stopAll();
    }

    public String runInline(String source) {
        inlineScript.setValue(source == null ? "" : source);
        scriptFile.setValue("");
        ScriptRun run = ScriptRun.inline(handle(INLINE_LABEL), inlineScript.value());
        runs.add(run);
        setEnabled(true);
        run.start(EbslServices.platform());
        status = "running " + run.handle();
        return run.handle();
    }

    public String runFile(String file) {
        loadFile(file);
        String normalized = scriptFile.value();
        ScriptRun run = ScriptRun.file(handle(EbslScriptManager.stripExtension(normalized)), normalized);
        runs.add(run);
        setEnabled(true);
        run.start(EbslServices.platform());
        status = "running " + run.handle();
        return run.handle();
    }

    public void loadFile(String file) {
        scriptFile.setValue(EbslScriptManager.normalizeFileName(file));
        status = "loaded " + scriptFile.value();
    }

    public int stop(String selector) {
        String normalized = normalizeSelector(selector);
        if (normalized.isBlank() || "all".equals(normalized)) {
            return stopAll();
        }
        int stopped = 0;
        Iterator<ScriptRun> iterator = runs.iterator();
        while (iterator.hasNext()) {
            ScriptRun run = iterator.next();
            if (run.matches(normalized)) {
                run.stop();
                iterator.remove();
                stopped++;
            }
        }
        if (runs.isEmpty()) {
            setEnabled(false);
        }
        status = stopped == 0 ? "no script matched " + selector : "stopped " + stopped + " script(s)";
        return stopped;
    }

    public int stopAll() {
        int stopped = runs.size();
        for (ScriptRun run : runs) {
            run.stop();
        }
        runs.clear();
        setEnabled(false);
        status = stopped == 0 ? "idle" : "stopped " + stopped + " script(s)";
        return stopped;
    }

    public String status() {
        return status;
    }

    public List<String> activeLines() {
        if (runs.isEmpty()) {
            return List.of("no active scripts");
        }
        return runs.stream().map(ScriptRun::summary).toList();
    }

    public List<String> activeFiles() {
        return runs.stream()
            .map(ScriptRun::fileName)
            .filter(file -> !file.isBlank())
            .map(EbslScriptManager::normalizeFileName)
            .distinct()
            .toList();
    }

    public List<String> activeSelectors() {
        List<String> selectors = new ArrayList<>();
        selectors.add("all");
        for (ScriptRun run : runs) {
            selectors.add(run.handle());
            selectors.add(run.label());
        }
        return selectors.stream().distinct().toList();
    }

    private String activeStatus() {
        return runs.size() == 1 ? runs.getFirst().summary() : runs.size() + " scripts running";
    }

    private String handle(String label) {
        return label + "#" + nextRunId++;
    }

    private static String normalizeSelector(String selector) {
        String normalized = selector == null ? "" : selector.trim().toLowerCase(Locale.ROOT).replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        if (slash >= 0) {
            normalized = normalized.substring(slash + 1);
        }
        return normalized;
    }

    private static final class ScriptRun {
        private final String handle;
        private final String label;
        private final String fileName;
        private final String inlineSource;
        private EbslProgram program;
        private EbslRunner runner;
        private String loadedSource = "";
        private String status = "queued";
        private boolean terminal;

        private ScriptRun(String handle, String label, String fileName, String inlineSource) {
            this.handle = handle;
            this.label = label;
            this.fileName = fileName;
            this.inlineSource = inlineSource;
        }

        static ScriptRun file(String handle, String fileName) {
            return new ScriptRun(handle, EbslScriptManager.stripExtension(fileName), fileName, null);
        }

        static ScriptRun inline(String handle, String source) {
            return new ScriptRun(handle, INLINE_LABEL, "", source);
        }

        void tick(EbslPlatform platform, boolean restartOnChange) {
            String source = source(platform);
            if (program == null || !source.equals(loadedSource)) {
                compile(source);
                if (restartOnChange) {
                    start(platform);
                }
            }
            if (runner == null) {
                start(platform);
            }
            if (runner == null) {
                return;
            }
            runner.tick();
            status = runner.status();
        }

        void start(EbslPlatform platform) {
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

        void stop() {
            if (runner != null) {
                runner.stop();
                runner = null;
            }
            status = "stopped";
        }

        boolean done() {
            return terminal || (runner != null && runner.done());
        }

        boolean matches(String selector) {
            return handle.equalsIgnoreCase(selector)
                || label.equalsIgnoreCase(selector)
                || fileName.equalsIgnoreCase(selector)
                || EbslScriptManager.normalizeFileName(selector).equalsIgnoreCase(fileName);
        }

        String handle() {
            return handle;
        }

        String label() {
            return label;
        }

        String fileName() {
            return fileName;
        }

        String summary() {
            return handle + " [" + label + "] " + status;
        }

        private void compile(String source) {
            try {
                loadedSource = source;
                program = EbslScriptEngine.compile(source);
                runner = null;
                terminal = false;
                status = "compiled";
            } catch (RuntimeException exception) {
                program = null;
                runner = null;
                terminal = true;
                status = "compile error: " + exception.getMessage();
            }
        }

        private String source(EbslPlatform platform) {
            if (inlineSource != null) {
                return inlineSource;
            }
            return platform.storage().loadText(EbslScriptManager.path(fileName)).orElse(EbslScriptManager.DEFAULT_SOURCE);
        }
    }
}
