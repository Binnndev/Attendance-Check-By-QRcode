package com.attendance.backend.common.persistence;

import com.attendance.backend.common.db.MysqlUuidSwap;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;
import java.util.UUID;

public class MysqlUuidBinary16SwapType implements UserType<UUID> {

    private static final long serialVersionUID = 1L;

    @Override
    public int getSqlType() {
        return Types.BINARY;
    }

    @Override
    public Class<UUID> returnedClass() {
        return UUID.class;
    }

    @Override
    public boolean equals(UUID x, UUID y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(UUID x) {
        return Objects.hashCode(x);
    }

    @Override
    public UUID nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        byte[] bytes = rs.getBytes(position);
        return bytes == null ? null : MysqlUuidSwap.toUuid(bytes);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, UUID value, int index, SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.BINARY);
        } else {
            st.setBytes(index, MysqlUuidSwap.toBytes(value));
        }
    }

    @Override
    public UUID deepCopy(UUID value) {
        return value; // immutable
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(UUID value) {
        return value;
    }

    @Override
    public UUID assemble(Serializable cached, Object owner) {
        return (UUID) cached;
    }

    @Override
    public UUID replace(UUID detached, UUID managed, Object owner) {
        return detached;
    }
}