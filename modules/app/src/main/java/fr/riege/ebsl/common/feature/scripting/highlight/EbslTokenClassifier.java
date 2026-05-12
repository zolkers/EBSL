package fr.riege.ebsl.common.feature.scripting.highlight;

@FunctionalInterface
public interface EbslTokenClassifier {
    EbslTokenKind classify(String token, boolean firstToken);
}
