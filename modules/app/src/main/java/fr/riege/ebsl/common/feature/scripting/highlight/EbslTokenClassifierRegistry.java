package fr.riege.ebsl.common.feature.scripting.highlight;

import fr.riege.ebsl.common.feature.scripting.blocks.EbslBlockStatementType;
import fr.riege.ebsl.common.feature.scripting.conditions.EbslConditionOperatorType;
import fr.riege.ebsl.common.feature.scripting.parser.EbslSyntax;
import fr.riege.ebsl.common.feature.scripting.registry.EbslNodeRegistry;
import fr.riege.ebsl.common.feature.scripting.registry.EbslSensorRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class EbslTokenClassifierRegistry {
    private static final Pattern NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final Pattern DURATION = Pattern.compile("-?\\d+(\\.\\d+)?(ms|t|s|m)");
    private static final List<EbslTokenClassifier> CLASSIFIERS = new ArrayList<>();

    static {
        register((token, firstToken) -> token.startsWith(EbslSyntax.VARIABLE_PREFIX) ? EbslTokenKind.VARIABLE : null);
        register((token, firstToken) -> DURATION.matcher(token).matches() ? EbslTokenKind.DURATION : null);
        register((token, firstToken) -> NUMBER.matcher(token).matches() ? EbslTokenKind.NUMBER : null);
        register((token, firstToken) -> isBlockToken(token) ? EbslTokenKind.BLOCK : null);
        register((token, firstToken) -> isOperator(token) ? EbslTokenKind.OPERATOR : null);
        register((token, firstToken) -> isControl(token) ? EbslTokenKind.CONTROL : null);
        register((token, firstToken) -> isSensor(token) ? EbslTokenKind.SENSOR : null);
        register((token, firstToken) -> firstToken && EbslNodeRegistry.get(token) != null ? EbslTokenKind.COMMAND : null);
    }

    private EbslTokenClassifierRegistry() {
    }

    public static void register(EbslTokenClassifier classifier) {
        CLASSIFIERS.add(classifier);
    }

    public static EbslTokenKind classify(String token, boolean firstToken) {
        for (EbslTokenClassifier classifier : CLASSIFIERS) {
            EbslTokenKind kind = classifier.classify(token, firstToken);
            if (kind != null) {
                return kind;
            }
        }
        return EbslTokenKind.IDENTIFIER;
    }

    private static boolean isBlockToken(String token) {
        return EbslSyntax.BLOCK_OPEN.equals(token) || EbslSyntax.BLOCK_CLOSE.equals(token);
    }

    private static boolean isSensor(String token) {
        return EbslSensorRegistry.definition(normalize(token)) != null;
    }

    private static boolean isControl(String token) {
        String normalized = normalize(token);
        for (EbslBlockStatementType type : EbslBlockStatementType.values()) {
            if (type.id().equals(normalized) || type.aliases().contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOperator(String token) {
        String normalized = normalize(token);
        for (EbslConditionOperatorType type : EbslConditionOperatorType.values()) {
            if (type.id().equals(normalized) || type.aliases().contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String token) {
        return token == null ? "" : token.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
