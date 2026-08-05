package com.cyitianyou.copywslpath

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

class PluginDescriptorTest {
    @Test
    fun groupsCopyActionsUnderCopyPathAsPopup() {
        val descriptorUrl = javaClass.getResource("/META-INF/plugin.xml")
        assertNotNull(descriptorUrl)

        val document = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(descriptorUrl!!.openStream())
        val group = (0 until document.getElementsByTagName("group").length)
            .map { document.getElementsByTagName("group").item(it) as Element }
            .firstOrNull { it.getAttribute("id") == "com.cyitianyou.copywslpath.copyPathGroup" }

        assertNotNull(group)
        val copyGroup = group!!

        assertEquals("Copy Path As", copyGroup.getAttribute("text"))
        assertEquals("com.cyitianyou.copywslpath.CopyPathGroup", copyGroup.getAttribute("class"))
        assertEquals("true", copyGroup.getAttribute("popup"))
        assertEquals("AllIcons.Actions.Copy", copyGroup.getAttribute("icon"))
        assertEquals("actions", copyGroup.parentNode.nodeName)

        val actionIds = (0 until copyGroup.getElementsByTagName("action").length)
            .map { copyGroup.getElementsByTagName("action").item(it) as Element }
            .map { it.getAttribute("id") }
            .toSet()

        assertEquals(
            setOf(
                "com.cyitianyou.copywslpath.copyPath",
                "com.cyitianyou.copywslpath.copyRealPath",
                "com.cyitianyou.copywslpath.copyWslPath",
                "com.cyitianyou.copywslpath.copyRealWslPath"
            ),
            actionIds
        )
        assertTrue(copyGroup.getElementsByTagName("add-to-group").length == 1)
    }
}
