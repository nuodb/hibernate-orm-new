package org.hibernate.processor.test.data.basic;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

import java.util.Set;

@Entity
public class Author {
    @Id
    String ssn;
    String name;

//    @Embedded
//    Address address;

    @ManyToMany
    Set<Book> books;
}

