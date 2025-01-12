package de.domschmidt.koku.controller.documents;

import com.google.common.collect.ImmutableMap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import de.domschmidt.koku.dto.formular.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.*;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
public class DocumentPdfService {

    public static final String CHECKBOX_EMPTY_BASE64_SVG = "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHN0eWxlPSJ3aWR0aDo0OHB4O2hlaWdodDo0OHB4OyIgdmlld0JveD0iMCAwIDI0IDI0Ij4KICAgICAgICAgICAgICAgICAgICA8cGF0aCBmaWxsPSIjMDAwMDAwIiBuZy1hdHRyLWQ9Int7aWNvbi5kYXRhfX0iIGQ9Ik0xOSwzSDVDMy44OSwzIDMsMy44OSAzLDVWMTlBMiwyIDAgMCwwIDUsMjFIMTlBMiwyIDAgMCwwIDIxLDE5VjVDMjEsMy44OSAyMC4xLDMgMTksM00xOSw1VjE5SDVWNUgxOVoiLz4KICAgICAgICAgICAgICAgIDwvc3ZnPg==";
    public static final String CHECKBOX_CHECKED_BASE64_SVG = "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHN0eWxlPSJ3aWR0aDo0OHB4O2hlaWdodDo0OHB4OyIgdmlld0JveD0iMCAwIDI0IDI0Ij4KICAgICAgICAgICAgICAgICAgICA8cGF0aCBmaWxsPSIjMDAwMDAwIiBuZy1hdHRyLWQ9Int7aWNvbi5kYXRhfX0iIGQ9Ik0xOSwxOUg1VjVIMTVWM0g1QzMuODksMyAzLDMuODkgMyw1VjE5QTIsMiAwIDAsMCA1LDIxSDE5QTIsMiAwIDAsMCAyMSwxOVYxMUgxOU03LjkxLDEwLjA4TDYuNSwxMS41TDExLDE2TDIxLDZMMTkuNTksNC41OEwxMSwxMy4xN0w3LjkxLDEwLjA4WiIvPgogICAgICAgICAgICAgICAgPC9zdmc+";

    public static final int COL_COUNT = 12;

    public ByteArrayOutputStream createPdfByDynamicDocument(
            final FormularDto formularDto
    ) {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // step 1: creation of a document-object
        final Rectangle pageSize = PageSize.A4;
        final Document document = new Document(pageSize);
        try {
            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            PdfWriter pdfWriter = PdfWriter.getInstance(document, outputStream);

            // step 3: we open the document
            document.open();

            document.add(convertRows(document, pdfWriter, formularDto.getRows()));

        } catch (final DocumentException de) {
            log.error("Unable to create PDF", de);
        } catch (final IOException ioe) {
            log.error("Unable to write PDF", ioe);
        }
        document.close();

        return outputStream;
    }

    private PdfPTable convertRows(
            final Document outDocument,
            final PdfWriter pdfWriter,
            final List<FormularRowDto> rows
    ) throws IOException {
        final PdfPTable table = new PdfPTable(COL_COUNT);
        table.setWidthPercentage(100);
        for (final FormularRowDto formularRow : rows) {

            for (final FormularItemDto formularItem : formularRow.getItems()) {

                final int colspan = getColspan(formularItem);

                if (formularItem instanceof final TextFormularItemDto castedFieldType) {
                    final Paragraph paragraph = new Paragraph(StringUtils.defaultString(castedFieldType.getText(), ""));
                    paragraph.setAlignment(getHorizontalAlignment(formularItem));
                    if (castedFieldType.getFontSize() != null) {
                        Integer fontSize = castedFieldType.getFontSize();
                        if (fontSize == null) {
                            fontSize = 12;
                        }
                        paragraph.getFont().setSize(fontSize);
                    }
                    final PdfPCell currentCell = new PdfPCell(paragraph);
                    currentCell.setColspan(colspan);
                    currentCell.setHorizontalAlignment(getHorizontalAlignment(formularItem));
                    currentCell.setPadding(5);
                    currentCell.setBorderWidth(0);
                    currentCell.setUseAscender(true);
                    currentCell.setVerticalAlignment(getVerticalAlignment(formularRow));
                    table.addCell(currentCell);
                } else if (formularItem instanceof final ActivityPriceListFormularItemDto castedFieldType) {
                    final PdfPTable convertedInnerTable = convertRows(outDocument, pdfWriter, castedFieldType.getEvaluatedData());

                    final PdfPCell currentCell = new PdfPCell(convertedInnerTable);
                    currentCell.setColspan(colspan);
                    currentCell.setHorizontalAlignment(getHorizontalAlignment(formularItem));
                    currentCell.setPadding(0);
                    currentCell.setBorderWidth(0);
                    currentCell.setUseAscender(true);
                    currentCell.setVerticalAlignment(getVerticalAlignment(formularRow));

                    table.addCell(currentCell);
                } else if (formularItem instanceof final SVGFormularItemDto castedFieldType) {
                    final Image svgImage = createImageFromSvgBase64(pdfWriter, castedFieldType.getSvgContentBase64encoded());
                    svgImage.setAlignment(getHorizontalAlignment(formularItem));
                    svgImage.scaleToFit(castedFieldType.getMaxWidthInPx(), outDocument.getPageSize().getHeight());

                    final PdfPCell currentCell = new PdfPCell(svgImage);
                    currentCell.setColspan(colspan);
                    currentCell.setHorizontalAlignment(getHorizontalAlignment(formularItem));
                    currentCell.setPadding(5);
                    currentCell.setBorderWidth(0);
                    currentCell.setUseAscender(true);
                    currentCell.setVerticalAlignment(getVerticalAlignment(formularRow));
                    table.addCell(currentCell);
                } else if (formularItem instanceof SignatureFormularItemDto) {
                    final String dataUriString = StringUtils.defaultString(((SignatureFormularItemDto) formularItem).getDataUri());
                    final String base64ImageContent = dataUriString.substring(dataUriString.indexOf(",") + 1);
                    final Image image;
                    if (!dataUriString.trim().isEmpty()) {
                        if (dataUriString.startsWith("data:image/svg+xml;")) {
                            // svg detected;
                            image = createImageFromSvgBase64(pdfWriter, base64ImageContent);
                        } else {
                            image = Image.getInstance(Base64.getDecoder().decode(base64ImageContent));
                        }
                    } else {
                        // do nothing
                        image = Image.getInstance("");
                    }
                    image.setAlignment(getHorizontalAlignment(formularItem));
                    final float fieldWidth = ((outDocument.getPageSize().getWidth() - outDocument.leftMargin() - outDocument.rightMargin()) / 12) * colspan;
                    image.scaleToFit(fieldWidth, fieldWidth / 3);

                    final PdfPCell currentCell = new PdfPCell(image);
                    currentCell.setColspan(colspan);
                    currentCell.setHorizontalAlignment(getHorizontalAlignment(formularItem));
                    currentCell.setPadding(5);
                    currentCell.setBorderWidth(0);
                    currentCell.setUseAscender(true);
                    currentCell.setVerticalAlignment(getVerticalAlignment(formularRow));
                    table.addCell(currentCell);
                } else if (formularItem instanceof CheckboxFormularItemDto) {
                    // build label
                    Integer fontSize = ((CheckboxFormularItemDto) formularItem).getFontSize();
                    if (fontSize == null) {
                        fontSize = 12;
                    }

                    // build checkbox img
                    final Image checkboxImage;
                    if (((CheckboxFormularItemDto) formularItem).isValue()) {
                        checkboxImage = createImageFromSvgBase64(pdfWriter, CHECKBOX_CHECKED_BASE64_SVG);
                    } else {
                        checkboxImage = createImageFromSvgBase64(pdfWriter, CHECKBOX_EMPTY_BASE64_SVG);
                    }
                    final PdfPCell imageCell = new PdfPCell(checkboxImage, true);
                    imageCell.setVerticalAlignment(getVerticalAlignment(formularRow));
                    imageCell.setBorderWidth(0);
                    imageCell.setPaddingLeft(1);
                    imageCell.setPaddingRight(1);

                    final PdfPTable checkboxTable = new PdfPTable(1);
                    checkboxTable.setHorizontalAlignment(getHorizontalAlignment(formularItem));
                    checkboxTable.setWidths(new float[] {fontSize});
                    checkboxTable.setTotalWidth(fontSize + 5);
                    checkboxTable.setLockedWidth(true);

                    // add checkbox image
                    checkboxTable.addCell(imageCell);

                    final PdfPCell currentCell = new PdfPCell(checkboxTable);
                    currentCell.setColspan(colspan);
                    currentCell.setPadding(5);
                    currentCell.setBorderWidth(0);
                    currentCell.setUseAscender(true);
                    imageCell.setVerticalAlignment(getVerticalAlignment(formularRow));
                    table.addCell(currentCell);
                } else if (formularItem instanceof final QrCodeFormularItemDto castedField) {
                    final String content = castedField.getValue();

                    QRCodeWriter barcodeWriter = new QRCodeWriter();
                    final BitMatrix bitMatrix;
                    try {
                        BigDecimal maxWidth = (BigDecimal.valueOf(outDocument.getPageSize().getWidth() - outDocument.leftMargin() - outDocument.rightMargin()).divide(new BigDecimal(12), RoundingMode.DOWN)).multiply(new BigDecimal(colspan));
                        if (castedField.getWidthPercentage() != null) {
                            maxWidth = maxWidth.multiply(new BigDecimal(castedField.getWidthPercentage())).divide(new BigDecimal(100), RoundingMode.DOWN);
                        }
                        final BigDecimal calculatedWidthAndHeight;
                        if (castedField.getMaxWidthInPx() != null && maxWidth.compareTo(new BigDecimal(castedField.getMaxWidthInPx())) > 0) {
                            calculatedWidthAndHeight = new BigDecimal(castedField.getMaxWidthInPx());
                        } else {
                            calculatedWidthAndHeight = maxWidth;
                        }
                        bitMatrix = barcodeWriter.encode(
                                content,
                                BarcodeFormat.QR_CODE,
                                calculatedWidthAndHeight.intValue(),
                                calculatedWidthAndHeight.intValue(),
                                ImmutableMap.of(
                                        EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H
                                )
                        );

                        final Image image = Image.getInstance(
                                MatrixToImageWriter.toBufferedImage(bitMatrix),
                                null,
                                false
                        );
                        image.setAlignment(getHorizontalAlignment(formularItem));
                        image.scaleToFit(calculatedWidthAndHeight.intValue(), calculatedWidthAndHeight.intValue());
                        final PdfPCell currentCell = new PdfPCell(image);
                        currentCell.setColspan(colspan);
                        currentCell.setHorizontalAlignment(getHorizontalAlignment(formularItem));
                        currentCell.setPadding(5);
                        currentCell.setBorderWidth(0);
                        currentCell.setUseAscender(true);
                        currentCell.setVerticalAlignment(getVerticalAlignment(formularRow));
                        table.addCell(currentCell);
                    } catch (final WriterException we) {
                        log.error("Unexpected Exception", we);
                    }
                } else if (formularItem instanceof final DateFormularItemDto castedDateField) {
                    LocalDate value = castedDateField.getValue();
                    final Paragraph paragraph;
                    if (value != null) {
                        paragraph = new Paragraph(value.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                    } else {
                        paragraph = new Paragraph("");
                    }
                    paragraph.setAlignment(getHorizontalAlignment(formularItem));
                    if (castedDateField.getFontSize() != null) {
                        Integer fontSize = castedDateField.getFontSize();
                        if (fontSize == null) {
                            fontSize = 12;
                        }
                        paragraph.getFont().setSize(fontSize);
                    }
                    final PdfPCell currentCell = new PdfPCell(paragraph);
                    currentCell.setColspan(colspan);
                    currentCell.setHorizontalAlignment(getHorizontalAlignment(formularItem));
                    currentCell.setPadding(5);
                    currentCell.setBorderWidth(0);
                    currentCell.setUseAscender(true);
                    currentCell.setVerticalAlignment(getVerticalAlignment(formularRow));
                    table.addCell(currentCell);
                } else {
                    System.out.println("unknown type");
                }
            }
        }

        // set border of default cell to be invisible
        table.getDefaultCell().setBorder(0);
        // fill the rest of the row using the default cell
        table.completeRow();

        return table;
    }

    private int getVerticalAlignment(final FormularRowDto formularRow) {
        final int result;
        switch (formularRow.getAlign()) {
            case CENTER:
                result = Paragraph.ALIGN_MIDDLE;
                break;
            case BOTTOM:
                result = Paragraph.ALIGN_BOTTOM;
                break;
            case TOP:
            default:
                result = Paragraph.ALIGN_TOP;
                break;
        }
        return result;
    }

    private int getColspan(FormularItemDto formularItem) {
        int result = 12;

        if (formularItem.getMd() != null) {
            result = formularItem.getMd();
        } else if (formularItem.getSm() != null) {
            result = formularItem.getSm();
        } else if (formularItem.getXs() != null) {
            result = formularItem.getXs();
        }

        return result;
    }

    private int getHorizontalAlignment(FormularItemDto formularItem) {
        final int result;
        switch (formularItem.getAlign()) {
            case CENTER:
                result = Paragraph.ALIGN_CENTER;
                break;
            case RIGHT:
                result = Paragraph.ALIGN_RIGHT;
                break;
            case LEFT:
            default:
                result = Paragraph.ALIGN_LEFT;
                break;
        }
        return result;
    }


    private Image createImageFromSvgBase64(final PdfWriter pdfWriter, final String base64Svg) throws IOException {
        final int heightInPx = 40; // default height limit

        final SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
        final SVGDocument svgDocument = factory.createSVGDocument("http://www.w3.org/2000/svg", new ByteArrayInputStream(Base64.getDecoder().decode(base64Svg)));
        final UserAgent userAgent = new UserAgentAdapter();
        final DocumentLoader loader = new DocumentLoader(userAgent);
        final BridgeContext context = new BridgeContext(userAgent, loader);
        context.setDynamicState(BridgeContext.DYNAMIC);
        final GVTBuilder builder = new GVTBuilder();
        org.apache.batik.gvt.GraphicsNode rootGraphicsNode = builder.build(context, svgDocument);
        final float svgImageWidthInPx = (float) rootGraphicsNode.getPrimitiveBounds().getWidth();
        final float svgImageHeightInPx = (float) rootGraphicsNode.getPrimitiveBounds().getHeight();

        final float widthAccordingToAspectRatio = (svgImageWidthInPx / svgImageHeightInPx) * heightInPx;
        svgDocument.getDocumentElement().setAttribute("width", String.valueOf(widthAccordingToAspectRatio));
        svgDocument.getDocumentElement().setAttribute("height", String.valueOf(heightInPx));
        svgDocument.getDocumentElement().setAttribute("color", "#000000");

        final PdfTemplate template = PdfTemplate.createTemplate(pdfWriter, widthAccordingToAspectRatio, heightInPx);
        final Graphics2D g2d = template.createGraphics(template.getWidth(), template.getHeight());
        g2d.setRenderingHint(RenderingHintsKeyExt.KEY_TRANSCODING, RenderingHintsKeyExt.VALUE_TRANSCODING_PRINTING);

        try {
            rootGraphicsNode.paint(g2d);
        } finally {
            g2d.dispose();
        }
        return Image.getInstance(template);
    }

}
