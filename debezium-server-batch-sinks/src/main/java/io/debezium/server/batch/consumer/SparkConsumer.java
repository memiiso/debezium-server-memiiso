/*
 *
 *  * Copyright memiiso Authors.
 *  *
 *  * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 */

package io.debezium.server.batch.consumer;

import io.debezium.server.batch.BatchUtil;
import io.debezium.server.batch.cache.BatchJsonlinesFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.Instant;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.types.StructType;

/**
 * Implementation of the consumer that delivers the messages into Amazon S3 destination.
 *
 * @author Ismail Simsek
 */

@Dependent
@Default
public class SparkConsumer extends AbstractSparkConsumer {

  public SparkConsumer() {
    super();

    LOGGER.info("Starting Spark Consumer({})", this.getClass().getName());
    LOGGER.info("Spark save format is '{}'", saveFormat);
  }

  @Override
  public void uploadDestination(String destination) {
    Instant start = Instant.now();
    // upload different destinations parallel but same destination serial
    BatchJsonlinesFile tempFile = this.cache.getJsonLines(destination);
    if (tempFile == null) {
      LOGGER.debug("No data to upload for destination: {}", destination);
      return;
    }
    // Read DF with Schema if schema enabled and exists in the event message
    StructType dfSchema = BatchUtil.getSparkDfSchema(tempFile.getSchema());

    if (LOGGER.isTraceEnabled()) {
      final String fileName = tempFile.getFile().getName();
      try (BufferedReader br = new BufferedReader(new FileReader(tempFile.getFile().getAbsolutePath()))) {
        String line;
        while ((line = br.readLine()) != null) {
          LOGGER.trace("SparkConsumer.uploadDestination Json file:{} line val:{}", fileName, line);
        }
      } catch (Exception e) {
        LOGGER.warn("Exception happened during debug logging!", e);
      }
    }

    if (dfSchema != null) {
      LOGGER.debug("Reading data with schema definition. Schema:\n{}", dfSchema);
    } else {
      LOGGER.debug("Reading data without schema definition");
    }

    String s3File = map(destination);

    Dataset<Row> df = spark.read().schema(dfSchema).json(tempFile.getFile().getAbsolutePath());
    // serialize same destination uploads
    synchronized (uploadLock.computeIfAbsent(destination, k -> new Object())) {
      df.write()
          .mode(SaveMode.Append)
          .format(saveFormat)
          .save(bucket + "/" + s3File);
      LOGGER.info("Uploaded {} rows, schema:{}, file size:{} upload time:{}, " +
              "cache size(est): {} saved to:'{}'",
          df.count(),
          dfSchema != null,
          tempFile.getFile().length(),
          Duration.between(start, Instant.now()),
          this.cache.getEstimatedCacheSize(destination),
          s3File);
    }

    if (LOGGER.isTraceEnabled()) {
      df.toJavaRDD().foreach(x ->
          LOGGER.trace("SparkConsumer.uploadDestination row val:{}", x.toString())
      );
    }
    df.unpersist();

    if (tempFile.getFile() != null && tempFile.getFile().exists()) {
      tempFile.getFile().delete();
    }
    threadPool.logThredPoolStatus(destination);
  }


}
