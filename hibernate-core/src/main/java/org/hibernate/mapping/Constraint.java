/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.MappingException;
import org.hibernate.boot.model.relational.Exportable;

/**
 * A mapping model object representing a constraint on a relational database table.
 *
 * @author Gavin King
 * @author Brett Meyer
 */
public abstract class Constraint implements Exportable, Serializable {

	private String name;
	private final ArrayList<Column> columns = new ArrayList<>();
	private Table table;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addColumn(Column column) {
		if ( !columns.contains( column ) ) {
			columns.add( column );
		}
	}

	public void addColumns(Value value) {
		for ( Selectable selectable : value.getSelectables() ) {
			if ( selectable.isFormula() ) {
				throw new MappingException( "constraint involves a formula: " + name );
			}
			else {
				addColumn( (Column) selectable );
			}
		}
	}

	/**
	 * @return true if this constraint already contains a column with same name.
	 */
	public boolean containsColumn(Column column) {
		return columns.contains( column );
	}

	public int getColumnSpan() {
		return columns.size();
	}

	public Column getColumn(int i) {
		return columns.get( i );
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public String toString() {
		return getClass().getSimpleName() + '(' + getTable().getName() + getColumns() + ") as " + name;
	}
}
