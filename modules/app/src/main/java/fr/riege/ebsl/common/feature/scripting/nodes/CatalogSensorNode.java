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
package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.registry.EbslSensorRegistry;

public final class CatalogSensorNode extends AbstractEbslNode {
    private final EbslSensorRegistry.SensorDefinition definition;

    public CatalogSensorNode(EbslSensorRegistry.SensorDefinition definition) {
        super(definition.id());
        this.definition = definition;
    }

    @Override
    protected void registerSettings() {
        registerSetting(new StringSetting("output", "Output", "result"));
        for (EbslSensorRegistry.SensorParameter parameter : definition.parameters()) {
            registerSetting(new StringSetting(parameter.id(), parameter.label(), parameter.defaultValue()));
        }
    }

    @Override
    @SuppressWarnings("java:S3516")
    public int start(EbslNodeInvocation invocation) {
        if (invocation.args().isEmpty()) {
            return 0;
        }
        invocation.runtime().setVariable(invocation.arg(0), invocation.runtime().sensor(id(), invocation.args().subList(1, invocation.args().size())));
        return 0;
    }
}
