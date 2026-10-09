package com.epam.resourceservice.service;

import com.epam.resourceservice.exception.InvalidResourceIdException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class IdParser {

    private static final int MAX_CSV_LENGTH = 200;

    public List<Long> parse(String csv) {
        if (csv == null || csv.isBlank()) {
            throw new InvalidResourceIdException("ID list must not be empty");
        }

        if (csv.length() > MAX_CSV_LENGTH) {
            throw new InvalidResourceIdException(
                    String.format("CSV string is too long: received %s characters, maximum allowed is 200", csv.length())
            );
        }

        String[] tokens = csv.split(",", -1);
        List<Long> ids = new ArrayList<>(tokens.length);

        for (String token : tokens) {
            String value = token.trim();

            if (!value.matches("\\d+")) {
                throw new InvalidResourceIdException(
                        String.format("Invalid ID format: '%s'. Only positive integers are allowed", value)
                );
            }

            try {
                long id = Long.parseLong(value);

                if (id <= 0) {
                    throw new InvalidResourceIdException(
                            String.format("Invalid ID format: '%s'. Only positive integers are allowed", value)
                    );
                }

                ids.add(id);
            } catch (NumberFormatException exception) {
                throw new InvalidResourceIdException(
                        String.format("Invalid ID format: '%s'. Only positive integers are allowed", value)
                );
            }
        }

        return ids.stream().distinct().toList();
    }
}