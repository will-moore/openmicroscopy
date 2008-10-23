/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.mock;

import java.sql.Timestamp;
import java.util.HashMap;

import net.sf.ehcache.Cache;
import ome.model.meta.Session;
import ome.services.blitz.fire.SessionManagerI;
import ome.services.blitz.test.utests.TestCache;
import ome.system.OmeroContext;
import ome.system.Roles;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceFactoryPrxHelper;
import omero.constants.CLIENTUUID;
import omero.util.ObjectFactoryRegistrar;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.aop.target.HotSwappableTargetSource;

import Ice.InitializationData;

public class MockFixture {

    private final MockObjectTestCase test;
    private final Ice.Communicator communicator;
    private final Ice.ObjectAdapter adapter;

    private final OmeroContext ctx;
    private final SessionManagerI sm;

    public MockFixture(MockObjectTestCase test) throws Exception {
        this(test, new OmeroContext(new String[] { "classpath:omero/test.xml",
                "classpath:ome/services/blitz-servantDefinitions.xml",
                "classpath:ome/services/throttling/throttling.xml",
                "classpath:ome/services/messaging.xml", }));
    }

    public MockFixture(MockObjectTestCase test, OmeroContext ctx) {

        this.test = test;
        this.ctx = ctx;
        this.sm = (SessionManagerI) ctx.getBean("BlitzManager");

        // --------------------------------------------

        InitializationData id = new InitializationData();
        id.properties = Ice.Util.createProperties();

        //
        // The follow properties are necessary for Gateway
        //

        // Collocation isn't working (but should)
        id.properties.setProperty("Ice.Default.CollocationOptimized", "0");
        // Gateway calls back on the SF and so needs another thread or
        // blocks.
        id.properties.setProperty("Ice.ThreadPool.Client.Size", "2");
        id.properties.setProperty("Ice.ThreadPool.Client.SizeMax", "50");
        id.properties.setProperty("Ice.ThreadPool.Server.Size", "10");
        id.properties.setProperty("Ice.ThreadPool.Server.SizeMax", "100");
        // For testing large calls
        id.properties.setProperty("Ice.MessageSizeMax", "4096");

        communicator = Ice.Util.initialize(id);
        adapter = communicator.createObjectAdapterWithEndpoints("BlitzAdapter",
                "default -h 127.0.0.1");
        ObjectFactoryRegistrar.registerObjectFactory(communicator,
                ObjectFactoryRegistrar.INSTANCE);
        adapter.activate();
        // The following is a bit of spring magic so that we can configure
        // the adapter in code. If this can be pushed to BlitzConfiguration
        // for example then we might not need it here anymore.
        HotSwappableTargetSource ts = (HotSwappableTargetSource) ctx
                .getBean("swappableAdapterSource");
        ts.swap(adapter);
    }

    public void tearDown() {
        this.communicator.destroy();
        this.ctx.closeAll();
    }

    public OmeroContext getContext() {
        return this.ctx;
    }

    public SessionManagerI getSessionManager() {
        return this.sm;
    }

    public ServiceFactoryPrx createServiceFactory() throws Exception {
        Session s = session();
        Cache cache = cache();
        return createServiceFactory(s, cache, "single-client");
    }

    public ServiceFactoryPrx createServiceFactory(Session s) throws Exception {
        Cache cache = cache();
        return createServiceFactory(s, cache, "single-client");
    }

    public ServiceFactoryPrx createServiceFactory(Session s, Cache cache,
            String clientId) throws Exception {
        mock("securityMock").expects(test.once()).method("getSecurityRoles")
                .will(test.returnValue(new Roles()));
        mock("sessionsMock").expects(test.once()).method("create").will(
                test.returnValue(s));
        mock("sessionsMock").expects(test.once()).method("inMemoryCache").will(
                test.returnValue(cache));
        mock("methodMock").expects(test.atLeastOnce()).method("isActive").will(
                test.returnValue(false));
        return ServiceFactoryPrxHelper.uncheckedCast(sm.create("name", null,
                this.current("create", clientId)));
    }

    Mock mock(String name) {
        return (Mock) ctx.getBean(name);
    }

    Cache cache() {
        return new TestCache();
    }

    Ice.Current current(String method) {
        return current(method, "my-client-uuid");
    }

    Ice.Current current(String method, String clientId) {
        Ice.Current current = new Ice.Current();
        current.operation = method;
        current.adapter = adapter;
        current.ctx = new HashMap<String, String>();
        current.ctx.put(CLIENTUUID.value, clientId);
        return current;
    }

    Session session() {
        Session session = new Session();
        session.setUuid("my-session-uuid");
        session.setStarted(new Timestamp(System.currentTimeMillis()));
        session.setTimeToIdle(0L);
        session.setTimeToLive(0L);
        return session;
    }

    Mock blitzMock(Class serviceClass) {
        String name = serviceClass.getName();
        name = name.replaceFirst("omero", "ome").replace("PrxHelper", "");
        // WORKAROUND
        if (name.equals("ome.api.RenderingEngine")) {
            name = "omeis.providers.re.RenderingEngine";
        }
        Mock mock = mock("mock-" + name);
        if (mock == null) {
            throw new RuntimeException("No mock for serviceClass");
        }
        return mock;
    }

}
