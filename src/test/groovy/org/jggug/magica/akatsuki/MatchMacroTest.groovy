package org.jggug.magica.akatsuki

class MatchMacroTest extends GroovyTestCase {

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
}
