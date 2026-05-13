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
package fr.riege.ebsl.common.feature.scripting.blocks;

/**
 * Parses one block-style statement in the EBSL scripting language.
 *
 * <p>Handlers convert source-level block invocations into structured parser results while keeping grammar extensions isolated.</p>
 */
@FunctionalInterface
public interface EbslBlockStatementHandler {
    /**
     * Parses the supplied block statement invocation.
 *
     * @param invocation the invocation state for the current script or parser operation
     * @return the value defined by this contract
     */
    EbslBlockStatementResult parse(EbslBlockStatementInvocation invocation);
}
