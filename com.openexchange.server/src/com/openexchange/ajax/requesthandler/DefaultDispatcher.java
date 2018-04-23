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

package com.openexchange.ajax.requesthandler;

import static com.openexchange.ajax.AJAXServlet.PARAMETER_ALLOW_ENQUEUE;
import static com.openexchange.ajax.requesthandler.AJAXRequestDataTools.parseBoolParameter;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.exception.ExceptionUtils;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult.ResultType;
import com.openexchange.ajax.requesthandler.EnqueuableAJAXActionService.Result;
import com.openexchange.ajax.requesthandler.jobqueue.DefaultJob;
import com.openexchange.ajax.requesthandler.jobqueue.EnqueuedException;
import com.openexchange.ajax.requesthandler.jobqueue.JobKey;
import com.openexchange.ajax.requesthandler.jobqueue.JobQueueExceptionCodes;
import com.openexchange.ajax.requesthandler.jobqueue.JobQueueService;
import com.openexchange.continuation.Continuation;
import com.openexchange.continuation.ContinuationException;
import com.openexchange.continuation.ContinuationExceptionCodes;
import com.openexchange.continuation.ContinuationRegistryService;
import com.openexchange.continuation.ContinuationResponse;
import com.openexchange.exception.OXException;
import com.openexchange.framework.request.DefaultRequestContext;
import com.openexchange.framework.request.RequestContext;
import com.openexchange.framework.request.RequestContextHolder;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogProperties;
import com.openexchange.net.ssl.config.UserAwareSSLConfigurationService;
import com.openexchange.net.ssl.exception.SSLExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DefaultDispatcher} - The default {@link Dispatcher dispatcher} implementation.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DefaultDispatcher implements Dispatcher {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultDispatcher.class);

    private final DispatcherListenerRegistry listenerRegistry;

    private final ConcurrentMap<StrPair, Boolean> fallbackSessionActionsCache;
    private final ConcurrentMap<StrPair, Boolean> publicSessionAuthCache;
    private final ConcurrentMap<StrPair, Boolean> omitSessionActionsCache;
    private final ConcurrentMap<StrPair, Boolean> noSecretCallbackCache;
    private final ConcurrentMap<StrPair, Boolean> preferStreamCache;
    private final ConcurrentMap<StrPair, Boolean> enqueueableCache;

    private final ConcurrentMap<String, AJAXActionServiceFactory> actionFactories;
    private final Queue<AJAXActionCustomizerFactory> customizerFactories;
    private final Queue<AJAXActionAnnotationProcessor> annotationProcessors;

    /**
     * Initializes a new {@link DefaultDispatcher}.
     */
    public DefaultDispatcher() {
        this(null);
    }

    /**
     * Initializes a new {@link DefaultDispatcher}.
     */
    public DefaultDispatcher(DispatcherListenerRegistry listenerRegistry) {
        super();
        fallbackSessionActionsCache = new ConcurrentHashMap<StrPair, Boolean>(128, 0.9f, 1);
        publicSessionAuthCache = new ConcurrentHashMap<StrPair, Boolean>(128, 0.9f, 1);
        omitSessionActionsCache = new ConcurrentHashMap<StrPair, Boolean>(128, 0.9f, 1);
        noSecretCallbackCache = new ConcurrentHashMap<StrPair, Boolean>(128, 0.9f, 1);
        preferStreamCache = new ConcurrentHashMap<StrPair, Boolean>(128, 0.9f, 1);
        enqueueableCache = new ConcurrentHashMap<StrPair, Boolean>(128, 0.9f, 1);

        actionFactories = new ConcurrentHashMap<String, AJAXActionServiceFactory>(64, 0.9f, 1);
        customizerFactories = new ConcurrentLinkedQueue<AJAXActionCustomizerFactory>();
        annotationProcessors = new ConcurrentLinkedQueue<AJAXActionAnnotationProcessor>();

        this.listenerRegistry = null == listenerRegistry ? DispatcherListenerRegistry.NOOP_REGISTRY : listenerRegistry;
    }

    @Override
    public AJAXState begin() throws OXException {
        return new AJAXState();
    }

    @Override
    public void end(final AJAXState state) {
        if (null != state) {
            state.close();
        }
    }

    @Override
    public boolean handles(final String module) {
        return actionFactories.containsKey(module);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final AJAXState state, final ServerSession session) throws OXException {
        if (null == session) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_SESSION);
        }

        addLogProperties(requestData, false);
        List<AJAXActionCustomizer> customizers = determineCustomizers(requestData, session);
        try {
            // Customize request data
            AJAXRequestData modifiedRequestData = customizeRequest(requestData, customizers, session);

            // Set request context
            RequestContext requestContext = buildRequestContext(modifiedRequestData);
            RequestContextHolder.set(requestContext);

            // Determine action factory and yield an action executing the request
            AJAXActionServiceFactory factory = lookupFactory(modifiedRequestData.getModule());
            if (factory == null) {
                throw AjaxExceptionCodes.UNKNOWN_MODULE.create(modifiedRequestData.getModule());
            }

            AJAXActionService action = factory.createActionService(modifiedRequestData.getAction());
            if (action == null) {
                throw AjaxExceptionCodes.UNKNOWN_ACTION_IN_MODULE.create(modifiedRequestData.getAction(), modifiedRequestData.getModule());
            }

            // Load request body if stream is not preferred
            if (false == preferStream(modifiedRequestData.getModule(), modifiedRequestData.getAction(), action)) {
                AJAXRequestDataTools.loadRequestBody(modifiedRequestData);
            }

            // Validate request headers for caching
            {
                AJAXRequestResult etagResult = checkResultNotModified(action, modifiedRequestData, session);
                if (etagResult != null) {
                    return etagResult;
                }
            }

            // Validate request headers for resume
            {
                AJAXRequestResult failedResult = checkRequestPreconditions(action, modifiedRequestData, session);
                if (failedResult != null) {
                    return failedResult;
                }
            }

            // Check for action annotations
            for (AJAXActionAnnotationProcessor annotationProcessor : annotationProcessors) {
                if (annotationProcessor.handles(action)) {
                    annotationProcessor.process(action, modifiedRequestData, session);
                }
            }

            if (parseBoolParameter(PARAMETER_ALLOW_ENQUEUE, modifiedRequestData)) {
                // Check if action service is enqueue-able
                Result enqueueableResult = enqueueable(modifiedRequestData.getModule(), modifiedRequestData.getAction(), action, modifiedRequestData, session);
                if (enqueueableResult.isEnqueueable()) {
                    // Enqueue as dispatcher job and watch it
                    JobQueueService jobQueue = ServerServiceRegistry.getInstance().getService(JobQueueService.class);
                    if (null != jobQueue) {
                        // Check for already running job of that kind
                        JobKey optionalKey = enqueueableResult.getOptionalKey();
                        if (optionalKey != null) {
                            UUID contained = jobQueue.contains(optionalKey);
                            if (null != contained) {
                                throw JobQueueExceptionCodes.ALREADY_RUNNING.create(UUIDs.getUnformattedString(contained), session.getUserId(), session.getContextId());
                            }
                        }

                        // Build job
                        DefaultJob.Builder job = DefaultJob.builder(true, this)
                            .setAction(action)
                            .setCustomizers(customizers)
                            .setFactory(factory)
                            .setRequestContext(requestContext)
                            .setRequestData(modifiedRequestData)
                            .setSession(session)
                            .setState(state)
                            .setKey(optionalKey);

                        // Enqueue (if not computed in time)
                        try {
                            long maxRequestAgeMillis = jobQueue.getMaxRequestAgeMillis();
                            return jobQueue.enqueueAndWait(job.build(), maxRequestAgeMillis, TimeUnit.MILLISECONDS).get(true);
                        } catch (InterruptedException e) {
                            // Keep interrupted state
                            Thread.currentThread().interrupt();
                            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, "Interrupted");
                        } catch (EnqueuedException e) {
                            // Result not computed in time
                            LOG.debug("Action \"{}\" of module \"{}\" could not be executed in time for user {} in context {}.", requestData.getAction(), requestData.getModule(), I(session.getUserId()), I(session.getContextId()), e);
                            return new AJAXRequestResult(e.getJobInfo(), "enqueued");
                        }
                    }
                }
            }

            return doPerform(action, factory, modifiedRequestData, state, customizers, session);
        } catch (OXException e) {
            for (AJAXActionCustomizer customizer : customizers) {
                if (customizer instanceof AJAXExceptionHandler) {
                    try {
                        ((AJAXExceptionHandler) customizer).exceptionOccurred(requestData, e, session);
                    } catch (Exception x) {
                        // Discard. Not our problem, we need to get on with this!
                    }
                }
            }
            if (SSLExceptionCode.PREFIX.equals(e.getPrefix())) {
                UserAwareSSLConfigurationService userAwareSSLConfigurationService = ServerServiceRegistry.getInstance().getService(UserAwareSSLConfigurationService.class);
                if (null != userAwareSSLConfigurationService) {
                    if (userAwareSSLConfigurationService.isAllowedToDefineTrustLevel(session.getUserId(), session.getContextId())) {
                        throw SSLExceptionCode.UNTRUSTED_CERT_USER_CONFIG.create(e.getDisplayArgs()[0]);
                    }
                }
            }
            // MW-445: Try to unpack and throw the OXException
            if (e.getCause() instanceof SSLException) {
                Throwable t = ExceptionUtils.getRootCause(e);
                if (t instanceof OXException) {
                    OXException oxe = (OXException) t;
                    throw oxe;
                }
            }
            throw e;
        } catch (RuntimeException e) {
            if ("org.mozilla.javascript.WrappedException".equals(e.getClass().getName())) {
                // Handle special Rhino wrapper error
                Throwable wrapped = e.getCause();
                if (wrapped instanceof OXException) {
                    throw (OXException) wrapped;
                }
            }

            // Wrap unchecked exception
            addLogProperties(requestData, true);
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            RequestContextHolder.reset();
        }
    }

    /**
     * Checks if given action wants to read data form possibly available request body stream by itself.
     *
     * @param module The module identifier
     * @param action The action identifier
     * @param actionService The action service to perform
     * @return <code>true</code> if stream is preferred; otherwise <code>false</code>
     */
    private boolean preferStream(String module, String action, AJAXActionService actionService) {
        StrPair key = new StrPair(module, action);
        Boolean ret = preferStreamCache.get(key);
        if (null == ret) {
            final DispatcherNotes actionMetadata = getActionMetadata(actionService);
            ret = actionMetadata == null ? Boolean.FALSE : Boolean.valueOf(actionMetadata.preferStream());
            preferStreamCache.put(key, ret);
        }
        return ret.booleanValue();
    }

    /**
     * Checks if given action is enqueue-able.
     *
     * @param module The module identifier
     * @param action The action identifier
     * @param actionService The action service to perform
     * @param requestData The AJAX request data
     * @param session The associated session
     * @return <code>true</code> if enqueue-able; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    private EnqueuableAJAXActionService.Result enqueueable(String module, String action, AJAXActionService actionService, AJAXRequestData requestData, ServerSession session) throws OXException {
        // Check by action service instance
        if (actionService instanceof EnqueuableAJAXActionService) {
            return ((EnqueuableAJAXActionService) actionService).isEnqueueable(requestData, session);
        }

        // Check by dispatcher annotation
        StrPair key = new StrPair(module, action);
        Boolean ret = enqueueableCache.get(key);
        if (null == ret) {
            final DispatcherNotes actionMetadata = getActionMetadata(actionService);
            ret = actionMetadata == null ? Boolean.FALSE : Boolean.valueOf(actionMetadata.enqueueable());
            enqueueableCache.put(key, ret);
        }
        return EnqueuableAJAXActionServices.resultFor(ret.booleanValue());
    }

    private RequestContext buildRequestContext(AJAXRequestData requestData) throws OXException {
        HostData hostData = requestData.getHostData();
        if (hostData == null) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("Host data was null. AJAX request data has not been initialized correctly!");
        }

        DefaultRequestContext context = new DefaultRequestContext();
        context.setHostData(hostData);
        context.setUserAgent(requestData.getUserAgent());
        return context;
    }

    /**
     * Determines all {@link AJAXActionCustomizer} instances that can potentially modify the request data.
     *
     * @param requestData The request data
     * @param session The session
     * @return A list of customizers meant to be called for the request object.
     */
    private List<AJAXActionCustomizer> determineCustomizers(AJAXRequestData requestData, ServerSession session) {
        /*
         * Create customizers
         */
        List<AJAXActionCustomizer> todo = new ArrayList<AJAXActionCustomizer>(4);
        for (AJAXActionCustomizerFactory customizerFactory : customizerFactories) {
            AJAXActionCustomizer customizer = customizerFactory.createCustomizer(requestData, session);
            if (customizer != null) {
                todo.add(customizer);
            }
        }
        return todo;
    }

    /**
     * Customizes a request by calling {@link AJAXActionCustomizer#incoming(AJAXRequestData, ServerSession)} on every
     * passed customizer with the given request data. After this call returns, the list of customizers contains all
     * instances that need to be called after the requests action was performed.
     *
     * @param requestData The request data
     * @param customizers The customizers to call. Must be mutable.
     * @param session The session
     * @return The (potentially) modified request data
     * @throws OXException
     */
    private AJAXRequestData customizeRequest(AJAXRequestData requestData, List<AJAXActionCustomizer> customizers, ServerSession session) throws OXException {
        /*
         * Iterate customizers for AJAXRequestData
         */
        AJAXRequestData modifiedRequestData = requestData;
        List<AJAXActionCustomizer> outgoing = new ArrayList<AJAXActionCustomizer>(4);
        while (!customizers.isEmpty()) {
            final Iterator<AJAXActionCustomizer> iterator = customizers.iterator();
            while (iterator.hasNext()) {
                final AJAXActionCustomizer customizer = iterator.next();
                try {
                    final AJAXRequestData modified = customizer.incoming(modifiedRequestData, session);
                    if (modified != null) {
                        modifiedRequestData = modified;
                    }
                    outgoing.add(customizer);
                    iterator.remove();
                } catch (final FlowControl.Later l) {
                    // Remains in list and is therefore retried
                }
            }
        }

        customizers.clear();
        customizers.addAll(outgoing);
        return modifiedRequestData;
    }

    /**
     * Checks if potential HTTP preconditions are fulfilled. Namely the following headers are checked:
     * <ul>
     * <li>If-Match</li>
     * <li>If-Unmodified-Since</li>
     * </ul>
     *
     * For every header that is part of the request and supported by the given action, the precondition is
     * checked.
     *
     * @param action The target action for the given request
     * @param requestData The request data
     * @param session The session
     * @return <code>null</code> if the request shall be processed normally. If a precondition fails, an
     *         according {@link AJAXRequestResult} with code {@link HttpServletResponse#SC_PRECONDITION_FAILED} is returned,
     *         which should be directly written out to the client.
     */
    private AJAXRequestResult checkRequestPreconditions(AJAXActionService action, AJAXRequestData requestData, ServerSession session) throws OXException {
        // If-Match header should contain "*" or ETag. If not, then return 412.
        String ifMatch = requestData.getHeader("If-Match");
        if (ifMatch != null && (action instanceof ETagAwareAJAXActionService) && (("*".equals(ifMatch)) || ((ETagAwareAJAXActionService) action).checkETag(ifMatch, requestData, session))) {
            final AJAXRequestResult failedResult = new AJAXRequestResult();
            failedResult.setHttpStatusCode(HttpServletResponse.SC_PRECONDITION_FAILED);
            return failedResult;
        }

        // If-Unmodified-Since header should be greater than LastModified. If not, then return 412.
        if (action instanceof LastModifiedAwareAJAXActionService) {
            long ifUnmodifiedSince = Tools.optHeaderDate(requestData.getHeader("If-Unmodified-Since"));
            if (ifUnmodifiedSince >= 0 && ((LastModifiedAwareAJAXActionService) action).checkLastModified(ifUnmodifiedSince + 1000, requestData, session)) {
                final AJAXRequestResult failedResult = new AJAXRequestResult();
                failedResult.setHttpStatusCode(HttpServletResponse.SC_PRECONDITION_FAILED);
                return failedResult;
            }
        }

        return null;
    }

    /**
     * Checks if the requested result has not changed since the last request in terms of HTTP caching headers.
     * Namely the following headers are checked:
     * <ul>
     * <li>If-None-Match</li>
     * <li>If-Modified-Since</li>
     * </ul>
     *
     * @param action The target action for the given request
     * @param requestData The request data
     * @param session The session
     * @return An {@link AJAXRequestResult} with {@link ResultType#ETAG}, that causes a <code>304 Not Modified</code> with
     *         no further processing of the request. If checks against those headers are not supported by the underlying action or
     *         if the result has changed since the last request <code>null</code> is returned.
     * @throws OXException
     */
    private AJAXRequestResult checkResultNotModified(AJAXActionService action, AJAXRequestData requestData, ServerSession session) throws OXException {
        // If-None-Match header should contain "*" or ETag. If so, then return 304.
        final String eTag = requestData.getETag();
        if (null != eTag && (action instanceof ETagAwareAJAXActionService) && (("*".equals(eTag)) || ((ETagAwareAJAXActionService) action).checkETag(eTag, requestData, session))) {
            final AJAXRequestResult etagResult = new AJAXRequestResult();
            etagResult.setType(AJAXRequestResult.ResultType.ETAG);
            final long newExpires = requestData.getExpires();
            if (newExpires > 0) {
                etagResult.setExpires(newExpires);
            }
            return etagResult;
        }

        // If-Modified-Since header should be greater than LastModified. If so, then return 304.
        // This header is ignored if any If-None-Match header is specified.
        if (null == eTag && (action instanceof LastModifiedAwareAJAXActionService)) {
            final long lastModified = requestData.getLastModified();
            if (lastModified >= 0 && ((LastModifiedAwareAJAXActionService) action).checkLastModified(lastModified + 1000, requestData, session)) {
                final AJAXRequestResult etagResult = new AJAXRequestResult();
                etagResult.setType(AJAXRequestResult.ResultType.ETAG);
                final long newExpires = requestData.getExpires();
                if (newExpires > 0) {
                    etagResult.setExpires(newExpires);
                }
                return etagResult;
            }
        }

        return null;
    }

    // ------------------------------------------------ Execution stuff -------------------------------------------------------

    /**
     * Actually performs this dispatcher task
     *
     * @param action The action to perform
     * @param factory The associated factory
     * @param requestData The request data
     * @param state The option state
     * @param customizers Available customizers
     * @param session The associated session
     * @return The result
     * @throws OXException If handling fails
     */
    public AJAXRequestResult doPerform(AJAXActionService action, AJAXActionServiceFactory factory, AJAXRequestData requestData, AJAXState state, List<AJAXActionCustomizer> customizers, ServerSession session) throws OXException {
        // State already initialized for module?
        if (state != null && factory instanceof AJAXStateHandler) {
            final AJAXStateHandler handler = (AJAXStateHandler) factory;
            if (state.addInitializer(requestData.getModule(), handler)) {
                handler.initialize(state);
            }
        }
        requestData.setState(state);

        // Ensure requested format
        if (requestData.getFormat() == null) {
            requestData.setFormat("apiResponse");
        }

        // Grab applicable dispatcher listeners
        List<DispatcherListener> dispatcherListeners = listenerRegistry.getDispatcherListenersFor(requestData);

        // Perform request
        AJAXRequestResult result = callAction(action, requestData, dispatcherListeners, session);
        if (AJAXRequestResult.ResultType.DIRECT == result.getType()) {
            // No further processing
            return contributeDispatcherListeners(result, dispatcherListeners);
        }
        if (AJAXRequestResult.ResultType.ENQUEUED == result.getType()) {
            // No further processing
            return contributeDispatcherListeners(result, dispatcherListeners);
        }
        result = customizeResult(requestData, result, customizers, session);
        return contributeDispatcherListeners(result, dispatcherListeners);
    }

    private AJAXRequestResult contributeDispatcherListeners(AJAXRequestResult requestResult, List<DispatcherListener> dispatcherListeners) {
        if (null != requestResult && null != dispatcherListeners) {
            requestResult.addPostProcessor(new DispatcherListenerPostProcessor(dispatcherListeners));
        }
        return requestResult;
    }

    /**
     * Finally calls the requested action with the given request data and returns the result.
     *
     * @param action The action to call
     * @param requestData The request data
     * @param optListeners The optional dispatcher listeners that receive call-backs or <code>null</code>
     * @param session The session
     * @return The actions result
     * @throws OXException If action fails to handle the request data
     */
    private AJAXRequestResult callAction(AJAXActionService action, AJAXRequestData requestData, List<DispatcherListener> optListeners, ServerSession session) throws OXException {
        if ((null == optListeners) || (optListeners.isEmpty())) {
            return doCallAction(action, requestData, session);
        }

        AJAXRequestResult result = null;
        Exception exc = null;
        try {
            triggerOnRequestInitialized(requestData, optListeners);
            result = doCallAction(action, requestData, session);
            return result;
        } catch (OXException e) {
            exc = e;
            throw e;
        } catch (RuntimeException e) {
            exc = e;
            throw e;
        } finally {
            triggerOnRequestPerformed(requestData, result, exc, optListeners);
        }
    }

    private AJAXRequestResult doCallAction(AJAXActionService action, AJAXRequestData requestData, ServerSession session) throws OXException {
        AJAXRequestResult result = null;

        try {
            result = action.perform(requestData, session);
            if (null == result) {
                // Huh...?!
                addLogProperties(requestData, true);
                throw AjaxExceptionCodes.UNEXPECTED_RESULT.create(AJAXRequestResult.class.getSimpleName(), "null");
            }
        } catch (final IllegalStateException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final ContinuationException e) {
            result = handleContinuationException(e, session);
        } finally {
            requestData.cleanUploads();
        }

        return result.setRequestData(requestData);
    }

    /**
     * Customizes a result object by calling {@link AJAXActionCustomizer#outgoing(AJAXRequestData, AJAXRequestResult, ServerSession)} on every
     * passed customizer with the given request data and result object.
     *
     * @param requestData The request data
     * @param result The result object
     * @param session The session
     * @return The potentially modified result object
     * @throws OXException
     */
    private AJAXRequestResult customizeResult(AJAXRequestData requestData, AJAXRequestResult result, List<AJAXActionCustomizer> customizers, ServerSession session) throws OXException {
        /*
         * Iterate customizers in reverse oder for request data and result pair
         */
        Collections.reverse(customizers);
        List<AJAXActionCustomizer> outgoing = new LinkedList<AJAXActionCustomizer>(customizers);
        AJAXRequestResult modifiedResult = result;
        while (!outgoing.isEmpty()) {
            final Iterator<AJAXActionCustomizer> iterator = outgoing.iterator();
            while (iterator.hasNext()) {
                final AJAXActionCustomizer customizer = iterator.next();
                try {
                    final AJAXRequestResult modified = customizer.outgoing(requestData, modifiedResult, session);
                    if (modified != null) {
                        modifiedResult = modified;

                        // Check (again) for direct result type
                        if (AJAXRequestResult.ResultType.DIRECT == modifiedResult.getType()) {
                            // No further processing
                            return modifiedResult;
                        }
                    }
                    iterator.remove();
                } catch (final FlowControl.Later l) {
                    // Remains in list and is therefore retried
                }
            }
        }

        return modifiedResult;
    }

    private void triggerOnRequestInitialized(AJAXRequestData requestData, List<DispatcherListener> dispatcherListeners) throws OXException {
        for (DispatcherListener dispatcherListener : dispatcherListeners) {
            dispatcherListener.onRequestInitialized(requestData);
        }
    }

    private void triggerOnRequestPerformed(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e, List<DispatcherListener> dispatcherListeners) throws OXException {
        for (DispatcherListener dispatcherListener : dispatcherListeners) {
            dispatcherListener.onRequestPerformed(requestData, requestResult, e);
        }
    }

    /**
     * Handles specified <code>ContinuationException</code> instance.
     *
     * @param e The exception to handle
     * @param session The associated session
     * @return The AJAX result
     * @throws OXException If <code>ContinuationException</code> does not signal special error code <code>CONTINUATION-0003</code>
     *             (Scheduled for continuation: &lt;uuid&gt;)
     */
    private AJAXRequestResult handleContinuationException(final ContinuationException e, final ServerSession session) throws OXException {
        if (!ContinuationExceptionCodes.SCHEDULED_FOR_CONTINUATION.equals(e)) {
            throw e;
        }
        final UUID uuid = e.getUuid();
        if (null == uuid) {
            throw e;
        }
        final ContinuationRegistryService continuationRegistry = ServerServiceRegistry.getInstance().getService(ContinuationRegistryService.class);
        if (null == continuationRegistry) {
            throw e;
        }
        final Continuation<Object> continuation = continuationRegistry.getContinuation(uuid, session);
        if (null == continuation) {
            throw e;
        }
        try {
            final ContinuationResponse<Object> cr = continuation.getNextResponse(1000, TimeUnit.NANOSECONDS);
            return new AJAXRequestResult(cr.getValue(), cr.getTimeStamp(), cr.getFormat()).setContinuationUuid(cr.isCompleted() ? null : uuid);
        } catch (final InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(ie, ie.getMessage());
        }
    }

    /**
     * Adds request-specific log properties (action and module information) as well as optional query string information.
     *
     * @param requestData The request data
     * @param withQueryString Whether to include query string or not
     */
    private void addLogProperties(final AJAXRequestData requestData, final boolean withQueryString) {
        if (null != requestData) {
            LogProperties.putProperty(LogProperties.Name.AJAX_ACTION, requestData.getAction());
            LogProperties.putProperty(LogProperties.Name.AJAX_MODULE, requestData.getModule());

            if (withQueryString) {
                Map<String, String> parameters = requestData.getParameters();
                StringBuilder sb = new StringBuilder(256);
                sb.append('"');
                boolean first = true;
                for (final Entry<String, String> entry : parameters.entrySet()) {
                    if (first) {
                        sb.append('?');
                        first = false;
                    } else {
                        sb.append('&');
                    }
                    String value = LogProperties.getSanitizedValue(entry.getKey(), entry.getValue());
                    sb.append(entry.getKey()).append('=').append(value);
                }
                sb.append('"');
                LogProperties.putProperty(LogProperties.Name.SERVLET_QUERY_STRING, sb.toString());
            }
        }
    }

    /**
     * Looks-up denoted factory
     *
     * @param module The module to look-up for
     * @return The factory or <code>null</code>
     */
    @Override
    public AJAXActionServiceFactory lookupFactory(final String module) {
        AJAXActionServiceFactory serviceFactory = actionFactories.get(module);
        if (null == serviceFactory) {
            final int pos = module.indexOf('/');
            if (pos > 0) {
                // Fallback for backwards compatibility. File Download Actions sometimes append the filename to the module.
                serviceFactory = actionFactories.get(module.substring(0, pos));
            }
        }
        return serviceFactory;
    }

    private DispatcherNotes getActionMetadata(final AJAXActionService action) {
        return null == action ? null : action.getClass().getAnnotation(DispatcherNotes.class);
    }

    /**
     * Registers specified factory under given module.
     *
     * @param module The module
     * @param factory The factory (possibly annotated with {@link Module})
     */
    public void register(final String module, final AJAXActionServiceFactory factory) {
        synchronized (actionFactories) {
            AJAXActionServiceFactory current = actionFactories.putIfAbsent(module, factory);
            if (null != current) {
                try {
                    current = actionFactories.get(module);
                    final Module moduleAnnotation = current.getClass().getAnnotation(Module.class);
                    if (null == moduleAnnotation) {
                        final StringBuilder sb = new StringBuilder(512).append("There is already a factory associated with module \"");
                        sb.append(module).append("\": ").append(current.getClass().getName());
                        sb.append(". Therefore registration is denied for factory \"").append(factory.getClass().getName());
                        sb.append("\". Unless these two factories provide the \"").append(Module.class.getName()).append("\" annotation to specify what actions are supported by each factory.");
                        LOG.warn(sb.toString());
                    } else {
                        final CombinedActionFactory combinedFactory;
                        if (current instanceof CombinedActionFactory) {
                            combinedFactory = (CombinedActionFactory) current;
                        } else {
                            combinedFactory = new CombinedActionFactory();
                            combinedFactory.add(current);
                            actionFactories.put(module, combinedFactory);
                        }
                        combinedFactory.add(factory);
                    }
                } catch (final IllegalArgumentException e) {
                    LOG.error(e.getMessage());
                }
            }
        }
    }

    /**
     * Adds specified customizer factory.
     *
     * @param factory The customizer factory
     */
    public void addCustomizer(final AJAXActionCustomizerFactory factory) {
        this.customizerFactories.add(factory);
    }

    /**
     * Removes the specified customizer factory
     *
     * @param factory The customizer factory
     */
    public void removeCustomizer(AJAXActionCustomizerFactory factory) {
        this.customizerFactories.remove(factory);
    }

    /**
     * Releases specified factory from given module.
     *
     * @param module The module
     * @param factory The factory (possibly annotated with {@link Module})
     */
    public void remove(final String module, final AJAXActionServiceFactory factory) {
        synchronized (actionFactories) {
            final AJAXActionServiceFactory removed = actionFactories.remove(module);
            if (removed instanceof CombinedActionFactory) {
                final CombinedActionFactory combinedFactory = (CombinedActionFactory) removed;
                combinedFactory.remove(factory);
                if (!combinedFactory.isEmpty()) {
                    actionFactories.put(module, combinedFactory);
                }
            }
        }
    }

    /**
     * Adds an {@link AJAXActionAnnotationProcessor}.
     *
     * @param processor The processor
     */
    public void addAnnotationProcessor(AJAXActionAnnotationProcessor processor) {
        if (!annotationProcessors.contains(processor)) {
            annotationProcessors.add(processor);
        }
    }

    /**
     * Removes an {@link AJAXActionAnnotationProcessor}.
     *
     * @param processor The processor
     */
    public void removeAnnotationProcessor(AJAXActionAnnotationProcessor processor) {
        annotationProcessors.remove(processor);
    }

    private AJAXActionService getActionServiceSafe(final String action, final AJAXActionServiceFactory factory) {
        try {
            return factory.createActionService(action);
        } catch (final Exception e) {
            LOG.trace("Failed to create action {} from factory {}", action, factory.getClass().getName(), e);
            return null;
        }
    }

    @Override
    public boolean mayUseFallbackSession(final String module, final String action) throws OXException {
        final StrPair key = new StrPair(module, action);
        Boolean ret = fallbackSessionActionsCache.get(key);
        if (null == ret) {
            final AJAXActionServiceFactory factory = lookupFactory(module);
            if (factory == null) {
                return false;
            }
            final DispatcherNotes actionMetadata = getActionMetadata(getActionServiceSafe(action, factory));
            ret = actionMetadata == null ? Boolean.FALSE : Boolean.valueOf(actionMetadata.allowPublicSession());
            fallbackSessionActionsCache.put(key, ret);
        }
        return ret.booleanValue();
    }

    @Override
    public boolean mayPerformPublicSessionAuth(final String module, final String action) throws OXException {
        final StrPair key = new StrPair(module, action);
        Boolean ret = publicSessionAuthCache.get(key);
        if (null == ret) {
            final AJAXActionServiceFactory factory = lookupFactory(module);
            if (factory == null) {
                return false;
            }
            final DispatcherNotes actionMetadata = getActionMetadata(getActionServiceSafe(action, factory));
            ret = actionMetadata == null ? Boolean.FALSE : Boolean.valueOf(actionMetadata.publicSessionAuth());
            publicSessionAuthCache.put(key, ret);
        }
        return ret.booleanValue();
    }

    @Override
    public boolean mayOmitSession(final String module, final String action) throws OXException {
        final StrPair key = new StrPair(module, action);
        Boolean ret = omitSessionActionsCache.get(key);
        if (null == ret) {
            final AJAXActionServiceFactory factory = lookupFactory(module);
            if (factory == null) {
                return false;
            }
            final DispatcherNotes actionMetadata = getActionMetadata(getActionServiceSafe(action, factory));
            ret = actionMetadata == null ? Boolean.FALSE : Boolean.valueOf(actionMetadata.noSession());
            omitSessionActionsCache.put(key, ret);
        }
        return ret.booleanValue();
    }

    @Override
    public boolean noSecretCallback(String module, String action) throws OXException {
        final StrPair key = new StrPair(module, action);
        Boolean ret = noSecretCallbackCache.get(key);
        if (null == ret) {
            final AJAXActionServiceFactory factory = lookupFactory(module);
            if (factory == null) {
                ret = Boolean.FALSE;
            } else {
                final DispatcherNotes actionMetadata = getActionMetadata(getActionServiceSafe(action, factory));
                ret = actionMetadata == null ? Boolean.FALSE : Boolean.valueOf(actionMetadata.noSecretCallback());
            }
            noSecretCallbackCache.put(key, ret);
        }
        return ret.booleanValue();
    }

    private static final class StrPair {

        private final String str1;
        private final String str2;
        private final int hash;

        StrPair(String str1, String str2) {
            super();
            this.str1 = str1;
            this.str2 = str2;
            final int prime = 31;
            int result = 1;
            result = prime * result + ((str1 == null) ? 0 : str1.hashCode());
            result = prime * result + ((str2 == null) ? 0 : str2.hashCode());
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof StrPair)) {
                return false;
            }
            StrPair other = (StrPair) obj;
            if (str1 == null) {
                if (other.str1 != null) {
                    return false;
                }
            } else if (!str1.equals(other.str1)) {
                return false;
            }
            if (str2 == null) {
                if (other.str2 != null) {
                    return false;
                }
            } else if (!str2.equals(other.str2)) {
                return false;
            }
            return true;
        }

    }// End of class Strings

}
