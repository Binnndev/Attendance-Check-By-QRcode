package com.attendance.backend.stats.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class AttendanceStatsQueryRepositoryImpl implements AttendanceStatsQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AttendanceStatsQueryRepositoryImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public StudentSummaryRow findStudentSummary(UUID userId) {
        String sql = """
                select
                    count(*) as total_sessions,
                    coalesce(sum(case when sa.attendance_status = 'PRESENT' then 1 else 0 end), 0) as present_count,
                    coalesce(sum(case when sa.attendance_status = 'LATE' then 1 else 0 end), 0) as late_count,
                    coalesce(sum(case when sa.attendance_status = 'ABSENT' then 1 else 0 end), 0) as absent_count,
                    coalesce(sum(case when sa.attendance_status = 'EXCUSED' then 1 else 0 end), 0) as excused_count
                from session_attendance sa
                join attendance_sessions s
                  on s.id = sa.session_id
                where sa.user_id = UUID_TO_BIN(:userId, 1)
                  and s.status = 'CLOSED'
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId.toString());

        return jdbc.queryForObject(sql, params, studentSummaryRowMapper());
    }

    @Override
    public Page<GroupSummaryRow> findGroupSummaryPage(UUID groupId, int page, int size) {
        String dataSql = """
                select
                    bin_to_uuid(gm.user_id, 1) as user_id,
                    u.full_name as full_name,
                    u.email as email,
                    coalesce(sumx.total_sessions, 0) as total_sessions,
                    coalesce(sumx.present_count, 0) as present_count,
                    coalesce(sumx.late_count, 0) as late_count,
                    coalesce(sumx.absent_count, 0) as absent_count,
                    coalesce(sumx.excused_count, 0) as excused_count
                from group_members gm
                join users u
                  on u.id = gm.user_id
                 and u.deleted_at is null
                left join (
                    select
                        sa.user_id as user_id,
                        count(*) as total_sessions,
                        coalesce(sum(case when sa.attendance_status = 'PRESENT' then 1 else 0 end), 0) as present_count,
                        coalesce(sum(case when sa.attendance_status = 'LATE' then 1 else 0 end), 0) as late_count,
                        coalesce(sum(case when sa.attendance_status = 'ABSENT' then 1 else 0 end), 0) as absent_count,
                        coalesce(sum(case when sa.attendance_status = 'EXCUSED' then 1 else 0 end), 0) as excused_count
                    from session_attendance sa
                    join attendance_sessions s
                      on s.id = sa.session_id
                    where s.group_id = UUID_TO_BIN(:groupId, 1)
                      and s.status = 'CLOSED'
                    group by sa.user_id
                ) sumx
                  on sumx.user_id = gm.user_id
                where gm.group_id = UUID_TO_BIN(:groupId, 1)
                  and gm.member_status = 'APPROVED'
                order by u.full_name asc, u.email asc, bin_to_uuid(gm.user_id, 1) asc
                limit :limit offset :offset
                """;

        String countSql = """
                select count(*)
                from group_members gm
                join users u
                  on u.id = gm.user_id
                 and u.deleted_at is null
                where gm.group_id = UUID_TO_BIN(:groupId, 1)
                  and gm.member_status = 'APPROVED'
                """;

        int offset = page * size;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("groupId", groupId.toString())
                .addValue("limit", size)
                .addValue("offset", offset);

        List<GroupSummaryRow> items = jdbc.query(dataSql, params, groupSummaryRowMapper());
        Long totalElements = jdbc.queryForObject(countSql, params, Long.class);
        long safeTotalElements = totalElements == null ? 0L : totalElements;
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) safeTotalElements / size);

        return new PageImpl<>(items, org.springframework.data.domain.PageRequest.of(page, size), safeTotalElements);
    }

    @Override
    public boolean groupExists(UUID groupId) {
        String sql = """
                select count(*)
                from class_groups g
                where g.id = UUID_TO_BIN(:groupId, 1)
                  and g.deleted_at is null
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("groupId", groupId.toString());

        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    @Override
    public boolean isOwnerOrCoHost(UUID groupId, UUID userId) {
        String sql = """
                select count(*)
                from group_members gm
                where gm.group_id = UUID_TO_BIN(:groupId, 1)
                  and gm.user_id = UUID_TO_BIN(:userId, 1)
                  and gm.member_status = 'APPROVED'
                  and gm.role in ('OWNER', 'CO_HOST')
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("groupId", groupId.toString())
                .addValue("userId", userId.toString());

        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    private RowMapper<StudentSummaryRow> studentSummaryRowMapper() {
        return (rs, rowNum) -> new StudentSummaryRow(
                rs.getLong("total_sessions"),
                rs.getLong("present_count"),
                rs.getLong("late_count"),
                rs.getLong("absent_count"),
                rs.getLong("excused_count")
        );
    }

    private RowMapper<GroupSummaryRow> groupSummaryRowMapper() {
        return (rs, rowNum) -> new GroupSummaryRow(
                UUID.fromString(rs.getString("user_id")),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getLong("total_sessions"),
                rs.getLong("present_count"),
                rs.getLong("late_count"),
                rs.getLong("absent_count"),
                rs.getLong("excused_count")
        );
    }
}