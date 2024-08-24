/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.xml.internal.db;

import org.hibernate.boot.jaxb.mapping.spi.JaxbForeignKeyImpl;
import org.hibernate.boot.models.JpaAnnotations;
import org.hibernate.boot.models.annotations.internal.ForeignKeyJpaAnnotation;
import org.hibernate.boot.models.xml.spi.XmlDocumentContext;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.models.spi.MutableMemberDetails;

import jakarta.persistence.ForeignKey;

/**
 * @author Steve Ebersole
 */
public class ForeignKeyProcessing {

	public static void applyForeignKey(
			JaxbForeignKeyImpl jaxbForeignKey,
			MutableMemberDetails memberDetails,
			XmlDocumentContext xmlDocumentContext) {
		if ( jaxbForeignKey == null ) {
			return;
		}

		createForeignKeyAnnotation( jaxbForeignKey, memberDetails, xmlDocumentContext );
	}

	public static ForeignKey createForeignKeyAnnotation(
			JaxbForeignKeyImpl jaxbForeignKey,
			MutableMemberDetails memberDetails,
			XmlDocumentContext xmlDocumentContext) {
		if ( jaxbForeignKey == null ) {
			return null;
		}
		final ForeignKeyJpaAnnotation foreignKeyUsage = (ForeignKeyJpaAnnotation) memberDetails.applyAnnotationUsage(
				JpaAnnotations.FOREIGN_KEY,
				xmlDocumentContext.getModelBuildingContext()
		);
		transferFkDetails( jaxbForeignKey, foreignKeyUsage, xmlDocumentContext );
		return foreignKeyUsage;
	}

	private static void transferFkDetails(
			JaxbForeignKeyImpl jaxbForeignKey,
			ForeignKeyJpaAnnotation foreignKeyUsage,
			XmlDocumentContext xmlDocumentContext) {
		if ( StringHelper.isNotEmpty( jaxbForeignKey.getName() ) ) {
			foreignKeyUsage.name( jaxbForeignKey.getName() );
		}

		if ( jaxbForeignKey.getConstraintMode() != null ) {
			foreignKeyUsage.value( jaxbForeignKey.getConstraintMode() );
		}

		if ( StringHelper.isNotEmpty( jaxbForeignKey.getForeignKeyDefinition() ) ) {
			foreignKeyUsage.foreignKeyDefinition( jaxbForeignKey.getForeignKeyDefinition() );
		}
		if ( StringHelper.isNotEmpty( jaxbForeignKey.getOptions() ) ) {
			foreignKeyUsage.options( jaxbForeignKey.getOptions() );
		}
	}

	public static ForeignKeyJpaAnnotation createNestedForeignKeyAnnotation(
			JaxbForeignKeyImpl jaxbForeignKey,
			XmlDocumentContext xmlDocumentContext) {
		final ForeignKeyJpaAnnotation foreignKeyUsage = JpaAnnotations.FOREIGN_KEY.createUsage( xmlDocumentContext.getModelBuildingContext() );
		if ( jaxbForeignKey != null ) {
			transferFkDetails( jaxbForeignKey, foreignKeyUsage, xmlDocumentContext );
		}

		return foreignKeyUsage;
	}

}
