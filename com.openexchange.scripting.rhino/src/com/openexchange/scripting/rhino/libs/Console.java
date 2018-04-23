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

package com.openexchange.scripting.rhino.libs;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import org.slf4j.LoggerFactory;
import com.openexchange.scripting.rhino.JSON;

public class Console {

	public static void initialize(Scriptable scope, String name) {
		try {
			Context.enter();
			ScriptableObject.putProperty(scope, "console", Context.javaToJS(new Console(name), scope));
		} finally {
			Context.exit();
		}

	}

	private org.slf4j.Logger log = null;

	public Console(String def) {
		log = LoggerFactory.getLogger(def);
	}

	public void log(Object... values) {
		log.info(toString(values));
	}

	public void warn(Object... values) {
		log.warn(toString(values));
	}

	public void debug(Object... values) {
		log.debug(toString(values));
	}

	public void error(Object... values) {
		log.error(toString(values));
	}

	public void fatal(Object... values) {
		log.error(toString(values));
	}

	public void info(Object... values) {
		log.info(toString(values));
	}

	private String toString(Object[] values) {
		if (values == null || values.length == 0) {
			return "";
		}
		StringBuilder b = new StringBuilder();
		for (Object o : values) {
			if (o instanceof Wrapper) {
				o = ((Wrapper) o).unwrap();
			}
			if (o instanceof Scriptable) {
				b.append(JSON.stringify(o));
			}  else if (o instanceof Undefined) {
				b.append("undefined");
			} else {
				b.append(o);
			}
			b.append(", ");
		}
		b.setLength(b.length()-2);
		return b.toString();
	}

}
