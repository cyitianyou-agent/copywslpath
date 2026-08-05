package com.cyitianyou.copywslpath

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PathResolverTest {
    @Test
    fun returnsResolvedPath() {
        val result = PathResolver.resolveRealPath("E:\\link", { "E:\\physical\\target" })

        assertEquals("E:\\physical\\target", result)
    }

    @Test
    fun wrapsResolverFailureWithReadableMessage() {
        val error = assertThrows<IllegalArgumentException> {
            PathResolver.resolveRealPath("E:\\missing", { throw IllegalStateException("ENOENT") })
        }

        assertEquals("Failed to resolve real path: ENOENT", error.message)
    }

    @Test
    fun trimsPathBeforeResolving() {
        var received = ""
        PathResolver.resolveRealPath("  E:\\link  ") {
            received = it
            it
        }

        assertEquals("E:\\link", received)
    }
}
