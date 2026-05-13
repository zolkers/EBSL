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
