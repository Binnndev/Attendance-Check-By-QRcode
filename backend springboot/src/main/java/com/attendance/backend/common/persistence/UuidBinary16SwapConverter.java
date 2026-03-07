package com.attendance.backend.common.persistence;

import com.attendance.backend.common.db.MysqlUuidSwap;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

@Converter(autoApply = false)
public class UuidBinary16SwapConverter implements AttributeConverter<UUID, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(UUID attribute) {
        return attribute == null ? null : MysqlUuidSwap.toBytes(attribute);
    }

    @Override
    public UUID convertToEntityAttribute(byte[] dbData) {
        return dbData == null ? null : MysqlUuidSwap.toUuid(dbData);
    }
}