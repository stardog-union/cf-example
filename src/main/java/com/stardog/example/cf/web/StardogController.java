package com.stardog.example.cf.web;

import com.complexible.common.rdf.model.StardogValueFactory;
import com.complexible.common.rdf.model.Values;
import com.complexible.stardog.api.Connection;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;


@Controller
@EnableAutoConfiguration
public class StardogController {

    private CloudFoundryConnectionFactory mFactory = new CloudFoundryConnectionFactory();
    private static final Logger mLogger = LoggerFactory.getLogger(StardogController.class);
    private static final ValueFactory mVF = StardogValueFactory.instance();
    private static final String mPrefix = "http://stardog.com/pcf/example/";

    /**
     * Adds the connecting IP and the time of the connection to the database.
     */
    @RequestMapping(
        value = "/add",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    String addRequest(HttpServletRequest request) {
        JsonObject aOutJson = new JsonObject();

        try {
            mLogger.info("add called");

            IRI aSubject = Values.iri(mPrefix + UUID.randomUUID().toString());
            IRI aIpPredicate = mVF.createIRI(mPrefix + "hasIp");
            IRI aIpObject = mVF.createIRI("urn:" + request.getRemoteAddr());

            String aConnectionId = Long.toString(System.currentTimeMillis());
            IRI aTimePredicate = mVF.createIRI(mPrefix + "connectedAt");
            IRI aTimeObject = mVF.createIRI("urn:" + aConnectionId);

            Connection aConn = mFactory.connect();

            aConn.begin();
            aConn.add().statement(aSubject, aIpPredicate, aIpObject);
            aConn.add().statement(aSubject, aTimePredicate, aTimeObject);
            aConn.commit();
            aConn.close();

            aOutJson.addProperty("ip", request.getRemoteAddr());
            aOutJson.addProperty("time", aConnectionId);
            aOutJson.addProperty("status", "SUCCESS");
            aOutJson.addProperty("message", "Successfully added a new connection");
        }
        catch (RuntimeException rt) {
            aOutJson.addProperty("status", "FAILED");
            aOutJson.addProperty("message", rt.toString());
        }
        return aOutJson.toString();
    }

    /**
     * Deletes everything from the database
     */
    @RequestMapping(
        value = "/clear",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    String deleteAll(HttpServletRequest request) {
        JsonObject aOutJson = new JsonObject();

        try {
            mLogger.info("clear called");
            Connection aConn = mFactory.connect();

            aConn.begin();
            aConn.update("DELETE { ?s ?p ?o } WHERE { ?s ?p ?o }").execute();
            aConn.commit();
            aConn.close();

            aOutJson.addProperty("status", "SUCCESS");
            aOutJson.addProperty("message", "Successfully deleted all entries");
        }
        catch (RuntimeException rt) {
            aOutJson.addProperty("status", "FAILED");
            aOutJson.addProperty("message", rt.toString());
        }
        return aOutJson.toString();
    }

    /**
     * List all the current entries in the database
     */
    @RequestMapping(
        value = "/select",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    String getAll(HttpServletRequest request) {
        JsonObject aOutJson = new JsonObject();
        try {
            mLogger.info("select called");

            IRI aIpPredicate = mVF.createIRI(mPrefix + "hasIp");
            IRI aTimePredicate = mVF.createIRI(mPrefix + "connectedAt");

            Connection aConn = mFactory.connect();
            String aSelectCommand = "select ?s ?ip ?time where { ?s <" + aIpPredicate + "> ?ip ." +
                                    "?s <" + aTimePredicate + "> ?time }";
            TupleQueryResult aRes = aConn.select(aSelectCommand).execute();

            JsonArray aIPTimeList = new JsonArray();
            while (aRes.hasNext()) {
                JsonObject aIPTimeEntry = new JsonObject();

                BindingSet aBs = aRes.next();
                IRI aIPContext = (IRI) aBs.getValue("ip");
                IRI aTimeContext = (IRI) aBs.getValue("time");

                aIPTimeEntry.addProperty("IP", aIPContext.getLocalName());
                aIPTimeEntry.addProperty("time", aTimeContext.getLocalName());
                aIPTimeList.add(aIPTimeEntry);
            }
            aRes.close();
            aConn.close();

            aOutJson.add("connections", aIPTimeList);
            aOutJson.addProperty("status", "SUCCESS");
            aOutJson.addProperty("message", "Got the listing");
        }
        catch (RuntimeException rt) {
            aOutJson.addProperty("status", "FAILED");
            aOutJson.addProperty("message", rt.toString());
        }
        return aOutJson.toString();
    }

    /**
     * Return the VCAP_SERVICES environment variable as a JSON blob
     */
    @RequestMapping(
        value = "/vcap",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    String vcapDump(HttpServletRequest request) {
        mLogger.debug("select called");
        return System.getenv("VCAP_SERVICES");
    }
}
