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

import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.registry.EbslNodeRegistry;

import java.util.Collection;

public final class ScriptingNodeCatalog {
    ScriptingNodeCatalog() {
    }

    public EbslNode get(String id) {
        return EbslNodeRegistry.get(id);
    }

    public EbslNode create(String id) {
        return EbslNodeRegistry.create(id);
    }

    public Collection<EbslNode> nodes() {
        return EbslNodeRegistry.nodes();
    }

    public Collection<EbslNode> canonicalNodes() {
        return EbslNodeRegistry.canonicalNodes();
    }

    public Collection<EbslNode> canonical() {
        return canonicalNodes();
    }
}
