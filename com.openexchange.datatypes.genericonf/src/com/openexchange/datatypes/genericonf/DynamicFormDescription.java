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

package com.openexchange.datatypes.genericonf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link DynamicFormDescription}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DynamicFormDescription implements Iterable<FormElement> {

    private final List<FormElement> formElements;

    private final Map<String, FormElement> namedElements = new HashMap<String, FormElement>();

    public DynamicFormDescription() {
        formElements = new ArrayList<FormElement>();
    }

    @Override
    public Iterator<FormElement> iterator() {
        return formElements.iterator();
    }

    public List<FormElement> getFormElements() {
        return Collections.unmodifiableList(formElements);
    }

    public void addFormElement(FormElement formElement) {
        formElements.add(formElement);
        namedElements.put(formElement.getName(), formElement);
    }

    public void removeFormElement(FormElement formElement) {
        formElements.remove(formElement);
        namedElements.remove(formElement.getName());
    }

    public DynamicFormDescription add(FormElement formElement) {
        addFormElement(formElement);
        return this;
    }

    public List<Object> doSwitch(WidgetSwitcher switcher, Object... args) {
        List<Object> retvals = new ArrayList<Object>(formElements.size());
        for (FormElement element : formElements) {
            retvals.add(element.doSwitch(switcher, args));
        }
        return retvals;
    }

    public void iterate(DynamicFormIterator iterator, Map<String, Object> content) {
        for (FormElement element : formElements) {
            try {
                String name = element.getName();
                if (content.containsKey(name)) {
                    iterator.handle(element, content.get(name));
                }
            } catch (IterationBreak e) {
                return;
            }
        }
    }

    public Set<String> getMissingMandatoryFields(Map<String, Object> content) {
        Set<String> missing = new HashSet<String>();
        for (FormElement element : formElements) {
            if (element.isMandatory() && !content.containsKey(element.getName())) {
                missing.add(element.getName());
            }
        }
        return missing;
    }

    public FormElement getField(String col) {
        return namedElements.get(col);
    }

}
