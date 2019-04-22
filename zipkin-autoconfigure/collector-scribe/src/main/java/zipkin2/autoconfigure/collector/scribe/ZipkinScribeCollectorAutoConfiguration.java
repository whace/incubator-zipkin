/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package zipkin2.autoconfigure.collector.scribe;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.collector.CollectorMetrics;
import zipkin2.collector.CollectorSampler;
import zipkin2.collector.scribe.ScribeCollector;
import zipkin2.storage.StorageComponent;

/**
 * This collector accepts Scribe logs in a specified category. Each log entry is expected to contain
 * a single span, which is TBinaryProtocol big-endian, then base64 encoded. Decoded spans are stored
 * asynchronously.
 */
@Configuration
@EnableConfigurationProperties(ZipkinScribeCollectorProperties.class)
@ConditionalOnProperty(value = "zipkin.collector.scribe.enabled", havingValue = "true")
class ZipkinScribeCollectorAutoConfiguration {
  /** The init method will block until the scribe port is listening, or crash on port conflict */
  @Bean(initMethod = "start")
  ScribeCollector scribe(
      ZipkinScribeCollectorProperties scribe,
      CollectorSampler sampler,
      CollectorMetrics metrics,
      StorageComponent storage) {
    return scribe.toBuilder().sampler(sampler).metrics(metrics).storage(storage).build();
  }
}
