package com.cyitianyou.copywslpath

/**
 * Converts a Windows drive-qualified path into the WSL mount-path format.
 *
 * The converter deliberately accepts only absolute drive paths. UNC paths,
 * relative paths, and native Linux paths are rejected instead of being
 * guessed, because an incorrect conversion would copy a path that looks
 * valid but points to the wrong resource.
 */
object PathConverter {
    private val drivePathRegex = Regex("^([A-Za-z]):\\\\(.*)$")

    /**
     * Converts paths such as `E:\\Work\\demo` to `/mnt/e/Work/demo`.
     *
     * Trimming and the trailing slash for a Windows drive root intentionally
     * match the existing VS Code implementation so both plugins produce the
     * same clipboard value for the same input.
     */
    fun toWslPath(winPath: String): String {
        val pathValue = winPath.trim()
        val matchValue = drivePathRegex.matchEntire(pathValue)
            ?: throw IllegalArgumentException("Only Windows drive paths like C:\\... are supported.")
        val driveValue = matchValue.groupValues[1].lowercase()
        val tailValue = matchValue.groupValues[2].replace('\\', '/')

        return "/mnt/$driveValue/$tailValue"
    }
}
