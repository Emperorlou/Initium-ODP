package com.universeprojects.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * This is a base class, for a page controller
 *
 * Brief description of the system:
 *  - A page is implemented via a combination of an underlying "controller" class, and a matching JSP file
 *  - The controllers contain all the Java code, and the JSPs contain all of the markup-related stuff (HTML, CSS, etc)
 *
 * Example of a simple scenario, for a page that lists the registered members on a website:
 *  - The page is identified with a simple string identifier, "members"
 *  - The JSP has a name that matches the page identifier, "members.jsp"
 *  - The controller extends this class, and is named MembersController
 *
 */
public abstract class PageController {

    protected final String pageName;

    /**
     * Creates an instance of a controller for a page in the web-application.<br/>
     * The controller is in charge of pre-processing requests, before the actual page is displayed.
     *
     * @param pageName The name of the page (unique identifier), for example - "news"
     *
     */
    protected PageController(String pageName) {
        if (Strings.isEmpty(pageName)) {
            throw new IllegalArgumentException("Page name can't be empty");
        }
        if (!PageControllerFilter.isValidPageName(pageName)) {
            throw new IllegalArgumentException("Invalid page name: " + Strings.inQuotes(pageName));
        }

        this.pageName = pageName;
    }

    /**
     * Returns the page name registered with this controller
     */
    protected String getPageName() {
        return pageName;
    }

    private ThreadLocal<HttpServletRequest> threadLocalRequest = new ThreadLocal<>();
    private ThreadLocal<HttpServletResponse> threadLocalResponse = new ThreadLocal<>();
    private ThreadLocal<ServletContext> threadLocalServletContext = new ThreadLocal<>();


    void setupThreadLocal(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
        threadLocalRequest.set(request);
        threadLocalResponse.set(response);
        threadLocalServletContext.set(servletContext);
    }

    void clearThreadLocal() {
        threadLocalRequest.set(null);
        threadLocalResponse.set(null);
        threadLocalServletContext.set(null);
    }

    protected HttpServletRequest getRequest() {
        return threadLocalRequest.get();
    }

    protected HttpServletResponse getResponse() {
        return threadLocalResponse.get();
    }

    protected ServletContext getServletContext() {return threadLocalServletContext.get(); }

    /**
     * (To be implemented by child class)
     * Handles a GET request, to load the page
     *
     * @return - If processing the request establishes that we would like to proceed to display the page,
     *         this method returns the resource address of the corresponding JSP. For example, "/WEB-INF/pages/news.jsp"<br/>
     *         It's recommended to store your JSPs in the WEB-INF directory in order to prevent direct access from the browser
     *         <p>
     *         - If processing the request establishes that we don't want to display the page, this method returns NULL.
     *         For example, in the event of a redirect to another URL or an HTTP error.
     *         These instructions are expected to already be applied to the request object.
     *
     */
    protected abstract String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

}
