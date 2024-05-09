package com.github.mky2

import com.github.mky2.outline.BaseTocVisitor
import com.github.mky2.outline.EntryNode
import com.github.mky2.outline.IndentProcessor
import com.github.mky2.outline.preorder
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDDocumentInformation
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import java.io.File
import java.nio.file.Files

// fixme
val PDF_ENV: String = System.getenv("IN_PDF")
val TOC_ENV: String = System.getenv("IN_TOC")

fun main() {
    val pdfFile = File(PDF_ENV)
    val outFile = pdfFile.parentFile.resolve("out.pdf")

    if (Files.deleteIfExists(outFile.toPath())) {
        println("Deleted $outFile!")
    }

    Loader.loadPDF(pdfFile).use { pdf ->
        pdf.updateOutlineFrom(File(TOC_ENV))
        pdf.printMeta()
        print("Saving... ")
        pdf.save(outFile)
        println("Done")
    }
}

fun PDDocument.newOutlineItem(node: EntryNode) = PDOutlineItem().apply {
    setDestination(getPage(node.data.page))
    title = node.data.title
}

fun PDDocument.updateOutlineFrom(file: File) {
    documentCatalog.documentOutline = PDDocumentOutline().apply {
        val tree = file.useLines { lines ->
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
            newOutlineItem(child).also {
                parent?.addLast(it) ?: addLast(it)
            }
        }
    }
}

fun PDDocument.insertToolKitInfo() {

}

fun PDDocument.printMeta() {
    documentInformation.run {
        println("Author $author")
        println("Creator: $creator")
        println("CreationDate: $creationDate")
        println("Producer: $producer")
        println("Subject: $subject")
        println("Title: $title")
        println("Trapped: $trapped")
    }
}

operator fun PDDocumentInformation.get(key: String): String = getCustomMetadataValue(key)
operator fun PDDocumentInformation.set(key: String, value: String) = setCustomMetadataValue(key, value)