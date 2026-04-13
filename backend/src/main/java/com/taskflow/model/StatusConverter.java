package com.taskflow.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusConverter implements AttributeConverter<Task.Status, String> {

    @Override
    public String convertToDatabaseColumn(Task.Status status) {
        if (status == null) {
            return null;
        }
        return status.toDbValue();
    }

    @Override
    public Task.Status convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return Task.Status.fromString(dbData);
    }
}
