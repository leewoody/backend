package liquibase.change.core;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;

public class CreateViewChangeTest extends StandardChangeTest {


     @Test
    public void getRefactoringName() throws Exception {
        assertEquals("createView", ChangeFactory.getInstance().getChangeMetaData(new CreateViewChange()).getName());
    }

     @Test
    public void generateStatement() throws Exception {

//        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
//            public void performTest(Database database) throws Exception {
//                CreateViewChange change = new CreateViewChange();
//                change.setSchemaName("SCHEMA_NAME");
//                change.setViewName("VIEW_NAME");
//                change.setSelectQuery("SELECT * FROM EXISTING_TABLE");
//
//                SqlStatement[] sqlStatements = change.generateStatements(database);
//                assertEquals(1, sqlStatements.length);
//                assertTrue(sqlStatements[0] instanceof CreateViewStatement);
//
//                assertEquals("SCHEMA_NAME", ((CreateViewStatement) sqlStatements[0]).getSchemaName());
//                assertEquals("VIEW_NAME", ((CreateViewStatement) sqlStatements[0]).getViewName());
//                assertEquals("SELECT * FROM EXISTING_TABLE", ((CreateViewStatement) sqlStatements[0]).getSelectQuery());
//            }
//        });
    }

     @Test
    public void getConfirmationMessage() throws Exception {
        CreateViewChange change = new CreateViewChange();
        change.setViewName("VIEW_NAME");

        assertEquals("View VIEW_NAME created", change.getConfirmationMessage());
    }
}
