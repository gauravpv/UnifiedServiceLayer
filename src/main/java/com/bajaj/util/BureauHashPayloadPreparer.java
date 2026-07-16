package com.bajaj.util;

import com.bajaj.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class BureauHashPayloadPreparer {

    private static final Set<String> DEMOGRAPHIC_EXCLUDED = Set.of(
            "caseId",
            "product",
            "loanapplication",
            "ids",
            "rashaan_card",
            "sourcing_branch",
            "sourcing_channel",
            "loan_type",
            "requested_loan_amount",
            "program_type",
            "constitution",
            "customer_type",
            "employment_type",
            "nature_of_business"
    );

    private static final Set<String> ADDRESS_EXCLUDED = Set.of(
            "address1",
            "address2",
            "address3"
    );

    public JsonNode prepare(JsonNode data) {
        if (data == null || data.isNull()) {
            throw new BadRequestException("Bureau data block is required for cache hash");
        }
        if (!data.hasNonNull("demographic") || !data.get("demographic").isObject()) {
            throw new BadRequestException("Bureau data.demographic is required for cache hash");
        }

        ObjectNode copy = data.deepCopy();
        ObjectNode demographic = (ObjectNode) copy.get("demographic");
        DEMOGRAPHIC_EXCLUDED.forEach(demographic::remove);

        JsonNode addressNode = demographic.get("residential_address");
        if (addressNode != null && addressNode.isObject()) {
            ObjectNode address = (ObjectNode) addressNode;
            ADDRESS_EXCLUDED.forEach(address::remove);
            if (address.isEmpty()) {
                demographic.remove("residential_address");
            }
        }

        return copy;
    }
}
