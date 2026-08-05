package com.cyitianyou.copywslpath

import java.nio.file.Paths

/**
 * Resolves a Windows path through the host file system so junctions and
 * symbolic links point to their physical destination before copying.
 */
object PathResolver {
    /**
     * Resolves a path and preserves a small injectable seam for unit tests.
     *
     * On Windows, `Path.toRealPath()` follows the file-system reparse points
     * used by directory junctions and symbolic links. The injected function
     * keeps the public behavior testable without requiring a Windows fixture
     * on the Linux-based development environment.
     */
    fun resolveRealPath(
        path: String,
        resolver: (String) -> String = { Paths.get(it).toRealPath().toString() }
    ): String {
        val pathValue = path.trim()

        return try {
            resolver(pathValue)
        } catch (error: Exception) {
            val messageValue = error.message ?: error::class.simpleName.orEmpty()
            throw IllegalArgumentException("Failed to resolve real path: $messageValue", error)
        }
    }
}
