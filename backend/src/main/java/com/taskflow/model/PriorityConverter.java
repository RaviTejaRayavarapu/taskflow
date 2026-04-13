package com.taskflow.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PriorityConverter implements AttributeConverter<Task.Priority, String> {

    @Override
    public String convertToDatabaseColumn(Task.Priority priority) {
        if (priority == null) {
            return null;
        }
        return priority.toDbValue();
    }

    @Override
    public Task.Priority convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return Task.Priority.fromString(dbData);
    }
}
