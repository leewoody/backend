
package com.openexchange.mail.utils;

import static org.junit.Assert.assertEquals;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.mail.internet.InternetAddress;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactUtil;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * Tests for class {@link MsisdnUtility}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.2.2
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ UserStorage.class, ServerServiceRegistry.class, ContactUtil.class })
public class MsisdnUtilityTest {

    @Mock
    private Session mockedSession;

    @Mock
    private User mockedUser;

    @Mock
    private ServerServiceRegistry serverServiceRegistry;

    @Mock
    private ContactService contactService;

    Set<String> numbers = new TreeSet<String>();

    Contact contact = new Contact();

    @Before
    public void setUp() throws OXException {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(UserStorage.class);
        PowerMockito.mockStatic(ServerServiceRegistry.class);
        PowerMockito.mockStatic(ContactUtil.class);

        PowerMockito.when(mockedSession.getUserId()).thenReturn(1);
        PowerMockito.when(mockedSession.getContextId()).thenReturn(1);
        PowerMockito.when(mockedUser.getContactId()).thenReturn(1);
        PowerMockito.when(serverServiceRegistry.getService(ContactService.class)).thenReturn(contactService);
        PowerMockito.when(
            contactService.getContact(mockedSession, Integer.toString(FolderObject.SYSTEM_LDAP_FOLDER_ID), Integer.toString(1))).thenReturn(
                contact);
        PowerMockito.when(
            contactService.getUser(mockedSession, 1)).thenReturn(
                contact);
        PowerMockito.when(UserStorage.getStorageUser(1, 1)).thenReturn(mockedUser);
        PowerMockito.when(ServerServiceRegistry.getInstance()).thenReturn(serverServiceRegistry);
        PowerMockito.when(ContactUtil.gatherTelephoneNumbers(contact)).thenReturn(numbers);

    }

    @Test
    public final void testAddMsisdnAddress_noNumberFound_returnWithoutAddedNumber() {
        final Set<InternetAddress> validAddrs = new HashSet<InternetAddress>();

        MsisdnUtility.addMsisdnAddress(validAddrs, mockedSession);

        assertEquals(numbers.size(), validAddrs.size());
    }

    @Test
    public final void testAddMsisdnAddress_oneNumberFound_returnWithAddedNumber() {
        numbers.add("myNumber");

        final Set<InternetAddress> validAddrs = new HashSet<InternetAddress>();

        MsisdnUtility.addMsisdnAddress(validAddrs, mockedSession);

        assertEquals(numbers.size(), validAddrs.size());
    }

    @Test
    public final void testAddMsisdnAddress_fiveNumberFound_returnWithAddedNumber() {
        numbers.add("myNumber0");
        numbers.add("myNumber1");
        numbers.add("myNumber2");
        numbers.add("myNumber3");
        numbers.add("myNumber4");

        final Set<InternetAddress> validAddrs = new HashSet<InternetAddress>();

        MsisdnUtility.addMsisdnAddress(validAddrs, mockedSession);

        assertEquals(numbers.size(), validAddrs.size());
    }

    @Test
    public final void testAddMsisdnAddress_noContactFound_return() {
        PowerMockito.when(mockedUser.getContactId()).thenReturn(0);

        final Set<InternetAddress> validAddrs = new HashSet<InternetAddress>();

        MsisdnUtility.addMsisdnAddress(validAddrs, mockedSession);

        assertEquals(0, validAddrs.size());
    }

    @Test
    public final void testAddMsisdnAddress_contactServiceNull_return() {
        PowerMockito.when(serverServiceRegistry.getService(ContactService.class)).thenReturn(null);

        final Set<InternetAddress> validAddrs = new HashSet<InternetAddress>();

        MsisdnUtility.addMsisdnAddress(validAddrs, mockedSession);

        assertEquals(0, validAddrs.size());
    }

    @Test
    public final void testAddMsisdnAddress_contactNull_return() throws OXException {
        PowerMockito.when(
            contactService.getContact(mockedSession, Integer.toString(FolderObject.SYSTEM_LDAP_FOLDER_ID), Integer.toString(1))).thenReturn(
                null);

        final Set<InternetAddress> validAddrs = new HashSet<InternetAddress>();

        MsisdnUtility.addMsisdnAddress(validAddrs, mockedSession);

        assertEquals(0, validAddrs.size());
    }
}
