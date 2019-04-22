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
package zipkin2.storage.cassandra;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.auto.value.AutoValue;
import zipkin2.storage.cassandra.internal.call.DeduplicatingVoidCallFactory;
import zipkin2.storage.cassandra.internal.call.ResultSetFutureCall;

import static zipkin2.storage.cassandra.Schema.TABLE_SERVICE_SPANS;

final class InsertServiceSpan extends ResultSetFutureCall<Void> {

  @AutoValue
  abstract static class Input {
    static Input create(String service, String span) {
      return new AutoValue_InsertServiceSpan_Input(service, span);
    }

    abstract String service();

    abstract String span();

    Input() {
    }
  }

  static class Factory extends DeduplicatingVoidCallFactory<Input> {
    final Session session;
    final PreparedStatement preparedStatement;

    Factory(CassandraStorage storage) {
      super(storage.autocompleteTtl(), storage.autocompleteCardinality());
      session = storage.session();
      Insert insertQuery = QueryBuilder.insertInto(TABLE_SERVICE_SPANS)
        .value("service", QueryBuilder.bindMarker("service"))
        .value("span", QueryBuilder.bindMarker("span"));
      preparedStatement = session.prepare(insertQuery);
    }

    Input newInput(String service, String span) {
      return Input.create(service, span);
    }

    @Override protected InsertServiceSpan newCall(Input input) {
      return new InsertServiceSpan(this, input);
    }
  }

  final Factory factory;
  final Input input;

  InsertServiceSpan(Factory factory, Input input) {
    this.factory = factory;
    this.input = input;
  }

  @Override protected ResultSetFuture newFuture() {
    return factory.session.executeAsync(factory.preparedStatement.bind()
      .setString("service", input.service())
      .setString("span", input.span()));
  }

  @Override public Void map(ResultSet input) {
    return null;
  }

  @Override public String toString() {
    return input.toString().replace("Input", "InsertServiceSpan");
  }

  @Override public InsertServiceSpan clone() {
    return new InsertServiceSpan(factory, input);
  }
}
