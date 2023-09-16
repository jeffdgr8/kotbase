/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Adapted from https://gist.github.com/konrad-kaminski/d7808070f4218349674589e1dc97264a
 */

package kotlinx.coroutines.sync

import kotlinx.coroutines.TestBase
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals

class CountDownLatchTest : TestBase() {

    @Test
    fun testSimple() = runTest {
        val latch = CountDownLatch(2)
        expect(1)
        launch {
            expect(4)
            latch.await() // suspends
            expect(7) // now latch is down
        }
        expect(2)
        latch.countDown()
        expect(3)
        yield()
        expect(5)
        latch.countDown()
        expect(6)
        yield()
        finish(8)
    }

    @Test
    fun countDownTest() {
        val latch = CountDownLatch(3)
        assertEquals(3, latch.getCount())
        latch.countDown()
        assertEquals(2, latch.getCount())
        latch.countDown()
        assertEquals(1, latch.getCount())
        latch.countDown()
        assertEquals(0, latch.getCount())
    }
}
