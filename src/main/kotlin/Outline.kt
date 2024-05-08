package com.github.mky2

typealias Page = Int

sealed class Node {
    private val children: MutableList<EntryNode> = mutableListOf()

    fun visit(run: (EntryNode) -> Unit) {
        children.forEach(run)
    }

    fun addChild(child: EntryNode) = children.addLast(child)
}

fun <T> Node.preorder(parent: T? = null, run: (T?, EntryNode) -> T): Unit =
    visit { it.preorder(run(parent, it), run) }

data class NodeData(
    val title: String,
    val page: Page,
)

data class EntryNode(val data: NodeData) : Node()
class RootNode : Node()