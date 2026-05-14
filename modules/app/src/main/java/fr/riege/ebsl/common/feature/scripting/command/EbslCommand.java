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

package fr.riege.ebsl.common.feature.scripting.command;

import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeCategory;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.manager.EbslNodeTemplate;
import fr.riege.ebsl.common.feature.registry.FeatureRegistries;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptTask;
import fr.riege.ebsl.common.feature.terminal.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class EbslCommand {
    static final String USAGE = "ebsl <run|inline|stop|status|tasks> [args]";
    private static final List<EbslNodeType> SCRIPT_BLOCKS = List.of(
        EbslNodeType.EVENT_FUNCTION,
        EbslNodeType.CONTROL_IF,
        EbslNodeType.CONTROL_IF_ELSE,
        EbslNodeType.CONTROL_REPEAT,
        EbslNodeType.CONTROL_REPEAT_UNTIL,
        EbslNodeType.CONTROL_FOREVER
    );

    private EbslCommand() {
    }

    public static CommandSpec spec() {
        return CommandSpec.named("ebsl")
            .description("Run and inspect EBSL scripts")
            .usage(USAGE)
            .bothScopes()
            .argument(CommandArgument.dynamic("action", EbslCommandAction::ids))
            .completion(CommandCompletion.builder()
                .dynamic(EbslCommand::suggest)
                .dynamic(EbslCommand::suggest)
                .build())
            .executes(EbslCommand::execute)
            .build();
    }

    private static CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() == 0) {
            return CommandResult.ok("Usage: " + USAGE);
        }
        return EbslCommandActionRegistry.execute(ctx.arg(0), ctx);
    }

    static CommandResult runFile(CommandContext ctx) {
        if (ctx.argCount() != 2) {
            return CommandResult.badUsage("ebsl run <file>");
        }
        String handle = EbslScriptTask.INSTANCE.runFile(ctx.arg(1));
        return CommandResult.ok("Running EBSL file " + ctx.arg(1) + " as " + handle);
    }

    static CommandResult runInline(CommandContext ctx) {
        if (ctx.argCount() < 2) {
            return CommandResult.badUsage("ebsl inline <script tokens>");
        }
        List<String> parts = new ArrayList<>();
        for (int i = 1; i < ctx.argCount(); i++) {
            parts.add(ctx.arg(i));
        }
        String handle = EbslScriptTask.INSTANCE.runInline(String.join(" ", parts));
        return CommandResult.ok("Running inline EBSL as " + handle);
    }

    static CommandResult stop(CommandContext ctx) {
        String selector = ctx.argCount() >= 2 ? ctx.arg(1) : "all";
        int stopped = EbslScriptTask.INSTANCE.stop(selector);
        if (stopped == 0) {
            return CommandResult.ok("No EBSL script matched " + selector + ".");
        }
        return CommandResult.ok("Stopped " + stopped + " EBSL script(s).");
    }

    @SuppressWarnings("java:S1172")
    static CommandResult status(CommandContext ctx) {
        List<String> lines = new ArrayList<>();
        lines.add("EBSL: " + EbslScriptTask.INSTANCE.status());
        lines.addAll(EbslScriptTask.INSTANCE.activeLines());
        return CommandResult.ok(lines);
    }

    static List<String> taskLines() {
        List<String> lines = new ArrayList<>();
        lines.add("EBSL executable nodes: " + FeatureRegistries.scripting().nodes().canonical().size());
        for (EbslNodeCategory category : EbslNodeCategory.values()) {
            boolean header = appendRegisteredNodes(lines, category, false);
            appendScriptBlocks(lines, category, header);
        }
        return lines;
    }

    private static boolean appendRegisteredNodes(List<String> lines, EbslNodeCategory category, boolean header) {
        boolean hasHeader = header;
        for (EbslNode node : FeatureRegistries.scripting().nodes().canonical()) {
            hasHeader = appendTaskLine(lines, category, EbslNodeTemplate.of(node), hasHeader);
        }
        return hasHeader;
    }

    private static void appendScriptBlocks(List<String> lines, EbslNodeCategory category, boolean header) {
        boolean hasHeader = header;
        for (EbslNodeType block : SCRIPT_BLOCKS) {
            hasHeader = appendTaskLine(lines, category, EbslNodeTemplate.of(block), hasHeader);
        }
    }

    private static boolean appendTaskLine(List<String> lines, EbslNodeCategory category, EbslNodeTemplate template, boolean header) {
        if (template.category() != category) {
            return header;
        }
        if (!header) {
            lines.add("[" + category.id() + "]");
        }
        lines.add("  " + template.command());
        return true;
    }

    private static List<String> suggest(CommandCompletion.Context context) {
        if (context.argIndex() == 0) {
            return filter(EbslCommandAction.ids(), context.partial());
        }
        if ("tasks".equalsIgnoreCase(context.previousArg(0))) {
            return filter(taskIds(), context.partial());
        }
        if ("stop".equalsIgnoreCase(context.previousArg(0))) {
            return filter(EbslScriptTask.INSTANCE.activeSelectors(), context.partial());
        }
        return List.of();
    }

    private static List<String> taskIds() {
        List<String> ids = new ArrayList<>();
        for (EbslNode node : FeatureRegistries.scripting().nodes().canonical()) {
            ids.add(node.id());
        }
        for (EbslNodeType block : SCRIPT_BLOCKS) {
            ids.add(block.id());
        }
        return List.copyOf(ids);
    }

    private static List<String> filter(List<String> values, String partial) {
        String query = partial == null ? "" : partial.toLowerCase(Locale.ROOT);
        return values.stream()
            .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(query))
            .toList();
    }
}
