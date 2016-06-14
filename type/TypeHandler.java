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
package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 泛型接口 包装数据库返回数据类型
 * 
 * @author Seven 
 * @data 2016-2016年4月12日-下午5:01:40
 * @param <T>
 */
public interface TypeHandler<T> {

	void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

	T getResult(ResultSet rs, String columnName) throws SQLException;

	T getResult(ResultSet rs, int columnIndex) throws SQLException;

	T getResult(CallableStatement cs, int columnIndex) throws SQLException;

}
