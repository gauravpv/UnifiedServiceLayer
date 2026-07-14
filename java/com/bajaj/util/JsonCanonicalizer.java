package com.bajaj.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class JsonCanonicalizer {

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    public String canonicalize(Object value) {
        try {
            JsonNode tree = (value instanceof JsonNode jn) ? jn : mapper.valueToTree(value);
            return mapper.writeValueAsString(sortKeys(tree));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to canonicalize JSON", e);
        }
    }

    private JsonNode sortKeys(JsonNode node) {
        if (node == null || node.isNull()) return node;
        if (node.isObject()) {
            List<String> keys = new ArrayList<>();
            node.fieldNames().forEachRemaining(keys::add);
            Collections.sort(keys);
            ObjectNode out = mapper.createObjectNode();
            for (String k : keys) out.set(k, sortKeys(node.get(k)));
            return out;
        }
        if (node.isArray()) {
            ArrayNode out = mapper.createArrayNode();
            node.elements().forEachRemaining(e -> out.add(sortKeys(e)));
            return out;
        }
        return node;
    }
}
