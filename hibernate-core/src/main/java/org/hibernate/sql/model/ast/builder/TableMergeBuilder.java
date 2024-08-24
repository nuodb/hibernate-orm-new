/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.model.ast.builder;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.sql.model.MutationOperation;
import org.hibernate.sql.model.MutationTarget;
import org.hibernate.sql.model.TableMapping;
import org.hibernate.sql.model.ast.ColumnValueBinding;
import org.hibernate.sql.model.ast.MutatingTableReference;
import org.hibernate.sql.model.ast.RestrictedTableMutation;
import org.hibernate.sql.model.internal.OptionalTableUpdate;
import org.hibernate.sql.model.internal.TableUpdateNoSet;

import java.util.List;

/**
 * @author Gavin King
 */
public class TableMergeBuilder<O extends MutationOperation> extends AbstractTableUpdateBuilder<O> {

	public TableMergeBuilder(
			MutationTarget<?> mutationTarget,
			TableMapping tableMapping,
			SessionFactoryImplementor sessionFactory) {
		super( mutationTarget, tableMapping, sessionFactory );
	}

	public TableMergeBuilder(
			MutationTarget<?> mutationTarget,
			MutatingTableReference tableReference,
			SessionFactoryImplementor sessionFactory) {
		super( mutationTarget, tableReference, sessionFactory );
	}

	@SuppressWarnings("unchecked")
	@Override
	public RestrictedTableMutation<O> buildMutation() {
		final List<ColumnValueBinding> valueBindings = combine( getValueBindings(), getKeyBindings(), getLobValueBindings() );
		if ( valueBindings.isEmpty() ) {
			return (RestrictedTableMutation<O>) new TableUpdateNoSet( getMutatingTable(), getMutationTarget() );
		}

		// TODO: add getMergeDetails()
//		if ( getMutatingTable().getTableMapping().getUpdateDetails().getCustomSql() != null ) {
//			return (RestrictedTableMutation<O>) new TableUpdateCustomSql(
//					getMutatingTable(),
//					getMutationTarget(),
//					getSqlComment(),
//					valueBindings,
//					getKeyRestrictionBindings(),
//					getOptimisticLockBindings()
//			);
//		}

		return (RestrictedTableMutation<O>) new OptionalTableUpdate(
				getMutatingTable(),
				getMutationTarget(),
				valueBindings,
				getKeyRestrictionBindings(),
				getOptimisticLockBindings()
		);
	}
}
