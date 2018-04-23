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

package com.openexchange.report.appsuite.jobs;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.ldap.User;
import com.openexchange.report.appsuite.ContextReport;
import com.openexchange.report.appsuite.ReportContextHandler;
import com.openexchange.report.appsuite.ReportExceptionCodes;
import com.openexchange.report.appsuite.ReportService;
import com.openexchange.report.appsuite.ReportUserHandler;
import com.openexchange.report.appsuite.UserReport;
import com.openexchange.report.appsuite.UserReportCumulator;
import com.openexchange.report.appsuite.internal.ReportProperties;
import com.openexchange.report.appsuite.internal.Services;
import com.openexchange.report.appsuite.serialization.Report;
import com.openexchange.user.UserService;

/**
 * The {@link AnalyzeContextBatch} class is the workhorse of the reporting system. It runs the reports on a batch of
 * context ids.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 */
public class AnalyzeContextBatch implements Callable<Integer>, Serializable {

    private static final long serialVersionUID = -578253218760102061L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AnalyzeContextBatch.class);

    private final String uuid;
    private String reportType;
    private List<Integer> contextIds;
    private final Report report;

    /**
     *
     * Initializes a new {@link AnalyzeContextBatch} with a report instead of just a report-type.
     * This means, additional options are selected and stored in the report object.
     *
     * @param uuid The uuid of the report we're running
     * @param reportType The type of report that is being run
     * @param chunk a list of context IDs to analyze
     */
    public AnalyzeContextBatch(String uuid, Report report, List<Integer> chunk) {
        super();
        this.uuid = uuid;
        this.report = report;
        this.reportType = report.getType();
        this.contextIds = chunk;
    }

    @Override
    public Integer call() throws Exception {
        LOG.debug("Starting context processing ot one schema, for report with uuid: " + this.uuid + " and context amount: " + this.contextIds.size());
        Thread currentThread = Thread.currentThread();
        int previousPriority = currentThread.getPriority();
        currentThread.setPriority(ReportProperties.getThreadPriority());

        try {
            if (reportType == null) {
                reportType = "default";
            }

            for (int i = contextIds.size(); (!currentThread.isInterrupted()) && (i-- > 0);) {
                Integer ctxId = contextIds.get(i);
                ReportService reportService = Services.getService(ReportService.class);
                ContextReport contextReport = null;
                try {
                    Context ctx = loadContext(ctxId);
                    contextReport = new ContextReport(uuid, reportType, ctx);

                    handleContext(contextReport);
                    handleUsersGuestsLinks(ctx, contextReport);

                    reportService.finishContext(contextReport);
                } catch (OXException oxException) {
                    if (ContextExceptionCodes.UPDATE.equals(oxException)) {
                        reportService.abortGeneration(uuid, reportType, "Not all schemas are up to date! Please ensure schema up-to-dateness (e. g. by calling 'runupdate' CLT).");
                        break;
                    }
                    if (ReportExceptionCodes.REPORT_GENERATION_CANCELED.equals(oxException)) {
                        LOG.info("Stop execution of report generation due to an user instruction!" , oxException);
                        contextIds = Collections.emptyList();
                        reportService.abortGeneration(uuid, reportType, "Cancelled report generation based on user interaction.");
                        break;
                    }
                    LOG.error("Exception thrown while loading context. Skip report for context {}. Move to next context", ctxId, oxException);
                    reportService.abortContextReport(uuid, reportType);
                    this.report.addError(oxException);
                    continue;
                } catch (Exception e) {
                    LOG.error("Unexpected error while context report generation!", e);
                    reportService.abortContextReport(uuid, reportType);
                }
            }
        } finally {
            currentThread.setPriority(previousPriority);
        }
        return contextIds.size();
    }

    /**
     * Run the report for the given context, to get/set all relevant data.
     *
     * @param contextReport
     * @throws OXException
     */
    private void handleContext(ContextReport contextReport) {
        // Run all Context Analyzers that apply to this reportType
        for (ReportContextHandler contextHandler : Services.getContextHandlers()) {
            if (contextHandler.appliesTo(reportType)) {
                contextHandler.runContextReport(contextReport);
            }
        }
    }

    /**
     * Handles users for the given context
     *
     * @param ctx
     * @param contextReport
     * @throws OXException
     */
    private void handleUsersGuestsLinks(Context ctx, ContextReport contextReport) throws OXException {
        // Next, let's look at all the users in this context
        User[] loadUsers = loadUsers(ctx);

        for (User user : loadUsers) {
            boolean skip = false;
            UserReport userReport = new UserReport(uuid, reportType, ctx, user, contextReport);
            // Are extended options available?
            if (this.report != null) {
                userReport.setReportConfig(this.report.getReportConfig());
                //Add user to context
                contextReport.getUserList().add(user.getId());
                contextReport.setReportConfig(this.report.getReportConfig());
            }
            // Run User Analyzers
            for (ReportUserHandler userHandler : Services.getUserHandlers()) {
                if (userHandler.appliesTo(reportType)) {
                    try {
                        userHandler.runUserReport(userReport);
                    } catch (OXException e) {
                        LOG.error("", e);
                        contextReport.getUserList().remove((Integer) user.getId());
                        skip = true;
                        if (this.report != null) {
                            this.report.addError(e);
                        }
                    }
                }
            }
            if (skip) {
                continue;
            }
            // Compact User Analysis and add to context report
            for (UserReportCumulator cumulator : Services.getUserReportCumulators()) {
                if (cumulator.appliesTo(reportType)) {
                    cumulator.merge(userReport, contextReport);
                }
            }
        }

    }

    protected User[] loadUsers(Context ctx) throws OXException {
        return Services.getService(UserService.class).getUser(ctx, true, false);
    }

    protected Context loadContext(int contextId) throws OXException {
        return Services.getService(ContextService.class).getContext(contextId);
    }
}
