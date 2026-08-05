package com.cyitianyou.copywslpath

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PathConverterTest {
    @Test
    fun convertsUppercaseDrivePath() {
        assertEquals(
            "/mnt/e/MyWorks/demo.txt",
            PathConverter.toWslPath("E:\\MyWorks\\demo.txt")
        )
    }

    @Test
    fun convertsLowercaseDrivePath() {
        assertEquals(
            "/mnt/c/Users/demo/file.txt",
            PathConverter.toWslPath("c:\\Users\\demo\\file.txt")
        )
    }

    @Test
    fun convertsWindowsRootPathWithTrailingSlash() {
        assertEquals("/mnt/c/", PathConverter.toWslPath("C:\\"))
    }

    @Test
    fun trimsInputBeforeConverting() {
        assertEquals("/mnt/e/Work/demo", PathConverter.toWslPath("  E:\\Work\\demo  "))
    }

    @Test
    fun rejectsEmptyInput() {
        assertThrows<IllegalArgumentException> {
            PathConverter.toWslPath("")
        }
    }

    @Test
    fun rejectsUncPath() {
        assertThrows<IllegalArgumentException> {
            PathConverter.toWslPath("\\\\server\\share\\file.txt")
        }
    }

    @Test
    fun rejectsRelativePath() {
        assertThrows<IllegalArgumentException> {
            PathConverter.toWslPath("src\\main\\file.txt")
        }
    }
}
