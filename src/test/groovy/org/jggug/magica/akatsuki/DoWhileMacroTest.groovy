package org.jggug.magica.akatsuki

class DoWhileMacroTest extends GroovyTestCase {

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
}
