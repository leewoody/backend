
package com.openexchange.webdav.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.jdom2.Namespace;
import org.junit.Test;
import com.openexchange.test.XMLCompare;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.util.Utils;

public class PropfindTest extends ActionTestCase {

    private static final Namespace TEST_NS = Namespace.getNamespace("http://www.open-xchange.com/namespace/webdav-test");

    private WebdavPath INDEX_HTML_URL = null;
    private WebdavPath DEVELOPMENT_URL = null;
    private WebdavPath SPECIAL_URL;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        INDEX_HTML_URL = testCollection.dup().append(new WebdavPath("index.html"));
        DEVELOPMENT_URL = testCollection.dup().append("development");
        SPECIAL_URL = testCollection.dup().append("special characters?");

    }

    //TODO: noroot

    @Test
    public void testOneProperty() throws Exception {
        final Date lastModified = factory.resolveResource(INDEX_HTML_URL).getLastModified();

        final String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:prop><D:getlastmodified/></D:prop></D:propfind>";
        final String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\"><D:response><D:href>http://localhost/" + INDEX_HTML_URL + "</D:href><D:propstat><D:prop><D:getlastmodified>" + Utils.convert(lastModified) + "</D:getlastmodified></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";

        MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        MockWebdavResponse res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(INDEX_HTML_URL);

        final WebdavAction action = new WebdavPropfindAction(new Protocol());
        action.perform(req, res);

        assertEquals(Protocol.SC_MULTISTATUS, res.getStatus());

        final XMLCompare compare = new XMLCompare();
        compare.setCheckTextNames("getlastmodified", "status");
        assertTrue(compare.compare(expect, res.getResponseBodyAsString()));

        req = new MockWebdavRequest(factory, "http://localhost/");
        res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(INDEX_HTML_URL);
        req.setHeader("depth", "0");
        action.perform(req, res);

        assertEquals(Protocol.SC_MULTISTATUS, res.getStatus());

        assertTrue(compare.compare(expect, res.getResponseBodyAsString()));

    }

    @Test
    public void testManyProperties() throws Exception {
        final Date lastModified = factory.resolveResource(INDEX_HTML_URL).getLastModified();

        final String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:prop><D:getlastmodified/><D:displayname/><D:resourcetype/></D:prop></D:propfind>";
        final String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\"><D:response><D:href>http://localhost/" + INDEX_HTML_URL + "</D:href><D:propstat><D:prop><D:getlastmodified>" + Utils.convert(lastModified) + "</D:getlastmodified><D:displayname>index.html</D:displayname><D:resourcetype /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(INDEX_HTML_URL);

        final WebdavAction action = new WebdavPropfindAction(new Protocol());
        action.perform(req, res);
        assertEquals(Protocol.SC_MULTISTATUS, res.getStatus());

        final XMLCompare compare = new XMLCompare();
        compare.setCheckTextNames("getlastmodified", "displayname", "resourcetype", "status");

        assertTrue(compare.compare(expect, res.getResponseBodyAsString()));
    }

    @Test
    public void testPropNames() throws Exception {

        final String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:propname /></D:propfind>";
        final String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><multistatus xmlns=\"DAV:\"><response><href>http://localhost/" + INDEX_HTML_URL + "</href><propstat><prop><getlastmodified /><creationdate /><resourcetype /><displayname /><getcontentlanguage /><getcontentlength /><getcontenttype /><getetag /><lockdiscovery /><supportedlock /><source /></prop><status>HTTP/1.1 200 OK</status></propstat></response></multistatus>";

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(INDEX_HTML_URL);

        final WebdavAction action = new WebdavPropfindAction(new Protocol());
        action.perform(req, res);
        assertEquals(Protocol.SC_MULTISTATUS, res.getStatus());

        final XMLCompare compare = new XMLCompare();
        compare.setCheckTextNames("creationdate", "resourcetype", "displayname", "getcontenttype", "getcontentlanguage", "getcontentlength", "getlastmodified", "getetag", "source", "status", "lockdiscovery", "supportedlock");
        assertTrue(compare.compare(expect, res.getResponseBodyAsString()));
    }

    @Test
    public void testAllProperties() throws Exception {

        final WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);

        final String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:allprop /></D:propfind>";
        final String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><multistatus xmlns=\"DAV:\"><response><href>http://localhost/" + INDEX_HTML_URL + "</href><propstat><prop><getlastmodified>" + Utils.convert(resource.getLastModified()) + "</getlastmodified> <creationdate>" + Utils.convert(resource.getCreationDate()) + "</creationdate><resourcetype /><displayname>" + resource.getDisplayName() + "</displayname><getcontentlanguage/><getcontentlength>" + resource.getLength() + "</getcontentlength><getcontenttype>" + resource.getContentType() + "</getcontenttype><getetag>" + resource.getETag() + "</getetag><lockdiscovery /><supportedlock><lockentry><lockscope><exclusive/></lockscope><locktype><write/></locktype></lockentry><lockentry><lockscope><shared/></lockscope><locktype><write/></locktype></lockentry></supportedlock><source /></prop><status>HTTP/1.1 200 OK</status></propstat></response></multistatus>";

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(INDEX_HTML_URL);

        final WebdavAction action = new WebdavPropfindAction(new Protocol());
        action.perform(req, res);
        assertEquals(Protocol.SC_MULTISTATUS, res.getStatus());
        final XMLCompare compare = new XMLCompare();
        compare.setCheckTextNames("creationdate", "resourcetype", "displayname", "getcontenttype", "getcontentlanguage", "getcontentlength", "getlastmodified", "getetag", "source", "lockdiscovery", "supportedlock"); // FIXME: lockdiscovery und supportedlock
        assertTrue(compare.compare(expect, res.getResponseBodyAsString()));
    }

    @Test
    public void testCollection() throws Exception {
        final String PM_URL = testCollection + "/pm";
        final String INDEX_HTML_URL = testCollection + "/index.html";
        final String SITEMAP_HTML_URL = testCollection + "/sitemap.html";
        final String GUI_URL = DEVELOPMENT_URL + "/gui";
        final String INDEX3_HTML_URL = GUI_URL + "/index3.html";
        final String SPECIAL_CHARACTERS_URL = testCollection + "/special%20characters%3F";

        final String testCollDispName = factory.resolveResource(testCollection).getDisplayName();

        String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:prop><D:displayname/><D:resourcetype /></D:prop></D:propfind>";
        String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\"><D:response><D:href>http://localhost/" + DEVELOPMENT_URL + "</D:href><D:propstat><D:prop><D:displayname>development</D:displayname><D:resourcetype><D:collection /></D:resourcetype></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";

        MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        MockWebdavResponse res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(DEVELOPMENT_URL);
        req.setHeader("depth", "0");

        final WebdavAction action = new WebdavPropfindAction(new Protocol());
        action.perform(req, res);
        assertEquals(Protocol.SC_MULTISTATUS, res.getStatus());

        final XMLCompare compare = new XMLCompare();
        compare.setCheckTextNames("displayname", "status");
        assertTrue(compare.compare(expect, res.getResponseBodyAsString()));

        body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:prop><D:displayname/></D:prop></D:propfind>";
        expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\"><D:response><D:href>http://localhost" + testCollection + "</D:href><D:propstat><D:prop><D:displayname>" + testCollDispName + "</D:displayname></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response><D:response><D:href>http://localhost/" + DEVELOPMENT_URL + "</D:href><D:propstat><D:prop><D:displayname>development</D:displayname></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response><D:response><D:href>http://localhost/" + PM_URL + "</D:href><D:propstat><D:prop><D:displayname>pm</D:displayname></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response><D:response><D:href>http://localhost/" + INDEX_HTML_URL + "</D:href><D:propstat><D:prop><D:displayname>index.html</D:displayname></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response><D:response><D:href>http://localhost/" + SITEMAP_HTML_URL + "</D:href><D:propstat><D:prop><D:displayname>sitemap.html</D:displayname></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response><D:response><D:href>http://localhost/" + SPECIAL_CHARACTERS_URL + "</D:href><D:propstat><D:prop><D:displayname>special characters?</D:displayname></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";
        res = new MockWebdavResponse();

        req = new MockWebdavRequest(factory, "http://localhost/");

        req.setBodyAsString(body);
        req.setHeader("depth", "1");
        req.setUrl(testCollection);

        action.perform(req, res);
        assertTrue(compare.compare(expect, res.getResponseBodyAsString()));

        expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\"><D:response><D:href>http://localhost/" + DEVELOPMENT_URL + "</D:href><D:propstat><D:prop><D:displayname>development</D:displayname></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response><D:response><D:href>http://localhost/" + GUI_URL + "</D:href><D:propstat><D:prop><D:displayname>gui</D:displayname></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response><D:response><D:href>http://localhost/" + INDEX3_HTML_URL + "</D:href><D:propstat><D:prop><D:displayname>index3.html</D:displayname></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";
        res = new MockWebdavResponse();
        req = new MockWebdavRequest(factory, "http://localhost/");

        req.setBodyAsString(body);
        req.setHeader("depth", "infinity");
        req.setUrl(DEVELOPMENT_URL);

        action.perform(req, res);
        assertEquals(Protocol.SC_MULTISTATUS, res.getStatus());

        assertTrue(compare.compare(expect, res.getResponseBodyAsString()));

    }

    @Test
    public void testXMLProperty() throws Exception {
        final WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
        final WebdavProperty property = new WebdavProperty();
        property.setNamespace(TEST_NS.getURI());
        property.setName("test");
        property.setValue("<quark xmlns=\"http://www.open-xchange.com/namespace/webdav-test\"> In the left corner: The incredible Tessssssst Vallllhhhhuuuuuueeeee!</quark>");
        property.setXML(true);
        resource.putProperty(property);
        resource.save();

        final String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\" xmlns:OX=\"" + TEST_NS.getURI() + "\"><D:prop><OX:test/></D:prop></D:propfind>";
        final String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\" xmlns:OX=\"" + TEST_NS.getURI() + "\"><D:response><D:href>http://localhost/" + INDEX_HTML_URL + "</D:href><D:propstat><D:prop><OX:test><OX:quark> In the left corner: The incredible Tessssssst Vallllhhhhuuuuuueeeee!</OX:quark></OX:test></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(INDEX_HTML_URL);

        final WebdavAction action = new WebdavPropfindAction(new Protocol());
        action.perform(req, res);
        assertEquals(Protocol.SC_MULTISTATUS, res.getStatus());

        final XMLCompare compare = new XMLCompare();
        compare.setCheckTextNames("test", "quark", "status");

        assertTrue(compare.compare(expect, res.getResponseBodyAsString()));

    }

    @Test
    public void testMissingProperty() throws Exception {

        final Date lastModified = factory.resolveResource(INDEX_HTML_URL).getLastModified();

        final String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:prop><D:getlastmodified/><D:displayname/><D:resourcetype/><testProp xmlns=\"" + TEST_NS.getURI() + "\"/></D:prop></D:propfind>";
        final String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\"><D:response><D:href>http://localhost/" + INDEX_HTML_URL + "</D:href><D:propstat><D:prop><D:getlastmodified>" + Utils.convert(lastModified) + "</D:getlastmodified><D:displayname>index.html</D:displayname><D:resourcetype /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat><D:propstat><D:prop><testProp xmlns=\"" + TEST_NS.getURI() + "\"/></D:prop><D:status>HTTP/1.1 404 NOT FOUND</D:status></D:propstat></D:response></D:multistatus>";

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(INDEX_HTML_URL);

        final WebdavAction action = new WebdavPropfindAction(new Protocol());
        action.perform(req, res);
        assertEquals(Protocol.SC_MULTISTATUS, res.getStatus());

        final XMLCompare compare = new XMLCompare();
        compare.setCheckTextNames("getlastmodified", "displayname", "resourcetype", "status", "testProp");

        assertTrue(compare.compare(expect, res.getResponseBodyAsString()));
    }

    @Test
    public void testURLEncodedHREF() throws Exception {

        final Date lastModified = factory.resolveResource(SPECIAL_URL).getLastModified();

        final String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:prop><D:getlastmodified/></D:prop></D:propfind>";
        final String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\"><D:response><D:href>http://localhost" + testCollection + "/special%20characters%3F/</D:href><D:propstat><D:prop><D:getlastmodified>" + Utils.convert(lastModified) + "</D:getlastmodified></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(SPECIAL_URL);

        final WebdavAction action = new WebdavPropfindAction(new Protocol());
        action.perform(req, res);
        assertEquals(Protocol.SC_MULTISTATUS, res.getStatus());

        final XMLCompare compare = new XMLCompare();
        compare.setCheckTextNames("href");

        String response = res.getResponseBodyAsString();
        assertTrue(compare.compare(expect, response));
    }

    // Bug 9837
    @Test
    public void testURLEncodedHREFWithSlash() throws Exception {
        final WebdavPath problematicUrl = testCollection.dup().append("contains//slahes\\\\and backslashes");
        factory.resolveCollection(problematicUrl).save();
        final Date lastModified = factory.resolveResource(problematicUrl).getLastModified();

        final String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:prop><D:getlastmodified/></D:prop></D:propfind>";
        final String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\"><D:response><D:href>http://localhost" + testCollection + "/contains%2F%2Fslahes%5C%5Cand%20backslashes/</D:href><D:propstat><D:prop><D:getlastmodified>" + Utils.convert(lastModified) + "</D:getlastmodified></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(problematicUrl);

        final WebdavAction action = new WebdavPropfindAction(new Protocol());
        action.perform(req, res);
        assertEquals(Protocol.SC_MULTISTATUS, res.getStatus());

        final XMLCompare compare = new XMLCompare();
        compare.setCheckTextNames("href");

        String response = res.getResponseBodyAsString();
        assertTrue(compare.compare(expect, response));

    }

}
