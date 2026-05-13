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

package fr.riege.ebsl.common.feature.scripting;

import fr.riege.ebsl.common.feature.scripting.runtime.EbslRunner;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptRuntime;

import java.util.List;

public record EbslNodeInvocation(List<String> args, EbslScriptRuntime runtime, EbslRunner runner) {
    public EbslNodeInvocation {
        args = List.copyOf(args);
    }

    public boolean has(int index) {
        return index >= 0 && index < args.size();
    }

    public String arg(int index) {
        return args.get(index);
    }
}
