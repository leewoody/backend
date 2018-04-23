package liquibase.change.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropUniqueConstraintStatement;

public class DropUniqueConstraintChangeTest  extends StandardChangeTest {
    private DropUniqueConstraintChange change;

    @Before
    public void setUp() throws Exception {
        change = new DropUniqueConstraintChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TAB_NAME");
        change.setConstraintName("UQ_CONSTRAINT");
    }

     @Test
    public void getRefactoringName() throws Exception {
        assertEquals("dropUniqueConstraint", ChangeFactory.getInstance().getChangeMetaData(change).getName());
    }

     @Test
    public void generateStatement() throws Exception {
        SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof DropUniqueConstraintStatement);
        assertEquals("SCHEMA_NAME", ((DropUniqueConstraintStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TAB_NAME", ((DropUniqueConstraintStatement) sqlStatements[0]).getTableName());
        assertEquals("UQ_CONSTRAINT", ((DropUniqueConstraintStatement) sqlStatements[0]).getConstraintName());
    }

     @Test
    public void getConfirmationMessage() throws Exception {
        assertEquals("Unique constraint UQ_CONSTRAINT dropped from TAB_NAME", change.getConfirmationMessage());
    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        return database instanceof SQLiteDatabase;
    }

}