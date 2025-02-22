/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.criteria;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
* @author <a href="mailto:stliu@hibernate.org">Strong Liu</a>
*/
@Entity
@Table(name = "roles")
public class Role extends VersionedRecord {
	@Id
	@Enumerated(EnumType.STRING)
	Code code;
}
