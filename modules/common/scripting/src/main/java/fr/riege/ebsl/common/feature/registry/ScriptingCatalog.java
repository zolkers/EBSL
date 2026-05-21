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

package fr.riege.ebsl.common.feature.registry;

public final class ScriptingCatalog {
    private final ScriptingNodeCatalog nodes = new ScriptingNodeCatalog();
    private final ScriptingSensorCatalog sensors = new ScriptingSensorCatalog();
    private final ScriptingBlockCatalog blocks = new ScriptingBlockCatalog();
    private final ScriptingConditionCatalog conditions = new ScriptingConditionCatalog();
    private final ScriptingSyntaxCatalog syntax = new ScriptingSyntaxCatalog();

    ScriptingCatalog() {
    }

    public ScriptingNodeCatalog nodes() {
        return nodes;
    }

    public ScriptingSensorCatalog sensors() {
        return sensors;
    }

    public ScriptingBlockCatalog blocks() {
        return blocks;
    }

    public ScriptingConditionCatalog conditions() {
        return conditions;
    }

    public ScriptingSyntaxCatalog syntax() {
        return syntax;
    }
}
