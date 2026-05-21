/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.feature.scripting.runtime;

import fr.riege.ebsl.common.core.settings.BooleanSetting;
import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.scripting.manager.EbslScriptManager;
import fr.riege.ebsl.common.feature.scripting.parser.EbslProgram;
import fr.riege.ebsl.common.feature.task.AbstractBotTask;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.service.EbslServices;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("java:S6548")
public final class EbslScriptTask extends AbstractBotTask {
    public static final EbslScriptTask INSTANCE = new EbslScriptTask();

    private static final String INLINE_LABEL = "inline";

    private final BooleanSetting restartOnChange = registerSetting(
        new BooleanSetting("restart_on_change", "Restart on change", true));
    private final StringSetting scriptFile = registerSetting(new StringSetting("script_file", "Script file", EbslScriptManager.DEFAULT_FILE));
    private final StringSetting inlineScript = registerSetting(new StringSetting("inline_script", "Inline script", EbslScriptManager.DEFAULT_SOURCE));

    private final List<ScriptRun> runs = new ArrayList<>();
    private int nextRunId = 1;
    private EbslScriptTaskStatus status = EbslScriptTaskStatus.IDLE;
    private String statusDetail = "";

    private EbslScriptTask() {
        super("ebsl_script", "EBSL Script", "Runs .ebsl task scripts inspired by Pathmind node workflows.");
    }

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
            if (lastFinished == null) {
                setStatus(EbslScriptTaskStatus.IDLE);
            } else {
                setStatus(EbslScriptTaskStatus.SUMMARY, lastFinished);
            }
            return;
        }
        setStatus(EbslScriptTaskStatus.RUNNING, activeStatus());
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
        setStatus(EbslScriptTaskStatus.RUNNING, run.handle());
        return run.handle();
    }

    public String runFile(String file) {
        loadFile(file);
        String normalized = scriptFile.value();
        ScriptRun run = ScriptRun.file(handle(EbslScriptManager.stripExtension(normalized)), normalized);
        runs.add(run);
        setEnabled(true);
        run.start(EbslServices.platform());
        setStatus(EbslScriptTaskStatus.RUNNING, run.handle());
        return run.handle();
    }

    public void loadFile(String file) {
        scriptFile.setValue(EbslScriptManager.normalizeFileName(file));
        setStatus(EbslScriptTaskStatus.LOADED, scriptFile.value());
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
        if (stopped == 0) {
            setStatus(EbslScriptTaskStatus.NO_SCRIPT_MATCHED, selector);
        } else {
            setStatus(EbslScriptTaskStatus.STOPPED, stopped + " script(s)");
        }
        return stopped;
    }

    public int stopAll() {
        int stopped = runs.size();
        for (ScriptRun run : runs) {
            run.stop();
        }
        runs.clear();
        setEnabled(false);
        if (stopped == 0) {
            setStatus(EbslScriptTaskStatus.IDLE);
        } else {
            setStatus(EbslScriptTaskStatus.STOPPED, stopped + " script(s)");
        }
        return stopped;
    }

    public String status() {
        return status.message(statusDetail);
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

    private void setStatus(EbslScriptTaskStatus status) {
        setStatus(status, "");
    }

    private void setStatus(EbslScriptTaskStatus status, String detail) {
        this.status = status;
        this.statusDetail = detail == null ? "" : detail;
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
        private EbslScriptRunStatus status = EbslScriptRunStatus.QUEUED;
        private String statusDetail = "";
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
            setStatus(EbslScriptRunStatus.RUNNER_STATUS, runner.status());
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
            setStatus(EbslScriptRunStatus.RUNNING);
        }

        void stop() {
            if (runner != null) {
                runner.stop();
                runner = null;
            }
            setStatus(EbslScriptRunStatus.STOPPED);
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
            return handle + " [" + label + "] " + status.message(statusDetail);
        }

        private void compile(String source) {
            try {
                loadedSource = source;
                program = EbslScriptEngine.compile(source);
                runner = null;
                terminal = false;
                setStatus(EbslScriptRunStatus.COMPILED);
            } catch (RuntimeException exception) {
                program = null;
                runner = null;
                terminal = true;
                setStatus(EbslScriptRunStatus.COMPILE_ERROR, exception.getMessage());
            }
        }

        private void setStatus(EbslScriptRunStatus status) {
            setStatus(status, "");
        }

        private void setStatus(EbslScriptRunStatus status, String detail) {
            this.status = status;
            this.statusDetail = detail == null ? "" : detail;
        }

        private String source(EbslPlatform platform) {
            if (inlineSource != null) {
                return inlineSource;
            }
            return new EbslScriptManager(platform.storage()).executableSource(fileName);
        }
    }
}
