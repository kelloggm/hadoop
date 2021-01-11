/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hdfs.protocol.datatransfer;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.io.IOUtils;
import org.checkerframework.checker.calledmethods.qual.EnsuresCalledMethods;
import org.checkerframework.checker.mustcall.qual.MustCallChoice;
import org.checkerframework.checker.objectconstruction.qual.Owning;

/**
 * A little struct class to wrap an InputStream and an OutputStream.
 */
@InterfaceAudience.Private
public class IOStreamPair implements Closeable {
  public final @Owning InputStream in;
  public final @Owning OutputStream out;

  public IOStreamPair(@Owning InputStream in,@Owning OutputStream out) {
    this.in = in;
    this.out = out;
  }

  @Override
  @EnsuresCalledMethods(value = {"this.in", "this.out"}, methods = {"close"})
  public void close() throws IOException {
    IOUtils.closeStream(in);
    IOUtils.closeStream(out);
  }
}
