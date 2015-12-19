package org.jggug.magica.akatsuki

class MacroUtilsTest extends GroovyTestCase {

	void testDoWhile1() {
        assertScript """

		def x = 0
		dowhile ( x == 0 ) {
			println x
            x++
		}
		assert x == 1
"""
    }

	void testDoWhile2() {
        assertScript """

		def x = 0
		dowhile ({
			println x
            x++
		}, x == 0)
		assert x == 1
"""
    }

	void testMatch1() {
        assertScript """

        def fact(num) {
        	return match(num) {
        		when String then fact(num.toInteger())
        		when (0|1) then 1
        		when 2 then 2
        		orElse num * fact(num - 1)
        	}
        }

        assert fact("5") == 120
"""
    }

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

    void testDoWithData1() {
        assertScript """
            doWithData {
                dowith:
                    assert a + b == c

                where:
                    a | b || c
                    1 | 2 || 3
                    4 | 5 || 9
                    7 | 8 || 15
            }
"""
    }
}
