package de.mephisto.vpin.server.util;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;

import java.sql.SQLException;
import java.util.EnumSet;

public class IncrementGenerator implements BeforeExecutionGenerator {

    private long value = -1;

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }

    @Override
    public synchronized Object generate(SharedSessionContractImplementor session,
                                        Object owner,
                                        Object currentValue,
                                        EventType eventType) {
        if (value < 0) {
            String tableName = session.getFactory()
                    .getMappingMetamodel()
                    .getEntityDescriptor(owner.getClass())
                    .getMappedTableDetails()
                    .getTableName();

            Long max = session.doReturningWork(connection -> {
                try (var stmt = connection.createStatement();
                     var rs = stmt.executeQuery("select max(id) from " + tableName)) {
                    return rs.next() ? rs.getLong(1) : 0L;
                }
            });
            value = (max != null) ? max : 0L;
        }
        return ++value;
    }
}