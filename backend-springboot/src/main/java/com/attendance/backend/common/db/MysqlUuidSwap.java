package com.attendance.backend.common.db;

import java.nio.ByteBuffer;
import java.util.UUID;

public final class MysqlUuidSwap {

    private MysqlUuidSwap() {}
    public static byte[] toBytes(UUID uuid) {
        if (uuid == null) return null;

        byte[] b = new byte[16];
        ByteBuffer.wrap(b)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits());

        byte[] out = new byte[16];
        out[0] = b[6]; out[1] = b[7];
        out[2] = b[4]; out[3] = b[5];
        out[4] = b[0]; out[5] = b[1]; out[6] = b[2]; out[7] = b[3];
        System.arraycopy(b, 8, out, 8, 8);
        return out;
    }

    public static UUID toUuid(byte[] dbData) {
        if (dbData == null) return null;
        if (dbData.length != 16) throw new IllegalArgumentException("UUID binary must be 16 bytes");

        byte[] tmp = new byte[16];
        tmp[0] = dbData[4]; tmp[1] = dbData[5]; tmp[2] = dbData[6]; tmp[3] = dbData[7];
        tmp[4] = dbData[2]; tmp[5] = dbData[3];
        tmp[6] = dbData[0]; tmp[7] = dbData[1];
        System.arraycopy(dbData, 8, tmp, 8, 8);

        ByteBuffer bb = ByteBuffer.wrap(tmp);
        return new UUID(bb.getLong(), bb.getLong());
    }
}