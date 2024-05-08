package com.github.mky2

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest

val PDF_DIR: String? = System.getenv("PDFS_DIR")
val TOC_DIR: String? = System.getenv("TOC_DIR")

val PDF_ENV: String = System.getenv("IN_PDF")
val TOC_ENV: String = System.getenv("IN_TOC")

fun main() {
    val pdfFile = File(PDF_ENV)
    val outFile = pdfFile.parentFile.resolve("new.pdf")

    Files.deleteIfExists(outFile.toPath())

    pdfFile.let(Loader::loadPDF).use { pdf ->
        pdf.documentCatalog.documentOutline = PDDocumentOutline().apply {
            val tree = File(TOC_ENV).useLines { lines ->
                val indentProc = IndentProcessor()
                val v = BaseTocVisitor()
                lines.forEach {
                    val deltaDepth = indentProc.computeChangeInDepth(it)
                    v.changeInDepth(deltaDepth)
                    v.visitLine(it)
                }
                v.root
            }

            tree.preorder<PDOutlineItem> { parent, child ->
                pdf.newOutlineItem(child).also {
                    parent?.addLast(it) ?: addLast(it)
                }
            }
        }
        pdf.save(outFile)
    }
    println("Done")
}

fun PDDocument.newOutlineItem(node: EntryNode) = PDOutlineItem().apply {
    setDestination(getPage(node.data.page))
    title = node.data.title
}

fun PDOutlineNode.countNode(): Int =
    children()?.fold(0) { a, child -> a + child.countNode() + 1 } ?: 0

@OptIn(ExperimentalStdlibApi::class)
fun File.sha1(): String {
    val digest = MessageDigest.getInstance("SHA1")
    return digest.digest(readBytes()).toHexString()
}