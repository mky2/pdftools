package com.github.mky2

interface TocVisitor {
    fun visitLine(text: String)
    fun changeInDepth(n: Int)
}

class BaseTocVisitor : TocVisitor {
    private var offset = 0
    private var parents = mutableListOf<Node>(RootNode())
    private var lastEntry: EntryNode? = null
    val root: Node get() = parents.first()

    override fun visitLine(text: String) {
        val content = text.trim()

        // process command
        if (processCommands(content)) return

        ENTRY_REGEX.matchEntire(content)?.run {
            val (title, page) = destructured
            val data = NodeData(title, page.toInt() + offset)
            val entryNode = EntryNode(data)

            lastEntry = entryNode
            parents.last().addChild(entryNode)
        }
    }

    private fun processCommands(content: String): Boolean {
        ADD_OFFSET_REGEX.matchEntire(content)?.run {
            val (n) = destructured
            offset += n.toInt()
            return true
        }

        MINUS_OFFSET_REGEX.matchEntire(content)?.run {
            val (n) = destructured
            offset -= n.toInt()
            return true
        }
        return false
    }

    override fun changeInDepth(n: Int) {
        when {
            n == 0 -> {}
            n == 1 -> parents.addLast(lastEntry!!) // todo check
            n < 0 -> repeat(-n) { parents.removeLast() }
            else -> throw RuntimeException("illegal depth")
        }
    }

    companion object {
        val ENTRY_REGEX = Regex("^(.*)\\s+(\\d+)\$")
        val ADD_OFFSET_REGEX = Regex("^\\+(\\d+)\$")
        val MINUS_OFFSET_REGEX = Regex("^-(\\d+)\$")
    }
}

class IndentProcessor {
    private var indentation = mutableListOf(0)
    private var depth = 0

    fun computeDepth(text: String): Int {
        val content = text.trimStart()
        val indent = text.length - content.length

        when {
            indent > indentation.last() -> {
                depth += 1
                indentation.addLast(indent)
            }
            indent < indentation.last() -> {
                while (indent < indentation.last()) {
                    depth -= 1
                    indentation.removeLast()
                }
                if (indent != indentation.last())
                    throw RuntimeException("error format") // todo
            }
        }
        return depth
    }

    fun computeChangeInDepth(text: String): Int {
        val old = depth
        return computeDepth(text) - old
    }
}