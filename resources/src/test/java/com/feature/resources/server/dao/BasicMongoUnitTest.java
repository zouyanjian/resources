package com.feature.resources.server.dao;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;
import de.flapdoodle.embedmongo.MongoDBRuntime;
import de.flapdoodle.embedmongo.MongodExecutable;
import de.flapdoodle.embedmongo.MongodProcess;
import de.flapdoodle.embedmongo.config.MongodConfig;
import de.flapdoodle.embedmongo.distribution.Version;
import de.flapdoodle.embedmongo.runtime.Network;
import lombok.Getter;
import lombok.ToString;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * User: ZouYanjian
 * Date: 12-6-19
 * Time: 下午3:59
 * FileName:BasicMongoUnitTest
 */
@ToString
public class BasicMongoUnitTest {
    private static final int port = 12345;
    private static MongodProcess mongodProcess;
    private static MongodExecutable mongodExecutable = null;
    private static final MongoDBRuntime runtime = MongoDBRuntime.getDefaultInstance();

    private static Mongo mongo;
    @Getter
    private static Datastore datastore = null;

    @BeforeClass
    public static void initDb() {
        try {
            mongodExecutable = runtime.prepare(new MongodConfig(Version.V2_1_1, port, Network.localhostIsIPv6()));
            mongodProcess = mongodExecutable.start();
            mongo = new Mongo("localhost", port);
            datastore = new Morphia().createDatastore(mongo, "test");
        } catch (UnknownHostException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @AfterClass
    public static void shudownDB() {
        if (mongodProcess != null) mongodProcess.stop();
    }


}