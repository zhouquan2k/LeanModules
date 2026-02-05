package io.leanddd.module.pdf;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import io.leanddd.component.framework.Context;
import io.leanddd.component.framework.MetadataProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PdfUtil {

    protected PdfDocument pdf;
    protected PdfFont font;
    protected PdfFormXObject pageNumberTemplate;

    private static MetadataProvider metadataProvider;

    private static int pageNumberPosX = 300;
    private static int pageNumberPosY = 10;

    public PdfUtil() {
        if (metadataProvider == null) {
            metadataProvider = Context.getBean(MetadataProvider.class);
        }
    }

    public PdfUtil(boolean isTest) {
    }

    public void init(OutputStream output, boolean needPageNumber) {

        PdfWriter writer = new PdfWriter(output);
        pdf = new PdfDocument(writer);

        try {
            font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", true); //PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (needPageNumber) {
            addPageNumbers();
        }
    }

    public void exit() {
        if (pageNumberTemplate != null) {
            addPageNumbers2();
        }
    }

    // 添加页码事件处理
    private void addPageNumbers() {
        pageNumberTemplate = new PdfFormXObject(new Rectangle(pageNumberPosX, pageNumberPosY, 30, 30));
        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new IEventHandler() {
            @Override
            public void handleEvent(Event event) {
                PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
                PdfDocument pdfDoc = docEvent.getDocument();
                PdfPage page = docEvent.getPage();
                int pageNumber = pdfDoc.getPageNumber(page);

                PdfCanvas canvas = new PdfCanvas(page);
                canvas.beginText();
                canvas.setFontAndSize(font, 12);
                canvas.moveText(page.getPageSize().getWidth() / 2 - 15, pageNumberPosY);
                canvas.showText(String.format("%d / ", pageNumber));
                canvas.endText();
                canvas.stroke();
                canvas.addXObject(pageNumberTemplate, 0, 0);
                canvas.release();
            }
        });
    }

    private void addPageNumbers2() {
        PdfCanvas canvas = new PdfCanvas(pageNumberTemplate, pdf);
        canvas.beginText();
        canvas.setFontAndSize(font, 12);
        canvas.moveText(pageNumberPosX, pageNumberPosY);
        var text = Integer.toString(pdf.getNumberOfPages());
        canvas.showText(text);
        canvas.endText();
        canvas.release();
    }

    protected Cell createCell(String content) {
        if (content == null) content = "";
        Cell cell = new Cell().add(new Paragraph(content).setCharacterSpacing(1.5f)).setFont(font).setFontSize(10);
        cell.setPadding(3);
        cell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        return cell;
    }

    protected Cell createCell(String content, int colspan, int rowspan) {
        if (content == null) content = "";
        Cell cell = new Cell(rowspan, colspan).add(new Paragraph(content).setCharacterSpacing(1.5f)).setFont(font).setFontSize(10);
        cell.setPadding(3);
        cell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        return cell;
    }

    protected String getDictionaryLabel(String dictName, String value) {
        var dict = metadataProvider.getDictionary(dictName, null);
        var dictItem = dict.get(value);
        return dictItem != null ? dictItem.getLabel() : "?";
    }

    protected String getDecimalValue(Double value) {
        return value == null ? null : value == Math.floor(value) ? String.valueOf(value.intValue()) : String.valueOf(value);
    }

    protected String formatDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    // public abstract void print(OutputStream output, Object param);
}
