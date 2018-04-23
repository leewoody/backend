package liquibase.serializer.core.yaml;

import org.junit.Test;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.changelog.ChangeSet;

public class YamlChangeLogSerializerTest {

    @Test
    public void serialize__change() {
        ChangeSet changeSet = new ChangeSet("test1", "nvoxland", false, true, "/test/me.txt", null, null, null);
        CreateTableChange change = new CreateTableChange();
        change.setTableName("testTable");
        change.addColumn(new ColumnConfig().setName("id").setType("int"));
        change.addColumn(new ColumnConfig().setName("name").setType("varchar(255)"));
        changeSet.addChange(change);

        String out = new YamlChangeLogSerializer().serialize(changeSet, false);

        System.out.println(out);
    }

//    @Test
//    public void serialize_changelog() {
//        ChangeSet changeSet = new ChangeSet("test1", "nvoxland", false, true, "/test/me.txt", null, null);
//        CreateTableChange change = new CreateTableChange();
//        change.setTableName("testTable");
//        change.addColumn(new ColumnConfig().setName("id").setType("int"));
//        change.addColumn(new ColumnConfig().setName("name").setType("varchar(255)"));
//        changeSet.addChange(change);
//
//        DatabaseChangeLog changeLog = new DatabaseChangeLog("physical/path.txt");
//        changeLog.addChangeSet(changeSet);
//
//        String out = new YamlChangeLogSerializer().serialize(changeLog);
//
//        System.out.println(out);
//    }
}