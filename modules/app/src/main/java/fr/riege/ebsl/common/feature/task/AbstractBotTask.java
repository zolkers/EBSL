package fr.riege.ebsl.common.feature.task;

import fr.riege.ebsl.common.feature.AbstractEnabledFeature;

public abstract class AbstractBotTask extends AbstractEnabledFeature implements BotTask {
    protected AbstractBotTask(String id, String displayName, String description) {
        super(id, displayName, description);
    }
}
