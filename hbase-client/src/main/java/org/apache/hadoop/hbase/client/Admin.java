/**
 *
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
package org.apache.hadoop.hbase.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Abortable;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.ClusterStatus.Options;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.NamespaceNotFoundException;
import org.apache.hadoop.hbase.ProcedureInfo;
import org.apache.hadoop.hbase.RegionLoad;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.TableExistsException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.apache.hadoop.hbase.classification.InterfaceAudience;
import org.apache.hadoop.hbase.client.replication.TableCFs;
import org.apache.hadoop.hbase.client.security.SecurityCapability;
import org.apache.hadoop.hbase.ipc.CoprocessorRpcChannel;
import org.apache.hadoop.hbase.procedure2.LockInfo;
import org.apache.hadoop.hbase.quotas.QuotaFilter;
import org.apache.hadoop.hbase.quotas.QuotaRetriever;
import org.apache.hadoop.hbase.quotas.QuotaSettings;
import org.apache.hadoop.hbase.regionserver.wal.FailedLogCloseException;
import org.apache.hadoop.hbase.replication.ReplicationException;
import org.apache.hadoop.hbase.replication.ReplicationPeerConfig;
import org.apache.hadoop.hbase.replication.ReplicationPeerDescription;
import org.apache.hadoop.hbase.snapshot.HBaseSnapshotException;
import org.apache.hadoop.hbase.snapshot.RestoreSnapshotException;
import org.apache.hadoop.hbase.snapshot.SnapshotCreationException;
import org.apache.hadoop.hbase.snapshot.UnknownSnapshotException;
import org.apache.hadoop.hbase.util.Pair;

/**
 * The administrative API for HBase. Obtain an instance from an {@link Connection#getAdmin()} and
 * call {@link #close()} afterwards.
 * <p>Admin can be used to create, drop, list, enable and disable tables, add and drop table
 * column families and other administrative operations.
 *
 * @see ConnectionFactory
 * @see Connection
 * @see Table
 * @since 0.99.0
 */
@InterfaceAudience.Public
public interface Admin extends Abortable, Closeable {
  int getOperationTimeout();

  @Override
  void abort(String why, Throwable e);

  @Override
  boolean isAborted();

  /**
   * @return Connection used by this object.
   */
  Connection getConnection();

  /**
   * @param tableName Table to check.
   * @return True if table exists already.
   * @throws IOException
   */
  boolean tableExists(final TableName tableName) throws IOException;

  /**
   * List all the userspace tables.
   *
   * @return - returns an array of read-only HTableDescriptors
   * @throws IOException if a remote or network exception occurs
   * @deprecated since 2.0 version and will be removed in 3.0 version.
   *             use {@link #listTableDescriptors()}
   */
  @Deprecated
  HTableDescriptor[] listTables() throws IOException;

  /**
   * List all the userspace tables.
   *
   * @return - returns a list of TableDescriptors
   * @throws IOException if a remote or network exception occurs
   */
  List<TableDescriptor> listTableDescriptors() throws IOException;

  /**
   * List all the userspace tables matching the given pattern.
   *
   * @param pattern The compiled regular expression to match against
   * @return - returns an array of read-only HTableDescriptors
   * @throws IOException if a remote or network exception occurs
   * @see #listTables()
   * @deprecated since 2.0 version and will be removed in 3.0 version.
   *             use {@link #listTableDescriptors(java.util.regex.Pattern)}
   */
  @Deprecated
  HTableDescriptor[] listTables(Pattern pattern) throws IOException;

  /**
   * List all the userspace tables matching the given pattern.
   *
   * @param pattern The compiled regular expression to match against
   * @return - returns a list of TableDescriptors
   * @throws IOException if a remote or network exception occurs
   * @see #listTables()
   */
  List<TableDescriptor> listTableDescriptors(Pattern pattern) throws IOException;

  /**
   * List all the userspace tables matching the given regular expression.
   *
   * @param regex The regular expression to match against
   * @return - returns an array of read-only HTableDescriptors
   * @throws IOException if a remote or network exception occurs
   * @see #listTables(java.util.regex.Pattern)
   * @deprecated since 2.0 version and will be removed in 3.0 version.
   *             use {@link #listTableDescriptors(java.lang.String)}
   */
  @Deprecated
  HTableDescriptor[] listTables(String regex) throws IOException;

  /**
   * List all the userspace tables matching the given regular expression.
   *
   * @param regex The regular expression to match against
   * @return - returns a list of TableDescriptors
   * @throws IOException if a remote or network exception occurs
   * @see #listTables(java.util.regex.Pattern)
   */
  List<TableDescriptor> listTableDescriptors(String regex) throws IOException;

  /**
   * List all the tables matching the given pattern.
   *
   * @param pattern The compiled regular expression to match against
   * @param includeSysTables False to match only against userspace tables
   * @return - returns an array of read-only HTableDescriptors
   * @throws IOException if a remote or network exception occurs
   * @see #listTables()
   * @deprecated since 2.0 version and will be removed in 3.0 version.
   *             use {@link #listTableDescriptors(java.util.regex.Pattern, boolean)}
   */
  @Deprecated
  HTableDescriptor[] listTables(Pattern pattern, boolean includeSysTables)
      throws IOException;

  /**
   * List all the tables matching the given pattern.
   *
   * @param pattern The compiled regular expression to match against
   * @param includeSysTables False to match only against userspace tables
   * @return - returns a list of TableDescriptors
   * @throws IOException if a remote or network exception occurs
   * @see #listTables()
   */
  List<TableDescriptor> listTableDescriptors(Pattern pattern, boolean includeSysTables)
      throws IOException;

  /**
   * List all the tables matching the given pattern.
   *
   * @param regex The regular expression to match against
   * @param includeSysTables False to match only against userspace tables
   * @return - returns an array of read-only HTableDescriptors
   * @throws IOException if a remote or network exception occurs
   * @see #listTables(java.util.regex.Pattern, boolean)
   * @deprecated since 2.0 version and will be removed in 3.0 version.
   *             use {@link #listTableDescriptors(java.lang.String, boolean)}
   */
  @Deprecated
  HTableDescriptor[] listTables(String regex, boolean includeSysTables)
      throws IOException;

  /**
   * List all the tables matching the given pattern.
   *
   * @param regex The regular expression to match against
   * @param includeSysTables False to match only against userspace tables
   * @return - returns a list of TableDescriptors
   * @throws IOException if a remote or network exception occurs
   * @see #listTables(java.util.regex.Pattern, boolean)
   */
  List<TableDescriptor> listTableDescriptors(String regex, boolean includeSysTables)
      throws IOException;

  /**
   * List all of the names of userspace tables.
   *
   * @return TableName[] table names
   * @throws IOException if a remote or network exception occurs
   */
  TableName[] listTableNames() throws IOException;

  /**
   * List all of the names of userspace tables.
   * @param pattern The regular expression to match against
   * @return TableName[] table names
   * @throws IOException if a remote or network exception occurs
   */
  TableName[] listTableNames(Pattern pattern) throws IOException;

  /**
   * List all of the names of userspace tables.
   * @param regex The regular expression to match against
   * @return TableName[] table names
   * @throws IOException if a remote or network exception occurs
   */
  TableName[] listTableNames(String regex) throws IOException;

  /**
   * List all of the names of userspace tables.
   * @param pattern The regular expression to match against
   * @param includeSysTables False to match only against userspace tables
   * @return TableName[] table names
   * @throws IOException if a remote or network exception occurs
   */
  TableName[] listTableNames(final Pattern pattern, final boolean includeSysTables)
      throws IOException;

  /**
   * List all of the names of userspace tables.
   * @param regex The regular expression to match against
   * @param includeSysTables False to match only against userspace tables
   * @return TableName[] table names
   * @throws IOException if a remote or network exception occurs
   */
  TableName[] listTableNames(final String regex, final boolean includeSysTables)
      throws IOException;

  /**
   * Method for getting the tableDescriptor
   *
   * @param tableName as a {@link TableName}
   * @return the read-only tableDescriptor
   * @throws org.apache.hadoop.hbase.TableNotFoundException
   * @throws IOException if a remote or network exception occurs
   * @deprecated since 2.0 version and will be removed in 3.0 version.
   *             use {@link #listTableDescriptor(TableName)}
   */
  @Deprecated
  HTableDescriptor getTableDescriptor(final TableName tableName)
      throws TableNotFoundException, IOException;

  /**
   * Method for getting the tableDescriptor
   *
   * @param tableName as a {@link TableName}
   * @return the tableDescriptor
   * @throws org.apache.hadoop.hbase.TableNotFoundException
   * @throws IOException if a remote or network exception occurs
   */
  TableDescriptor listTableDescriptor(final TableName tableName)
      throws TableNotFoundException, IOException;

  /**
   * Creates a new table. Synchronous operation.
   *
   * @param desc table descriptor for table
   * @throws IllegalArgumentException if the table name is reserved
   * @throws org.apache.hadoop.hbase.MasterNotRunningException if master is not running
   * @throws org.apache.hadoop.hbase.TableExistsException if table already exists (If concurrent
   * threads, the table may have been created between test-for-existence and attempt-at-creation).
   * @throws IOException if a remote or network exception occurs
   */
  void createTable(TableDescriptor desc) throws IOException;

  /**
   * Creates a new table with the specified number of regions.  The start key specified will become
   * the end key of the first region of the table, and the end key specified will become the start
   * key of the last region of the table (the first region has a null start key and the last region
   * has a null end key). BigInteger math will be used to divide the key range specified into enough
   * segments to make the required number of total regions. Synchronous operation.
   *
   * @param desc table descriptor for table
   * @param startKey beginning of key range
   * @param endKey end of key range
   * @param numRegions the total number of regions to create
   * @throws IllegalArgumentException if the table name is reserved
   * @throws org.apache.hadoop.hbase.MasterNotRunningException if master is not running
   * @throws org.apache.hadoop.hbase.TableExistsException if table already exists (If concurrent
   * threads, the table may have been created between test-for-existence and attempt-at-creation).
   * @throws IOException
   */
  void createTable(TableDescriptor desc, byte[] startKey, byte[] endKey, int numRegions)
      throws IOException;

  /**
   * Creates a new table with an initial set of empty regions defined by the specified split keys.
   * The total number of regions created will be the number of split keys plus one. Synchronous
   * operation. Note : Avoid passing empty split key.
   *
   * @param desc table descriptor for table
   * @param splitKeys array of split keys for the initial regions of the table
   * @throws IllegalArgumentException if the table name is reserved, if the split keys are repeated
   * and if the split key has empty byte array.
   * @throws org.apache.hadoop.hbase.MasterNotRunningException if master is not running
   * @throws org.apache.hadoop.hbase.TableExistsException if table already exists (If concurrent
   * threads, the table may have been created between test-for-existence and attempt-at-creation).
   * @throws IOException
   */
  void createTable(final TableDescriptor desc, byte[][] splitKeys) throws IOException;

  /**
   * Creates a new table but does not block and wait for it to come online.
   * You can use Future.get(long, TimeUnit) to wait on the operation to complete.
   * It may throw ExecutionException if there was an error while executing the operation
   * or TimeoutException in case the wait timeout was not long enough to allow the
   * operation to complete.
   * Throws IllegalArgumentException Bad table name, if the split keys
   *    are repeated and if the split key has empty byte array.
   *
   * @param desc table descriptor for table
   * @param splitKeys keys to check if the table has been created with all split keys
   * @throws IOException if a remote or network exception occurs
   * @return the result of the async creation. You can use Future.get(long, TimeUnit)
   *    to wait on the operation to complete.
   */
  Future<Void> createTableAsync(final TableDescriptor desc, final byte[][] splitKeys)
      throws IOException;

  /**
   * Deletes a table. Synchronous operation.
   *
   * @param tableName name of table to delete
   * @throws IOException if a remote or network exception occurs
   */
  void deleteTable(final TableName tableName) throws IOException;

  /**
   * Deletes the table but does not block and wait for it be completely removed.
   * You can use Future.get(long, TimeUnit) to wait on the operation to complete.
   * It may throw ExecutionException if there was an error while executing the operation
   * or TimeoutException in case the wait timeout was not long enough to allow the
   * operation to complete.
   *
   * @param tableName name of table to delete
   * @throws IOException if a remote or network exception occurs
   * @return the result of the async delete. You can use Future.get(long, TimeUnit)
   *    to wait on the operation to complete.
   */
  Future<Void> deleteTableAsync(TableName tableName) throws IOException;

  /**
   * Deletes tables matching the passed in pattern and wait on completion. Warning: Use this method
   * carefully, there is no prompting and the effect is immediate. Consider using {@link
   * #listTableDescriptors(java.lang.String)}
   * and {@link #deleteTable(org.apache.hadoop.hbase.TableName)}
   *
   * @param regex The regular expression to match table names against
   * @return Table descriptors for tables that couldn't be deleted.
   *         The return htds are read-only
   * @throws IOException
   * @see #deleteTables(java.util.regex.Pattern)
   * @see #deleteTable(org.apache.hadoop.hbase.TableName)
   * @deprecated since 2.0 version and will be removed in 3.0 version
   *             This is just a trivial helper method without any magic.
   *             Consider using {@link #listTableDescriptors(java.lang.String)}
   *             and {@link #enableTable(org.apache.hadoop.hbase.TableName)}
   */
  @Deprecated
  HTableDescriptor[] deleteTables(String regex) throws IOException;

  /**
   * Delete tables matching the passed in pattern and wait on completion. Warning: Use this method
   * carefully, there is no prompting and the effect is immediate. Consider using {@link
   * #listTableDescriptors(java.util.regex.Pattern)} and
   * {@link #deleteTable(org.apache.hadoop.hbase.TableName)}
   *
   * @param pattern The pattern to match table names against
   * @return Table descriptors for tables that couldn't be deleted
   *         The return htds are read-only
   * @throws IOException
   * @deprecated since 2.0 version and will be removed in 3.0 version
   *             This is just a trivial helper method without any magic.
   *             Consider using {@link #listTableDescriptors(java.util.regex.Pattern)}
   *             and {@link #enableTable(org.apache.hadoop.hbase.TableName)}
   */
  @Deprecated
  HTableDescriptor[] deleteTables(Pattern pattern) throws IOException;

  /**
   * Truncate a table.
   * Synchronous operation.
   *
   * @param tableName name of table to truncate
   * @param preserveSplits True if the splits should be preserved
   * @throws IOException if a remote or network exception occurs
   */
  public void truncateTable(final TableName tableName, final boolean preserveSplits)
      throws IOException;

  /**
   * Truncate the table but does not block and wait for it be completely enabled. You can use
   * Future.get(long, TimeUnit) to wait on the operation to complete. It may throw
   * ExecutionException if there was an error while executing the operation or TimeoutException in
   * case the wait timeout was not long enough to allow the operation to complete.
   * @param tableName name of table to delete
   * @param preserveSplits true if the splits should be preserved
   * @throws IOException if a remote or network exception occurs
   * @return the result of the async truncate. You can use Future.get(long, TimeUnit) to wait on the
   *         operation to complete.
   */
  Future<Void> truncateTableAsync(final TableName tableName, final boolean preserveSplits)
      throws IOException;

  /**
   * Enable a table.  May timeout.  Use {@link #enableTableAsync(org.apache.hadoop.hbase.TableName)}
   * and {@link #isTableEnabled(org.apache.hadoop.hbase.TableName)} instead. The table has to be in
   * disabled state for it to be enabled.
   *
   * @param tableName name of the table
   * @throws IOException if a remote or network exception occurs There could be couple types of
   * IOException TableNotFoundException means the table doesn't exist. TableNotDisabledException
   * means the table isn't in disabled state.
   * @see #isTableEnabled(org.apache.hadoop.hbase.TableName)
   * @see #disableTable(org.apache.hadoop.hbase.TableName)
   * @see #enableTableAsync(org.apache.hadoop.hbase.TableName)
   */
  void enableTable(final TableName tableName) throws IOException;

  /**
   * Enable the table but does not block and wait for it be completely enabled.
   * You can use Future.get(long, TimeUnit) to wait on the operation to complete.
   * It may throw ExecutionException if there was an error while executing the operation
   * or TimeoutException in case the wait timeout was not long enough to allow the
   * operation to complete.
   *
   * @param tableName name of table to delete
   * @throws IOException if a remote or network exception occurs
   * @return the result of the async enable. You can use Future.get(long, TimeUnit)
   *    to wait on the operation to complete.
   */
  Future<Void> enableTableAsync(final TableName tableName) throws IOException;

  /**
   * Enable tables matching the passed in pattern and wait on completion. Warning: Use this method
   * carefully, there is no prompting and the effect is immediate. Consider using {@link
   * #listTableDescriptors(java.lang.String)} and {@link #enableTable(org.apache.hadoop.hbase.TableName)}
   *
   * @param regex The regular expression to match table names against
   * @throws IOException
   * @return Table descriptors for tables that couldn't be enabled.
   *         The return HTDs are read-only.
   * @see #enableTables(java.util.regex.Pattern)
   * @see #enableTable(org.apache.hadoop.hbase.TableName)
   * @deprecated since 2.0 version and will be removed in 3.0 version
   *             This is just a trivial helper method without any magic.
   *             Consider using {@link #listTableDescriptors(java.lang.String)}
   *             and {@link #enableTable(org.apache.hadoop.hbase.TableName)}
   */
  @Deprecated
  HTableDescriptor[] enableTables(String regex) throws IOException;

  /**
   * Enable tables matching the passed in pattern and wait on completion. Warning: Use this method
   * carefully, there is no prompting and the effect is immediate. Consider using {@link
   * #listTableDescriptors(java.util.regex.Pattern)} and
   * {@link #enableTable(org.apache.hadoop.hbase.TableName)}
   *
   * @param pattern The pattern to match table names against
   * @throws IOException
   * @return Table descriptors for tables that couldn't be enabled.
   *         The return HTDs are read-only.
   * @deprecated since 2.0 version and will be removed in 3.0 version
   *             This is just a trivial helper method without any magic.
   *             Consider using {@link #listTableDescriptors(java.util.regex.Pattern)}
   *             and {@link #enableTable(org.apache.hadoop.hbase.TableName)}
   */
  @Deprecated
  HTableDescriptor[] enableTables(Pattern pattern) throws IOException;

  /**
   * Disable the table but does not block and wait for it be completely disabled.
   * You can use Future.get(long, TimeUnit) to wait on the operation to complete.
   * It may throw ExecutionException if there was an error while executing the operation
   * or TimeoutException in case the wait timeout was not long enough to allow the
   * operation to complete.
   *
   * @param tableName name of table to delete
   * @throws IOException if a remote or network exception occurs
   * @return the result of the async disable. You can use Future.get(long, TimeUnit)
   *    to wait on the operation to complete.
   */
  Future<Void> disableTableAsync(final TableName tableName) throws IOException;

  /**
   * Disable table and wait on completion.  May timeout eventually.  Use {@link
   * #disableTableAsync(org.apache.hadoop.hbase.TableName)} and
   * {@link #isTableDisabled(org.apache.hadoop.hbase.TableName)} instead. The table has to be in
   * enabled state for it to be disabled.
   *
   * @param tableName
   * @throws IOException There could be couple types of IOException TableNotFoundException means the
   * table doesn't exist. TableNotEnabledException means the table isn't in enabled state.
   */
  void disableTable(final TableName tableName) throws IOException;

  /**
   * Disable tables matching the passed in pattern and wait on completion. Warning: Use this method
   * carefully, there is no prompting and the effect is immediate. Consider using {@link
   * #listTableDescriptors(java.lang.String)}
   * and {@link #disableTable(org.apache.hadoop.hbase.TableName)}
   *
   * @param regex The regular expression to match table names against
   * @return Table descriptors for tables that couldn't be disabled
   *         The return htds are read-only
   * @throws IOException
   * @see #disableTables(java.util.regex.Pattern)
   * @see #disableTable(org.apache.hadoop.hbase.TableName)
   * @deprecated since 2.0 version and will be removed in 3.0 version
   *             This is just a trivial helper method without any magic.
   *             Consider using {@link #listTableDescriptors(java.lang.String)}
   *             and {@link #disableTable(org.apache.hadoop.hbase.TableName)}
   */
  @Deprecated
  HTableDescriptor[] disableTables(String regex) throws IOException;

  /**
   * Disable tables matching the passed in pattern and wait on completion. Warning: Use this method
   * carefully, there is no prompting and the effect is immediate. Consider using {@link
   * #listTableDescriptors(java.util.regex.Pattern)} and
   * {@link #disableTable(org.apache.hadoop.hbase.TableName)}
   *
   * @param pattern The pattern to match table names against
   * @return Table descriptors for tables that couldn't be disabled
   *         The return htds are read-only
   * @throws IOException
   * @deprecated since 2.0 version and will be removed in 3.0 version
   *             This is just a trivial helper method without any magic.
   *             Consider using {@link #listTableDescriptors(java.util.regex.Pattern)}
   *             and {@link #disableTable(org.apache.hadoop.hbase.TableName)}
   */
  @Deprecated
  HTableDescriptor[] disableTables(Pattern pattern) throws IOException;

  /**
   * @param tableName name of table to check
   * @return true if table is on-line
   * @throws IOException if a remote or network exception occurs
   */
  boolean isTableEnabled(TableName tableName) throws IOException;

  /**
   * @param tableName name of table to check
   * @return true if table is off-line
   * @throws IOException if a remote or network exception occurs
   */
  boolean isTableDisabled(TableName tableName) throws IOException;

  /**
   * @param tableName name of table to check
   * @return true if all regions of the table are available
   * @throws IOException if a remote or network exception occurs
   */
  boolean isTableAvailable(TableName tableName) throws IOException;

  /**
   * Use this api to check if the table has been created with the specified number of splitkeys
   * which was used while creating the given table. Note : If this api is used after a table's
   * region gets splitted, the api may return false.
   *
   * @param tableName name of table to check
   * @param splitKeys keys to check if the table has been created with all split keys
   * @throws IOException if a remote or network excpetion occurs
   */
  boolean isTableAvailable(TableName tableName, byte[][] splitKeys) throws IOException;

  /**
   * Get the status of alter command - indicates how many regions have received the updated schema
   * Asynchronous operation.
   *
   * @param tableName TableName instance
   * @return Pair indicating the number of regions updated Pair.getFirst() is the regions that are
   * yet to be updated Pair.getSecond() is the total number of regions of the table
   * @throws IOException if a remote or network exception occurs
   */
  Pair<Integer, Integer> getAlterStatus(final TableName tableName) throws IOException;

  /**
   * Get the status of alter command - indicates how many regions have received the updated schema
   * Asynchronous operation.
   *
   * @param tableName name of the table to get the status of
   * @return Pair indicating the number of regions updated Pair.getFirst() is the regions that are
   * yet to be updated Pair.getSecond() is the total number of regions of the table
   * @throws IOException if a remote or network exception occurs
   * @deprecated Since 2.0.0. Will be removed in 3.0.0. Use {@link #getAlterStatus(TableName)}
   *     instead.
   */
  @Deprecated
  Pair<Integer, Integer> getAlterStatus(final byte[] tableName) throws IOException;

  /**
   * Add a column family to an existing table. Asynchronous operation.
   *
   * @param tableName name of the table to add column family to
   * @param columnFamily column family descriptor of column family to be added
   * @throws IOException if a remote or network exception occurs
   * @deprecated As of release 2.0.0.
   *             (<a href="https://issues.apache.org/jira/browse/HBASE-1989">HBASE-1989</a>).
   *             This will be removed in HBase 3.0.0.
   *             Use {@link #addColumnFamily(TableName, ColumnFamilyDescriptor)}.
   */
  @Deprecated
  default void addColumn(final TableName tableName, final ColumnFamilyDescriptor columnFamily)
    throws IOException {
    addColumnFamily(tableName, columnFamily);
  }

  /**
   * Add a column family to an existing table.
   *
   * @param tableName name of the table to add column family to
   * @param columnFamily column family descriptor of column family to be added
   * @throws IOException if a remote or network exception occurs
   */
  void addColumnFamily(final TableName tableName, final ColumnFamilyDescriptor columnFamily)
    throws IOException;

  /**
   * Add a column family to an existing table. Asynchronous operation.
   * You can use Future.get(long, TimeUnit) to wait on the operation to complete.
   * It may throw ExecutionException if there was an error while executing the operation
   * or TimeoutException in case the wait timeout was not long enough to allow the
   * operation to complete.
   *
   * @param tableName name of the table to add column family to
   * @param columnFamily column family descriptor of column family to be added
   * @throws IOException if a remote or network exception occurs
   * @return the result of the async add column family. You can use Future.get(long, TimeUnit) to
   *         wait on the operation to complete.
   */
  Future<Void> addColumnFamilyAsync(final TableName tableName, final ColumnFamilyDescriptor columnFamily)
      throws IOException;

  /**
   * Delete a column family from a table. Asynchronous operation.
   *
   * @param tableName name of table
   * @param columnFamily name of column family to be deleted
   * @throws IOException if a remote or network exception occurs
   * @deprecated As of release 2.0.0.
   *             (<a href="https://issues.apache.org/jira/browse/HBASE-1989">HBASE-1989</a>).
   *             This will be removed in HBase 3.0.0.
   *             Use {@link #deleteColumnFamily(TableName, byte[])}}.
   */
  @Deprecated
  void deleteColumn(final TableName tableName, final byte[] columnFamily) throws IOException;

  /**
   * Delete a column family from a table. Asynchronous operation.
   *
   * @param tableName name of table
   * @param columnFamily name of column family to be deleted
   * @throws IOException if a remote or network exception occurs
   */
  void deleteColumnFamily(final TableName tableName, final byte[] columnFamily) throws IOException;

  /**
   * Delete a column family from a table. Asynchronous operation.
   * You can use Future.get(long, TimeUnit) to wait on the operation to complete.
   * It may throw ExecutionException if there was an error while executing the operation
   * or TimeoutException in case the wait timeout was not long enough to allow the
   * operation to complete.
   *
   * @param tableName name of table
   * @param columnFamily name of column family to be deleted
   * @throws IOException if a remote or network exception occurs
   * @return the result of the async delete column family. You can use Future.get(long, TimeUnit) to
   *         wait on the operation to complete.
   */
  Future<Void> deleteColumnFamilyAsync(final TableName tableName, final byte[] columnFamily)
      throws IOException;

  /**
   * Modify an existing column family on a table.
   *
   * @param tableName name of table
   * @param columnFamily new column family descriptor to use
   * @throws IOException if a remote or network exception occurs
   * @deprecated As of release 2.0.0.
   *             (<a href="https://issues.apache.org/jira/browse/HBASE-1989">HBASE-1989</a>).
   *             This will be removed in HBase 3.0.0.
   *             Use {@link #modifyColumnFamily(TableName, ColumnFamilyDescriptor)}.
   */
  @Deprecated
  default void modifyColumn(final TableName tableName, final ColumnFamilyDescriptor columnFamily)
      throws IOException {
    modifyColumnFamily(tableName, columnFamily);
  }

  /**
   * Modify an existing column family on a table.
   *
   * @param tableName name of table
   * @param columnFamily new column family descriptor to use
   * @throws IOException if a remote or network exception occurs
   */
  void modifyColumnFamily(final TableName tableName, final ColumnFamilyDescriptor columnFamily)
      throws IOException;

  /**
   * Modify an existing column family on a table. Asynchronous operation.
   * You can use Future.get(long, TimeUnit) to wait on the operation to complete.
   * It may throw ExecutionException if there was an error while executing the operation
   * or TimeoutException in case the wait timeout was not long enough to allow the
   * operation to complete.
   *
   * @param tableName name of table
   * @param columnFamily new column family descriptor to use
   * @throws IOException if a remote or network exception occurs
   * @return the result of the async modify column family. You can use Future.get(long, TimeUnit) to
   *         wait on the operation to complete.
   */
  Future<Void> modifyColumnFamilyAsync(TableName tableName, ColumnFamilyDescriptor columnFamily)
      throws IOException;

  /**
   * Uses {@link #unassign(byte[], boolean)} to unassign the region. For expert-admins.
   *
   * @param regionname region name to close
   * @param serverName Deprecated. Not used.
   * @throws IOException if a remote or network exception occurs
   * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0
   *             (<a href="https://issues.apache.org/jira/browse/HBASE-18231">HBASE-18231</a>).
   *             Use {@link #unassign(byte[], boolean)}.
   */
  @Deprecated
  void closeRegion(final String regionname, final String serverName) throws IOException;

  /**
   * Uses {@link #unassign(byte[], boolean)} to unassign the region. For expert-admins.
   *
   * @param regionname region name to close
   * @param serverName Deprecated. Not used.
   * @throws IOException if a remote or network exception occurs
   * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0
   *             (<a href="https://issues.apache.org/jira/browse/HBASE-18231">HBASE-18231</a>).
   *             Use {@link #unassign(byte[], boolean)}.
   */
  @Deprecated
  void closeRegion(final byte[] regionname, final String serverName) throws IOException;

  /**
   * Uses {@link #unassign(byte[], boolean)} to unassign the region. For expert-admins.
   *
   * @param encodedRegionName The encoded region name; i.e. the hash that makes up the region name
   * suffix: e.g. if regionname is
   * <code>TestTable,0094429456,1289497600452.527db22f95c8a9e0116f0cc13c680396.</code>,
   * then the encoded region name is: <code>527db22f95c8a9e0116f0cc13c680396</code>.
   * @param serverName Deprecated. Not used.
   * @return Deprecated. Returns true always.
   * @throws IOException if a remote or network exception occurs
   * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0
   *             (<a href="https://issues.apache.org/jira/browse/HBASE-18231">HBASE-18231</a>).
   *             Use {@link #unassign(byte[], boolean)}.
   */
  @Deprecated
  boolean closeRegionWithEncodedRegionName(final String encodedRegionName, final String serverName)
      throws IOException;

  /**
   * Used {@link #unassign(byte[], boolean)} to unassign the region. For expert-admins.
   *
   * @param sn Deprecated. Not used.
   * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0
   *             (<a href="https://issues.apache.org/jira/browse/HBASE-18231">HBASE-18231</a>).
   *             Use {@link #unassign(byte[], boolean)}.
   */
  @Deprecated
  void closeRegion(final ServerName sn, final HRegionInfo hri) throws IOException;

  /**
   * Get all the online regions on a region server.
   */
  List<HRegionInfo> getOnlineRegions(final ServerName sn) throws IOException;

  /**
   * Flush a table. Synchronous operation.
   *
   * @param tableName table to flush
   * @throws IOException if a remote or network exception occurs
   */
  void flush(final TableName tableName) throws IOException;

  /**
   * Flush an individual region. Synchronous operation.
   *
   * @param regionName region to flush
   * @throws IOException if a remote or network exception occurs
   */
  void flushRegion(final byte[] regionName) throws IOException;

  /**
   * Compact a table. Asynchronous operation.
   *
   * @param tableName table to compact
   * @throws IOException if a remote or network exception occurs
   */
  void compact(final TableName tableName) throws IOException;

  /**
   * Compact an individual region. Asynchronous operation.
   *
   * @param regionName region to compact
   * @throws IOException if a remote or network exception occurs
   */
  void compactRegion(final byte[] regionName) throws IOException;

  /**
   * Compact a column family within a table. Asynchronous operation.
   *
   * @param tableName table to compact
   * @param columnFamily column family within a table
   * @throws IOException if a remote or network exception occurs
   */
  void compact(final TableName tableName, final byte[] columnFamily)
    throws IOException;

  /**
   * Compact a column family within a region. Asynchronous operation.
   *
   * @param regionName region to compact
   * @param columnFamily column family within a region
   * @throws IOException if a remote or network exception occurs
   */
  void compactRegion(final byte[] regionName, final byte[] columnFamily)
    throws IOException;

  /**
   * Major compact a table. Asynchronous operation.
   *
   * @param tableName table to major compact
   * @throws IOException if a remote or network exception occurs
   */
  void majorCompact(TableName tableName) throws IOException;

  /**
   * Major compact a table or an individual region. Asynchronous operation.
   *
   * @param regionName region to major compact
   * @throws IOException if a remote or network exception occurs
   */
  void majorCompactRegion(final byte[] regionName) throws IOException;

  /**
   * Major compact a column family within a table. Asynchronous operation.
   *
   * @param tableName table to major compact
   * @param columnFamily column family within a table
   * @throws IOException if a remote or network exception occurs
   */
  void majorCompact(TableName tableName, final byte[] columnFamily)
    throws IOException;

  /**
   * Major compact a column family within region. Asynchronous operation.
   *
   * @param regionName egion to major compact
   * @param columnFamily column family within a region
   * @throws IOException if a remote or network exception occurs
   */
  void majorCompactRegion(final byte[] regionName, final byte[] columnFamily)
    throws IOException;

  /**
   * Compact all regions on the region server
   * @param sn the region server name
   * @param major if it's major compaction
   * @throws IOException
   * @throws InterruptedException
   */
  public void compactRegionServer(final ServerName sn, boolean major)
    throws IOException, InterruptedException;

  /**
   * Move the region <code>r</code> to <code>dest</code>.
   *
   * @param encodedRegionName The encoded region name; i.e. the hash that makes up the region name
   * suffix: e.g. if regionname is
   * <code>TestTable,0094429456,1289497600452.527db22f95c8a9e0116f0cc13c680396.</code>,
   * then the encoded region name is: <code>527db22f95c8a9e0116f0cc13c680396</code>.
   * @param destServerName The servername of the destination regionserver.  If passed the empty byte
   * array we'll assign to a random server.  A server name is made of host, port and startcode.
   * Here is an example: <code> host187.example.com,60020,1289493121758</code>
   * @throws IOException if we can't find a region named
   * <code>encodedRegionName</code>
   */
  void move(final byte[] encodedRegionName, final byte[] destServerName)
      throws IOException;

  /**
   * @param regionName Region name to assign.
   */
  void assign(final byte[] regionName)
      throws IOException;

  /**
   * Unassign a region from current hosting regionserver.  Region will then be assigned to a
   * regionserver chosen at random.  Region could be reassigned back to the same server.  Use {@link
   * #move(byte[], byte[])} if you want to control the region movement.
   *
   * @param regionName Region to unassign. Will clear any existing RegionPlan if one found.
   * @param force If true, force unassign (Will remove region from regions-in-transition too if
   * present. If results in double assignment use hbck -fix to resolve. To be used by experts).
   */
  void unassign(final byte[] regionName, final boolean force)
      throws IOException;

  /**
   * Offline specified region from master's in-memory state. It will not attempt to reassign the
   * region as in unassign. This API can be used when a region not served by any region server and
   * still online as per Master's in memory state. If this API is incorrectly used on active region
   * then master will loose track of that region. This is a special method that should be used by
   * experts or hbck.
   *
   * @param regionName Region to offline.
   * @throws IOException
   */
  void offline(final byte[] regionName) throws IOException;

  /**
   * Turn the load balancer on or off.
   *
   * @param synchronous If true, it waits until current balance() call, if outstanding, to return.
   * @return Previous balancer value
   */
  boolean setBalancerRunning(final boolean on, final boolean synchronous)
      throws IOException;

  /**
   * Invoke the balancer.  Will run the balancer and if regions to move, it will go ahead and do the
   * reassignments.  Can NOT run for various reasons.  Check logs.
   *
   * @return True if balancer ran, false otherwise.
   */
  boolean balancer() throws IOException;

  /**
   * Invoke the balancer.  Will run the balancer and if regions to move, it will
   * go ahead and do the reassignments. If there is region in transition, force parameter of true
   * would still run balancer. Can *not* run for other reasons.  Check
   * logs.
   * @param force whether we should force balance even if there is region in transition
   * @return True if balancer ran, false otherwise.
   */
  boolean balancer(boolean force) throws IOException;

  /**
   * Query the current state of the balancer
   *
   * @return true if the balancer is enabled, false otherwise.
   */
  boolean isBalancerEnabled() throws IOException;

  /**
   * Invoke region normalizer. Can NOT run for various reasons.  Check logs.
   *
   * @return True if region normalizer ran, false otherwise.
   */
  boolean normalize() throws IOException;

  /**
   * Query the current state of the region normalizer
   *
   * @return true if region normalizer is enabled, false otherwise.
   */
  boolean isNormalizerEnabled() throws IOException;

  /**
   * Turn region normalizer on or off.
   *
   * @return Previous normalizer value
   */
  boolean setNormalizerRunning(final boolean on)
    throws IOException;

  /**
   * Enable/Disable the catalog janitor
   *
   * @param enable if true enables the catalog janitor
   * @return the previous state
   */
  boolean enableCatalogJanitor(boolean enable) throws IOException;

  /**
   * Ask for a scan of the catalog table
   *
   * @return the number of entries cleaned
   */
  int runCatalogScan() throws IOException;

  /**
   * Query on the catalog janitor state (Enabled/Disabled?)
   *
   */
  boolean isCatalogJanitorEnabled() throws IOException;

  /**
   * Enable/Disable the cleaner chore
   *
   * @param on if true enables the cleaner chore
   * @return the previous state
   * @throws IOException
   */
  public boolean setCleanerChoreRunning(final boolean on) throws IOException;

  /**
   * Ask for cleaner chore to run
   *
   * @return True if cleaner chore ran, false otherwise
   * @throws IOException
   */
  public boolean runCleanerChore() throws IOException;

  /**
   * Query on the cleaner chore state (Enabled/Disabled?)
   *
   * @throws IOException
   */
  public boolean isCleanerChoreEnabled() throws IOException;

  /**
   * Merge two regions. Asynchronous operation.
   *
   * @param nameOfRegionA encoded or full name of region a
   * @param nameOfRegionB encoded or full name of region b
   * @param forcible true if do a compulsory merge, otherwise we will only merge two adjacent
   * regions
   * @throws IOException
   * @deprecated Since 2.0. Will be removed in 3.0. Use
   *     {@link #mergeRegionsAsync(byte[], byte[], boolean)} instead.
   */
  @Deprecated
  void mergeRegions(final byte[] nameOfRegionA, final byte[] nameOfRegionB,
      final boolean forcible) throws IOException;


  /**
   * Merge two regions. Asynchronous operation.
   *
   * @param nameOfRegionA encoded or full name of region a
   * @param nameOfRegionB encoded or full name of region b
   * @param forcible true if do a compulsory merge, otherwise we will only merge
   *          two adjacent regions
   * @throws IOException
   */
  Future<Void> mergeRegionsAsync(
      final byte[] nameOfRegionA,
      final byte[] nameOfRegionB,
      final boolean forcible) throws IOException;

  /**
   * Merge regions. Asynchronous operation.
   *
   * @param nameofRegionsToMerge encoded or full name of daughter regions
   * @param forcible true if do a compulsory merge, otherwise we will only merge
   *          adjacent regions
   * @throws IOException
   */
  Future<Void> mergeRegionsAsync(
      final byte[][] nameofRegionsToMerge,
      final boolean forcible) throws IOException;

  /**
   * Split a table. Asynchronous operation.
   *
   * @param tableName table to split
   * @throws IOException if a remote or network exception occurs
   */
  void split(final TableName tableName) throws IOException;

  /**
   * Split an individual region. Asynchronous operation.
   *
   * @param regionName region to split
   * @throws IOException if a remote or network exception occurs
   * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0
   *             (<a href="https://issues.apache.org/jira/browse/HBASE-18229">HBASE-18229</a>).
   *             Use {@link #splitRegionAsync(byte[], byte[])}.
   */
  @Deprecated
  void splitRegion(final byte[] regionName) throws IOException;

  /**
   * Split a table. Asynchronous operation.
   *
   * @param tableName table to split
   * @param splitPoint the explicit position to split on
   * @throws IOException if a remote or network exception occurs
   */
  void split(final TableName tableName, final byte[] splitPoint)
    throws IOException;

  /**
   * Split an individual region. Asynchronous operation.
   *
   * @param regionName region to split
   * @param splitPoint the explicit position to split on
   * @throws IOException if a remote or network exception occurs
   * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0
   *             (<a href="https://issues.apache.org/jira/browse/HBASE-18229">HBASE-18229</a>).
   *             Use {@link #splitRegionAsync(byte[], byte[])}.
   */
  @Deprecated
  void splitRegion(final byte[] regionName, final byte[] splitPoint)
    throws IOException;

  /**
   * Split an individual region. Asynchronous operation.
   * @param regionName region to split
   * @param splitPoint the explicit position to split on
   * @throws IOException if a remote or network exception occurs
   */
  Future<Void> splitRegionAsync(byte[] regionName, byte[] splitPoint)
    throws IOException;

  /**
   * Modify an existing table, more IRB friendly version.
   *
   * @param tableName name of table.
   * @param td modified description of the table
   * @throws IOException if a remote or network exception occurs
   * @deprecated since 2.0 version and will be removed in 3.0 version.
   *             use {@link #modifyTable(TableDescriptor)}
   */
  @Deprecated
  void modifyTable(final TableName tableName, final TableDescriptor td)
      throws IOException;

  /**
   * Modify an existing table, more IRB friendly version.
   *
   * @param td modified description of the table
   * @throws IOException if a remote or network exception occurs
   */
  void modifyTable(final TableDescriptor td) throws IOException;

  /**
   * Modify an existing table, more IRB friendly version. Asynchronous operation.  This means that
   * it may be a while before your schema change is updated across all of the table.
   * You can use Future.get(long, TimeUnit) to wait on the operation to complete.
   * It may throw ExecutionException if there was an error while executing the operation
   * or TimeoutException in case the wait timeout was not long enough to allow the
   * operation to complete.
   *
   * @param tableName name of table.
   * @param td modified description of the table
   * @throws IOException if a remote or network exception occurs
   * @return the result of the async modify. You can use Future.get(long, TimeUnit) to wait on the
   *     operation to complete
   * @deprecated since 2.0 version and will be removed in 3.0 version.
   *             use {@link #modifyTableAsync(TableDescriptor)}
   */
  @Deprecated
  Future<Void> modifyTableAsync(final TableName tableName, final TableDescriptor td)
      throws IOException;

  /**
   * Modify an existing table, more IRB friendly version. Asynchronous operation.  This means that
   * it may be a while before your schema change is updated across all of the table.
   * You can use Future.get(long, TimeUnit) to wait on the operation to complete.
   * It may throw ExecutionException if there was an error while executing the operation
   * or TimeoutException in case the wait timeout was not long enough to allow the
   * operation to complete.
   *
   * @param td description of the table
   * @throws IOException if a remote or network exception occurs
   * @return the result of the async modify. You can use Future.get(long, TimeUnit) to wait on the
   *     operation to complete
   */
  Future<Void> modifyTableAsync(TableDescriptor td)
      throws IOException;

  /**
   * Shuts down the HBase cluster
   *
   * @throws IOException if a remote or network exception occurs
   */
  void shutdown() throws IOException;

  /**
   * Shuts down the current HBase master only. Does not shutdown the cluster.
   *
   * @throws IOException if a remote or network exception occurs
   * @see #shutdown()
   */
  void stopMaster() throws IOException;

  /**
   * Check whether Master is in maintenance mode
   *
   * @throws IOException if a remote or network exception occurs
   */
  boolean isMasterInMaintenanceMode()  throws IOException;

  /**
   * Stop the designated regionserver
   *
   * @param hostnamePort Hostname and port delimited by a <code>:</code> as in
   * <code>example.org:1234</code>
   * @throws IOException if a remote or network exception occurs
   */
  void stopRegionServer(final String hostnamePort) throws IOException;

  /**
   * @return cluster status
   * @throws IOException if a remote or network exception occurs
   */
  ClusterStatus getClusterStatus() throws IOException;

  /**
   * Get cluster status with options to filter out unwanted status.
   * @return cluster status
   * @throws IOException if a remote or network exception occurs
   */
  ClusterStatus getClusterStatus(Options options) throws IOException;

  /**
   * Get {@link RegionLoad} of all regions hosted on a regionserver.
   *
   * @param sn region server from which regionload is required.
   * @return region load map of all regions hosted on a region server
   * @throws IOException if a remote or network exception occurs
   */
  Map<byte[], RegionLoad> getRegionLoad(ServerName sn) throws IOException;

  /**
   * Get {@link RegionLoad} of all regions hosted on a regionserver for a table.
   *
   * @param sn region server from which regionload is required.
   * @param tableName get region load of regions belonging to the table
   * @return region load map of all regions of a table hosted on a region server
   * @throws IOException if a remote or network exception occurs
   */
  Map<byte[], RegionLoad> getRegionLoad(ServerName sn, TableName tableName) throws IOException;

  /**
   * @return Configuration used by the instance.
   */
  Configuration getConfiguration();

  /**
   * Create a new namespace. Blocks until namespace has been successfully created or an exception
   * is thrown.
   *
   * @param descriptor descriptor which describes the new namespace
   */
  void createNamespace(final NamespaceDescriptor descriptor)
  throws IOException;

  /**
   * Create a new namespace
   *
   * @param descriptor descriptor which describes the new namespace
   * @return the result of the async create namespace operation. Use Future.get(long, TimeUnit) to
   *  wait on the operation to complete.
   */
  Future<Void> createNamespaceAsync(final NamespaceDescriptor descriptor)
  throws IOException;

  /**
   * Modify an existing namespace.  Blocks until namespace has been successfully modified or an
   * exception is thrown.
   *
   * @param descriptor descriptor which describes the new namespace
   */
  void modifyNamespace(final NamespaceDescriptor descriptor)
  throws IOException;

  /**
   * Modify an existing namespace
   *
   * @param descriptor descriptor which describes the new namespace
   * @return the result of the async modify namespace operation. Use Future.get(long, TimeUnit) to
   *  wait on the operation to complete.
   */
  Future<Void> modifyNamespaceAsync(final NamespaceDescriptor descriptor)
  throws IOException;

  /**
   * Delete an existing namespace. Only empty namespaces (no tables) can be removed.
   * Blocks until namespace has been successfully deleted or an
   * exception is thrown.
   *
   * @param name namespace name
   */
  void deleteNamespace(final String name) throws IOException;

  /**
   * Delete an existing namespace. Only empty namespaces (no tables) can be removed.
   *
   * @param name namespace name
   * @return the result of the async delete namespace operation. Use Future.get(long, TimeUnit) to
   *  wait on the operation to complete.
   */
  Future<Void> deleteNamespaceAsync(final String name) throws IOException;

  /**
   * Get a namespace descriptor by name
   *
   * @param name name of namespace descriptor
   * @return A descriptor
   * @throws org.apache.hadoop.hbase.NamespaceNotFoundException
   * @throws IOException if a remote or network exception occurs
   */
  NamespaceDescriptor getNamespaceDescriptor(final String name)
  throws NamespaceNotFoundException, IOException;

  /**
   * List available namespace descriptors
   *
   * @return List of descriptors
   */
  NamespaceDescriptor[] listNamespaceDescriptors()
  throws IOException;

  /**
   * Get list of table descriptors by namespace
   *
   * @param name namespace name
   * @return HTD[] the read-only tableDescriptors
   * @throws IOException
   * @deprecated since 2.0 version and will be removed in 3.0 version.
   *             use {@link #listTableDescriptorsByNamespace(byte[])}
   */
  @Deprecated
  HTableDescriptor[] listTableDescriptorsByNamespace(final String name)
      throws IOException;

  /**
   * Get list of table descriptors by namespace
   *
   * @param name namespace name
   * @return returns a list of TableDescriptors
   * @throws IOException
   */
  List<TableDescriptor> listTableDescriptorsByNamespace(final byte[] name)
      throws IOException;

  /**
   * Get list of table names by namespace
   *
   * @param name namespace name
   * @return The list of table names in the namespace
   * @throws IOException
   */
  TableName[] listTableNamesByNamespace(final String name)
      throws IOException;

  /**
   * Get the regions of a given table.
   *
   * @param tableName the name of the table
   * @return List of {@link HRegionInfo}.
   * @throws IOException
   */
  List<HRegionInfo> getTableRegions(final TableName tableName)
    throws IOException;

  @Override
  void close() throws IOException;

  /**
   * Get tableDescriptors
   *
   * @param tableNames List of table names
   * @return HTD[] the read-only tableDescriptors
   * @throws IOException if a remote or network exception occurs
   * @deprecated since 2.0 version and will be removed in 3.0 version.
   *             use {@link #listTableDescriptors(List)}
   */
  @Deprecated
  HTableDescriptor[] getTableDescriptorsByTableName(List<TableName> tableNames)
    throws IOException;

  /**
   * Get tableDescriptors
   *
   * @param tableNames List of table names
   * @return returns a list of TableDescriptors
   * @throws IOException if a remote or network exception occurs
   */
  List<TableDescriptor> listTableDescriptors(List<TableName> tableNames)
    throws IOException;

  /**
   * Get tableDescriptors
   *
   * @param names List of table names
   * @return HTD[] the read-only tableDescriptors
   * @throws IOException if a remote or network exception occurs
   * @deprecated since 2.0 version and will be removed in 3.0 version.
   *             use {@link #listTableDescriptors(List)}
   */
  @Deprecated
  HTableDescriptor[] getTableDescriptors(List<String> names)
    throws IOException;

  /**
   * abort a procedure
   * @param procId ID of the procedure to abort
   * @param mayInterruptIfRunning if the proc completed at least one step, should it be aborted?
   * @return true if aborted, false if procedure already completed or does not exist
   * @throws IOException
   */
  boolean abortProcedure(
      final long procId,
      final boolean mayInterruptIfRunning) throws IOException;

  /**
   * Abort a procedure but does not block and wait for it be completely removed.
   * You can use Future.get(long, TimeUnit) to wait on the operation to complete.
   * It may throw ExecutionException if there was an error while executing the operation
   * or TimeoutException in case the wait timeout was not long enough to allow the
   * operation to complete.
   *
   * @param procId ID of the procedure to abort
   * @param mayInterruptIfRunning if the proc completed at least one step, should it be aborted?
   * @return true if aborted, false if procedure already completed or does not exist
   * @throws IOException
   */
  Future<Boolean> abortProcedureAsync(
    final long procId,
    final boolean mayInterruptIfRunning) throws IOException;

  /**
   * List procedures
   * @return procedure list
   * @throws IOException
   */
  ProcedureInfo[] listProcedures()
      throws IOException;

  /**
   * List locks.
   * @return lock list
   * @throws IOException if a remote or network exception occurs
   */
  LockInfo[] listLocks()
      throws IOException;

  /**
   * Roll the log writer. I.e. for filesystem based write ahead logs, start writing to a new file.
   *
   * Note that the actual rolling of the log writer is asynchronous and may not be complete when
   * this method returns. As a side effect of this call, the named region server may schedule
   * store flushes at the request of the wal.
   *
   * @param serverName The servername of the regionserver.
   * @throws IOException if a remote or network exception occurs
   * @throws org.apache.hadoop.hbase.regionserver.wal.FailedLogCloseException
   */
  void rollWALWriter(ServerName serverName) throws IOException, FailedLogCloseException;

  /**
   * Helper delegage to getClusterStatus().getMasterCoprocessors().
   * @return an array of master coprocessors
   * @see org.apache.hadoop.hbase.ClusterStatus#getMasterCoprocessors()
   */
  String[] getMasterCoprocessors() throws IOException;

  /**
   * Get the current compaction state of a table. It could be in a major compaction, a minor
   * compaction, both, or none.
   *
   * @param tableName table to examine
   * @return the current compaction state
   * @throws IOException if a remote or network exception occurs
   */
  CompactionState getCompactionState(final TableName tableName)
    throws IOException;

  /**
   * Get the current compaction state of region. It could be in a major compaction, a minor
   * compaction, both, or none.
   *
   * @param regionName region to examine
   * @return the current compaction state
   * @throws IOException if a remote or network exception occurs
   */
  CompactionState getCompactionStateForRegion(
    final byte[] regionName) throws IOException;

  /**
   * Get the timestamp of the last major compaction for the passed table
   *
   * The timestamp of the oldest HFile resulting from a major compaction of that table,
   * or 0 if no such HFile could be found.
   *
   * @param tableName table to examine
   * @return the last major compaction timestamp or 0
   * @throws IOException if a remote or network exception occurs
   */
  long getLastMajorCompactionTimestamp(final TableName tableName)
    throws IOException;

  /**
   * Get the timestamp of the last major compaction for the passed region.
   *
   * The timestamp of the oldest HFile resulting from a major compaction of that region,
   * or 0 if no such HFile could be found.
   *
   * @param regionName region to examine
   * @return the last major compaction timestamp or 0
   * @throws IOException if a remote or network exception occurs
   */
  long getLastMajorCompactionTimestampForRegion(final byte[] regionName)
      throws IOException;

  /**
   * Take a snapshot for the given table. If the table is enabled, a FLUSH-type snapshot will be
   * taken. If the table is disabled, an offline snapshot is taken. Snapshots are considered unique
   * based on <b>the name of the snapshot</b>. Attempts to take a snapshot with the same name (even
   * a different type or with different parameters) will fail with a {@link
   * org.apache.hadoop.hbase.snapshot.SnapshotCreationException} indicating the duplicate naming.
   * Snapshot names follow the same naming constraints as tables in HBase. See {@link
   * org.apache.hadoop.hbase.TableName#isLegalFullyQualifiedTableName(byte[])}.
   *
   * @param snapshotName name of the snapshot to be created
   * @param tableName name of the table for which snapshot is created
   * @throws IOException if a remote or network exception occurs
   * @throws org.apache.hadoop.hbase.snapshot.SnapshotCreationException if snapshot creation failed
   * @throws IllegalArgumentException if the snapshot request is formatted incorrectly
   */
  void snapshot(final String snapshotName, final TableName tableName)
      throws IOException, SnapshotCreationException, IllegalArgumentException;

  /**
   * Create a timestamp consistent snapshot for the given table. Snapshots are considered unique
   * based on <b>the name of the snapshot</b>. Attempts to take a snapshot with the same name (even
   * different type or with different parameters) will fail with a {@link SnapshotCreationException}
   * indicating the duplicate naming. Snapshot names follow the same naming constraints as tables in
   * HBase.
   *
   * @param snapshotName name of the snapshot to be created
   * @param tableName name of the table for which snapshot is created
   * @throws IOException if a remote or network exception occurs
   * @throws SnapshotCreationException if snapshot creation failed
   * @throws IllegalArgumentException if the snapshot request is formatted incorrectly
   */
  void snapshot(final byte[] snapshotName, final TableName tableName)
      throws IOException, SnapshotCreationException, IllegalArgumentException;

  /**
   * Create typed snapshot of the table. Snapshots are considered unique based on <b>the name of the
   * snapshot</b>. Attempts to take a snapshot with the same name (even a different type or with
   * different parameters) will fail with a {@link SnapshotCreationException} indicating the
   * duplicate naming. Snapshot names follow the same naming constraints as tables in HBase. See
   * {@link org.apache.hadoop.hbase.TableName#isLegalFullyQualifiedTableName(byte[])}.
   *
   * @param snapshotName name to give the snapshot on the filesystem. Must be unique from all other
   * snapshots stored on the cluster
   * @param tableName name of the table to snapshot
   * @param type type of snapshot to take
   * @throws IOException we fail to reach the master
   * @throws SnapshotCreationException if snapshot creation failed
   * @throws IllegalArgumentException if the snapshot request is formatted incorrectly
   */
  void snapshot(final String snapshotName,
      final TableName tableName,
      SnapshotType type) throws IOException, SnapshotCreationException,
      IllegalArgumentException;

  /**
   * Take a snapshot and wait for the server to complete that snapshot (blocking). Only a single
   * snapshot should be taken at a time for an instance of HBase, or results may be undefined (you
   * can tell multiple HBase clusters to snapshot at the same time, but only one at a time for a
   * single cluster). Snapshots are considered unique based on <b>the name of the snapshot</b>.
   * Attempts to take a snapshot with the same name (even a different type or with different
   * parameters) will fail with a {@link SnapshotCreationException} indicating the duplicate naming.
   * Snapshot names follow the same naming constraints as tables in HBase. See {@link
   * org.apache.hadoop.hbase.TableName#isLegalFullyQualifiedTableName(byte[])}. You should probably
   * use {@link #snapshot(String, org.apache.hadoop.hbase.TableName)} or
   * {@link #snapshot(byte[], org.apache.hadoop.hbase.TableName)} unless you are sure about the type
   * of snapshot that you want to take.
   *
   * @param snapshot snapshot to take
   * @throws IOException or we lose contact with the master.
   * @throws SnapshotCreationException if snapshot failed to be taken
   * @throws IllegalArgumentException if the snapshot request is formatted incorrectly
   */
  void snapshot(SnapshotDescription snapshot)
      throws IOException, SnapshotCreationException, IllegalArgumentException;

  /**
   * Take a snapshot without waiting for the server to complete that snapshot (asynchronous) Only a
   * single snapshot should be taken at a time, or results may be undefined.
   *
   * @param snapshot snapshot to take
   * @throws IOException if the snapshot did not succeed or we lose contact with the master.
   * @throws SnapshotCreationException if snapshot creation failed
   * @throws IllegalArgumentException if the snapshot request is formatted incorrectly
   */
  void takeSnapshotAsync(SnapshotDescription snapshot)
      throws IOException, SnapshotCreationException;

  /**
   * Check the current state of the passed snapshot. There are three possible states: <ol>
   * <li>running - returns <tt>false</tt></li> <li>finished - returns <tt>true</tt></li>
   * <li>finished with error - throws the exception that caused the snapshot to fail</li> </ol> The
   * cluster only knows about the most recent snapshot. Therefore, if another snapshot has been
   * run/started since the snapshot you are checking, you will receive an {@link
   * org.apache.hadoop.hbase.snapshot.UnknownSnapshotException}.
   *
   * @param snapshot description of the snapshot to check
   * @return <tt>true</tt> if the snapshot is completed, <tt>false</tt> if the snapshot is still
   * running
   * @throws IOException if we have a network issue
   * @throws org.apache.hadoop.hbase.snapshot.HBaseSnapshotException if the snapshot failed
   * @throws org.apache.hadoop.hbase.snapshot.UnknownSnapshotException if the requested snapshot is
   * unknown
   */
  boolean isSnapshotFinished(final SnapshotDescription snapshot)
      throws IOException, HBaseSnapshotException, UnknownSnapshotException;

  /**
   * Restore the specified snapshot on the original table. (The table must be disabled) If the
   * "hbase.snapshot.restore.take.failsafe.snapshot" configuration property is set to true, a
   * snapshot of the current table is taken before executing the restore operation. In case of
   * restore failure, the failsafe snapshot will be restored. If the restore completes without
   * problem the failsafe snapshot is deleted.
   *
   * @param snapshotName name of the snapshot to restore
   * @throws IOException if a remote or network exception occurs
   * @throws org.apache.hadoop.hbase.snapshot.RestoreSnapshotException if snapshot failed to be
   * restored
   * @throws IllegalArgumentException if the restore request is formatted incorrectly
   */
  void restoreSnapshot(final byte[] snapshotName) throws IOException, RestoreSnapshotException;

  /**
   * Restore the specified snapshot on the original table. (The table must be disabled) If the
   * "hbase.snapshot.restore.take.failsafe.snapshot" configuration property is set to true, a
   * snapshot of the current table is taken before executing the restore operation. In case of
   * restore failure, the failsafe snapshot will be restored. If the restore completes without
   * problem the failsafe snapshot is deleted.
   *
   * @param snapshotName name of the snapshot to restore
   * @throws IOException if a remote or network exception occurs
   * @throws RestoreSnapshotException if snapshot failed to be restored
   * @throws IllegalArgumentException if the restore request is formatted incorrectly
   */
  void restoreSnapshot(final String snapshotName) throws IOException, RestoreSnapshotException;

  /**
   * Restore the specified snapshot on the original table. (The table must be disabled) If the
   * "hbase.snapshot.restore.take.failsafe.snapshot" configuration property is set to true, a
   * snapshot of the current table is taken before executing the restore operation. In case of
   * restore failure, the failsafe snapshot will be restored. If the restore completes without
   * problem the failsafe snapshot is deleted.
   *
   * @param snapshotName name of the snapshot to restore
   * @throws IOException if a remote or network exception occurs
   * @throws RestoreSnapshotException if snapshot failed to be restored
   * @return the result of the async restore snapshot. You can use Future.get(long, TimeUnit)
   *    to wait on the operation to complete.
   */
  Future<Void> restoreSnapshotAsync(final String snapshotName)
      throws IOException, RestoreSnapshotException;

  /**
   * Restore the specified snapshot on the original table. (The table must be disabled) If
   * 'takeFailSafeSnapshot' is set to true, a snapshot of the current table is taken before
   * executing the restore operation. In case of restore failure, the failsafe snapshot will be
   * restored. If the restore completes without problem the failsafe snapshot is deleted. The
   * failsafe snapshot name is configurable by using the property
   * "hbase.snapshot.restore.failsafe.name".
   *
   * @param snapshotName name of the snapshot to restore
   * @param takeFailSafeSnapshot true if the failsafe snapshot should be taken
   * @throws IOException if a remote or network exception occurs
   * @throws RestoreSnapshotException if snapshot failed to be restored
   * @throws IllegalArgumentException if the restore request is formatted incorrectly
   */
  void restoreSnapshot(final byte[] snapshotName, final boolean takeFailSafeSnapshot)
      throws IOException, RestoreSnapshotException;

  /**
   * Restore the specified snapshot on the original table. (The table must be disabled) If
   * 'takeFailSafeSnapshot' is set to true, a snapshot of the current table is taken before
   * executing the restore operation. In case of restore failure, the failsafe snapshot will be
   * restored. If the restore completes without problem the failsafe snapshot is deleted. The
   * failsafe snapshot name is configurable by using the property
   * "hbase.snapshot.restore.failsafe.name".
   *
   * @param snapshotName name of the snapshot to restore
   * @param takeFailSafeSnapshot true if the failsafe snapshot should be taken
   * @throws IOException if a remote or network exception occurs
   * @throws RestoreSnapshotException if snapshot failed to be restored
   * @throws IllegalArgumentException if the restore request is formatted incorrectly
   */
  void restoreSnapshot(final String snapshotName, final boolean takeFailSafeSnapshot)
      throws IOException, RestoreSnapshotException;

  /**
   * Restore the specified snapshot on the original table. (The table must be disabled) If
   * 'takeFailSafeSnapshot' is set to true, a snapshot of the current table is taken before
   * executing the restore operation. In case of restore failure, the failsafe snapshot will be
   * restored. If the restore completes without problem the failsafe snapshot is deleted. The
   * failsafe snapshot name is configurable by using the property
   * "hbase.snapshot.restore.failsafe.name".
   * @param snapshotName name of the snapshot to restore
   * @param takeFailSafeSnapshot true if the failsafe snapshot should be taken
   * @param restoreAcl true to restore acl of snapshot
   * @throws IOException if a remote or network exception occurs
   * @throws RestoreSnapshotException if snapshot failed to be restored
   * @throws IllegalArgumentException if the restore request is formatted incorrectly
   */
  void restoreSnapshot(final String snapshotName, final boolean takeFailSafeSnapshot,
      final boolean restoreAcl) throws IOException, RestoreSnapshotException;

  /**
   * Create a new table by cloning the snapshot content.
   *
   * @param snapshotName name of the snapshot to be cloned
   * @param tableName name of the table where the snapshot will be restored
   * @throws IOException if a remote or network exception occurs
   * @throws TableExistsException if table to be created already exists
   * @throws RestoreSnapshotException if snapshot failed to be cloned
   * @throws IllegalArgumentException if the specified table has not a valid name
   */
  void cloneSnapshot(final byte[] snapshotName, final TableName tableName)
      throws IOException, TableExistsException, RestoreSnapshotException;

  /**
   * Create a new table by cloning the snapshot content.
   * @param snapshotName name of the snapshot to be cloned
   * @param tableName name of the table where the snapshot will be restored
   * @param restoreAcl true to clone acl into newly created table
   * @throws IOException if a remote or network exception occurs
   * @throws TableExistsException if table to be created already exists
   * @throws RestoreSnapshotException if snapshot failed to be cloned
   * @throws IllegalArgumentException if the specified table has not a valid name
   */
  void cloneSnapshot(final String snapshotName, final TableName tableName, final boolean restoreAcl)
      throws IOException, TableExistsException, RestoreSnapshotException;

  /**
   * Create a new table by cloning the snapshot content.
   *
   * @param snapshotName name of the snapshot to be cloned
   * @param tableName name of the table where the snapshot will be restored
   * @throws IOException if a remote or network exception occurs
   * @throws TableExistsException if table to be created already exists
   * @throws RestoreSnapshotException if snapshot failed to be cloned
   * @throws IllegalArgumentException if the specified table has not a valid name
   */
  void cloneSnapshot(final String snapshotName, final TableName tableName)
      throws IOException, TableExistsException, RestoreSnapshotException;

  /**
   * Create a new table by cloning the snapshot content, but does not block
   * and wait for it be completely cloned.
   * You can use Future.get(long, TimeUnit) to wait on the operation to complete.
   * It may throw ExecutionException if there was an error while executing the operation
   * or TimeoutException in case the wait timeout was not long enough to allow the
   * operation to complete.
   *
   * @param snapshotName name of the snapshot to be cloned
   * @param tableName name of the table where the snapshot will be restored
   * @throws IOException if a remote or network exception occurs
   * @throws TableExistsException if table to be cloned already exists
   * @return the result of the async clone snapshot. You can use Future.get(long, TimeUnit)
   *    to wait on the operation to complete.
   */
  Future<Void> cloneSnapshotAsync(final String snapshotName, final TableName tableName)
      throws IOException, TableExistsException;

  /**
   * Execute a distributed procedure on a cluster.
   *
   * @param signature A distributed procedure is uniquely identified by its signature (default the
   * root ZK node name of the procedure).
   * @param instance The instance name of the procedure. For some procedures, this parameter is
   * optional.
   * @param props Property/Value pairs of properties passing to the procedure
   * @throws IOException
   */
  void execProcedure(String signature, String instance, Map<String, String> props)
      throws IOException;

  /**
   * Execute a distributed procedure on a cluster.
   *
   * @param signature A distributed procedure is uniquely identified by its signature (default the
   * root ZK node name of the procedure).
   * @param instance The instance name of the procedure. For some procedures, this parameter is
   * optional.
   * @param props Property/Value pairs of properties passing to the procedure
   * @return data returned after procedure execution. null if no return data.
   * @throws IOException
   */
  byte[] execProcedureWithRet(String signature, String instance, Map<String, String> props)
      throws IOException;

  /**
   * Check the current state of the specified procedure. There are three possible states: <ol>
   * <li>running - returns <tt>false</tt></li> <li>finished - returns <tt>true</tt></li>
   * <li>finished with error - throws the exception that caused the procedure to fail</li> </ol>
   *
   * @param signature The signature that uniquely identifies a procedure
   * @param instance The instance name of the procedure
   * @param props Property/Value pairs of properties passing to the procedure
   * @return true if the specified procedure is finished successfully, false if it is still running
   * @throws IOException if the specified procedure finished with error
   */
  boolean isProcedureFinished(String signature, String instance, Map<String, String> props)
      throws IOException;

  /**
   * List completed snapshots.
   *
   * @return a list of snapshot descriptors for completed snapshots
   * @throws IOException if a network error occurs
   */
  List<SnapshotDescription> listSnapshots() throws IOException;

  /**
   * List all the completed snapshots matching the given regular expression.
   *
   * @param regex The regular expression to match against
   * @return - returns a List of SnapshotDescription
   * @throws IOException if a remote or network exception occurs
   */
  List<SnapshotDescription> listSnapshots(String regex) throws IOException;

  /**
   * List all the completed snapshots matching the given pattern.
   *
   * @param pattern The compiled regular expression to match against
   * @return - returns a List of SnapshotDescription
   * @throws IOException if a remote or network exception occurs
   */
  List<SnapshotDescription> listSnapshots(Pattern pattern) throws IOException;

  /**
   * List all the completed snapshots matching the given table name regular expression and snapshot
   * name regular expression.
   * @param tableNameRegex The table name regular expression to match against
   * @param snapshotNameRegex The snapshot name regular expression to match against
   * @return - returns a List of completed SnapshotDescription
   * @throws IOException if a remote or network exception occurs
   */
  List<SnapshotDescription> listTableSnapshots(String tableNameRegex,
      String snapshotNameRegex) throws IOException;

  /**
   * List all the completed snapshots matching the given table name regular expression and snapshot
   * name regular expression.
   * @param tableNamePattern The compiled table name regular expression to match against
   * @param snapshotNamePattern The compiled snapshot name regular expression to match against
   * @return - returns a List of completed SnapshotDescription
   * @throws IOException if a remote or network exception occurs
   */
  List<SnapshotDescription> listTableSnapshots(Pattern tableNamePattern,
      Pattern snapshotNamePattern) throws IOException;

  /**
   * Delete an existing snapshot.
   *
   * @param snapshotName name of the snapshot
   * @throws IOException if a remote or network exception occurs
   */
  void deleteSnapshot(final byte[] snapshotName) throws IOException;

  /**
   * Delete an existing snapshot.
   *
   * @param snapshotName name of the snapshot
   * @throws IOException if a remote or network exception occurs
   */
  void deleteSnapshot(final String snapshotName) throws IOException;

  /**
   * Delete existing snapshots whose names match the pattern passed.
   *
   * @param regex The regular expression to match against
   * @throws IOException if a remote or network exception occurs
   */
  void deleteSnapshots(final String regex) throws IOException;

  /**
   * Delete existing snapshots whose names match the pattern passed.
   *
   * @param pattern pattern for names of the snapshot to match
   * @throws IOException if a remote or network exception occurs
   */
  void deleteSnapshots(final Pattern pattern) throws IOException;

  /**
   * Delete all existing snapshots matching the given table name regular expression and snapshot
   * name regular expression.
   * @param tableNameRegex The table name regular expression to match against
   * @param snapshotNameRegex The snapshot name regular expression to match against
   * @throws IOException if a remote or network exception occurs
   */
  void deleteTableSnapshots(String tableNameRegex, String snapshotNameRegex) throws IOException;

  /**
   * Delete all existing snapshots matching the given table name regular expression and snapshot
   * name regular expression.
   * @param tableNamePattern The compiled table name regular expression to match against
   * @param snapshotNamePattern The compiled snapshot name regular expression to match against
   * @throws IOException if a remote or network exception occurs
   */
  void deleteTableSnapshots(Pattern tableNamePattern, Pattern snapshotNamePattern)
      throws IOException;

  /**
   * Apply the new quota settings.
   *
   * @param quota the quota settings
   * @throws IOException if a remote or network exception occurs
   */
  void setQuota(final QuotaSettings quota) throws IOException;

  /**
   * Return a QuotaRetriever to list the quotas based on the filter.
   *
   * @param filter the quota settings filter
   * @return the quota retriever
   * @throws IOException if a remote or network exception occurs
   */
  QuotaRetriever getQuotaRetriever(final QuotaFilter filter) throws IOException;

  /**
   * Creates and returns a {@link com.google.protobuf.RpcChannel} instance connected to the active
   * master. <p> The obtained {@link com.google.protobuf.RpcChannel} instance can be used to access
   * a published coprocessor {@link com.google.protobuf.Service} using standard protobuf service
   * invocations: </p> <div style="background-color: #cccccc; padding: 2px">
   * <blockquote><pre>
   * CoprocessorRpcChannel channel = myAdmin.coprocessorService();
   * MyService.BlockingInterface service = MyService.newBlockingStub(channel);
   * MyCallRequest request = MyCallRequest.newBuilder()
   *     ...
   *     .build();
   * MyCallResponse response = service.myCall(null, request);
   * </pre></blockquote></div>
   *
   * @return A MasterCoprocessorRpcChannel instance
   */
  CoprocessorRpcChannel coprocessorService();


  /**
   * Creates and returns a {@link com.google.protobuf.RpcChannel} instance
   * connected to the passed region server.
   *
   * <p>
   * The obtained {@link com.google.protobuf.RpcChannel} instance can be used to access a published
   * coprocessor {@link com.google.protobuf.Service} using standard protobuf service invocations:
   * </p>
   *
   * <div style="background-color: #cccccc; padding: 2px">
   * <blockquote><pre>
   * CoprocessorRpcChannel channel = myAdmin.coprocessorService(serverName);
   * MyService.BlockingInterface service = MyService.newBlockingStub(channel);
   * MyCallRequest request = MyCallRequest.newBuilder()
   *     ...
   *     .build();
   * MyCallResponse response = service.myCall(null, request);
   * </pre></blockquote></div>
   *
   * @param sn the server name to which the endpoint call is made
   * @return A RegionServerCoprocessorRpcChannel instance
   */
  CoprocessorRpcChannel coprocessorService(ServerName sn);


  /**
   * Update the configuration and trigger an online config change
   * on the regionserver
   * @param server : The server whose config needs to be updated.
   * @throws IOException
   */
  void updateConfiguration(ServerName server) throws IOException;


  /**
   * Update the configuration and trigger an online config change
   * on all the regionservers
   * @throws IOException
   */
  void updateConfiguration() throws IOException;

  /**
   * Get the info port of the current master if one is available.
   * @return master info port
   * @throws IOException
   */
  public int getMasterInfoPort() throws IOException;

  /**
   * Compact a table. Asynchronous operation.
   *
   * @param tableName table to compact
   * @param compactType {@link org.apache.hadoop.hbase.client.CompactType}
   * @throws IOException
   * @throws InterruptedException
   */
  void compact(final TableName tableName, CompactType compactType)
    throws IOException, InterruptedException;

  /**
   * Compact a column family within a table. Asynchronous operation.
   *
   * @param tableName table to compact
   * @param columnFamily column family within a table
   * @param compactType {@link org.apache.hadoop.hbase.client.CompactType}
   * @throws IOException if not a mob column family or if a remote or network exception occurs
   * @throws InterruptedException
   */
  void compact(final TableName tableName, final byte[] columnFamily, CompactType compactType)
    throws IOException, InterruptedException;

  /**
   * Major compact a table. Asynchronous operation.
   *
   * @param tableName table to compact
   * @param compactType {@link org.apache.hadoop.hbase.client.CompactType}
   * @throws IOException
   * @throws InterruptedException
   */
  void majorCompact(final TableName tableName, CompactType compactType)
    throws IOException, InterruptedException;

  /**
   * Major compact a column family within a table. Asynchronous operation.
   *
   * @param tableName table to compact
   * @param columnFamily column family within a table
   * @param compactType {@link org.apache.hadoop.hbase.client.CompactType}
   * @throws IOException if not a mob column family or if a remote or network exception occurs
   * @throws InterruptedException
   */
  void majorCompact(final TableName tableName, final byte[] columnFamily, CompactType compactType)
    throws IOException, InterruptedException;

  /**
   * Get the current compaction state of a table. It could be in a compaction, or none.
   *
   * @param tableName table to examine
   * @param compactType {@link org.apache.hadoop.hbase.client.CompactType}
   * @return the current compaction state
   * @throws IOException if a remote or network exception occurs
   */
  CompactionState getCompactionState(final TableName tableName,
    CompactType compactType) throws IOException;

  /**
   * Return the set of supported security capabilities.
   * @throws IOException
   * @throws UnsupportedOperationException
   */
  List<SecurityCapability> getSecurityCapabilities() throws IOException;

  /**
   * Turn the Split or Merge switches on or off.
   *
   * @param enabled enabled or not
   * @param synchronous If true, it waits until current split() call, if outstanding, to return.
   * @param switchTypes switchType list {@link MasterSwitchType}
   * @return Previous switch value array
   */
  boolean[] setSplitOrMergeEnabled(final boolean enabled, final boolean synchronous,
                                   final MasterSwitchType... switchTypes) throws IOException;

  /**
   * Query the current state of the switch
   *
   * @return true if the switch is enabled, false otherwise.
   */
  boolean isSplitOrMergeEnabled(final MasterSwitchType switchType) throws IOException;

  /**
   * Add a new replication peer for replicating data to slave cluster
   * @param peerId a short name that identifies the peer
   * @param peerConfig configuration for the replication slave cluster
   * @throws IOException
   */
  default void addReplicationPeer(final String peerId, final ReplicationPeerConfig peerConfig)
      throws IOException {
  }

  /**
   * Remove a peer and stop the replication
   * @param peerId a short name that identifies the peer
   * @throws IOException
   */
  default void removeReplicationPeer(final String peerId) throws IOException {
  }

  /**
   * Restart the replication stream to the specified peer
   * @param peerId a short name that identifies the peer
   * @throws IOException
   */
  default void enableReplicationPeer(final String peerId) throws IOException {
  }

  /**
   * Stop the replication stream to the specified peer
   * @param peerId a short name that identifies the peer
   * @throws IOException
   */
  default void disableReplicationPeer(final String peerId) throws IOException {
  }

  /**
   * Returns the configured ReplicationPeerConfig for the specified peer
   * @param peerId a short name that identifies the peer
   * @return ReplicationPeerConfig for the peer
   * @throws IOException
   */
  default ReplicationPeerConfig getReplicationPeerConfig(final String peerId) throws IOException {
    return new ReplicationPeerConfig();
  }

  /**
   * Update the peerConfig for the specified peer
   * @param peerId a short name that identifies the peer
   * @param peerConfig new config for the peer
   * @throws IOException
   */
  default void updateReplicationPeerConfig(final String peerId,
      final ReplicationPeerConfig peerConfig) throws IOException {
  }

  /**
   * Append the replicable table-cf config of the specified peer
   * @param id a short that identifies the cluster
   * @param tableCfs A map from tableName to column family names
   * @throws ReplicationException
   * @throws IOException
   */
  default void appendReplicationPeerTableCFs(String id,
      Map<TableName, ? extends Collection<String>> tableCfs) throws ReplicationException,
      IOException {
  }

  /**
   * Remove some table-cfs from config of the specified peer
   * @param id a short name that identifies the cluster
   * @param tableCfs A map from tableName to column family names
   * @throws ReplicationException
   * @throws IOException
   */
  default void removeReplicationPeerTableCFs(String id,
      Map<TableName, ? extends Collection<String>> tableCfs) throws ReplicationException,
      IOException {
  }

  /**
   * Return a list of replication peers.
   * @return a list of replication peers description
   * @throws IOException
   */
  default List<ReplicationPeerDescription> listReplicationPeers() throws IOException {
    return new ArrayList<>();
  }

  /**
   * Return a list of replication peers.
   * @param regex The regular expression to match peer id
   * @return a list of replication peers description
   * @throws IOException
   */
  default List<ReplicationPeerDescription> listReplicationPeers(String regex) throws IOException {
    return new ArrayList<>();
  }

  /**
   * Return a list of replication peers.
   * @param pattern The compiled regular expression to match peer id
   * @return a list of replication peers description
   * @throws IOException
   */
  default List<ReplicationPeerDescription> listReplicationPeers(Pattern pattern) throws IOException {
    return new ArrayList<>();
  }

  /**
   * Mark a region server as draining to prevent additional regions from getting assigned to it.
   * @param servers List of region servers to drain.
   */
  void drainRegionServers(List<ServerName> servers) throws IOException;

  /**
   * List region servers marked as draining to not get additional regions assigned to them.
   * @return List of draining region servers.
   */
  List<ServerName> listDrainingRegionServers() throws IOException;

  /**
   * Remove drain from a region server to allow additional regions assignments.
   * @param servers List of region servers to remove drain from.
   */
  void removeDrainFromRegionServers(List<ServerName> servers) throws IOException;

  /**
   * Find all table and column families that are replicated from this cluster
   * @return the replicated table-cfs list of this cluster.
   */
  List<TableCFs> listReplicatedTableCFs() throws IOException;

  /**
   * Enable a table's replication switch.
   * @param tableName name of the table
   * @throws IOException if a remote or network exception occurs
   */
  void enableTableReplication(final TableName tableName) throws IOException;

  /**
   * Disable a table's replication switch.
   * @param tableName name of the table
   * @throws IOException if a remote or network exception occurs
   */
  void disableTableReplication(final TableName tableName) throws IOException;

  /**
   * Clear compacting queues on a regionserver.
   * @param sn the region server name
   * @param queues the set of queue name
   * @throws IOException if a remote or network exception occurs
   * @throws InterruptedException
   */
  void clearCompactionQueues(final ServerName sn, final Set<String> queues)
    throws IOException, InterruptedException;
}
