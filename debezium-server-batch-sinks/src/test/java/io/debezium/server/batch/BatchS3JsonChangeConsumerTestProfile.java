/*
 *
 *  * Copyright memiiso Authors.
 *  *
 *  * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 */

package io.debezium.server.batch;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.HashMap;
import java.util.Map;

import static io.debezium.server.batch.ConfigSource.S3_BUCKET;
import static io.debezium.server.batch.ConfigSource.S3_REGION;

public class BatchS3JsonChangeConsumerTestProfile implements QuarkusTestProfile {

  //This method allows us to override configuration properties.
  @Override
  public Map<String, String> getConfigOverrides() {
    Map<String, String> config = new HashMap<>();

    config.put("debezium.sink.type", "sparkbatch");
    config.put("quarkus.arc.selected-alternatives", "S3JsonWriter");
    config.put("debezium.sink.batch.s3.region", S3_REGION);
    config.put("debezium.sink.batch.s3.endpoint-override", "http://localhost:9000");
    config.put("debezium.sink.batch.s3.bucket-name", "s3a://" + S3_BUCKET);
    config.put("debezium.sink.batch.s3.credentials-use-instance-cred", "false");
    return config;
  }
}
