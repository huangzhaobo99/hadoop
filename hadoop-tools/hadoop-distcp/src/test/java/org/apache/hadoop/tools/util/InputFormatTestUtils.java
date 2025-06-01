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

package org.apache.hadoop.tools.util;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.tools.CopyListingFileStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class InputFormatTestUtils {

  /**
   * Verify split.
   *
   * @param conf     client conf
   * @param listFile copy listing path
   * @param splits   splits list
   * @throws IOException if there is io error
   */
  public static void checkSplits(Configuration conf, Path listFile, List<InputSplit> splits)
      throws IOException {
    long lastEnd = 0;

    // Verify if each split's start is matching with the previous end,
    // and we are not missing anything
    for (InputSplit split : splits) {
      FileSplit fileSplit = (FileSplit) split;
      long start = fileSplit.getStart();
      assertEquals(lastEnd, start);
      lastEnd = start + fileSplit.getLength();
    }

    // Verify there is nothing more to read from the input file
    SequenceFile.Reader reader = new SequenceFile.Reader(conf, SequenceFile.Reader.file(listFile));

    try {
      reader.seek(lastEnd);
      CopyListingFileStatus srcFileStatus = new CopyListingFileStatus();
      Text srcRelPath = new Text();
      assertFalse(reader.next(srcRelPath, srcFileStatus));
    } finally {
      IOUtils.closeStream(reader);
    }
  }
}
