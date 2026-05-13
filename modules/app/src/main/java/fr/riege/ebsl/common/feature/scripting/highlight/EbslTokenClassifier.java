package fr.riege.ebsl.common.feature.scripting.highlight;

/**
 * Classifies lexical tokens for script editor highlighting.
 *
 * <p>Classifiers are intentionally small and side-effect free so multiple token rules can be composed in priority order.</p>
 */
@FunctionalInterface
public interface EbslTokenClassifier {
    /**
     * Classifies the supplied movement context into the movement type used by planning, quality, and execution.
 *
     * @param token the token value
     * @param firstToken the first token value
     * @return the value defined by this contract
     */
    EbslTokenKind classify(String token, boolean firstToken);
}
