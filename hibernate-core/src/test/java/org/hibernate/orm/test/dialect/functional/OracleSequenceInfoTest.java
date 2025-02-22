/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.dialect.functional;

import org.hibernate.dialect.OracleDialect;
import org.hibernate.tool.schema.extract.spi.SequenceInformation;

import org.hibernate.testing.orm.junit.RequiresDialect;
import org.hibernate.testing.TestForIssue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Vlad Mihalcea
 */
@TestForIssue(jiraKey = "HHH-12973")
@RequiresDialect(value = OracleDialect.class)
public class OracleSequenceInfoTest extends
		SequenceInformationTest {

	@Override
	protected void assertProductSequence(SequenceInformation productSequenceInfo) {
		assertNull( productSequenceInfo.getStartValue() );
		assertEquals( Long.valueOf( 1 ), productSequenceInfo.getMinValue() );
		assertEquals( Long.valueOf( 10 ), productSequenceInfo.getIncrementValue() );
	}

	@Override
	protected void assertVehicleSequenceInfo(SequenceInformation vehicleSequenceInfo) {
		assertNull( vehicleSequenceInfo.getStartValue() );
		assertEquals( Long.valueOf( 1 ), vehicleSequenceInfo.getMinValue() );
		assertEquals( Long.valueOf( 1 ), vehicleSequenceInfo.getIncrementValue() );
	}
}
