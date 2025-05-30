/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.tools.mapred;

import java.io.DataOutputStream;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.task.JobContextImpl;
import org.apache.hadoop.security.Credentials;
import org.apache.hadoop.tools.CopyListing;
import org.apache.hadoop.tools.CopyListingFileStatus;
import org.apache.hadoop.tools.DistCpContext;
import org.apache.hadoop.tools.DistCpOptions;
import org.apache.hadoop.tools.StubContext;
import org.apache.hadoop.tools.util.InputFormatUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUniformPathsInputFormat {

  private static final Credentials CREDENTIALS = new Credentials();

  private static final int SIZEOF_EACH_FILE = 1024;

  private static MiniDFSCluster cluster;
  private static FileSystem fileSystem;
  private static URI uri;
  private static final String SOURCE = "/tmp/source/";
  private static final String TARGET = "/tmp/target/";
  private static final int FILES_NUM = 21;

  @BeforeAll
  public static void setup() throws Exception {
    Configuration conf = new Configuration();
    cluster = new MiniDFSCluster.Builder(conf)
        .numDataNodes(1)
        .format(true)
        .build();
    fileSystem = cluster.getFileSystem();
    uri = fileSystem.getUri();
    for (int i = 1; i <= FILES_NUM; i++) {
      createFile(new Path(SOURCE + "file" + i));
    }
  }

  @AfterAll
  public static void tearDown() throws Exception {
    cluster.shutdown();
  }

  @Test
  public void TestGetSplits() throws Exception {
    Path sourcePath = new Path(uri.toString() + SOURCE);
    Path targetPath = new Path(uri.toString() + TARGET);
    List<Path> sourceList = new ArrayList<>();
    sourceList.add(sourcePath);
    DistCpOptions distCpOptions =
        new DistCpOptions.Builder(sourceList, targetPath).maxMaps(5).build();
    DistCpContext context = new DistCpContext(distCpOptions);
    Configuration configuration = new Configuration();
    configuration.set("mapred.map.tasks", String.valueOf(context.getMaxMaps()));

    Path listFile = new Path(uri.toString() + "/tmp/testGetSplits/fileList.seq");
    CopyListing.getCopyListing(configuration, CREDENTIALS, context).buildListing(listFile, context);

    JobContext jobContext = new JobContextImpl(configuration, new JobID());
    UniformPathsInputFormat uniformPathsInputFormat = new UniformPathsInputFormat();
    List<InputSplit> splits = uniformPathsInputFormat.getSplits(jobContext);

    InputFormatUtils.checkSplits(fileSystem.getConf(), listFile, splits);

    int totalPaths = 0;
    for (InputSplit split : splits) {
      int currentSplitPaths = 0;
      RecordReader<Text, CopyListingFileStatus> recordReader =
          uniformPathsInputFormat.createRecordReader(split, null);
      StubContext stubContext = new StubContext(jobContext.getConfiguration(), recordReader, 0);
      final TaskAttemptContext taskAttemptContext = stubContext.getContext();
      recordReader.initialize(split, taskAttemptContext);
      while (recordReader.nextKeyValue()) {
        totalPaths++;
        currentSplitPaths++;
      }
      assertTrue(currentSplitPaths == 5 || currentSplitPaths == 2);
    }
    assertEquals(FILES_NUM + 1, totalPaths);
  }

  private static void createFile(Path path) throws Exception {
    try (DataOutputStream outputStream = fileSystem.create(path, true)) {
      outputStream.write(new byte[SIZEOF_EACH_FILE]);
    }
  }
}
