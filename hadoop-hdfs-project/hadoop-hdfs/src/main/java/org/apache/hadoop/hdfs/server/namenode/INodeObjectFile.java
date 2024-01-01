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
package org.apache.hadoop.hdfs.server.namenode;

import org.apache.hadoop.fs.permission.PermissionStatus;
import org.apache.hadoop.hdfs.protocol.ObjectKey;
import org.apache.hadoop.hdfs.server.blockmanagement.BlockStoragePolicySuite;
import org.apache.hadoop.security.AccessControlException;

public class INodeObjectFile extends INodeWithAdditionalFields {

  /**
   * A file is split into multiple ObjectKeys, and the maximum size of an ObjectFile is 256MB.
   */
  public static final long DEFAULT_BLOCK_SIZE_FOR_OBJECT_FILE = 256L * 1024 * 1024;

  /**
   * ObjectFile have replication factor of 1.
   */
  public static final short DEFAULT_REPL_FOR_OBJECT_FILE = 1;

  // Length for whole file.
  private final long length;
  /** Key in object storage. */
  private ObjectKey[] objectKeys;

  public static final ObjectKey[] EMPTY_ARRAY = {};

  INodeObjectFile(long id, byte[] name, PermissionStatus permissions, long modificationTime,
      long accessTime, long length, ObjectKey[] objectKeys) {
    super(id, name, permissions, modificationTime, accessTime);
    this.objectKeys = objectKeys;
    this.length = length;
  }

  @Override
  void recordModification(int latestSnapshotId) {

  }

  @Override
  public void cleanSubtree(ReclaimContext reclaimContext, int snapshotId, int priorSnapshotId) {

  }

  @Override
  public void destroyAndCollectBlocks(ReclaimContext reclaimContext) {

  }

  @Override
  public ContentSummaryComputationContext computeContentSummary(int snapshotId,
      ContentSummaryComputationContext summary) throws AccessControlException {
    return null;
  }

  @Override
  public QuotaCounts computeQuotaUsage(BlockStoragePolicySuite bsps, byte blockStoragePolicyId,
      boolean useCache, int lastSnapshotId) {
    return null;
  }

  @Override
  public byte getStoragePolicyID() {
    return 0;
  }

  @Override
  public byte getLocalStoragePolicyID() {
    return 0;
  }
}
