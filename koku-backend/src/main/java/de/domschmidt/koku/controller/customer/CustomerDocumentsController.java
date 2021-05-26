package de.domschmidt.koku.controller.customer;

import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.dto.UploadDto;
import de.domschmidt.koku.dto.formular.*;
import de.domschmidt.koku.persistence.dao.FileUploadRepository;
import de.domschmidt.koku.persistence.model.Customer;
import de.domschmidt.koku.persistence.model.dynamic_documents.DynamicDocument;
import de.domschmidt.koku.persistence.model.uploads.FileUpload;
import de.domschmidt.koku.service.ICustomerService;
import de.domschmidt.koku.service.impl.DocumentService;
import de.domschmidt.koku.service.impl.StorageService;
import de.domschmidt.koku.service.searchoptions.DocumentSearchOptions;
import de.domschmidt.koku.transformer.DynamicDocumentToFormularDtoTransformer;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.*;
import org.apache.batik.util.XMLResourceDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/customers/{customerId}/documents")
@Slf4j
public class CustomerDocumentsController extends AbstractController<DynamicDocument, FormularDto, DocumentSearchOptions> {

    public static final String CHECKBOX_EMPTY_BASE64_SVG = "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHN0eWxlPSJ3aWR0aDo0OHB4O2hlaWdodDo0OHB4OyIgdmlld0JveD0iMCAwIDI0IDI0Ij4KICAgICAgICAgICAgICAgICAgICA8cGF0aCBmaWxsPSIjMDAwMDAwIiBuZy1hdHRyLWQ9Int7aWNvbi5kYXRhfX0iIGQ9Ik0xOSwzSDVDMy44OSwzIDMsMy44OSAzLDVWMTlBMiwyIDAgMCwwIDUsMjFIMTlBMiwyIDAgMCwwIDIxLDE5VjVDMjEsMy44OSAyMC4xLDMgMTksM00xOSw1VjE5SDVWNUgxOVoiLz4KICAgICAgICAgICAgICAgIDwvc3ZnPg==";
    public static final String CHECKBOX_CHECKED_BASE64_SVG = "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHN0eWxlPSJ3aWR0aDo0OHB4O2hlaWdodDo0OHB4OyIgdmlld0JveD0iMCAwIDI0IDI0Ij4KICAgICAgICAgICAgICAgICAgICA8cGF0aCBmaWxsPSIjMDAwMDAwIiBuZy1hdHRyLWQ9Int7aWNvbi5kYXRhfX0iIGQ9Ik0xOSwxOUg1VjVIMTVWM0g1QzMuODksMyAzLDMuODkgMyw1VjE5QTIsMiAwIDAsMCA1LDIxSDE5QTIsMiAwIDAsMCAyMSwxOVYxMUgxOU03LjkxLDEwLjA4TDYuNSwxMS41TDExLDE2TDIxLDZMMTkuNTksNC41OEwxMSwxMy4xN0w3LjkxLDEwLjA4WiIvPgogICAgICAgICAgICAgICAgPC9zdmc+";

    public static final int COL_COUNT = 12;
    private final ICustomerService customerService;
    private final StorageService storageService;
    private final FileUploadRepository fileUploadRepository;
    private final DynamicDocumentToFormularDtoTransformer transformer;

    @Autowired
    public CustomerDocumentsController(final ICustomerService customerService,
                                       final StorageService storageService,
                                       final FileUploadRepository fileUploadRepository,
                                       final DocumentService documentService,
                                       final DynamicDocumentToFormularDtoTransformer transformer) {
        super(documentService, transformer);
        this.customerService = customerService;
        this.transformer = transformer;
        this.storageService = storageService;
        this.fileUploadRepository = fileUploadRepository;
    }

    @GetMapping(value = "/{id}")
    public FormularDto findByIdTransformed(@PathVariable("id") Long id, @PathVariable("customerId") Long customerId) {
        final Customer customer = this.customerService.findById(customerId);
        final DynamicDocument model = findById(id);
        if (model == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final Map<String, Object> replacementToken = new HashMap<>();
        replacementToken.put("customer", customer);

        return this.transformer.transformToDto(model, replacementToken);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UploadDto createByOpenPdf(@RequestBody final FormularDto formularDto, @PathVariable("customerId") Long customerId) throws IOException {
        final Customer customer = this.customerService.findById(customerId);
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

            for (final FormularRowDto formularRow : formularDto.getRows()) {

                final PdfPTable table = new PdfPTable(COL_COUNT);
                table.setWidthPercentage(100);
                int usedCols = 0;

                for (final FormularItemDto formularItem : formularRow.getItems()) {

                    final int colspan = getColspan(formularItem);
                    if (usedCols + colspan > COL_COUNT) {
                        while (usedCols < COL_COUNT) {
                            // fill the current row with stupid empty columns
                            // this is required to properly organize the grid
                            // the function table.completeRow() adds strange borders.
                            // So this is a clone of completeRow() with a difference in the border definition
                            // (border is removed)
                            final PdfPCell emptyColumnFiller = new PdfPCell((Phrase) null);
                            emptyColumnFiller.setBorderWidth(0);
                            table.addCell(emptyColumnFiller);
                            usedCols = usedCols + 1;
                        }
                        usedCols = 0;
                    }

                    if (formularItem instanceof TextFormularItemDto) {
                        final Paragraph paragraph = new Paragraph(((TextFormularItemDto) formularItem).getText());
                        paragraph.setAlignment(getAlignment(formularItem));
                        if (((TextFormularItemDto) formularItem).getFontSize() != null) {
                            final FontSizeDto fontSize = ((TextFormularItemDto) formularItem).getFontSize();
                            if (fontSize != null) {
                                paragraph.getFont().setSize(fontSize.getPdfPCellFontSize());
                            }
                        }
                        final PdfPCell currentCell = new PdfPCell(paragraph);
                        currentCell.setColspan(colspan);
                        currentCell.setHorizontalAlignment(getAlignment(formularItem));
                        currentCell.setPadding(5);
                        currentCell.setBorderWidth(0);
                        currentCell.setUseAscender(true);
                        currentCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        table.addCell(currentCell);
                    } else if (formularItem instanceof SVGFormularItemDto) {
                        final Image svgImage = createImageFromSvgBase64(pdfWriter, ((SVGFormularItemDto) formularItem).getSvgContentBase64encoded());
                        svgImage.setAlignment(getAlignment(formularItem));
                        svgImage.scaleToFit(((SVGFormularItemDto) formularItem).getMaxWidthInPx(), pageSize.getHeight());

                        final PdfPCell currentCell = new PdfPCell(svgImage);
                        currentCell.setColspan(colspan);
                        currentCell.setHorizontalAlignment(getAlignment(formularItem));
                        currentCell.setPadding(5);
                        currentCell.setBorderWidth(0);
                        currentCell.setUseAscender(true);
                        currentCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        table.addCell(currentCell);
                    } else if (formularItem instanceof SignatureFormularItemDto) {
                        final String dataUriString = ((SignatureFormularItemDto) formularItem).getDataUri();
                        final String base64ImageContent = dataUriString.substring(dataUriString.indexOf(",") + 1);
                        final Image image;
                        if (dataUriString.startsWith("data:image/svg+xml;")) {
                            // svg detected;
                            image = createImageFromSvgBase64(pdfWriter, base64ImageContent);
                        } else {
                            image = Image.getInstance(Base64.getDecoder().decode(base64ImageContent));
                        }
                        image.setAlignment(getAlignment(formularItem));
                        image.scaleToFit(300, 100);

                        final PdfPCell currentCell = new PdfPCell(image);
                        currentCell.setColspan(colspan);
                        currentCell.setHorizontalAlignment(getAlignment(formularItem));
                        currentCell.setPadding(5);
                        currentCell.setBorderWidth(0);
                        currentCell.setUseAscender(true);
                        currentCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        table.addCell(currentCell);
                    } else if (formularItem instanceof CheckboxFormularItemDto) {
                        // build label
                        final Chunk labelChunk = new Chunk(((CheckboxFormularItemDto) formularItem).getLabel());
                        final Paragraph labelParagraph = new Paragraph(labelChunk);
                        FontSizeDto fontSize = ((CheckboxFormularItemDto) formularItem).getFontSize();
                        if (fontSize == null) {
                            fontSize = FontSizeDto.MEDIUM;
                        }
                        labelParagraph.getFont().setSize(fontSize.getPdfPCellFontSize());
                        final PdfPCell labelCell = new PdfPCell(labelParagraph);
                        labelCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
                        labelCell.setUseAscender(true);
                        labelCell.setBorderWidth(0);
                        labelCell.setPaddingLeft(1);
                        labelCell.setPaddingRight(1);

                        // build checkbox img
                        final Image checkboxImage;
                        if (((CheckboxFormularItemDto) formularItem).isValue()) {
                            checkboxImage = createImageFromSvgBase64(pdfWriter, CHECKBOX_CHECKED_BASE64_SVG);
                        } else {
                            checkboxImage = createImageFromSvgBase64(pdfWriter, CHECKBOX_EMPTY_BASE64_SVG);
                        }
                        final PdfPCell imageCell = new PdfPCell(checkboxImage, true);
                        imageCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
                        labelCell.setUseAscender(true);
                        imageCell.setBorderWidth(0);
                        imageCell.setPaddingLeft(1);
                        imageCell.setPaddingRight(1);

                        //build wrapping table (checkbox|label)
                        final PdfPTable checkboxTable = new PdfPTable(2);
                        checkboxTable.setHorizontalAlignment(getAlignment(formularItem));
                        checkboxTable.setWidths(new float[] {fontSize.getPdfPCellFontSize(), labelChunk.getWidthPoint()});
                        // sum of label text width + checkbox width + padding l&r + magic number 1
                        checkboxTable.setTotalWidth(labelChunk.getWidthPoint() + fontSize.getPdfPCellFontSize() + 5);
                        checkboxTable.setLockedWidth(true);

                        // add checkbox image
                        checkboxTable.addCell(imageCell);

                        // add label
                        checkboxTable.addCell(labelCell);

                        final PdfPCell currentCell = new PdfPCell(checkboxTable);
                        currentCell.setColspan(colspan);
                        currentCell.setPadding(5);
                        currentCell.setBorderWidth(0);
                        currentCell.setUseAscender(true);
                        currentCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        table.addCell(currentCell);
                    }

                    usedCols = usedCols + colspan;
                }

                document.add(table);
            }
        } catch (final DocumentException de) {
            log.error("Unable to create PDF", de);
        } catch (final IOException ioe) {
            log.error("Unable to write PDF", ioe);
        }
        document.close();

        final FileUpload newUpload = this.storageService.store(new ByteArrayInputStream(outputStream.toByteArray()), formularDto.getDescription() + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-hh-mm-ss")) + ".pdf");
        if (customer.getUploads() == null) {
            customer.setUploads(new ArrayList<>());
        }
        newUpload.setCustomer(customer);
        this.fileUploadRepository.save(newUpload);
        return UploadDto.builder()
                .creationDate(newUpload.getCreationDate())
                .fileName(newUpload.getFileName())
                .uuid(newUpload.getUuid())
                .build();
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

    private int getAlignment(FormularItemDto formularItem) {
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

        try {
            rootGraphicsNode.paint(g2d);
        } finally {
            g2d.dispose();
        }
        return Image.getInstance(template);
    }

}
