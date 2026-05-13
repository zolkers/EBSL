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

package fr.riege.ebsl.common.feature.scripting.runtime;

/**
 * Represents an executable statement in the script runtime.
 *
 * <p>Statements advance by ticks and report the next execution step to the runner, allowing waits, branches, and commands to share one scheduler.</p>
 */
public interface EbslStatement {
    /**
     * Advances this component by one runtime tick.
 *
     * @param runtime the active script runtime
     * @param runner the runner coordinating statement execution
     * @return the value defined by this contract
     */
    EbslStep tick(EbslScriptRuntime runtime, EbslRunner runner);
}
