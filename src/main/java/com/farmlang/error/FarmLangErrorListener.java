package com.farmlang.error;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Error listener customizado para o compilador FarmLang.
 * Captura e formata erros léxicos e sintáticos com suporte a localização em Português.
 */
public class FarmLangErrorListener extends BaseErrorListener {

    private final List<String> errors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        if (recognizer instanceof Lexer) {
            String formattedMsg;
            if (msg == null || msg.isEmpty()) {
                formattedMsg = "Caractere invalido ''";
            } else {
                String charStr = msg;
                if (msg.startsWith("token recognition error at: ")) {
                    charStr = msg.substring("token recognition error at: ".length()).trim();
                }
                if (charStr.startsWith("Caractere invalido")) {
                    formattedMsg = charStr;
                } else if (charStr.length() >= 2 && charStr.startsWith("'") && charStr.endsWith("'")) {
                    formattedMsg = "Caractere invalido " + charStr;
                } else {
                    formattedMsg = "Caractere invalido '" + charStr + "'";
                }
            }
            errors.add("[ERRO LEXICO] Linha " + line + ": " + formattedMsg);
        } else {
            String description = (msg != null) ? msg : "";
            errors.add("[ERRO SINTATICO] Linha " + line + ": " + description);
        }
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void reportAndExit() {
        reportAndExit(System::exit);
    }

    public void reportAndExit(Consumer<Integer> exitHandler) {
        if (hasErrors()) {
            for (String error : errors) {
                System.err.println(error);
            }
            if (exitHandler != null) {
                exitHandler.accept(1);
            }
        }
    }
}
