package fr.riege.ebsl.common.feature.scripting.highlight;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EbslSyntaxHighlighterTest {
    @Test
    void classifiesScriptTokens() {
        List<List<EbslSyntaxToken>> lines = EbslSyntaxHighlighter.highlight("""
            forever {
              repeat_until sensor_targeted_block leaf {
                break_block leaf
                wait 2s # cool
              }
            }
            """);

        assertToken(lines.get(0), "forever", EbslTokenKind.CONTROL);
        assertToken(lines.get(0), "{", EbslTokenKind.BLOCK);
        assertToken(lines.get(1), "repeat_until", EbslTokenKind.CONTROL);
        assertToken(lines.get(1), "sensor_targeted_block", EbslTokenKind.SENSOR);
        assertToken(lines.get(2), "break_block", EbslTokenKind.COMMAND);
        assertToken(lines.get(3), "2s", EbslTokenKind.DURATION);
        assertTrue(lines.get(3).stream().anyMatch(token -> token.kind() == EbslTokenKind.COMMENT));
    }

    private static void assertToken(List<EbslSyntaxToken> tokens, String text, EbslTokenKind kind) {
        EbslSyntaxToken token = tokens.stream()
            .filter(candidate -> candidate.text().equals(text))
            .findFirst()
            .orElseThrow();
        assertEquals(kind, token.kind());
    }
}
