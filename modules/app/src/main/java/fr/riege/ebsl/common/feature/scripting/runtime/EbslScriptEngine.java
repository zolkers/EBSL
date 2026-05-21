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

import fr.riege.ebsl.common.feature.scripting.flow.EbslFlowLanguage;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphDocument;
import fr.riege.ebsl.common.feature.scripting.parser.EbslParser;
import fr.riege.ebsl.common.feature.scripting.parser.EbslProgram;
import fr.riege.ebsl.common.feature.scripting.parser.EbslTokenizer;
import fr.riege.ebsl.common.platform.EbslPlatform;

public final class EbslScriptEngine {
    private EbslScriptEngine() {
    }

    public static EbslProgram compile(String source) {
        return new EbslParser(EbslTokenizer.tokenize(source)).parse();
    }

    public static EbslRunner runner(EbslProgram program, EbslPlatform platform) {
        return new EbslRunner(program, platform);
    }

    public static EbslGraphRunner graphRunner(EbslGraphDocument document, EbslPlatform platform) {
        return new EbslGraphRunner(document, platform);
    }

    public static EbslGraphRunner flowRunner(String source, EbslPlatform platform) {
        return graphRunner(EbslFlowLanguage.parse(source), platform);
    }
}
