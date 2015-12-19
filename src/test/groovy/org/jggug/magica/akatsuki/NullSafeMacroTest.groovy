package org.jggug.magica.akatsuki

class NullSafeMacroTest extends GroovyTestCase {

    void testNullSafe1() {
        assertScript """

        class X {
            String name
            String hello() {
                "Hello, " + name
            }
            String hello(String argument) {
                "Hello, " + argument
            }
        }

        X obj = null
        assert nullSafe(obj.name) == null
"""
    }

    void testNullSafe2() {
        assertScript """

        class X {
            String nameX
            String hello() {
                "Hello, " + name
            }
            String hello(String argument) {
                "Hello, " + argument
            }
        }

        X obj = null
        assert nullSafe(obj.hello()) == null
"""
    }

    void testNullSafe3() {
        assertScript """

        class X {
            String name
            String hello() {
                "Hello, " + name
            }
            String hello(String argument) {
                "Hello, " + argument
            }
        }

        X obj = new X()
        X arg = null
        assert nullSafe(obj.hello(arg.name)) == "Hello, null"
"""
    }

    void testNullSafe4() {
        assertScript """

        class X {
            String name
            String hello() {
                "Hello, " + name
            }
            String hello(String argument) {
                "Hello, " + argument
            }
        }

        X obj = new X()
        X arg = null
        assert nullSafe(obj.hello(arg.hello())) == "Hello, null"
"""
    }

    void testNullSafe5() {
        assertScript """

class A {
    String hello() {
        "Hello!"
    }
}
class B {
    A a
}
class C {
    B b
}

C obj = new C()
assert nullSafe(obj.b.a.hello()) == null


"""
    }
}
