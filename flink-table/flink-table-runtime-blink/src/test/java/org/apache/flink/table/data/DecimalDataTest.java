/*
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

package org.apache.flink.table.data;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static org.apache.flink.table.data.DecimalDataUtils.abs;
import static org.apache.flink.table.data.DecimalDataUtils.add;
import static org.apache.flink.table.data.DecimalDataUtils.castFrom;
import static org.apache.flink.table.data.DecimalDataUtils.castToBoolean;
import static org.apache.flink.table.data.DecimalDataUtils.castToDecimal;
import static org.apache.flink.table.data.DecimalDataUtils.castToIntegral;
import static org.apache.flink.table.data.DecimalDataUtils.castToTimestamp;
import static org.apache.flink.table.data.DecimalDataUtils.ceil;
import static org.apache.flink.table.data.DecimalDataUtils.compare;
import static org.apache.flink.table.data.DecimalDataUtils.divide;
import static org.apache.flink.table.data.DecimalDataUtils.divideToIntegralValue;
import static org.apache.flink.table.data.DecimalDataUtils.doubleValue;
import static org.apache.flink.table.data.DecimalDataUtils.floor;
import static org.apache.flink.table.data.DecimalDataUtils.is32BitDecimal;
import static org.apache.flink.table.data.DecimalDataUtils.is64BitDecimal;
import static org.apache.flink.table.data.DecimalDataUtils.isByteArrayDecimal;
import static org.apache.flink.table.data.DecimalDataUtils.mod;
import static org.apache.flink.table.data.DecimalDataUtils.multiply;
import static org.apache.flink.table.data.DecimalDataUtils.negate;
import static org.apache.flink.table.data.DecimalDataUtils.sign;
import static org.apache.flink.table.data.DecimalDataUtils.signum;
import static org.apache.flink.table.data.DecimalDataUtils.sround;
import static org.apache.flink.table.data.DecimalDataUtils.subtract;

/**
 * Test for {@link DecimalData}.
 */
public class DecimalDataTest {

	@SuppressWarnings("ConstantConditions")
	@Test
	public void testNormal() {
		DecimalData decimal1 = DecimalData.fromUnscaledLong(10, 5, 0);
		DecimalData decimal2 = DecimalData.fromUnscaledLong(15, 5, 0);
		Assert.assertEquals(decimal1.hashCode(),
				DecimalData.fromBigDecimal(new BigDecimal(10), 5, 0).hashCode());
		Assert.assertEquals(decimal1, decimal1.copy());
		Assert.assertEquals(decimal1, DecimalData.fromUnscaledLong(decimal1.toUnscaledLong(), 5, 0));
		Assert.assertEquals(decimal1, DecimalData.fromUnscaledBytes(decimal1.toUnscaledBytes(), 5, 0));
		Assert.assertTrue(decimal1.compareTo(decimal2) < 0);
		Assert.assertEquals(1, signum(decimal1));
		Assert.assertEquals(10.5, doubleValue(castFrom(10.5, 5, 1)), 0.0);
		Assert.assertEquals(DecimalData.fromUnscaledLong(-10, 5, 0), negate(decimal1));
		Assert.assertEquals(decimal1, abs(decimal1));
		Assert.assertEquals(decimal1, abs(negate(decimal1)));
		Assert.assertEquals(25, add(decimal1, decimal2, 5, 0).toUnscaledLong());
		Assert.assertEquals(-5, subtract(decimal1, decimal2, 5, 0).toUnscaledLong());
		Assert.assertEquals(150, multiply(decimal1, decimal2, 5, 0).toUnscaledLong());
		Assert.assertEquals(0.67, doubleValue(divide(decimal1, decimal2, 5, 2)), 0.0);
		Assert.assertEquals(decimal1, mod(decimal1, decimal2, 5, 0));
		Assert.assertEquals(5, divideToIntegralValue(
				decimal1, DecimalData.fromUnscaledLong(2, 5, 0), 5, 0).toUnscaledLong());
		Assert.assertEquals(10, castToIntegral(decimal1));
		Assert.assertTrue(castToBoolean(decimal1));
		Assert.assertEquals(0, compare(decimal1, 10));
		Assert.assertTrue(compare(decimal1, 5) > 0);
		Assert.assertEquals(castFrom(1.0, 10, 5), sign(castFrom(5.556, 10, 5)));

		Assert.assertNull(DecimalData.fromBigDecimal(new BigDecimal(Long.MAX_VALUE), 5, 0));
		Assert.assertEquals(0, DecimalData.zero(5, 2).toBigDecimal().intValue());
		Assert.assertEquals(0, DecimalData.zero(20, 2).toBigDecimal().intValue());

		Assert.assertEquals(DecimalData.fromUnscaledLong(10, 5, 0), floor(castFrom(10.5, 5, 1)));
		Assert.assertEquals(DecimalData.fromUnscaledLong(11, 5, 0), ceil(castFrom(10.5, 5, 1)));
		Assert.assertEquals("5.00", castToDecimal(castFrom(5.0, 10, 1), 10, 2).toString());

		Assert.assertTrue(castToBoolean(castFrom(true, 5, 0)));
		Assert.assertEquals(5, castToIntegral(castFrom(5, 5, 0)));
		Assert.assertEquals(5, castToIntegral(castFrom("5", 5, 0)));
		Assert.assertEquals(5000, castToTimestamp(castFrom("5", 5, 0)));

		DecimalData newDecimal = castFrom(castFrom(10, 5, 2), 10, 4);
		Assert.assertEquals(10, newDecimal.precision());
		Assert.assertEquals(4, newDecimal.scale());

		Assert.assertTrue(is32BitDecimal(6));
		Assert.assertTrue(is64BitDecimal(11));
		Assert.assertTrue(isByteArrayDecimal(20));

		Assert.assertEquals(6, sround(castFrom(5.555, 5, 0), 1).toUnscaledLong());
		Assert.assertEquals(56, sround(castFrom(5.555, 5, 3), 1).toUnscaledLong());
	}

	@SuppressWarnings("ConstantConditions")
	@Test
	public void testNotCompact() {
		DecimalData decimal1 = DecimalData.fromBigDecimal(new BigDecimal(10), 20, 0);
		DecimalData decimal2 = DecimalData.fromBigDecimal(new BigDecimal(15), 20, 0);
		Assert.assertEquals(decimal1.hashCode(),
				DecimalData.fromBigDecimal(new BigDecimal(10), 20, 0).hashCode());
		Assert.assertEquals(decimal1, decimal1.copy());
		Assert.assertEquals(decimal1, DecimalData.fromBigDecimal(decimal1.toBigDecimal(), 20, 0));
		Assert.assertEquals(decimal1, DecimalData.fromUnscaledBytes(decimal1.toUnscaledBytes(), 20, 0));
		Assert.assertTrue(decimal1.compareTo(decimal2) < 0);
		Assert.assertEquals(1, signum(decimal1));
		Assert.assertEquals(10.5, doubleValue(castFrom(10.5, 20, 1)), 0.0);
		Assert.assertEquals(DecimalData.fromBigDecimal(new BigDecimal(-10), 20, 0), negate(decimal1));
		Assert.assertEquals(decimal1, abs(decimal1));
		Assert.assertEquals(decimal1, abs(negate(decimal1)));
		Assert.assertEquals(25, add(decimal1, decimal2, 20, 0).toBigDecimal().longValue());
		Assert.assertEquals(-5, subtract(decimal1, decimal2, 20, 0).toBigDecimal().longValue());
		Assert.assertEquals(150, multiply(decimal1, decimal2, 20, 0).toBigDecimal().longValue());
		Assert.assertEquals(0.67, doubleValue(divide(decimal1, decimal2, 20, 2)), 0.0);
		Assert.assertEquals(decimal1, mod(decimal1, decimal2, 20, 0));
		Assert.assertEquals(5, divideToIntegralValue(
				decimal1, DecimalData.fromBigDecimal(new BigDecimal(2), 20, 0), 20, 0).toBigDecimal().longValue());
		Assert.assertEquals(10, castToIntegral(decimal1));
		Assert.assertTrue(castToBoolean(decimal1));
		Assert.assertEquals(0, compare(decimal1, 10));
		Assert.assertTrue(compare(decimal1, 5) > 0);
		Assert.assertTrue(compare(DecimalData.fromBigDecimal(new BigDecimal("10.5"), 20, 2), 10) > 0);
		Assert.assertEquals(castFrom(1.0, 20, 5), sign(castFrom(5.556, 20, 5)));

		Assert.assertNull(DecimalData.fromBigDecimal(new BigDecimal(Long.MAX_VALUE), 5, 0));
		Assert.assertEquals(0, DecimalData.zero(20, 2).toBigDecimal().intValue());
		Assert.assertEquals(0, DecimalData.zero(20, 2).toBigDecimal().intValue());
	}

	@Test
	public void testToString() {
		String val = "0.0000000000000000001";
		Assert.assertEquals(val, castFrom(val, 39, val.length() - 2).toString());
		val = "123456789012345678901234567890123456789";
		Assert.assertEquals(val, castFrom(val, 39, 0).toString());
	}
}
