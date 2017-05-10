package com.stardog.example.cf.web;

import static org.junit.Assert.assertEquals;

import com.complexible.stardog.StardogException;
import org.junit.Test;

import java.util.UUID;

public class ConnectionFactoryTest {

    private static String mVcapTemplate = "{\"Stardog\":\n" +
            "   [\n" +
            "     {\"name\": \"stardog-service\",\n" +
            "      \"label\": \"Stardog\",\n" +
            "      \"tags\": [],\n" +
            "      \"plan\": \"shareddb\",\n" +
            "      \"credentials\": {\n" +
            "          \"db_name\": \"%s\",\n" +
            "          \"url\": \"%s\",\n" +
            "          \"password\": \"%s\",\n" +
            "          \"username\": \"%s\"\n" +
            "      }\n" +
            "     }\n" +
            "   ]\n" +
            "}";
    private static String mDbName = getRandomString();
    private static String mUsername = getRandomString();
    private static String mPassword = getRandomString();
    private static String mUrl = String.format("http://stardog.%s.com:5820", getRandomString());
    private static String mGoodVCap = String.format(mVcapTemplate, mDbName, mUrl, mPassword, mUsername);

    private static String getRandomString() {
        return UUID.randomUUID().toString().split("-")[3];
    }

    @Test
    public void TestSimpleVCap() throws Exception {
        CloudFoundryConnectionFactory aFact = new CloudFoundryConnectionFactory(mGoodVCap, "Stardog", "stardog-service", "shareddb");
        assertEquals(mDbName, aFact.getDatabaseName());
        assertEquals(mUsername, aFact.getUserName());
        assertEquals(mPassword, aFact.getPassword());
        assertEquals(mUrl, aFact.getSdUrl());
    }

    @Test
    public void TestSimpleGetFirst() throws Exception {
        CloudFoundryConnectionFactory aFact = new CloudFoundryConnectionFactory(mGoodVCap, "Stardog", null, null);
        assertEquals(mDbName, aFact.getDatabaseName());
        assertEquals(mUsername, aFact.getUserName());
        assertEquals(mPassword, aFact.getPassword());
        assertEquals(mUrl, aFact.getSdUrl());
    }

    @Test(expected=StardogException.class)
    public void TestNullVCap() throws Exception {
        new CloudFoundryConnectionFactory(null, "Stardog", "stardog-service", "shareddb");
    }

    @Test(expected=StardogException.class)
    public void TestEmptyVCap() throws Exception {
        new CloudFoundryConnectionFactory(" ", "Stardog", "stardog-service", "shareddb");
    }

    @Test(expected=StardogException.class)
    public void TestNoPlanVCap() throws Exception {
        new CloudFoundryConnectionFactory(mGoodVCap, "Stardog", "stardog-service", "notreal");
    }

    @Test(expected=StardogException.class)
    public void TestNoServiceVCap() throws Exception {
        new CloudFoundryConnectionFactory(mGoodVCap, "bad", "stardog-service", "notreal");
    }

    @Test(expected=StardogException.class)
    public void TestNoInstanceVCap() throws Exception {
        new CloudFoundryConnectionFactory(mGoodVCap, "Stardog", "bad", "notreal");
    }
}
