package com.itextpdf.matthias.mock_case;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;

import java.io.*;


public class MainClass {
    private static final String BASE_URI = "src/main/resources/";
    private static final String DEST = "results/Merged.pdf";
    static final String STD_FONT = StandardFonts.TIMES_BOLD;

    static {
        new File(DEST).getParentFile().mkdirs();
    }

    void createpdf() throws FileNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(BASE_URI + "X.pdf"));
        Document doc = new Document(pdf);
        doc.add(new Paragraph("Hi there!"));
        doc.close();
    }

    byte[] mergepdf(String inputFolder) throws IOException {
        File folder = new File(inputFolder);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument merged = new PdfDocument(new PdfWriter(baos));
        for (File pdfFile : folder.listFiles()) {
            PdfDocument pdf = new PdfDocument(new PdfReader(pdfFile));
            pdf.copyPagesTo(1, pdf.getNumberOfPages(), merged);
            pdf.close();
        }
        merged.close();
        baos.close();
        return baos.toByteArray();
    }

    public void manipulatepdf() throws IOException {
        PdfDocument pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(mergepdf(BASE_URI))), new PdfWriter(DEST));
        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new PrintPreview());
        pdf.close();
    }

    public static void main(String[] args) throws IOException {
        new MainClass().manipulatepdf();
    }

    private class PrintPreview implements IEventHandler {
        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent documentEvent = (PdfDocumentEvent) event;
            PdfDocument pdf = documentEvent.getDocument();
            PdfPage page = documentEvent.getPage();
            Rectangle currentCrop = page.getCropBox();
            PdfFont font = null;
            try {
                font = PdfFontFactory.createFont(STD_FONT);
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
            Paragraph p = new Paragraph("PRINTPREVIEW").setFont(font).setFontSize(20);
            PdfCanvas overContent = new PdfCanvas(page);

            overContent.setFillColor(ColorConstants.RED);
            overContent.saveState();
            PdfExtGState gs1 = new PdfExtGState();
            gs1.setFillOpacity(0.5f);
            overContent.setExtGState(gs1);
            Canvas canvas = new Canvas(overContent, pdf, pdf.getDefaultPageSize());
            canvas.showTextAligned(p, currentCrop.getWidth() / 2, currentCrop.getHeight() / 2, TextAlignment.CENTER);
            overContent.restoreState();
        }
    }
}
