/** 
 *  Generated by OpenJPA MetaModel Generator Tool.
**/

package common.advanced;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

@javax.persistence.metamodel.StaticMetamodel
(value=common.advanced.Person.class)
@javax.annotation.Generated
(value="org.apache.openjpa.persistence.meta.AnnotationProcessor6",date="Thu Apr 03 10:57:33 CEST 2014")
public class Person_ {
    public static volatile SingularAttribute<Person,Integer> age;
    public static volatile SetAttribute<Person,common.advanced.Person> children;
    public static volatile SingularAttribute<Person,common.advanced.Person> father;
    public static volatile SingularAttribute<Person,Long> id;
    public static volatile SingularAttribute<Person,common.advanced.Person> mother;
    public static volatile SingularAttribute<Person,String> name;
    public static volatile SingularAttribute<Person,common.advanced.Person> partner;
}
