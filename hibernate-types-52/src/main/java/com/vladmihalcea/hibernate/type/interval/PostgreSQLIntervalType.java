package com.vladmihalcea.hibernate.type.interval;

import com.vladmihalcea.hibernate.type.ImmutableType;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.postgresql.util.PGInterval;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Maps an {@link Duration} object type to a PostgreSQL Interval column type.
 * <p>
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 2.5.1
 */
public class PostgreSQLIntervalType extends ImmutableType<Duration> {

    public static final PostgreSQLIntervalType INSTANCE = new PostgreSQLIntervalType();

    public PostgreSQLIntervalType() {
        super(Duration.class);
    }

    @Override
    protected Duration get(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws SQLException {
        final PGInterval interval = (PGInterval) rs.getObject(names[0]);

        if (interval == null) {
            return null;
        }

        final int days = interval.getDays();
        final int hours = interval.getHours();
        final int minutes = interval.getMinutes();
        final double secs = interval.getSeconds();

        return Duration.ofDays(days)
                .plus(hours, ChronoUnit.HOURS)
                .plus(minutes, ChronoUnit.MINUTES)
                .plus((long) Math.floor(secs), ChronoUnit.SECONDS);
    }

    @Override
    protected void set(PreparedStatement st, Duration value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            final int days = (int) value.toDays();
            final int hours = (int) (value.toHours() % 24);
            final int minutes = (int) (value.toMinutes() % 60);
            final double secs = value.getSeconds() % 60;
            st.setObject(index, new PGInterval(0, 0, days, hours, minutes, secs));
        }
    }

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.OTHER};
    }

}
