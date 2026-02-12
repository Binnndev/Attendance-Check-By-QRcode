package com.attendance.backend.common.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.nio.ByteBuffer;
import java.util.UUID;

@Converter
public class UuidBinary16SwapConverter implements AttributeConverter<UUID, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(UUID uuid) {
        if (uuid == null) return null;
        byte[] std = toBytes(uuid);
        return swap(std);
    }

    @Override
    public UUID convertToEntityAttribute(byte[] dbData) {
        if (dbData == null) return null;
        byte[] std = unswap(dbData);
        return fromBytes(std);
    }

    private static byte[] toBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private static UUID fromBytes(byte[] b) {
        ByteBuffer bb = ByteBuffer.wrap(b);
        long msb = bb.getLong();
        long lsb = bb.getLong();
        return new UUID(msb, lsb);
    }

    private static byte[] swap(byte[] std) {
        byte[] s = new byte[16];
        s[0]=std[6]; s[1]=std[7];
        s[2]=std[4]; s[3]=std[5];
        s[4]=std[0]; s[5]=std[1]; s[6]=std[2]; s[7]=std[3];
        System.arraycopy(std, 8, s, 8, 8);
        return s;
    }

    private static byte[] unswap(byte[] s) {
        byte[] std = new byte[16];
        std[0]=s[4]; std[1]=s[5]; std[2]=s[6]; std[3]=s[7];
        std[4]=s[2]; std[5]=s[3];
        std[6]=s[0]; std[7]=s[1];
        System.arraycopy(s, 8, std, 8, 8);
        return std;
    }
}
