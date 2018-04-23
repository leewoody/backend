/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.contact.storage.ldap.mapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import javax.naming.ldap.SortKey;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.SortOrder;
import com.openexchange.contact.storage.ldap.LdapExceptionCodes;
import com.openexchange.contact.storage.ldap.id.LdapIDResolver;
import com.openexchange.contact.storage.ldap.internal.LdapResult;
import com.openexchange.contact.storage.ldap.internal.Tools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tools.mappings.DefaultMapper;
import com.openexchange.java.util.UUIDs;
import com.openexchange.search.Operand;
import com.openexchange.search.SingleSearchTerm;

/**
 * {@link LdapMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class LdapMapper extends DefaultMapper<Contact, ContactField> {

    /**
     * A generic contact mapper without ldap attribute information
     */
    public static final LdapMapper GENERIC;

    /**
     * Contains all possible contact mappings
     */
    protected static final EnumMap<ContactField, LdapMapping<? extends Object>> POSSIBLE_MAPPINGS;

    protected final EnumMap<ContactField, LdapMapping<? extends Object>> mappings;


    public LdapMapper(Properties mappingProps, String propertyPrefix) throws OXException {
        super();
        this.mappings = this.loadMappings(mappingProps, propertyPrefix.endsWith(".") ? propertyPrefix : propertyPrefix + ".");
    }

    public LdapMapper(String mappingFile, String propertyPrefix) throws OXException {
        this(Tools.loadProperties(mappingFile), propertyPrefix);
    }

    private LdapMapper(EnumMap<ContactField, LdapMapping<? extends Object>> mappings) {
        super();
        this.mappings = mappings;
    }

    public Comparator<Contact> getComparator(final SortOptions sortOptions) {
        if (null == sortOptions || SortOptions.EMPTY.equals(sortOptions) ||
            null == sortOptions.getOrder() || 0 == sortOptions.getOrder().length) {
            return null;
        }
        final Locale locale = Tools.getLocale(sortOptions);
        return new Comparator<Contact>() {
            @Override
            public int compare(Contact contact1, Contact contact2) {
                for (SortOrder sortOrder : sortOptions.getOrder()) {
                    LdapMapping<? extends Object> mapping = opt(sortOrder.getBy());
                    if (null != mapping) {
                        int comparison = mapping.compare(contact1, contact2, locale);
                        if (0 != comparison) {
                            return Order.DESCENDING.equals(sortOrder.getOrder()) ? -1 * comparison : comparison;
                        }
                    }
                }
                return 0;
            }
        };
    }

    public LdapMapping<? extends Object> getMapping(SingleSearchTerm term) {
        LdapMapping<? extends Object> mapping = null;
        for (Operand<?> operand : term.getOperands()) {
            if (Operand.Type.COLUMN.equals(operand.getType())) {
                if (null != mapping) {
                    throw new IllegalArgumentException("Unable to handle more than one COLUMN-type operand in single-searchterm.");
                }
                mapping = this.opt(getField(operand.getValue()));
            }
        }
        return mapping;
    }

    protected EnumMap<ContactField, LdapMapping<? extends Object>> loadMappings(Properties mappingProps, String propertyPrefix) throws OXException {
        EnumMap<ContactField, LdapMapping<? extends Object>> mappings =
            new EnumMap<ContactField, LdapMapping<? extends Object>>(ContactField.class);
        int beginIndex = propertyPrefix.length();
        for (Entry<Object, Object> prop : mappingProps.entrySet()) {
            /*
             * check property
             */
            String propertyValue = (String)prop.getValue();
            if (null == propertyValue || 0 == propertyValue.length()) {
                // not mapped
                continue;
            }
            String propertyName = prop.getKey().toString();
            if (false == propertyName.startsWith(propertyPrefix) || beginIndex > propertyName.length()) {
                // wrong prefix
                continue;
            }
            propertyName = propertyName.substring(beginIndex);
            /*
             * add ldap mapping
             */
            ContactField field = null;
            try {
                field = getField(propertyName);
            } catch (IllegalArgumentException e) {
                throw LdapExceptionCodes.UNKNOWN_CONTACT_PROPERTY.create(e, propertyName);
            }
            if (null == field) {
                throw LdapExceptionCodes.UNKNOWN_CONTACT_PROPERTY.create(propertyName);
            }
            mappings.put(field, createMapping(field, propertyValue));
        }
        return mappings;
    }

    /**
     *
     *
     * @param result
     * @param idResolver
     * @param fields
     * @return
     * @throws OXException
     */
    public Contact createContact(LdapResult result, LdapIDResolver idResolver, ContactField[] fields) throws OXException {
        Contact contact = this.newInstance();
        for (ContactField field : fields) {
            LdapMapping<? extends Object> mapping = this.opt(field);
            if (null != mapping) {
                mapping.setFrom(result, idResolver, contact);
            }
        }
        return contact;
    }

    @Override
    public LdapMapping<? extends Object> get(ContactField field) throws OXException {
        LdapMapping<? extends Object> mapping = this.opt(field);
        if (null == mapping) {
            throw OXException.notFound(field.toString());
        }
        return mapping;
    }

    @Override
    public LdapMapping<? extends Object> opt(ContactField field) {
        if (null == field) {
            throw new IllegalArgumentException("field");
        }
        return getMappings().get(field);
    }

    @Override
    public Contact newInstance() {
        return new Contact();
    }

    @Override
    public ContactField[] newArray(int size) {
        return new ContactField[size];
    }

    @Override
    protected EnumMap<ContactField, LdapMapping<? extends Object>> getMappings() {
        return this.mappings;
    }

    public String[] getLdapAttributes(ContactField... fields) {
        if (null == fields) {
            return new String[0];
        }
        Set<String> ldapAttributes = new HashSet<String>(fields.length);
        for (ContactField field : fields) {
            LdapMapping<? extends Object> ldapMapping = this.opt(field);
            if (null != ldapMapping) {
                if (null != ldapMapping.getLdapAttributeName()) {
                    ldapAttributes.add(ldapMapping.getLdapAttributeName());
                }
                if (null != ldapMapping.getAlternativeLdapAttributeName()) {
                    ldapAttributes.add(ldapMapping.getAlternativeLdapAttributeName());
                }
            }
        }
        return ldapAttributes.toArray(new String[ldapAttributes.size()]);
    }

    public SortKey[] getSortKeys(SortOptions sortOptions) throws OXException {
        if (null != sortOptions && false == SortOptions.EMPTY.equals(sortOptions)
            && null != sortOptions.getOrder() && 0 < sortOptions.getOrder().length) {
            List<SortKey> sortKeys = new ArrayList<SortKey>();
            for (SortOrder sortOrder : sortOptions.getOrder()) {
                LdapMapping<? extends Object> ldapMapping = this.get(sortOrder.getBy());
                if (null != ldapMapping && null != ldapMapping.getLdapAttributeName()) {
                    sortKeys.add(new SortKey(ldapMapping.getLdapAttributeName(), Order.ASCENDING.equals(sortOrder.getOrder()), null));
                }
            }
            return sortKeys.toArray(new SortKey[sortKeys.size()]);
        } else {
            return null;
        }
    }

    /**
     * Creates a suitable mapping for the supplied contact field and sets the
     * mapping's ldap attribute name.
     *
     * @param field
     * @param ldapAttribute
     * @return
     * @throws OXException
     */
    protected static LdapMapping<? extends Object> createMapping(ContactField field, String ldapAttribute) throws OXException {
        LdapMapping<? extends Object> ldapMapping = POSSIBLE_MAPPINGS.get(field);
        if (null == ldapMapping) {
            throw LdapExceptionCodes.UNKNOWN_CONTACT_PROPERTY.create(field);
        } else {
            ldapMapping = (LdapMapping<?>)ldapMapping.clone();
            String[] attributeNames = ldapAttribute.split(",");
            if (null ==  attributeNames || 0 == attributeNames.length) {
                throw LdapExceptionCodes.MISSING_CONFIG_VALUE.create(field);
            }
            ldapMapping.setLdapAttributeName(attributeNames[0]);
            if (1 < attributeNames.length) {
                ldapMapping.setAlternativeLdapAttributeName(attributeNames[1]);
            }
            return ldapMapping;
        }
    }

    static {
        EnumMap<ContactField, LdapMapping<? extends Object>> mappings =
            new EnumMap<ContactField, LdapMapping<? extends Object>>(ContactField.class);

        mappings.put(ContactField.DISPLAY_NAME, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setDisplayName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsDisplayName();
            }

            @Override
            public String get(Contact contact) {
                return contact.getDisplayName();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeDisplayName();
            }
        });

        mappings.put(ContactField.SUR_NAME, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setSurName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsSurName();
            }

            @Override
            public String get(Contact contact) {
                return contact.getSurName();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeSurName();
            }
        });

        mappings.put(ContactField.GIVEN_NAME, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setGivenName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsGivenName();
            }

            @Override
            public String get(Contact contact) {
                return contact.getGivenName();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeGivenName();
            }
        });

        mappings.put(ContactField.MIDDLE_NAME, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setMiddleName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsMiddleName();
            }

            @Override
            public String get(Contact contact) {
                return contact.getMiddleName();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeMiddleName();
            }
        });

        mappings.put(ContactField.SUFFIX, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setSuffix(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsSuffix();
            }

            @Override
            public String get(Contact contact) {
                return contact.getSuffix();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeSuffix();
            }
        });

        mappings.put(ContactField.TITLE, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setTitle(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTitle();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTitle();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTitle();
            }
        });

        mappings.put(ContactField.STREET_HOME, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setStreetHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsStreetHome();
            }

            @Override
            public String get(Contact contact) {
                return contact.getStreetHome();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeStreetHome();
            }
        });

        mappings.put(ContactField.POSTAL_CODE_HOME, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setPostalCodeHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsPostalCodeHome();
            }

            @Override
            public String get(Contact contact) {
                return contact.getPostalCodeHome();
            }

            @Override
            public void remove(Contact contact) {
                contact.removePostalCodeHome();
            }
        });

        mappings.put(ContactField.CITY_HOME, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setCityHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCityHome();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCityHome();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCityHome();
            }
        });

        mappings.put(ContactField.STATE_HOME, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setStateHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsStateHome();
            }

            @Override
            public String get(Contact contact) {
                return contact.getStateHome();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeStateHome();
            }
        });

        mappings.put(ContactField.COUNTRY_HOME, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setCountryHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCountryHome();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCountryHome();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCountryHome();
            }
        });

        mappings.put(ContactField.MARITAL_STATUS, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setMaritalStatus(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsMaritalStatus();
            }

            @Override
            public String get(Contact contact) {
                return contact.getMaritalStatus();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeMaritalStatus();
            }
        });

        mappings.put(ContactField.NUMBER_OF_CHILDREN, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setNumberOfChildren(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsNumberOfChildren();
            }

            @Override
            public String get(Contact contact) {
                return contact.getNumberOfChildren();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeNumberOfChildren();
            }
        });

        mappings.put(ContactField.PROFESSION, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setProfession(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsProfession();
            }

            @Override
            public String get(Contact contact) {
                return contact.getProfession();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeProfession();
            }
        });

        mappings.put(ContactField.NICKNAME, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setNickname(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsNickname();
            }

            @Override
            public String get(Contact contact) {
                return contact.getNickname();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeNickname();
            }
        });

        mappings.put(ContactField.SPOUSE_NAME, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setSpouseName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsSpouseName();
            }

            @Override
            public String get(Contact contact) {
                return contact.getSpouseName();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeSpouseName();
            }
        });

        mappings.put(ContactField.NOTE, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setNote(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsNote();
            }

            @Override
            public String get(Contact contact) {
                return contact.getNote();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeNote();
            }
        });

        mappings.put(ContactField.COMPANY, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setCompany(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCompany();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCompany();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCompany();
            }
        });

        mappings.put(ContactField.DEPARTMENT, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setDepartment(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsDepartment();
            }

            @Override
            public String get(Contact contact) {
                return contact.getDepartment();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeDepartment();
            }
        });

        mappings.put(ContactField.POSITION, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setPosition(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsPosition();
            }

            @Override
            public String get(Contact contact) {
                return contact.getPosition();
            }

            @Override
            public void remove(Contact contact) {
                contact.removePosition();
            }
        });

        mappings.put(ContactField.EMPLOYEE_TYPE, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setEmployeeType(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsEmployeeType();
            }

            @Override
            public String get(Contact contact) {
                return contact.getEmployeeType();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeEmployeeType();
            }
        });

        mappings.put(ContactField.ROOM_NUMBER, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setRoomNumber(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsRoomNumber();
            }

            @Override
            public String get(Contact contact) {
                return contact.getRoomNumber();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeRoomNumber();
            }
        });

        mappings.put(ContactField.STREET_BUSINESS, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setStreetBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsStreetBusiness();
            }

            @Override
            public String get(Contact contact) {
                return contact.getStreetBusiness();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeStreetBusiness();
            }
        });

        mappings.put(ContactField.POSTAL_CODE_BUSINESS, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setPostalCodeBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsPostalCodeBusiness();
            }

            @Override
            public String get(Contact contact) {
                return contact.getPostalCodeBusiness();
            }

            @Override
            public void remove(Contact contact) {
                contact.removePostalCodeBusiness();
            }
        });

        mappings.put(ContactField.CITY_BUSINESS, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setCityBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCityBusiness();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCityBusiness();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCityBusiness();
            }
        });

        mappings.put(ContactField.STATE_BUSINESS, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setStateBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsStateBusiness();
            }

            @Override
            public String get(Contact contact) {
                return contact.getStateBusiness();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeStateBusiness();
            }
        });

        mappings.put(ContactField.COUNTRY_BUSINESS, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setCountryBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCountryBusiness();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCountryBusiness();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCountryBusiness();
            }
        });

        mappings.put(ContactField.NUMBER_OF_EMPLOYEE, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setNumberOfEmployee(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsNumberOfEmployee();
            }

            @Override
            public String get(Contact contact) {
                return contact.getNumberOfEmployee();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeNumberOfEmployee();
            }
        });

        mappings.put(ContactField.SALES_VOLUME, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setSalesVolume(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsSalesVolume();
            }

            @Override
            public String get(Contact contact) {
                return contact.getSalesVolume();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeSalesVolume();
            }
        });

        mappings.put(ContactField.TAX_ID, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setTaxID(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTaxID();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTaxID();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTaxID();
            }
        });

        mappings.put(ContactField.COMMERCIAL_REGISTER, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setCommercialRegister(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCommercialRegister();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCommercialRegister();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCommercialRegister();
            }
        });

        mappings.put(ContactField.BRANCHES, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setBranches(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsBranches();
            }

            @Override
            public String get(Contact contact) {
                return contact.getBranches();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeBranches();
            }
        });

        mappings.put(ContactField.BUSINESS_CATEGORY, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setBusinessCategory(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsBusinessCategory();
            }

            @Override
            public String get(Contact contact) {
                return contact.getBusinessCategory();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeBusinessCategory();
            }
        });

        mappings.put(ContactField.INFO, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setInfo(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsInfo();
            }

            @Override
            public String get(Contact contact) {
                return contact.getInfo();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeInfo();
            }
        });

        mappings.put(ContactField.MANAGER_NAME, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setManagerName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsManagerName();
            }

            @Override
            public String get(Contact contact) {
                return contact.getManagerName();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeManagerName();
            }
        });

        mappings.put(ContactField.ASSISTANT_NAME, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setAssistantName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsAssistantName();
            }

            @Override
            public String get(Contact contact) {
                return contact.getAssistantName();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeAssistantName();
            }
        });

        mappings.put(ContactField.STREET_OTHER, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setStreetOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsStreetOther();
            }

            @Override
            public String get(Contact contact) {
                return contact.getStreetOther();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeStreetHome();
            }
        });

        mappings.put(ContactField.POSTAL_CODE_OTHER, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setPostalCodeOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsPostalCodeOther();
            }

            @Override
            public String get(Contact contact) {
                return contact.getPostalCodeOther();
            }

            @Override
            public void remove(Contact contact) {
                contact.removePostalCodeHome();
            }
        });

        mappings.put(ContactField.CITY_OTHER, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setCityOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCityOther();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCityOther();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCityOther();
            }
        });

        mappings.put(ContactField.STATE_OTHER, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setStateOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsStateOther();
            }

            @Override
            public String get(Contact contact) {
                return contact.getStateOther();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeStateOther();
            }
        });

        mappings.put(ContactField.COUNTRY_OTHER, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setCountryOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCountryOther();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCountryOther();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCountryOther();
            }
        });

        mappings.put(ContactField.TELEPHONE_ASSISTANT, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneAssistant(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneAssistant();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneAssistant();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneAssistant();
            }
        });

        mappings.put(ContactField.TELEPHONE_BUSINESS1, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneBusiness1(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneBusiness1();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneBusiness1();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneBusiness1();
            }
        });

        mappings.put(ContactField.TELEPHONE_BUSINESS2, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneBusiness2(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneBusiness2();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneBusiness2();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneBusiness2();
            }
        });

        mappings.put(ContactField.FAX_BUSINESS, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setFaxBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsFaxBusiness();
            }

            @Override
            public String get(Contact contact) {
                return contact.getFaxBusiness();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeFaxBusiness();
            }
        });

        mappings.put(ContactField.TELEPHONE_CALLBACK, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneCallback(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneCallback();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneCallback();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneCallback();
            }
        });

        mappings.put(ContactField.TELEPHONE_CAR, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneCar(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneCar();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneCar();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneCar();
            }
        });

        mappings.put(ContactField.TELEPHONE_COMPANY, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneCompany(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneCompany();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneCompany();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneCompany();
            }
        });

        mappings.put(ContactField.TELEPHONE_HOME1, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneHome1(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneHome1();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneHome1();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneHome1();
            }
        });

        mappings.put(ContactField.TELEPHONE_HOME2, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneHome2(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneHome2();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneHome2();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneHome2();
            }
        });

        mappings.put(ContactField.FAX_HOME, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setFaxHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsFaxHome();
            }

            @Override
            public String get(Contact contact) {
                return contact.getFaxHome();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeFaxHome();
            }
        });

        mappings.put(ContactField.TELEPHONE_ISDN, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneISDN(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneISDN();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneISDN();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneISDN();
            }
        });

        mappings.put(ContactField.CELLULAR_TELEPHONE1, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setCellularTelephone1(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCellularTelephone1();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCellularTelephone1();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCellularTelephone1();
            }
        });

        mappings.put(ContactField.CELLULAR_TELEPHONE2, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setCellularTelephone2(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCellularTelephone2();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCellularTelephone2();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCellularTelephone2();
            }
        });

        mappings.put(ContactField.TELEPHONE_OTHER, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneOther();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneOther();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneOther();
            }
        });

        mappings.put(ContactField.FAX_OTHER, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setFaxOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsFaxOther();
            }

            @Override
            public String get(Contact contact) {
                return contact.getFaxOther();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeFaxOther();
            }
        });

        mappings.put(ContactField.TELEPHONE_PAGER, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephonePager(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephonePager();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephonePager();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephonePager();
            }
        });

        mappings.put(ContactField.TELEPHONE_PRIMARY, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephonePrimary(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephonePrimary();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephonePrimary();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephonePrimary();
            }
        });

        mappings.put(ContactField.TELEPHONE_RADIO, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneRadio(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneRadio();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneRadio();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneRadio();
            }
        });

        mappings.put(ContactField.TELEPHONE_TELEX, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneTelex(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneTelex();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneTelex();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneTelex();
            }
        });

        mappings.put(ContactField.TELEPHONE_TTYTDD, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneTTYTTD(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneTTYTTD();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneTTYTTD();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneTTYTTD();
            }
        });

        mappings.put(ContactField.INSTANT_MESSENGER1, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setInstantMessenger1(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsInstantMessenger1();
            }

            @Override
            public String get(Contact contact) {
                return contact.getInstantMessenger1();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeInstantMessenger1();
            }
        });

        mappings.put(ContactField.INSTANT_MESSENGER2, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setInstantMessenger2(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsInstantMessenger2();
            }

            @Override
            public String get(Contact contact) {
                return contact.getInstantMessenger2();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeInstantMessenger2();
            }
        });

        mappings.put(ContactField.TELEPHONE_IP, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneIP(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneIP();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneIP();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneIP();
            }
        });

        mappings.put(ContactField.EMAIL1, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setEmail1(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsEmail1();
            }

            @Override
            public String get(Contact contact) {
                return contact.getEmail1();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeEmail1();
            }
        });

        mappings.put(ContactField.EMAIL2, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setEmail2(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsEmail2();
            }

            @Override
            public String get(Contact contact) {
                return contact.getEmail2();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeEmail2();
            }
        });

        mappings.put(ContactField.EMAIL3, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setEmail3(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsEmail3();
            }

            @Override
            public String get(Contact contact) {
                return contact.getEmail3();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeEmail3();
            }
        });

        mappings.put(ContactField.URL, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setURL(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsURL();
            }

            @Override
            public String get(Contact contact) {
                return contact.getURL();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeURL();
            }
        });

        mappings.put(ContactField.CATEGORIES, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setCategories(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCategories();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCategories();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCategories();
            }
        });

        mappings.put(ContactField.USERFIELD01, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField01(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField01();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField01();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField01();
            }
        });

        mappings.put(ContactField.USERFIELD02, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField02(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField02();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField02();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField02();
            }
        });

        mappings.put(ContactField.USERFIELD03, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField03(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField03();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField03();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField03();
            }
        });

        mappings.put(ContactField.USERFIELD04, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField04(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField04();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField04();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField04();
            }
        });

        mappings.put(ContactField.USERFIELD05, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField05(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField05();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField05();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField05();
            }
        });

        mappings.put(ContactField.USERFIELD06, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField06(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField06();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField06();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField06();
            }
        });

        mappings.put(ContactField.USERFIELD07, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField07(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField07();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField07();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField07();
            }
        });

        mappings.put(ContactField.USERFIELD08, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField08(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField08();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField08();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField08();
            }
        });

        mappings.put(ContactField.USERFIELD09, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField09(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField09();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField09();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField09();
            }
        });

        mappings.put(ContactField.USERFIELD10, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField10(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField10();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField10();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField10();
            }
        });

        mappings.put(ContactField.USERFIELD11, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField11(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField11();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField11();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField11();
            }
        });

        mappings.put(ContactField.USERFIELD12, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField12(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField12();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField12();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField12();
            }
        });

        mappings.put(ContactField.USERFIELD13, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField13(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField13();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField13();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField13();
            }
        });

        mappings.put(ContactField.USERFIELD14, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField14(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField14();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField14();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField14();
            }
        });

        mappings.put(ContactField.USERFIELD15, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField15(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField15();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField15();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField15();
            }
        });

        mappings.put(ContactField.USERFIELD16, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField16(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField16();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField16();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField16();
            }
        });

        mappings.put(ContactField.USERFIELD17, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField17(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField17();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField17();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField17();
            }
        });

        mappings.put(ContactField.USERFIELD18, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField18(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField18();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField18();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField18();
            }
        });

        mappings.put(ContactField.USERFIELD19, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField19(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField19();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField19();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField19();
            }
        });

        mappings.put(ContactField.USERFIELD20, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField20(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField20();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField20();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField20();
            }
        });

        mappings.put(ContactField.OBJECT_ID, new LdapIDMapping() {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setObjectID(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsObjectID();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getObjectID();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeObjectID();
            }
        });

        mappings.put(ContactField.NUMBER_OF_DISTRIBUTIONLIST, new LdapIntegerMapping() {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setNumberOfDistributionLists(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsNumberOfDistributionLists();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getNumberOfDistributionLists();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeNumberOfDistributionLists();
            }
        });

        mappings.put(ContactField.DISTRIBUTIONLIST, new LdapDistListMapping() {

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsDistributionLists();
            }

            @Override
            public void set(Contact contact, DistributionListEntryObject[] value) throws OXException {
                contact.setDistributionList(value);
            }

            @Override
            public DistributionListEntryObject[] get(Contact contact) {
                return contact.getDistributionList();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeDistributionLists();
            }
        });

        mappings.put(ContactField.FOLDER_ID, new LdapIntegerMapping() {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setParentFolderID(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsParentFolderID();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getParentFolderID();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeParentFolderID();
            }
        });

        mappings.put(ContactField.CONTEXTID, new LdapIntegerMapping() {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setContextId(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsContextId();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getContextId();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeContextID();
            }
        });

        mappings.put(ContactField.PRIVATE_FLAG, new LdapIntegerMapping() {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setPrivateFlag(1 == value.intValue());
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsPrivateFlag();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getPrivateFlag() ? Integer.valueOf(1) : Integer.valueOf(0);
            }

            @Override
            public void remove(Contact contact) {
                contact.removePrivateFlag();
            }
        });

        mappings.put(ContactField.CREATED_BY, new LdapIntegerMapping() {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setCreatedBy(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCreatedBy();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getCreatedBy();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCreatedBy();
            }
        });

        mappings.put(ContactField.MODIFIED_BY, new LdapIntegerMapping() {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setModifiedBy(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsModifiedBy();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getModifiedBy();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeModifiedBy();
            }
        });

        mappings.put(ContactField.CREATION_DATE, new LdapDateMapping() {

            @Override
            public void set(Contact contact, Date value) {
                contact.setCreationDate(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCreationDate();
            }

            @Override
            public Date get(Contact contact) {
                return contact.getCreationDate();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCreationDate();
            }
        });

        mappings.put(ContactField.LAST_MODIFIED, new LdapDateMapping() {

            @Override
            public void set(Contact contact, Date value) {
                contact.setLastModified(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsLastModified();
            }

            @Override
            public Date get(Contact contact) {
                return contact.getLastModified();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeLastModified();
            }
        });

        mappings.put(ContactField.BIRTHDAY, new LdapDateMapping() {

            @Override
            public void set(Contact contact, Date value) {
                contact.setBirthday(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsBirthday();
            }

            @Override
            public Date get(Contact contact) {
                return contact.getBirthday();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeBirthday();
            }
        });

        mappings.put(ContactField.ANNIVERSARY, new LdapDateMapping() {

            @Override
            public void set(Contact contact, Date value) {
                contact.setAnniversary(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsAnniversary();
            }

            @Override
            public Date get(Contact contact) {
                return contact.getAnniversary();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeAnniversary();
            }
        });

        mappings.put(ContactField.IMAGE1, new LdapImageMapping() {

            @Override
            public void set(Contact contact, byte[] value) {
                contact.setImage1(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsImage1();
            }

            @Override
            public byte[] get(Contact contact) {
                return contact.getImage1();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeImage1();
            }
        });

        mappings.put(ContactField.IMAGE1_CONTENT_TYPE, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setImageContentType(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsImageContentType();
            }

            @Override
            public String get(Contact contact) {
                return contact.getImageContentType();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeImageContentType();
            }

            @Override
            public String get(LdapResult result, LdapIDResolver idResolver) throws OXException {
                /*
                 * mapping determines it's property value based on the presence of an ldap image
                 */
                Object value = getValue(result);
                return null != value && byte[].class.isInstance(value) ? "image/jpeg" : null;
            }

        });

        mappings.put(ContactField.IMAGE_LAST_MODIFIED, new LdapDateMapping() {

            @Override
            public void set(Contact contact, Date value) {
                contact.setImageLastModified(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsImageLastModified();
            }

            @Override
            public Date get(Contact contact) {
                return contact.getImageLastModified();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeImageLastModified();
            }

            @Override
            public Date get(LdapResult result, LdapIDResolver idResolver) throws OXException {
                /*
                 * mapping determines it's property value based on the presence of an ldap image
                 */
                Object value = getValue(result);
                return null != value && byte[].class.isInstance(value) ? new Date(0) : null;
            }

        });

        mappings.put(ContactField.INTERNAL_USERID, new LdapIntegerMapping() {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setInternalUserId(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsInternalUserId();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getInternalUserId();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeInternalUserId();
            }
        });

        mappings.put(ContactField.COLOR_LABEL, new LdapIntegerMapping() {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setLabel(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsLabel();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getLabel();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeLabel();
            }
        });

        mappings.put(ContactField.FILE_AS, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setFileAs(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsFileAs();
            }

            @Override
            public String get(Contact contact) {
                return contact.getFileAs();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeFileAs();
            }
        });

        mappings.put(ContactField.DEFAULT_ADDRESS, new LdapIntegerMapping() {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setDefaultAddress(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsDefaultAddress();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getDefaultAddress();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeDefaultAddress();
            }
        });

        mappings.put(ContactField.MARK_AS_DISTRIBUTIONLIST, new LdapBooleanMapping() {

            @Override
            public void set(Contact contact, Boolean value) {
                contact.setMarkAsDistributionlist(value.booleanValue());
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsMarkAsDistributionlist();
            }

            @Override
            public Boolean get(Contact contact) {
                return Boolean.valueOf(contact.getMarkAsDistribtuionlist());
            }

            @Override
            public void remove(Contact contact) {
                contact.removeMarkAsDistributionlist();
            }

//            @Override
//            public Boolean get(LdapResult result, LdapIDResolver idResolver) throws OXException {
//                Attribute attribute = super.getAttribute(result);
//                if (null != attribute) {
//                    NamingEnumeration<?> values = null;
//                    try {
//                        values = attribute.getAll();
//                        while (values.hasMoreElements()) {
//                            String value = (String)values.nextElement();
//                            if (null != value && OBJECT_CLASS.equalsIgnoreCase(value)) {
//                                return Boolean.TRUE;
//                            }
//                        }
//                    } catch (NamingException e) {
//                        throw LdapExceptionCodes.ERROR_GETTING_ATTRIBUTE.create(e, e.getMessage());
//                    } finally {
//                        Tools.close(values);
//                    }
//                }
//                return Boolean.FALSE;
//            }
//
//            @Override
//            protected String encode(Boolean value, LdapIDResolver idResolver) throws OXException {
//                if (value.booleanValue()) {
//                    return OBJECT_CLASS;
//                } else {
//                    throw new UnsupportedOperationException("unable to encode mark as distribution list");
//                }
//            }
        });

        mappings.put(ContactField.NUMBER_OF_ATTACHMENTS, new LdapIntegerMapping() {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setNumberOfAttachments(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsNumberOfAttachments();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getNumberOfAttachments();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeNumberOfAttachments();
            }
        });

        mappings.put(ContactField.YOMI_FIRST_NAME, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setYomiFirstName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsYomiFirstName();
            }

            @Override
            public String get(Contact contact) {
                return contact.getYomiFirstName();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeYomiFirstName();
            }
        });

        mappings.put(ContactField.YOMI_LAST_NAME, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setYomiLastName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsYomiLastName();
            }

            @Override
            public String get(Contact contact) {
                return contact.getYomiLastName();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeYomiLastName();
            }
        });

        mappings.put(ContactField.YOMI_COMPANY, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setYomiCompany(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsYomiCompany();
            }

            @Override
            public String get(Contact contact) {
                return contact.getYomiCompany();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeYomiCompany();
            }
        });

        mappings.put(ContactField.NUMBER_OF_IMAGES, new LdapIntegerMapping() {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setNumberOfImages(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                //TODO: create Contact.containsNumberOfImages() method
                return contact.containsImage1();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getNumberOfImages();
            }

            @Override
            public void remove(Contact contact) {
                contact.setNumberOfImages(0);
            }

            @Override
            public Integer get(LdapResult result, LdapIDResolver idResolver) throws OXException {
                /*
                 * mapping determines it's property value based on the presence of an ldap image
                 */
                Object value = getValue(result);
                return null != value && byte[].class.isInstance(value) ? Integer.valueOf(1) : null;
            }
        });

        mappings.put(ContactField.HOME_ADDRESS, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setAddressHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsAddressHome();
            }

            @Override
            public String get(Contact contact) {
                return contact.getAddressHome();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeAddressHome();
            }
        });

        mappings.put(ContactField.BUSINESS_ADDRESS, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setAddressBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsAddressBusiness();
            }

            @Override
            public String get(Contact contact) {
                return contact.getAddressBusiness();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeAddressBusiness();
            }
        });

        mappings.put(ContactField.OTHER_ADDRESS, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setAddressOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsAddressOther();

            }

            @Override
            public String get(Contact contact) {
                return contact.getAddressOther();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeAddressOther();
            }
        });

        mappings.put(ContactField.UID, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setUid(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUid();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUid();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUid();
            }

            @Override
            public String get(LdapResult result, LdapIDResolver idResolver) throws OXException {
                Object value = super.getValue(result);
                if (null != value) {
                    if (byte[].class.isInstance(value) && UUIDs.UUID_BYTE_LENGTH == ((byte[])value).length) {
                        return UUIDs.toUUID((byte[])value).toString();
                    } else {
                        return value.toString();
                    }
                }
                return null;
            }

        });

        mappings.put(ContactField.FILENAME, new LdapStringMapping() {

            @Override
            public void set(Contact contact, String value) {
                contact.setFilename(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsFilename();
            }

            @Override
            public String get(Contact contact) {
                return contact.getFilename();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeFilename();
            }
        });

        POSSIBLE_MAPPINGS = mappings;
        GENERIC = new LdapMapper(POSSIBLE_MAPPINGS);
    }

    private static ContactField getField(Object value) {
        ContactField field = null;
        if (ContactField.class.isInstance(value)) {
            field = (ContactField)value;
        } else {
            field = getField(value.toString());
        }
        if (null == field) {
            throw new IllegalArgumentException("unable to determine contact field for value: " + value);
        }
        return field;
    }

    private static ContactField getField(String name) {
        try {
            return ContactField.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // try by similarity
            ContactField field = getBySimilarity(name);
            if (null == field) {
                throw e;
            } else {
                return field;
            }
        }
    }

    private static ContactField getBySimilarity(String name) {
        String normalized = name.replaceAll("[_\\. ]", "").toLowerCase();
        for (ContactField field : ContactField.values()) {
            if (field.toString().replaceAll("[_\\. ]", "").toLowerCase().equals(normalized)) {
                return field;
            }
        }
        return null;
    }

}
