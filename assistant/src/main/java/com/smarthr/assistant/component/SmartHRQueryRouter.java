package com.smarthr.assistant.component;

import org.springframework.stereotype.Component;

@Component
public class SmartHRQueryRouter {

    public QueryType classify(String message) {

        String m = message.toLowerCase();

        if (m.matches(".*\\b(hola|buenos días|buenas|hello|gracias)\\b.*")) {
            return QueryType.SMALL_TALK;
        }

        if (m.matches(".*\\b(cuántos|cuantos|número de|total de)\\b.*")) {
            return QueryType.AGGREGATION;
        }

        return QueryType.RAG;
    }
}

