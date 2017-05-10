// Copyright (c) 2010 - 2017, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// For more information about licensing and copyright of this software, please contact
// inquiries@clarkparsia.com or visit http://stardog.com

package com.stardog.example.cf.web;

import com.complexible.common.base.Options;
import com.complexible.stardog.StardogException;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;


/**
 * <p>A factory for creating connections to a Stardog database from within
 * a Cloud Foundry environment.</p>
 *
 * <p>This object inspects the environment variable VCAP_SERVICES to discover
 * information that will allow a connection to a Stardog database to be formed.
 * Many connections can be established via a single instantiation of this
 * object but the VCAP_SERVICES environment variable will only be evaluated
 * at construction as it is not expected to change in the process space.
 * </p>
 *
 * @author  John Bresnahan
 * @version 5.0
 */
public class CloudFoundryConnectionFactory {
    private final String mDatabaseName;
    private final String mSdUrl;
    private final String mUserName;
    private final String mPassword;
    private final ObjectMapper mMapper = new ObjectMapper();

    /**
     * Returns a factory that can create Stardog database connections in a
     * Cloud Foundry environment.
     *
     * @param theServiceName   The name given to the Stardog of the Cloud
     *                         Foundry service
     * @param theInstanceName  The name of the specific instance of the service.
     *                         If null the first matching instance will be used.
     * @param thePlanName      The name of the service broker plan.
     *
     * throws StardogException if VCAP_SERVICES is not configured with matching
     *                         information.
     */
    public CloudFoundryConnectionFactory(
                                            String theVcap,
                                            String theServiceName,
                                            String theInstanceName,
                                            String thePlanName) {
        if (theVcap == null || theVcap.trim() == "") {
            throw new StardogException("The VCAP_SERVICE variable is not set");
        }

        Map<String, List<Map>> aServiceList;
        try {
            aServiceList = mMapper.readValue(theVcap, Map.class);

            List<Map> serviceInstances = aServiceList.get(theServiceName);
            if (serviceInstances == null || serviceInstances.size() < 1) {
                throw new StardogException("VCAP_SERVICES did not contain a properly formed JSON document");
            }

            for (Map service : serviceInstances) {
                if ((theInstanceName == null || theInstanceName.equals(service.get("name")))
                    && (thePlanName == null || thePlanName.equals(service.get("plan")))) {
                    Map credentials = (Map)service.get("credentials");

                    mDatabaseName = (String) credentials.get("db_name");
                    mSdUrl = (String) credentials.get("url");
                    mUserName = (String) credentials.get("username");
                    mPassword = (String) credentials.get("password");
                    return;
                }
            }
            throw new StardogException(String.format("The requested plan %s was not found.", thePlanName));
        }
        catch (StardogException stardogEx) {
            throw stardogEx;
        }
        catch(Exception ex) {
            throw new StardogException("VCAP_SERVICES did not contain a properly formed JSON document");
        }
    }

    /**
     * Returns a factory for creating connections to a Stardog database with
     * the default service name.
     *
     * @param theInstanceName  The name of the specific instance of the service.
     *                         If null the first matching instance will be used.
     * @param thePlanName      The name of the service broker plan.
     */
    public CloudFoundryConnectionFactory(
                                            String theInstanceName, String thePlanName) {
        this(System.getenv("VCAP_SERVICES"), "Stardog", theInstanceName, thePlanName);
    }

    /**
     * Returns a factory for creating connections to a Stardog database with
     * the default service name and a null instance name.  If multiple instance
     * exist in the environment the first one will be returned.  If none exist
     * then a StardogException will be thrown.
     *
     * @param thePlanName      The name of the service broker plan.
     */
    public CloudFoundryConnectionFactory(String thePlanName) {
        this(System.getenv("VCAP_SERVICES"), "Stardog", null, thePlanName);
    }

    /**
     * Returns a factory for creating connections to a Stardog database with
     * all default values.  This will grab the first on in the list.
     */
    public CloudFoundryConnectionFactory() {
        this(System.getenv("VCAP_SERVICES"), "Stardog", null, null);
    }

    /**
     * Returns a connection to the Stardog database based on the state of this
     * object.
     *
     * @param theOptions Options that control the specific properties of this
     *                   database connection.
     * @return
     */
    public Connection connect(Options theOptions) {
        return ConnectionConfiguration.to(mDatabaseName).credentials(mUserName, mPassword).server(mSdUrl).with(theOptions).connect();
    }

    /**
     * Returns a connection to the Stardog database based on the state of this
     * object with all default connection options.
     *
     * @return
     */
    public Connection connect() {
        return this.connect(Options.empty());
    }

    // Getters and Setters are just for tests
    String getDatabaseName() {
        return mDatabaseName;
    }

    String getSdUrl() {
        return mSdUrl;
    }

    String getUserName() {
        return mUserName;
    }

    String getPassword() {
        return mPassword;
    }
}
