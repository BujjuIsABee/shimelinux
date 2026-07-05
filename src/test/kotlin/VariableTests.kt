/*
 * Copyright (c) 2026, Bujju
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *        following disclaimer.
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *        following disclaimer in the documentation and/or other materials provided with the distribution.
 *     3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote
 *        products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import com.group_finity.mascot.Mascot
import com.group_finity.mascot.environment.Area
import com.group_finity.mascot.environment.FloorCeiling
import com.group_finity.mascot.script.Variable
import com.group_finity.mascot.script.VariableMap
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.assertThrows
import java.awt.Point
import java.awt.Rectangle
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VariableTests {
    @Test
    fun `should not be able to access unauthorized classes`() {
        val variable = Variable.parse($$"${java.lang.Runtime.getRuntime().exec(['echo', 'test'])}")

        assertThrows<Exception> { variable!!.get(VariableMap()) }
    }

    @Test
    fun `should be able to access to math library`() {
        val variable = Variable.parse($$"${Math.random()}")

        val result = variable!!.get(VariableMap())

        assertContains(0.0..1.0, result as Double)
    }

    @Test
    fun `should be able to get mascot properties`() {
        val mascot = mockk<Mascot>()
        every { mascot.isLookRight } returns true

        val context = VariableMap()
        context["mascot"] = mascot

        val variable = Variable.parse($$"${mascot.lookRight}")

        val result = variable!!.get(context)

        assertEquals(true, result)
    }

    @Test
    fun `should be able to set mascot properties`() {
        val mascot = mockk<Mascot>()
        every { mascot.anchor } returns Point(5, 0)

        val context = VariableMap()
        context["mascot"] = mascot

        val variable = Variable.parse($$"${mascot.anchor.x *= 2}")

        variable!!.get(context)

        assertEquals(10, mascot.anchor.x)
    }

    @Test
    fun `should be able to check if mascot is on border`() {
        val mascot = mockk<Mascot>()
        every { mascot.anchor } returns Point(0, 1080)
        every { mascot.environment.workArea } returns Area().apply { set(Rectangle(0, 0, 1920, 1080)) }
        every { mascot.environment.floor } returns FloorCeiling(mascot.environment.workArea, true)

        val context = VariableMap()
        context["mascot"] = mascot

        val variable = Variable.parse($$"${mascot.environment.floor.isOn(mascot.anchor)}")

        val result = variable!!.get(context)

        assertEquals(true, result)
    }

    @Test
    fun `should return null when trying to get nonexistent variable`() {
        val mascot = mockk<Mascot>()
        every { mascot.variables } returns VariableMap()

        val context = VariableMap()
        context["mascot"] = mascot

        val variable = Variable.parse($$"${mascot.variables['test']}")

        val result = variable!!.get(context)

        assertNull(result)
    }

    @Test
    fun `should treat nonexistent variable as zero in mathematical operations`() {
        val mascot = mockk<Mascot>()
        every { mascot.variables } returns VariableMap()

        val context = VariableMap()
        context["mascot"] = mascot

        val variable = Variable.parse($$"${mascot.variables['test']++}")

        variable!!.get(context)

        assertEquals(1.0, mascot.variables["test"])
    }

    @Test
    fun `should treat nonexistent variable as zero in comparisons`() {
        val mascot = mockk<Mascot>()
        every { mascot.variables } returns VariableMap()

        val context = VariableMap()
        context["mascot"] = mascot

        val variable = Variable.parse($$"${mascot.variables['test'] < 1}")

        val result = variable!!.get(context)

        assertEquals(true, result)
    }
}
