package org.jggug.magica.akatsuki

class MacroUtilsTest extends GroovyTestCase {

    void testNewTrait1() {
        assertScript """

        trait Sample {
            String name
            int age
            String hello() {
                // assertScript の中で "Hello, ${name}" はアカン
                "Hello, my name is " + name + ", and my age is " + age
            }
        }

        def x = newTrait(Sample)
        assert x != null
        x.name = 'aaa'
        x.age = 10
        assert x.name == "aaa"
        assert x.age == 10
        assert x.hello() == "Hello, my name is aaa, and my age is 10"
"""
    }

    void testNewTrait2() {
        assertScript """

        trait Sample {
            String name
            int age
            String hello() {
                // assertScript の中で "Hello, ${name}" はアカン
                "Hello, my name is " + name + ", and my age is " + age
            }
        }

        def x = newTrait(Sample, name: "aaa", age: 10)
        assert x != null
        assert x.name == "aaa"
        assert x.age == 10
        assert x.hello() == "Hello, my name is aaa, and my age is 10"
"""
    }
}
