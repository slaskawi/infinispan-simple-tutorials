package org.infinispan.tutorial.simple.query;

import org.hibernate.search.annotations.Indexed;

@Indexed
public class Person extends PersonParent {

   public Person(String name, String surname) {
      super(name, surname);
   }

   @Override
   public String toString() {
      return "Person [name=" + name + ", surname=" + surname + "]";
   }
}