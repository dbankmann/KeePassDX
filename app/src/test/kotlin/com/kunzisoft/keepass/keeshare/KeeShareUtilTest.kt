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

import androidx.documentfile.provider.DocumentFile
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class KeeShareUtilTest {

    private lateinit var testDir: File

    @Before
    fun setUp() {
        testDir = File(System.getProperty("java.io.tmpdir"), "keeshare_test_${System.nanoTime()}")
        testDir.mkdirs()
    }

    @After
    fun tearDown() {
        testDir.deleteRecursively()
    }

    private fun docFile(): DocumentFile = DocumentFile.fromFile(testDir)

    @Test
    fun filtersKdbxOnly() {
        File(testDir, "device1.kdbx").createNewFile()
        File(testDir, "device2.kdbx").createNewFile()
        File(testDir, "notes.txt").createNewFile()
        File(testDir, "backup.zip").createNewFile()

        val files = KeeShareUtil.listContainerFiles(docFile())
        assertEquals(2, files.size)
        assertTrue(files.all { it.name?.endsWith(".kdbx") == true })
    }

    @Test
    fun caseInsensitiveExtension() {
        File(testDir, "test.KDBX").createNewFile()
        File(testDir, "test2.Kdbx").createNewFile()
        File(testDir, "test3.kDbX").createNewFile()

        val files = KeeShareUtil.listContainerFiles(docFile())
        assertEquals(3, files.size)
    }

    @Test
    fun emptyDirectory() {
        val files = KeeShareUtil.listContainerFiles(docFile())
        assertEquals(0, files.size)
    }

    @Test
    fun ignoresSubdirectories() {
        File(testDir, "subdir.kdbx").mkdirs()
        File(testDir, "real.kdbx").createNewFile()

        val files = KeeShareUtil.listContainerFiles(docFile())
        assertEquals(1, files.size)
        assertEquals("real.kdbx", files[0].name)
    }

    @Test
    fun ignoresFilesWithoutExtension() {
        File(testDir, "kdbx").createNewFile()
        File(testDir, "noext").createNewFile()

        val files = KeeShareUtil.listContainerFiles(docFile())
        assertEquals(0, files.size)
    }

    @Test
    fun handlesFileAsInputNotDirectory() {
        val file = File(testDir, "notadir.kdbx")
        file.createNewFile()

        val files = KeeShareUtil.listContainerFiles(DocumentFile.fromFile(file))
        assertEquals(0, files.size)
    }

    @Test
    fun manyContainerFiles() {
        repeat(50) { i ->
            File(testDir, "device_$i.kdbx").createNewFile()
        }
        File(testDir, "other.txt").createNewFile()

        val files = KeeShareUtil.listContainerFiles(docFile())
        assertEquals(50, files.size)
    }

    @Test
    fun kdbxInNameButNotExtension() {
        File(testDir, "kdbx_backup.txt").createNewFile()
        File(testDir, "my.kdbx.bak").createNewFile()

        val files = KeeShareUtil.listContainerFiles(docFile())
        assertEquals(0, files.size)
    }
}
