/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.filter.subclass.joined;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Entity
@Table(name="ZOOLOGY_HUMAN")
@FilterDef(name="iqRange", parameters=
{
		@ParamDef(name="min", type="integer"),
		@ParamDef(name="max", type="integer")
})
@Filter(name="iqRange", condition="HUMAN_IQ BETWEEN :min AND :max")
public class Human extends Mammal {
	@Column(name="HUMAN_IQ")
	private int iq;
	
	@ManyToOne
	private Club club;
	
	public int getIq() {
		return iq;
	}

	public void setIq(int iq) {
		this.iq = iq;
	}

	public Club getClub() {
		return club;
	}

	public void setClub(Club club) {
		this.club = club;
	}

}
