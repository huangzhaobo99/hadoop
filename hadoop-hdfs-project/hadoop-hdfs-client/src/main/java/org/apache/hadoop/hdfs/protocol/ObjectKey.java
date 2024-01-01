package org.apache.hadoop.hdfs.protocol;

import java.util.UUID;

/**
 * Key of ObjectFile.
 */
public class ObjectKey {

  public final static String FAKE_BP_SPLITTER = ":";

  private final UUID key; // ObjectKey
  private final long length; // The length of each ObjectFile

  public ObjectKey(UUID key, long length) {
    this.key = key;
    this.length = length;
  }

  public UUID getKey() {
    return key;
  }

  public long getLength() {
    return length;
  }

  public static String makeFakeBlockPoolID(String bpid, ObjectKey key) {
    return bpid + FAKE_BP_SPLITTER + key.toString();
  }

  public static boolean checkFakeBlockPoolID(String bpid) {
    return bpid.contains(FAKE_BP_SPLITTER);
  }

  public static String[] parseFakeBlockPoolID(String fake) {
    return fake.split(FAKE_BP_SPLITTER);
  }

  public static ObjectKey parseFromString(String s) {
    String[] split = s.split(",");
    return new ObjectKey(java.util.UUID.fromString(split[0]),
        Long.parseLong(split[1].trim()));
  }

  @Override
  public boolean equals(Object o) {
    return this == o || o instanceof ObjectKey && ((ObjectKey) o).key.equals(this.key)
        && ((ObjectKey) o).length == this.length;
  }

  @Override
  public String toString() {
    return String.format("%s, %d", key.toString(), length);
  }

}
