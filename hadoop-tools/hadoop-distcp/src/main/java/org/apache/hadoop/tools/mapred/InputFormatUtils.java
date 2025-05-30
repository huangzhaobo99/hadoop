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

import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.tools.DistCpConstants;
import org.apache.hadoop.tools.util.DistCpUtils;

public class InputFormatUtils {

  private static final Logger LOG = LoggerFactory.getLogger(InputFormatUtils.class);

  public static int getNumMapTasks(Configuration configuration) {
    return DistCpUtils.getInt(configuration, JobContext.NUM_MAPS);
  }

  public static int getNumberOfRecords(Configuration configuration) {
    return DistCpUtils.getInt(configuration, DistCpConstants.CONF_LABEL_TOTAL_NUMBER_OF_RECORDS);
  }

  public static SequenceFile.Reader getListingFileReader(Configuration configuration) {
    final Path listingFilePath = getListingFilePath(configuration);
    try {
      final FileSystem fileSystem = listingFilePath.getFileSystem(configuration);
      if (!fileSystem.exists(listingFilePath)) {
        throw new IllegalArgumentException("Listing file doesn't exist at: " + listingFilePath);
      }
      return new SequenceFile.Reader(configuration, SequenceFile.Reader.file(listingFilePath));
    } catch (IOException exception) {
      LOG.error("Couldn't find listing file at: {}", listingFilePath, exception);
      throw new IllegalArgumentException("Couldn't find listing-file at: " + listingFilePath,
          exception);
    }
  }

  public static Path getListingFilePath(Configuration configuration) {
    final String listingFilePathString =
        configuration.get(DistCpConstants.CONF_LABEL_LISTING_FILE_PATH, "");

    assert !listingFilePathString.isEmpty() : "Couldn't find listing file. Invalid input.";
    return new Path(listingFilePathString);
  }

  public static void addFileSplitToSplits(Path listingFilePath, long lastSplitStart,
      long lastPosition, long currentSplits, List<InputSplit> splits) {
    FileSplit split =
        new FileSplit(listingFilePath, lastSplitStart, lastPosition - lastSplitStart, null);
    LOG.debug("Creating split: {}, in split: {}", split, currentSplits);
    splits.add(split);
  }
}
