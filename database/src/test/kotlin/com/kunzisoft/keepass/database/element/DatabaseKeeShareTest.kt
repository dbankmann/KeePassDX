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
package com.kunzisoft.keepass.database.element

import com.kunzisoft.keepass.model.GroupInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseKeeShareTest {

    private lateinit var kdbxDatabase: Database
    private lateinit var emptyDatabase: Database

    @Before
    fun setUp() {
        kdbxDatabase = Database().apply {
            createData("TestDB", "Root", null)
        }
        emptyDatabase = Database()
    }

    @Test
    fun supportsKeeShareTrueForKDBX() {
        assertTrue(kdbxDatabase.supportsKeeShare)
    }

    @Test
    fun supportsKeeShareFalseForEmpty() {
        assertFalse(emptyDatabase.supportsKeeShare)
    }

    @Test
    fun forEachGroupWithCustomDataEmpty() {
        val groups = mutableListOf<Group>()
        kdbxDatabase.forEachGroupWithCustomData { groups.add(it) }
        assertEquals(0, groups.size)
    }

    @Test
    fun forEachGroupWithCustomDataFindsGroups() {
        val group = kdbxDatabase.rootGroup?.let { root ->
            kdbxDatabase.createGroup()?.apply {
                setGroupInfo(GroupInfo().apply { title = "Shared" })
                customData.put(CustomDataItem("KeeShare/Reference", "test"))
            }?.also { newGroup ->
                kdbxDatabase.addGroupTo(newGroup, root)
            }
        }

        val found = mutableListOf<Group>()
        kdbxDatabase.forEachGroupWithCustomData { found.add(it) }
        assertEquals(1, found.size)
        assertEquals("Shared", found[0].title)
    }

    @Test
    fun forEachGroupWithCustomDataSkipsEmptyCustomData() {
        kdbxDatabase.rootGroup?.let { root ->
            kdbxDatabase.createGroup()?.apply {
                setGroupInfo(GroupInfo().apply { title = "Normal" })
            }?.also { newGroup ->
                kdbxDatabase.addGroupTo(newGroup, root)
            }
        }

        val found = mutableListOf<Group>()
        kdbxDatabase.forEachGroupWithCustomData { found.add(it) }
        assertEquals(0, found.size)
    }

    @Test
    fun forEachGroupWithCustomDataNoOpForEmpty() {
        val found = mutableListOf<Group>()
        emptyDatabase.forEachGroupWithCustomData { found.add(it) }
        assertEquals(0, found.size)
    }
}
