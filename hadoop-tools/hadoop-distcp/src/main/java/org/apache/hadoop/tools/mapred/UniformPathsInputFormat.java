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
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileRecordReader;
import org.apache.hadoop.tools.CopyListingFileStatus;

/**
 * UniformPathsInputFormat extends the InputFormat class, to produce input-splits for DistCp.
 * It looks at the copy-listing and groups the contents into input-splits such
 * that the total-number of paths to be copied for each input split is uniform.
 */
public class UniformPathsInputFormat extends InputFormat<Text, CopyListingFileStatus> {

  /**
   * Implementation of InputFormat::getSplits(). Returns a list of InputSplits,
   * such that the number of paths to be copied for all the splits are approximately equal.
   *
   * @param context JobContext for the job.
   * @return The list of uniformly-distributed input-splits.
   * @throws IOException
   * @throws InterruptedException
   */
  @Override
  public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
    Configuration configuration = context.getConfiguration();
    int numSplits = InputFormatUtils.getNumMapTasks(configuration);

    if (numSplits == 0) {
      return new ArrayList<>();
    }
    return getSplits(configuration, numSplits, InputFormatUtils.getNumberOfRecords(configuration));
  }

  private List<InputSplit> getSplits(Configuration configuration, int numSplits, long totalPaths)
      throws IOException {
    List<InputSplit> splits = new ArrayList<>(numSplits);
    long pathsPerSplit = (long) Math.ceil(totalPaths * 1.0 / numSplits);

    long currentSplitPaths = 0;
    long lastSplitStart = 0;
    long lastPosition = 0;

    CopyListingFileStatus srcFileStatus = new CopyListingFileStatus();
    Text srcRelPath = new Text();

    final Path listingFilePath = InputFormatUtils.getListingFilePath(configuration);
    try (SequenceFile.Reader reader = InputFormatUtils.getListingFileReader(configuration)) {
      while (reader.next(srcRelPath, srcFileStatus)) {
        // If adding the current file would cause the paths per map to exceed limit.
        // Add the current file to new split.
        if (currentSplitPaths == pathsPerSplit && lastPosition != 0) {
          InputFormatUtils.addFileSplitToSplits(listingFilePath, lastSplitStart, lastPosition,
              currentSplitPaths, splits);
          lastSplitStart = lastPosition;
          currentSplitPaths = 0;
        }
        currentSplitPaths++;
        lastPosition = reader.getPosition();
      }
      if (lastPosition > lastSplitStart) {
        InputFormatUtils.addFileSplitToSplits(listingFilePath, lastSplitStart, lastPosition,
            currentSplitPaths, splits);
      }
    }
    return splits;
  }

  /**
   * Implementation of InputFormat::createRecordReader().
   *
   * @param split   The split for which the RecordReader is sought.
   * @param context The context of the current task-attempt.
   * @return A SequenceFileRecordReader instance, (since the copy-listing is a
   * simple sequence-file.)
   * @throws IOException
   * @throws InterruptedException
   */
  @Override
  public RecordReader<Text, CopyListingFileStatus> createRecordReader(InputSplit split,
      TaskAttemptContext context) throws IOException, InterruptedException {
    return new SequenceFileRecordReader<>();
  }
}
