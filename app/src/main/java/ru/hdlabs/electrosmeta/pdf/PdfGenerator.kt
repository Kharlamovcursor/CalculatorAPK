package ru.hdlabs.electrosmeta.pdf

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import ru.hdlabs.electrosmeta.data.local.dao.EstimateWithLines
import ru.hdlabs.electrosmeta.data.settings.AppSettings
import ru.hdlabs.electrosmeta.domain.CalculationResult
import ru.hdlabs.electrosmeta.domain.EstimateCalculator
import ru.hdlabs.electrosmeta.domain.formatRubles
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

class PdfGenerator(private val context: Context) {
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru", "RU"))

    fun generate(
        data: EstimateWithLines,
        settings: AppSettings,
        calculation: CalculationResult,
        outputFile: File,
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40f
        val contentWidth = pageWidth - 2 * margin
        var y = margin
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subtitlePaint = Paint().apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            textSize = 10f
            isAntiAlias = true
        }
        val smallPaint = Paint().apply {
            textSize = 9f
            isAntiAlias = true
        }
        val headerFillPaint = Paint().apply {
            color = 0xFFE8E8E8.toInt()
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = 0xFF333333.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            isAntiAlias = true
        }
        val tableHeaderPaint = Paint(bodyPaint).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val colWidths = floatArrayOf(28f, 212f, 42f, 48f, 62f, 73f)
        val colCount = colWidths.size
        val tableWidth = colWidths.sum()
        val tableLeft = margin + (contentWidth - tableWidth) / 2f
        val colLefts = FloatArray(colCount).also { lefts ->
            var x = tableLeft
            for (i in colWidths.indices) {
                lefts[i] = x
                x += colWidths[i]
            }
        }
        val rowLineHeight = 13f
        val cellPadH = 4f
        val cellPadV = 5f

        fun newPageIfNeeded(required: Float) {
            if (y + required > pageHeight - margin) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = margin
            }
        }

        fun drawCentered(text: String, paint: Paint, extraY: Float = 18f) {
            newPageIfNeeded(extraY)
            val tw = paint.measureText(text)
            canvas.drawText(text, margin + (contentWidth - tw) / 2f, y, paint)
            y += extraY
        }

        fun drawLeft(text: String, paint: Paint, lineHeight: Float = 14f) {
            newPageIfNeeded(lineHeight)
            canvas.drawText(text, margin, y, paint)
            y += lineHeight
        }

        drawCentered(settings.pdfTitle.ifBlank { "Коммерческое предложение" }, titlePaint, 22f)
        if (settings.companyName.isNotBlank()) {
            drawCentered(settings.companyName, subtitlePaint, 18f)
        }
        if (settings.performerTitle.isNotBlank()) {
            drawCentered(settings.performerTitle, bodyPaint, 16f)
        }

        y += 4f
        val requisites = buildList {
            if (settings.inn.isNotBlank()) add("ИНН ${settings.inn}")
            if (settings.ogrnip.isNotBlank()) add("ОГРНИП ${settings.ogrnip}")
            if (settings.phone.isNotBlank()) add("Тел.: ${settings.phone}")
            if (settings.telegram.isNotBlank()) add("Telegram: ${settings.telegram}")
            if (settings.maxContact.isNotBlank()) add("MAX: ${settings.maxContact}")
        }
        requisites.forEach { drawLeft(it, smallPaint, 12f) }
        y += 6f

        drawLeft("Дата: ${dateFormat.format(Date(data.estimate.createdAtMillis))}", bodyPaint)
        drawLeft("Документ № ${data.estimate.id}", bodyPaint)
        data.estimate.workTitle?.takeIf { it.isNotBlank() }?.let {
            drawLeft("Объект работ: $it", bodyPaint)
        }
        data.estimate.clientName?.takeIf { it.isNotBlank() }?.let {
            drawLeft("Заказчик: $it", bodyPaint)
        }
        data.estimate.clientAddress?.takeIf { it.isNotBlank() }?.let {
            drawLeft("Адрес объекта: $it", bodyPaint)
        }
        y += 8f

        if (settings.pdfIntro.isNotBlank()) {
            wrapText(settings.pdfIntro, bodyPaint, contentWidth).forEach { line ->
                newPageIfNeeded(14f)
                canvas.drawText(line, margin, y, bodyPaint)
                y += 13f
            }
            y += 8f
        }

        fun drawTableRow(
            cells: List<String>,
            paints: List<Paint>,
            alignments: List<TextAlign>,
            minHeight: Float,
            fill: Boolean,
        ): Float {
            val wrapped = cells.mapIndexed { index, text ->
                wrapText(text, paints[index], colWidths[index] - 2 * cellPadH)
            }
            val rowHeight = max(minHeight, wrapped.maxOf { it.size } * rowLineHeight + 2 * cellPadV)
            newPageIfNeeded(rowHeight + 2f)
            val top = y
            if (fill) {
                canvas.drawRect(tableLeft, top, tableLeft + tableWidth, top + rowHeight, headerFillPaint)
            }
            for (i in 0 until colCount) {
                val left = colLefts[i]
                canvas.drawRect(left, top, left + colWidths[i], top + rowHeight, borderPaint)
                val lines = wrapped[i]
                var textY = top + cellPadV + rowLineHeight - 3f
                lines.forEach { line ->
                    val paint = paints[i]
                    val x = when (alignments[i]) {
                        TextAlign.LEFT -> left + cellPadH
                        TextAlign.CENTER -> {
                            left + (colWidths[i] - paint.measureText(line)) / 2f
                        }
                        TextAlign.RIGHT -> {
                            left + colWidths[i] - cellPadH - paint.measureText(line)
                        }
                    }
                    canvas.drawText(line, x, textY, paint)
                    textY += rowLineHeight
                }
            }
            y = top + rowHeight
            return rowHeight
        }

        val headerCells = listOf("№", "Наименование", "Ед.изм.", "Кол-во", "Цена", "Сумма")
        val headerAlign = listOf(
            TextAlign.CENTER,
            TextAlign.LEFT,
            TextAlign.CENTER,
            TextAlign.CENTER,
            TextAlign.RIGHT,
            TextAlign.RIGHT,
        )
        drawTableRow(
            headerCells,
            List(colCount) { tableHeaderPaint },
            headerAlign,
            minHeight = 22f,
            fill = true,
        )

        data.lines.forEachIndexed { index, line ->
            val total = EstimateCalculator.lineTotalKopecks(line.quantity, line.unitPriceKopecks)
            val qtyText = if (line.quantity == line.quantity.toLong().toDouble()) {
                line.quantity.toLong().toString()
            } else {
                line.quantity.toString()
            }
            drawTableRow(
                listOf(
                    (index + 1).toString(),
                    line.name,
                    line.unit,
                    qtyText,
                    formatRubles(line.unitPriceKopecks),
                    formatRubles(total),
                ),
                listOf(bodyPaint, bodyPaint, bodyPaint, bodyPaint, bodyPaint, bodyPaint),
                headerAlign,
                minHeight = 20f,
                fill = false,
            )
        }

        y += 14f
        val totalsX = tableLeft + colWidths[0] + colWidths[1] + colWidths[2]

        fun drawTotalRow(label: String, value: String, bold: Boolean = false) {
            newPageIfNeeded(16f)
            val paint = if (bold) subtitlePaint else bodyPaint
            canvas.drawText(label, totalsX + cellPadH, y, paint)
            val valueX = tableLeft + tableWidth - cellPadH - paint.measureText(value)
            canvas.drawText(value, valueX, y, paint)
            y += 15f
        }

        drawTotalRow("Подытог:", formatRubles(calculation.subtotalKopecks))
        if (calculation.discountKopecks > 0) {
            drawTotalRow("Скидка:", formatRubles(calculation.discountKopecks))
        }
        if (data.estimate.extraFeeKopecks > 0) {
            drawTotalRow("Доплата:", formatRubles(data.estimate.extraFeeKopecks))
        }
        if (calculation.taxKopecks > 0 || data.estimate.taxPercent > 0) {
            drawTotalRow(
                "${settings.taxLabel} (${data.estimate.taxPercent}%):",
                formatRubles(calculation.taxKopecks),
            )
        }
        y += 2f
        drawTotalRow("ИТОГО:", formatRubles(calculation.totalKopecks), bold = true)

        data.estimate.comment?.takeIf { it.isNotBlank() }?.let { comment ->
            y += 8f
            wrapText("Примечание: $comment", bodyPaint, contentWidth).forEach { line ->
                newPageIfNeeded(14f)
                canvas.drawText(line, margin, y, bodyPaint)
                y += 13f
            }
        }

        y += 10f
        val validUntil = dateFormat.format(
            Date(data.estimate.createdAtMillis + settings.offerValidityDays * 24L * 60 * 60 * 1000),
        )
        drawLeft("Срок действия предложения: до $validUntil", smallPaint, 12f)

        if (settings.pdfFooter.isNotBlank()) {
            y += 4f
            wrapText(settings.pdfFooter, smallPaint, contentWidth).forEach { line ->
                newPageIfNeeded(12f)
                canvas.drawText(line, margin, y, smallPaint)
                y += 11f
            }
        }

        document.finishPage(page)
        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()
    }

    private enum class TextAlign { LEFT, CENTER, RIGHT }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isEmpty()) return listOf("")
        val words = text.split(' ')
        val lines = mutableListOf<String>()
        var current = ""
        for (word in words) {
            val test = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(test) <= maxWidth) {
                current = test
            } else {
                if (current.isNotEmpty()) lines.add(current)
                current = if (paint.measureText(word) <= maxWidth) {
                    word
                } else {
                    breakLongWord(word, paint, maxWidth, lines)
                    ""
                }
            }
        }
        if (current.isNotEmpty()) lines.add(current)
        return lines.ifEmpty { listOf("") }
    }

    private fun breakLongWord(word: String, paint: Paint, maxWidth: Float, lines: MutableList<String>) {
        var chunk = ""
        for (ch in word) {
            val test = chunk + ch
            if (paint.measureText(test) <= maxWidth) {
                chunk = test
            } else {
                if (chunk.isNotEmpty()) lines.add(chunk)
                chunk = ch.toString()
            }
        }
        if (chunk.isNotEmpty()) lines.add(chunk)
    }
}
