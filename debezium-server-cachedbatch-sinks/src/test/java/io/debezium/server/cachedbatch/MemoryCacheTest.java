/*
 *
 *  * Copyright memiiso Authors.
 *  *
 *  * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 */

package io.debezium.server.cachedbatch;

import io.debezium.server.cachedbatch.common.BaseSparkTest;
import io.debezium.server.cachedbatch.common.S3Minio;
import io.debezium.server.cachedbatch.common.SourcePostgresqlDB;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration test that verifies basic reading from PostgreSQL database and writing to s3 destination.
 *
 * @author Ismail Simsek
 */
@QuarkusTest
@QuarkusTestResource(S3Minio.class)
@QuarkusTestResource(SourcePostgresqlDB.class)
@TestProfile(MemoryCacheTestProfile.class)
@Disabled // @TODO fix
public class MemoryCacheTest extends BaseSparkTest {

  @Test
  @Disabled // @TODO fix
  public void testPerformance() throws Exception {
    PGCreateTestDataTable();
    PGLoadTestDataTable(100000);
  }

}
