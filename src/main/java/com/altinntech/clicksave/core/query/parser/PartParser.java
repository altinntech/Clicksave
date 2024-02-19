package com.altinntech.clicksave.core.query.parser;

import com.altinntech.clicksave.core.query.builder.QueryType;
import com.altinntech.clicksave.exceptions.WrongQueryMethodException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartParser {

    List<Part> parts = new ArrayList<>();
    List<CommonPart> serviceParts = new ArrayList<>();
    String fieldPartSplitPattern;

    public PartParser() {
        StringJoiner joiner = new StringJoiner("|");
        for (CommonPart part : CommonPart.values()) {
            if (part.getQualifier() == QueryType.ANY) {
                joiner.add(part.getPartName());
            }
        }
        this.fieldPartSplitPattern = "(" + joiner.toString() + ")";
    }

    public void parse(String methodName) {
        List<String> fieldParts = new ArrayList<>(Arrays.stream(methodName.split(fieldPartSplitPattern)).toList());
        fieldParts.set(0, removeServiceParts(fieldParts.getFirst()));
        serviceParts = extractServiceParts(methodName);

        for (int i = 0; i < serviceParts.size(); i++) {
            parts.add(serviceParts.get(i));
            if (i < fieldParts.size()) {
                Part part = new FieldPart(fieldParts.get(i));
                parts.add(part);
            }
        }

        if (!parts.getFirst().isServicePart()) {
            throw new WrongQueryMethodException(methodName + " is not valid query method");
        }
    }

    public List<Part> getParts() {
        return parts;
    }

    public List<CommonPart> getServiceParts() {
        return serviceParts;
    }

    private String removeServiceParts(String part) {
        for (CommonPart p : CommonPart.values()) {
            if (part.startsWith(p.getPartName())) {
                return part.replaceFirst(Pattern.quote(p.getPartName()), "");
            }
        }
        return part;
    }

    private List<CommonPart> extractServiceParts(String methodName) {
        List<CommonPart> serviceParts = new ArrayList<>();

        Pattern pattern = Pattern.compile(CommonPart.getPattern());
        Matcher matcher = pattern.matcher(methodName);

        while (matcher.find()) {
            String partValue = matcher.group();
            for (CommonPart part : CommonPart.values()) {
                if (part.getPartName().equals(partValue)) {
                    serviceParts.add(part);
                    break;
                }
            }
        }

        return serviceParts;
    }
}
