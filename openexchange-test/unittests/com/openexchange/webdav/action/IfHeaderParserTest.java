
package com.openexchange.webdav.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import com.openexchange.webdav.action.ifheader.IfHeader;
import com.openexchange.webdav.action.ifheader.IfHeaderEntity;
import com.openexchange.webdav.action.ifheader.IfHeaderList;
import com.openexchange.webdav.action.ifheader.IfHeaderParseException;
import com.openexchange.webdav.action.ifheader.IfHeaderParser;

public class IfHeaderParserTest {

    @Test
    public void testETag() throws Exception {
        final IfHeader ifHeader = new IfHeaderParser().parse("([etag])");

        assertEquals(1, ifHeader.getLists().size());

        final List<IfHeaderEntity> list = ifHeader.getList(0);

        assertEquals(1, list.size());

        final IfHeaderEntity entity = list.get(0);
        assertTrue(entity.isETag());
        assertTrue(entity.mustMatch());
        assertFalse(entity.isLockToken());
        assertEquals("etag", entity.getPayload());
    }

    @Test
    public void testLockToken() throws Exception {
        final IfHeader ifHeader = new IfHeaderParser().parse("(<http://www.open-xchange.com/webdav/12345>)");

        assertEquals(1, ifHeader.getLists().size());

        final List<IfHeaderEntity> list = ifHeader.getList(0);

        assertEquals(1, list.size());

        final IfHeaderEntity entity = list.get(0);
        assertTrue(entity.isLockToken());
        assertTrue(entity.mustMatch());
        assertEquals("http://www.open-xchange.com/webdav/12345", entity.getPayload());
    }

    @Test
    public void testTrimETag() throws Exception {
        final IfHeader ifHeader = new IfHeaderParser().parse("([   etag ])");

        assertEquals(1, ifHeader.getLists().size());

        final List<IfHeaderEntity> list = ifHeader.getList(0);

        assertEquals(1, list.size());

        final IfHeaderEntity entity = list.get(0);
        assertTrue(entity.isETag());
        assertTrue(entity.mustMatch());
        assertFalse(entity.isLockToken());
        assertEquals("etag", entity.getPayload());
    }

    @Test
    public void testTrimLockToken() throws Exception {
        final IfHeader ifHeader = new IfHeaderParser().parse("(<   http://www.open-xchange.com/webdav/12345 >)");

        assertEquals(1, ifHeader.getLists().size());

        final List<IfHeaderEntity> list = ifHeader.getList(0);

        assertEquals(1, list.size());

        final IfHeaderEntity entity = list.get(0);
        assertTrue(entity.mustMatch());
        assertTrue(entity.isLockToken());
        assertEquals("http://www.open-xchange.com/webdav/12345", entity.getPayload());
    }

    @Test
    public void testNot() throws Exception {
        final IfHeader ifHeader = new IfHeaderParser().parse("(Not [etag] <lockToken> Not <lockToken2>)");

        assertEquals(1, ifHeader.getLists().size());

        final List<IfHeaderEntity> list = ifHeader.getList(0);

        assertEquals(3, list.size());

        int found = 0;
        for (final IfHeaderEntity entity : list) {
            if ("etag".equals(entity.getPayload())) {
                assertFalse(entity.mustMatch());
                found++;
            } else if ("lockToken".equals(entity.getPayload())) {
                assertTrue(entity.mustMatch());
                found++;
            } else if ("lockToken2".equals(entity.getPayload())) {
                assertFalse(entity.mustMatch());
                found++;
            }
        }
        assertEquals(3, found);
    }

    @Test
    public void testMany() throws Exception {
        final IfHeader ifHeader = new IfHeaderParser().parse("([etag] <lockToken> [etag2] <lockToken2> <lockToken3> [etag3])");

        assertEquals(1, ifHeader.getLists().size());

        final List<IfHeaderEntity> list = ifHeader.getList(0);

        assertEquals(6, list.size());

        final Set<String> etags = new HashSet<String>(Arrays.asList("etag", "etag2", "etag3"));
        final Set<String> lockToken = new HashSet<String>(Arrays.asList("lockToken", "lockToken2", "lockToken3"));

        for (final IfHeaderEntity entity : list) {
            if (entity.isETag()) {
                assertTrue(entity.getPayload(), etags.remove(entity.getPayload()));
            } else {
                assertTrue(entity.getPayload(), lockToken.remove(entity.getPayload()));
            }
        }
        assertTrue(etags.toString(), etags.isEmpty());
        assertTrue(lockToken.toString(), lockToken.isEmpty());
    }

    @Test
    public void testUntaggedLists() throws Exception {
        final IfHeader ifHeader = new IfHeaderParser().parse("([etag]) (<lockToken>) ([etag2])");

        assertEquals(3, ifHeader.getLists().size());

        final Set<String> etags = new HashSet<String>(Arrays.asList("etag", "etag2"));
        final Set<String> lockToken = new HashSet<String>(Arrays.asList("lockToken"));

        for (final IfHeaderList list : ifHeader.getLists()) {
            assertEquals(1, list.size());
            assertFalse(list.isTagged());
            final IfHeaderEntity entity = list.get(0);
            if (entity.isETag()) {
                assertTrue(entity.getPayload(), etags.remove(entity.getPayload()));
            } else {
                assertTrue(entity.getPayload(), lockToken.remove(entity.getPayload()));
            }
        }
        assertTrue(etags.toString(), etags.isEmpty());
        assertTrue(lockToken.toString(), lockToken.isEmpty());

    }

    @Test
    public void testTaggedLists() throws Exception {
        final IfHeader ifHeader = new IfHeaderParser().parse("<http://myResource1> ([etag]) <  http://myResource2> (<lockToken>) <http://myResource3> ([etag2])");

        assertEquals(3, ifHeader.getLists().size());

        final Set<String> etags = new HashSet<String>(Arrays.asList("etag", "etag2"));
        final Set<String> lockToken = new HashSet<String>(Arrays.asList("lockToken"));
        final Set<String> resources = new HashSet<String>(Arrays.asList("http://myResource1", "http://myResource2", "http://myResource3"));

        for (final IfHeaderList list : ifHeader.getLists()) {
            assertEquals(1, list.size());
            assertTrue(list.isTagged());
            assertTrue(resources.remove(list.getTag()));
            final IfHeaderEntity entity = list.get(0);
            if (entity.isETag()) {
                assertTrue(entity.getPayload(), etags.remove(entity.getPayload()));
            } else {
                assertTrue(entity.getPayload(), lockToken.remove(entity.getPayload()));
            }
        }
        assertTrue(resources.isEmpty());
        assertTrue(etags.toString(), etags.isEmpty());
        assertTrue(lockToken.toString(), lockToken.isEmpty());
    }

    @Test
    public void testGetRelevant() throws Exception {
        final IfHeader ifHeader = new IfHeaderParser().parse("<http://myResource1> ([etag]) <  http://myResource1> (<lockToken>) <http://myResource3> ([etag2])");

        assertEquals(3, ifHeader.getLists().size());

        final Set<String> etags = new HashSet<String>(Arrays.asList("etag"));
        final Set<String> lockToken = new HashSet<String>(Arrays.asList("lockToken"));

        for (final IfHeaderList list : ifHeader.getRelevant("http://myResource1")) {
            assertEquals(1, list.size());
            assertTrue(list.isTagged());
            final IfHeaderEntity entity = list.get(0);
            if (entity.isETag()) {
                assertTrue(entity.getPayload(), etags.remove(entity.getPayload()));
            } else {
                assertTrue(entity.getPayload(), lockToken.remove(entity.getPayload()));
            }
        }
        assertTrue(etags.toString(), etags.isEmpty());
        assertTrue(lockToken.toString(), lockToken.isEmpty());
    }

    @Test
    public void testUnfinishedTag() throws Exception {
        assertException("<blabla ([gnatzel])", "Unfinished Tag", 1);
    }

    @Test
    public void testUnfinishedList() throws Exception {
        assertException("<blabla> ([gnatzel]", "Unfinished List", 10);
    }

    @Test
    public void testUnfinishedETag() throws Exception {
        assertException("<blabla> ([gnatzel)", "Unfinished ETag", 11);
    }

    @Test
    public void testUnfinishedLockToken() throws Exception {
        assertException("<blabla> (<gnatzel)", "Unfinished LockToken", 11);
    }

    @Test
    public void testInvalidNot() throws Exception {
        assertException("<blabla> (<gnatzel> NurgelWargelWahnsinn)", "Invalid character", 22);
    }

    private void assertException(final String ifHeader, final String errorMessage, final int column) {
        try {
            new IfHeaderParser().parse("([etag])");
        } catch (final IfHeaderParseException x) {
            assertTrue(x.getMessage().contains(errorMessage));
            assertEquals(column, x.getColumn());
        }
    }

}
