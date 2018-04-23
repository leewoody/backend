package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectCollection;
import liquibase.structure.core.Column;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DiffOutputControl {
    private boolean includeSchema;
    private boolean includeCatalog;
    private boolean includeTablespace;

    private String dataDir = null;
    private DatabaseObjectCollection alreadyHandledMissing= new DatabaseObjectCollection(new DatabaseForHash());
    private DatabaseObjectCollection alreadyHandledUnexpected = new DatabaseObjectCollection(new DatabaseForHash());
    private DatabaseObjectCollection alreadyHandledChanged = new DatabaseObjectCollection(new DatabaseForHash());

    public DiffOutputControl() {
        includeSchema = true;
        includeCatalog = true;
        includeTablespace = true;
    }

    public DiffOutputControl(boolean includeCatalog, boolean includeSchema, boolean includeTablespace) {
        this.includeSchema = includeSchema;
        this.includeCatalog = includeCatalog;
        this.includeTablespace = includeTablespace;
    }

    public boolean isIncludeSchema() {
        return includeSchema;
    }

    public DiffOutputControl setIncludeSchema(boolean includeSchema) {
        this.includeSchema = includeSchema;
        return this;
    }

    public boolean isIncludeCatalog() {
        return includeCatalog;
    }

    public DiffOutputControl setIncludeCatalog(boolean includeCatalog) {
        this.includeCatalog = includeCatalog;
        return this;
    }

    public boolean isIncludeTablespace() {
        return includeTablespace;
    }

    public DiffOutputControl setIncludeTablespace(boolean includeTablespace) {
        this.includeTablespace = includeTablespace;
        return this;
    }

    public String getDataDir() {
        return dataDir;
    }

    public DiffOutputControl setDataDir(String dataDir) {
        this.dataDir = dataDir;
        return this;
    }

    public void setAlreadyHandledMissing(DatabaseObject missingObject) {
        this.alreadyHandledMissing.add(missingObject);
    }

    public boolean alreadyHandledMissing(DatabaseObject missingObject, Database accordingTo) {
        return alreadyHandledMissing.contains(missingObject);
    }

    public void setAlreadyHandledUnexpected(DatabaseObject unexpectedObject) {
        this.alreadyHandledUnexpected.add(unexpectedObject);
    }

    public boolean alreadyHandledUnexpected(DatabaseObject unexpectedObject, Database accordingTo) {
        return alreadyHandledUnexpected.contains(unexpectedObject);    }

    public void setAlreadyHandledChanged(DatabaseObject changedObject) {
        this.alreadyHandledChanged.add(changedObject);
    }

    public boolean alreadyHandledChanged(DatabaseObject changedObject, Database accordingTo) {
        return alreadyHandledChanged.contains(changedObject);    }

    private static class DatabaseForHash extends H2Database {
        @Override
        public boolean isCaseSensitive() {
            return true;
        }
    }

}
