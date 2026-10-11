package ru.hdlabs.electrosmeta.pdf

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import ru.hdlabs.electrosmeta.R
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
        var y = margin
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            textSize = 11f
            isAntiAlias = true
        }
        val smallPaint = Paint().apply {
            textSize = 9f
            isAntiAlias = true
        }

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

        try {
            val logo = BitmapFactory.decodeResource(context.resources, R.drawable.hdlabs_logo)
            if (logo != null) {
                val scale = 120f / logo.width
                val h = logo.height * scale
                canvas.drawBitmap(logo, null, android.graphics.RectF(margin, y, margin + 120f, y + h), null)
                y += h + 12f
            }
        } catch (_: Exception) {
        }

        canvas.drawText(settings.pdfTitle, margin, y, titlePaint)
        y += 24f
        canvas.drawText(settings.companyName, margin, y, bodyPaint)
        y += 16f
        if (settings.performerTitle.isNotBlank()) {
            canvas.drawText(settings.performerTitle, margin, y, bodyPaint)
            y += 16f
        }
        val contacts = buildList {
            if (settings.phone.isNotBlank()) add("Тел.: ${settings.phone}")
            if (settings.telegram.isNotBlank()) add("Telegram: ${settings.telegram}")
            if (settings.maxContact.isNotBlank()) add("MAX: ${settings.maxContact}")
            if (settings.inn.isNotBlank()) add("ИНН: ${settings.inn}")
            if (settings.ogrnip.isNotBlank()) add("ОГРНИП: ${settings.ogrnip}")
        }
        contacts.forEach {
            canvas.drawText(it, margin, y, smallPaint)
            y += 14f
        }
        y += 8f
        canvas.drawText("Дата: ${dateFormat.format(Date(data.estimate.createdAtMillis))}", margin, y, bodyPaint)
        y += 16f
        canvas.drawText("Документ № ${data.estimate.id}", margin, y, bodyPaint)
        y += 20f

        data.estimate.clientName?.takeIf { it.isNotBlank() }?.let {
            canvas.drawText("Клиент: $it", margin, y, bodyPaint)
            y += 16f
        }
        data.estimate.clientAddress?.takeIf { it.isNotBlank() }?.let {
            canvas.drawText("Адрес объекта: $it", margin, y, bodyPaint)
            y += 16f
        }
        y += 8f

        wrapText(settings.pdfIntro, bodyPaint, pageWidth - 2 * margin).forEach { line ->
            newPageIfNeeded(16f)
            canvas.drawText(line, margin, y, bodyPaint)
            y += 14f
        }
        y += 12f

        newPageIfNeeded(20f)
        canvas.drawText("Наименование", margin, y, titlePaint.apply { textSize = 12f })
        canvas.drawText("Кол-во", 300f, y, titlePaint)
        canvas.drawText("Цена", 380f, y, titlePaint)
        canvas.drawText("Сумма", 480f, y, titlePaint)
        y += 18f

        data.lines.forEach { line ->
            val total = EstimateCalculator.lineTotalKopecks(line.quantity, line.unitPriceKopecks)
            newPageIfNeeded(32f)
            wrapText(line.name, bodyPaint, 240f).forEachIndexed { index, part ->
                if (index == 0) {
                    canvas.drawText(part, margin, y, bodyPaint)
                } else {
                    y += 14f
                    newPageIfNeeded(16f)
                    canvas.drawText(part, margin, y, bodyPaint)
                }
            }
            canvas.drawText("${line.quantity} ${line.unit}", 300f, y, bodyPaint)
            canvas.drawText(formatRubles(line.unitPriceKopecks), 380f, y, bodyPaint)
            canvas.drawText(formatRubles(total), 480f, y, bodyPaint)
            y += 18f
        }

        y += 12f
        newPageIfNeeded(80f)
        canvas.drawText("Подытог: ${formatRubles(calculation.subtotalKopecks)}", margin, y, bodyPaint)
        y += 14f
        if (calculation.discountKopecks > 0) {
            canvas.drawText("Скидка: ${formatRubles(calculation.discountKopecks)}", margin, y, bodyPaint)
            y += 14f
        }
        if (data.estimate.extraFeeKopecks > 0) {
            canvas.drawText("Доплата: ${formatRubles(data.estimate.extraFeeKopecks)}", margin, y, bodyPaint)
            y += 14f
        }
        if (settings.showComplexityInPdf && data.estimate.complexityMultiplier != 1.0) {
            canvas.drawText(
                "Коэффициент сложности: ${data.estimate.complexityMultiplier}",
                margin,
                y,
                bodyPaint,
            )
            y += 14f
        }
        if (calculation.taxKopecks > 0 || data.estimate.taxPercent > 0) {
            canvas.drawText(
                "${settings.taxLabel} (${data.estimate.taxPercent}%): ${formatRubles(calculation.taxKopecks)}",
                margin,
                y,
                bodyPaint,
            )
            y += 14f
        }
        canvas.drawText("ИТОГО: ${formatRubles(calculation.totalKopecks)}", margin, y, titlePaint.apply { textSize = 14f })
        y += 20f

        data.estimate.comment?.takeIf { it.isNotBlank() }?.let { comment ->
            wrapText("Комментарий: $comment", bodyPaint, pageWidth - 2 * margin).forEach { line ->
                newPageIfNeeded(16f)
                canvas.drawText(line, margin, y, bodyPaint)
                y += 14f
            }
        }

        y += 8f
        val validUntil = dateFormat.format(
            Date(data.estimate.createdAtMillis + settings.offerValidityDays * 24L * 60 * 60 * 1000),
        )
        canvas.drawText("Срок действия предложения: до $validUntil", margin, y, smallPaint)
        y += 16f

        wrapText(settings.pdfFooter, smallPaint, pageWidth - 2 * margin).forEach { line ->
            newPageIfNeeded(14f)
            canvas.drawText(line, margin, y, smallPaint)
            y += 12f
        }

        document.finishPage(page)
        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(' ')
        val lines = mutableListOf<String>()
        var current = ""
        for (word in words) {
            val test = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(test) <= maxWidth) {
                current = test
            } else {
                if (current.isNotEmpty()) lines.add(current)
                current = word
            }
        }
        if (current.isNotEmpty()) lines.add(current)
        return lines.ifEmpty { listOf("") }
    }
}
