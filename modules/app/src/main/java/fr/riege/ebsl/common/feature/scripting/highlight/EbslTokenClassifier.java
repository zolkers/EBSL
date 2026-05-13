package fr.riege.ebsl.common.feature.scripting.highlight;

/**
 * Defines the contract for {@code EbslTokenClassifier} implementations.
 */
@FunctionalInterface
public interface EbslTokenClassifier {
    EbslTokenKind classify(String token, boolean firstToken);
}
