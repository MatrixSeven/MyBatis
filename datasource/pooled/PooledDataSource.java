/*
 *    Copyright 2009-2012 The MyBatis Team
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.datasource.pooled;

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * This is a simple, synchronous, thread-safe database connection pool.
 * 连接池数据源
 */
public class PooledDataSource implements DataSource {

	private static final Log log = LogFactory.getLog(PooledDataSource.class);

	private final PoolState state = new PoolState(this);
	/**
	 *  数据库注册提供者（连接提供者）
	 */
	private final UnpooledDataSource dataSource;

	// OPTIONAL CONFIGURATION FIELDS
	protected int poolMaximumActiveConnections = 10;
	protected int poolMaximumIdleConnections = 5;
	protected int poolMaximumCheckoutTime = 20000;
	protected int poolTimeToWait = 20000;
	protected String poolPingQuery = "NO PING QUERY SET";
	protected boolean poolPingEnabled = false;
	protected int poolPingConnectionsNotUsedFor = 0;

	private int expectedConnectionTypeCode;

	public PooledDataSource() {
		dataSource = new UnpooledDataSource();
	}

	public PooledDataSource(String driver, String url, String username, String password) {
		dataSource = new UnpooledDataSource(driver, url, username, password);
		expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(),
				dataSource.getPassword());
	}

	public PooledDataSource(String driver, String url, Properties driverProperties) {
		dataSource = new UnpooledDataSource(driver, url, driverProperties);
		expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(),
				dataSource.getPassword());
	}

	public PooledDataSource(ClassLoader driverClassLoader, String driver, String url, String username,
			String password) {
		dataSource = new UnpooledDataSource(driverClassLoader, driver, url, username, password);
		expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(),
				dataSource.getPassword());
	}

	public PooledDataSource(ClassLoader driverClassLoader, String driver, String url, Properties driverProperties) {
		dataSource = new UnpooledDataSource(driverClassLoader, driver, url, driverProperties);
		expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(),
				dataSource.getPassword());
	}

	public Connection getConnection() throws SQLException {
		return popConnection(dataSource.getUsername(), dataSource.getPassword()).getProxyConnection();
	}

	public Connection getConnection(String username, String password) throws SQLException {
		return popConnection(username, password).getProxyConnection();
	}

	public void setLoginTimeout(int loginTimeout) throws SQLException {
		DriverManager.setLoginTimeout(loginTimeout);
	}

	public int getLoginTimeout() throws SQLException {
		return DriverManager.getLoginTimeout();
	}

	public void setLogWriter(PrintWriter logWriter) throws SQLException {
		DriverManager.setLogWriter(logWriter);
	}

	public PrintWriter getLogWriter() throws SQLException {
		return DriverManager.getLogWriter();
	}

	public void setDriver(String driver) {
		dataSource.setDriver(driver);
		forceCloseAll();
	}

	public void setUrl(String url) {
		dataSource.setUrl(url);
		forceCloseAll();
	}

	public void setUsername(String username) {
		dataSource.setUsername(username);
		forceCloseAll();
	}

	public void setPassword(String password) {
		dataSource.setPassword(password);
		forceCloseAll();
	}

	public void setDefaultAutoCommit(boolean defaultAutoCommit) {
		dataSource.setAutoCommit(defaultAutoCommit);
		forceCloseAll();
	}

	public void setDefaultTransactionIsolationLevel(Integer defaultTransactionIsolationLevel) {
		dataSource.setDefaultTransactionIsolationLevel(defaultTransactionIsolationLevel);
		forceCloseAll();
	}

	public void setDriverProperties(Properties driverProps) {
		dataSource.setDriverProperties(driverProps);
		forceCloseAll();
	}

	/*
	 * The maximum number of active connections
	 *
	 * @param poolMaximumActiveConnections The maximum number of active
	 * connections
	 */
	public void setPoolMaximumActiveConnections(int poolMaximumActiveConnections) {
		this.poolMaximumActiveConnections = poolMaximumActiveConnections;
		forceCloseAll();
	}

	/*
	 * The maximum number of idle connections
	 *
	 * @param poolMaximumIdleConnections The maximum number of idle connections
	 */
	public void setPoolMaximumIdleConnections(int poolMaximumIdleConnections) {
		this.poolMaximumIdleConnections = poolMaximumIdleConnections;
		forceCloseAll();
	}

	/*
	 * The maximum time a connection can be used before it *may* be given away
	 * again.
	 *
	 * @param poolMaximumCheckoutTime The maximum time
	 */
	public void setPoolMaximumCheckoutTime(int poolMaximumCheckoutTime) {
		this.poolMaximumCheckoutTime = poolMaximumCheckoutTime;
		forceCloseAll();
	}

	/*
	 * The time to wait before retrying to get a connection
	 *
	 * @param poolTimeToWait The time to wait
	 */
	public void setPoolTimeToWait(int poolTimeToWait) {
		this.poolTimeToWait = poolTimeToWait;
		forceCloseAll();
	}

	/*
	 * The query to be used to check a connection
	 *
	 * @param poolPingQuery The query
	 */
	public void setPoolPingQuery(String poolPingQuery) {
		this.poolPingQuery = poolPingQuery;
		forceCloseAll();
	}

	/*
	 * Determines if the ping query should be used.
	 *
	 * @param poolPingEnabled True if we need to check a connection before using
	 * it
	 */
	public void setPoolPingEnabled(boolean poolPingEnabled) {
		this.poolPingEnabled = poolPingEnabled;
		forceCloseAll();
	}

	/*
	 * If a connection has not been used in this many milliseconds, ping the
	 * database to make sure the connection is still good.
	 *
	 * @param milliseconds the number of milliseconds of inactivity that will
	 * trigger a ping
	 */
	public void setPoolPingConnectionsNotUsedFor(int milliseconds) {
		this.poolPingConnectionsNotUsedFor = milliseconds;
		forceCloseAll();
	}

	public String getDriver() {
		return dataSource.getDriver();
	}

	public String getUrl() {
		return dataSource.getUrl();
	}

	public String getUsername() {
		return dataSource.getUsername();
	}

	public String getPassword() {
		return dataSource.getPassword();
	}

	public boolean isAutoCommit() {
		return dataSource.isAutoCommit();
	}

	public Integer getDefaultTransactionIsolationLevel() {
		return dataSource.getDefaultTransactionIsolationLevel();
	}

	public Properties getDriverProperties() {
		return dataSource.getDriverProperties();
	}

	public int getPoolMaximumActiveConnections() {
		return poolMaximumActiveConnections;
	}

	public int getPoolMaximumIdleConnections() {
		return poolMaximumIdleConnections;
	}

	public int getPoolMaximumCheckoutTime() {
		return poolMaximumCheckoutTime;
	}

	public int getPoolTimeToWait() {
		return poolTimeToWait;
	}

	public String getPoolPingQuery() {
		return poolPingQuery;
	}

	public boolean isPoolPingEnabled() {
		return poolPingEnabled;
	}

	public int getPoolPingConnectionsNotUsedFor() {
		return poolPingConnectionsNotUsedFor;
	}

	/**
	 * Closes all active and idle connections in the pool
	 * 关闭所有连接
	 */
	public void forceCloseAll() {
		synchronized (state) {
			expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(),
					dataSource.getPassword());
			for (int i = state.activeConnections.size(); i > 0; i--) {
				try {
					PooledConnection conn = state.activeConnections.remove(i - 1);
					conn.invalidate();

					Connection realConn = conn.getRealConnection();
					if (!realConn.getAutoCommit()) {
						realConn.rollback();
					}
					realConn.close();
				} catch (Exception e) {
					// ignore
				}
			}
			for (int i = state.idleConnections.size(); i > 0; i--) {
				try {
					PooledConnection conn = state.idleConnections.remove(i - 1);
					conn.invalidate();

					Connection realConn = conn.getRealConnection();
					if (!realConn.getAutoCommit()) {
						realConn.rollback();
					}
					realConn.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("PooledDataSource forcefully closed/removed all connections.");
		}
	}

	public PoolState getPoolState() {
		return state;
	}

	private int assembleConnectionTypeCode(String url, String username, String password) {
		return ("" + url + username + password).hashCode();
	}

	protected void pushConnection(PooledConnection conn) throws SQLException {

		synchronized (state) {
			state.activeConnections.remove(conn);
			if (conn.isValid()) {
				if (state.idleConnections.size() < poolMaximumIdleConnections
						&& conn.getConnectionTypeCode() == expectedConnectionTypeCode) {
					state.accumulatedCheckoutTime += conn.getCheckoutTime();
					if (!conn.getRealConnection().getAutoCommit()) {
						conn.getRealConnection().rollback();
					}
					PooledConnection newConn = new PooledConnection(conn.getRealConnection(), this);
					state.idleConnections.add(newConn);
					newConn.setCreatedTimestamp(conn.getCreatedTimestamp());
					newConn.setLastUsedTimestamp(conn.getLastUsedTimestamp());
					conn.invalidate();
					if (log.isDebugEnabled()) {
						log.debug("Returned connection " + newConn.getRealHashCode() + " to pool.");
					}
					state.notifyAll();
				} else {
					state.accumulatedCheckoutTime += conn.getCheckoutTime();
					if (!conn.getRealConnection().getAutoCommit()) {
						conn.getRealConnection().rollback();
					}
					conn.getRealConnection().close();
					if (log.isDebugEnabled()) {
						log.debug("Closed connection " + conn.getRealHashCode() + ".");
					}
					conn.invalidate();
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug("A bad connection (" + conn.getRealHashCode()
							+ ") attempted to return to the pool, discarding connection.");
				}
				state.badConnectionCount++;
			}
		}
	}

	/**
	 * 获取数据库连接
	 * @author Seven
	 * @param username
	 * @param password
	 * @return
	 * @throws SQLException 
	 * @return PooledConnection
	 * @time 2016年10月9日-下午4:59:25
	 */
	private PooledConnection popConnection(String username, String password) throws SQLException {
		boolean countedWait = false;
		PooledConnection conn = null;
		long t = System.currentTimeMillis();
		int localBadConnectionCount = 0;

		while (conn == null) {
			synchronized (state) {
				//等待使用的连接
				if (state.idleConnections.size() > 0) {
					// Pool has available connection
					//取出连接
					conn = state.idleConnections.remove(0);
					if (log.isDebugEnabled()) {
						log.debug("Checked out connection " + conn.getRealHashCode() + " from pool.");
					}
				} else {
					// Pool does not have available connection
					//判断活跃的连接是否达到了最大数
					if (state.activeConnections.size() < poolMaximumActiveConnections) {
						// Can create new connection
						//获取一个新的数据库连接代理对象，dataSource 提供原始链接
						conn = new PooledConnection(dataSource.getConnection(), this);
						@SuppressWarnings("unused")
						// used in logging, if enabled
						//拿到真正的Connection对象
						Connection realConn = conn.getRealConnection();
						if (log.isDebugEnabled()) {
							log.debug("Created connection " + conn.getRealHashCode() + ".");
						}
					} else {
						// Cannot create new connection
						//连接数达到最大数，在连接容器里拿到活跃的连接
						PooledConnection oldestActiveConnection = state.activeConnections.get(0);
						//检查当前连接是否超时
						long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();
						//检查当前连接是否超时
						if (longestCheckoutTime > poolMaximumCheckoutTime) {
							// Can claim overdue connection
							state.claimedOverdueConnectionCount++;
							state.accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime;
							state.accumulatedCheckoutTime += longestCheckoutTime;
							state.activeConnections.remove(oldestActiveConnection);
							//检测是否自动提交，执行回滚
							if (!oldestActiveConnection.getRealConnection().getAutoCommit()) {
								oldestActiveConnection.getRealConnection().rollback();
							}
							//拿到当前代理的connect实体对象，再次获取connect对象
							conn = new PooledConnection(oldestActiveConnection.getRealConnection(), this);
							//恢复连接为不活跃的等待状态
							oldestActiveConnection.invalidate();
							if (log.isDebugEnabled()) {
								log.debug("Claimed overdue connection " + conn.getRealHashCode() + ".");
							}
						} else {
							// Must wait
							//没有超时，无比等待
							try {
								if (!countedWait) {
									//等待释放的连接数
									state.hadToWaitCount++;
									//等待标识符
									countedWait = true;
								}
								if (log.isDebugEnabled()) {
									log.debug("Waiting as long as " + poolTimeToWait + " milliseconds for connection.");
								}
								long wt = System.currentTimeMillis();
								state.wait(poolTimeToWait);
								//休眠花费时间
								state.accumulatedWaitTime += System.currentTimeMillis() - wt;
							} catch (InterruptedException e) {
								break;
							}
						}
					}
				}
				if (conn != null) {
					if (conn.isValid()) {
						if (!conn.getRealConnection().getAutoCommit()) {
							conn.getRealConnection().rollback();
						}
						//hashcode
						conn.setConnectionTypeCode(assembleConnectionTypeCode(dataSource.getUrl(), username, password));
						conn.setCheckoutTimestamp(System.currentTimeMillis());
						conn.setLastUsedTimestamp(System.currentTimeMillis());
						//加入活动连接
						state.activeConnections.add(conn);
						state.requestCount++;
						state.accumulatedRequestTime += System.currentTimeMillis() - t;
					} else {
						if (log.isDebugEnabled()) {
							log.debug("A bad connection (" + conn.getRealHashCode()
									+ ") was returned from the pool, getting another connection.");
						}
						state.badConnectionCount++;
						localBadConnectionCount++;
						conn = null;
						if (localBadConnectionCount > (poolMaximumIdleConnections + 3)) {
							if (log.isDebugEnabled()) {
								log.debug("PooledDataSource: Could not get a good connection to the database.");
							}
							throw new SQLException(
									"PooledDataSource: Could not get a good connection to the database.");
						}
					}
				}
			}

		}

		if (conn == null) {
			if (log.isDebugEnabled()) {
				log.debug(
						"PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
			}
			throw new SQLException(
					"PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
		}

		return conn;
	}

	/**
	 * Method to check to see if a connection is still usable
	 *
	 * @param conn - the connection to check
	 * 
	 * @return True if the connection is still usable
	 */
	protected boolean pingConnection(PooledConnection conn) {
		boolean result = true;

		try {
			//原始链接是否被关闭
			result = !conn.getRealConnection().isClosed();
		} catch (SQLException e) {
			if (log.isDebugEnabled()) {
				log.debug("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
			}
			result = false;
		}

		if (result) {
			if (poolPingEnabled) {
				if (poolPingConnectionsNotUsedFor >= 0
						&& conn.getTimeElapsedSinceLastUse() > poolPingConnectionsNotUsedFor) {
					try {
						if (log.isDebugEnabled()) {
							log.debug("Testing connection " + conn.getRealHashCode() + " ...");
						}
						Connection realConn = conn.getRealConnection();
						Statement statement = realConn.createStatement();
						ResultSet rs = statement.executeQuery(poolPingQuery);
						rs.close();
						statement.close();
						if (!realConn.getAutoCommit()) {
							realConn.rollback();
						}
						result = true;
						if (log.isDebugEnabled()) {
							log.debug("Connection " + conn.getRealHashCode() + " is GOOD!");
						}
					} catch (Exception e) {
						log.warn("Execution of ping query '" + poolPingQuery + "' failed: " + e.getMessage());
						try {
							conn.getRealConnection().close();
						} catch (Exception e2) {
							// ignore
						}
						result = false;
						if (log.isDebugEnabled()) {
							log.debug("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
						}
					}
				}
			}
		}
		return result;
	}

	/*
	 * Unwraps a pooled connection to get to the 'real' connection
	 *
	 * @param conn - the pooled connection to unwrap
	 * 
	 * @return The 'real' connection
	 */
	public static Connection unwrapConnection(Connection conn) {
		if (Proxy.isProxyClass(conn.getClass())) {
			InvocationHandler handler = Proxy.getInvocationHandler(conn);
			if (handler instanceof PooledConnection) {
				return ((PooledConnection) handler).getRealConnection();
			}
		}
		return conn;
	}

	protected void finalize() throws Throwable {
		forceCloseAll();
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLException(getClass().getName() + " is not a wrapper.");
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	public Logger getParentLogger() {
		return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); // requires JDK
															// version 1.6
	}

}
