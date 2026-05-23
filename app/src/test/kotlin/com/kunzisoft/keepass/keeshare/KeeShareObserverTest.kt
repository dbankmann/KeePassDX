/*
 * Copyright 2026 Jeremy Jamet / Kunzisoft.
 *
 * This file is part of KeePassDX.
 *
 *  KeePassDX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  KeePassDX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePassDX.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.kunzisoft.keepass.keeshare

import android.net.Uri
import android.os.Looper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class KeeShareObserverTest {

    private val resolver get() = RuntimeEnvironment.getApplication().contentResolver
    private val shadowResolver get() = Shadows.shadowOf(resolver)

    private fun idle() = Shadows.shadowOf(Looper.getMainLooper()).idle()

    @Test
    fun observeRegistersContentObserver() {
        val observer = KeeShareObserver { }
        val uri = Uri.parse("content://test/tree")

        observer.observe(resolver, uri)

        assertEquals(1, shadowResolver.getContentObservers(uri).size)
        observer.stopAll(resolver)
    }

    @Test
    fun stopUnregistersContentObserver() {
        val observer = KeeShareObserver { }
        val uri = Uri.parse("content://test/tree")

        observer.observe(resolver, uri)
        observer.stop(resolver, uri)

        assertEquals(0, shadowResolver.getContentObservers(uri).size)
    }

    @Test
    fun stopAllUnregistersAllObservers() {
        val observer = KeeShareObserver { }
        val uri1 = Uri.parse("content://test/tree1")
        val uri2 = Uri.parse("content://test/tree2")

        observer.observe(resolver, uri1)
        observer.observe(resolver, uri2)
        assertEquals(1, shadowResolver.getContentObservers(uri1).size)
        assertEquals(1, shadowResolver.getContentObservers(uri2).size)

        observer.stopAll(resolver)
        assertEquals(0, shadowResolver.getContentObservers(uri1).size)
        assertEquals(0, shadowResolver.getContentObservers(uri2).size)
    }

    @Test
    fun observeSameUriReplacesOldObserver() {
        val observer = KeeShareObserver { }
        val uri = Uri.parse("content://test/tree")

        observer.observe(resolver, uri)
        observer.observe(resolver, uri)

        assertEquals(1, shadowResolver.getContentObservers(uri).size)
        observer.stopAll(resolver)
    }

    @Test
    fun callbackFiresOnChange() {
        val count = AtomicInteger(0)
        val observer = KeeShareObserver { count.incrementAndGet() }
        val uri = Uri.parse("content://test/tree")

        observer.observe(resolver, uri)
        shadowResolver.getContentObservers(uri).first().dispatchChange(false, null)
        idle()

        assertEquals(1, count.get())
        observer.stopAll(resolver)
    }

    @Test
    fun callbackFiresMultipleTimes() {
        val count = AtomicInteger(0)
        val observer = KeeShareObserver { count.incrementAndGet() }
        val uri = Uri.parse("content://test/tree")

        observer.observe(resolver, uri)
        repeat(3) {
            shadowResolver.getContentObservers(uri).first().dispatchChange(false, null)
        }
        idle()

        assertEquals(3, count.get())
        observer.stopAll(resolver)
    }

    @Test
    fun callbackDoesNotFireAfterStop() {
        val count = AtomicInteger(0)
        val observer = KeeShareObserver { count.incrementAndGet() }
        val uri = Uri.parse("content://test/tree")

        observer.observe(resolver, uri)
        assertTrue(shadowResolver.getContentObservers(uri).isNotEmpty())

        observer.stop(resolver, uri)
        idle()

        assertEquals(0, count.get())
    }

    @Test
    fun stopOnUnobservedUriIsNoOp() {
        val observer = KeeShareObserver { }
        observer.stop(resolver, Uri.parse("content://test/never-observed"))
    }

    @Test
    fun multipleObserversFireIndependently() {
        val count1 = AtomicInteger(0)
        val count2 = AtomicInteger(0)
        val uri1 = Uri.parse("content://test/tree1")
        val uri2 = Uri.parse("content://test/tree2")

        val observer1 = KeeShareObserver { count1.incrementAndGet() }
        val observer2 = KeeShareObserver { count2.incrementAndGet() }

        observer1.observe(resolver, uri1)
        observer2.observe(resolver, uri2)

        shadowResolver.getContentObservers(uri1).first().dispatchChange(false, null)
        idle()

        assertEquals(1, count1.get())
        assertEquals(0, count2.get())

        observer1.stopAll(resolver)
        observer2.stopAll(resolver)
    }
}
