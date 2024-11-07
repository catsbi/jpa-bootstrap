package persistence.sql.context;

import java.sql.Connection;

public interface EntityPersister {

    <T> Object insert(T entity);

    <T> Object insert(T entity, T parentEntity);

    <T> void update(T entity, T snapshotEntity);

    <T> void delete(T entity);

    Connection getConnection();
}
