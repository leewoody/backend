package liquibase.structure;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractDatabaseObject implements DatabaseObject {

    private Map<String, Object> attributes = new HashMap<String, Object>();

    private UUID snapshotId;

    @Override
    public String getObjectTypeName() {
        return StringUtils.lowerCaseFirst(getClass().getSimpleName());
    }

    @Override
    public UUID getSnapshotId() {
        return snapshotId;
    }

    @Override
    public void setSnapshotId(UUID snapshotId) {
        if (snapshotId == null) {
            throw new UnexpectedLiquibaseException("Must be a non null uuid");
        }
        if (this.snapshotId != null) {
            throw new UnexpectedLiquibaseException("snapshotId already set");
        }
        this.snapshotId = snapshotId;
    }

    @Override
    public boolean snapshotByDefault() {
        return true;
    }

    @Override
    public int compareTo(Object o) {
        return this.getName().compareTo(((AbstractDatabaseObject) o).getName());
    }

    @Override
    public Set<String> getAttributes() {
        return attributes.keySet();
    }

    @Override
    public <T> T getAttribute(String attribute, Class<T> type) {
        return (T) attributes.get(attribute);
    }

    @Override
    public DatabaseObject setAttribute(String attribute, Object value) {
        if (value == null) {
            attributes.remove(attribute);
        } else {
            attributes.put(attribute, value);
        }
        return this;
    }
}
