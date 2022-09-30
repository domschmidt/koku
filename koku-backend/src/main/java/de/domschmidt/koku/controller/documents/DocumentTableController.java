package de.domschmidt.koku.controller.documents;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.jpa.impl.JPAQuery;
import de.domschmidt.datatable.dto.DataTableDto;
import de.domschmidt.datatable.dto.query.DataQuerySpecDto;
import de.domschmidt.datatable.factory.DataTableColumnExpressionFactory;
import de.domschmidt.datatable.factory.DataTableFactory;
import de.domschmidt.datatable.factory.DataTableLimitAndOffsetFactory;
import de.domschmidt.datatable.factory.DataTableOrderByFactory;
import de.domschmidt.datatable.factory.data.ColumnUageConfiguration;
import de.domschmidt.datatable.factory.data.ColumnUsageDescriptionBuilder;
import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.dto.formular.DocumentContextEnumDto;
import de.domschmidt.koku.dto.formular.FormularDto;
import de.domschmidt.koku.persistence.model.dynamic_documents.DynamicDocument;
import de.domschmidt.koku.persistence.model.dynamic_documents.QDynamicDocument;
import de.domschmidt.koku.service.impl.DocumentService;
import de.domschmidt.koku.service.searchoptions.DocumentSearchOptions;
import de.domschmidt.koku.transformer.DynamicDocumentToFormularDtoTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;

@Slf4j
@RestController
@RequestMapping("/documents")
public class DocumentTableController extends AbstractController<DynamicDocument, FormularDto, DocumentSearchOptions> {

    private static final QDynamicDocument qDynamicDocument = QDynamicDocument.dynamicDocument;

    private static final ColumnUageConfiguration FIXED_COLUMN_USAGE_CONFIGURATION = new ColumnUageConfiguration(
            new ColumnUsageDescriptionBuilder<>(DynamicDocument.Fields.id, "ID", qDynamicDocument.id).hidden(true).build(),
            new ColumnUsageDescriptionBuilder<>(DynamicDocument.Fields.description, "Name", qDynamicDocument.description).build()
    );
    private final EntityManager entityManager;
    private final DynamicDocumentToFormularDtoTransformer dynamicDocumentToFormularDtoTransformer;

    @Autowired
    public DocumentTableController(
            final DocumentService documentService,
            final DynamicDocumentToFormularDtoTransformer dynamicDocumentToFormularDtoTransformer,
            final EntityManager entityManager
    ) {
        super(documentService, dynamicDocumentToFormularDtoTransformer);
        this.entityManager = entityManager;
        this.dynamicDocumentToFormularDtoTransformer = dynamicDocumentToFormularDtoTransformer;
    }

    @PostMapping(value = "/context/{context}")
    public DataTableDto findAllByContext(
            @RequestBody final DataQuerySpecDto querySpec,
            @PathVariable("context") final DocumentContextEnumDto context
    ) {
        final DataTableLimitAndOffsetFactory dataTableLimitAndOffsetFactory = new DataTableLimitAndOffsetFactory(querySpec);
        final QueryResults<Tuple> listOfEntities = new JPAQuery<>(this.entityManager)
                .select(FIXED_COLUMN_USAGE_CONFIGURATION.getSelection())
                .where(ExpressionUtils.and(
                        new DataTableColumnExpressionFactory().buildExpressions(
                                FIXED_COLUMN_USAGE_CONFIGURATION,
                                querySpec
                        ),
                        qDynamicDocument.context.eq(dynamicDocumentToFormularDtoTransformer.transformContext(context))
                ))
                .from(qDynamicDocument)
                .orderBy(new DataTableOrderByFactory().buildOrderBySpecifiers(
                        FIXED_COLUMN_USAGE_CONFIGURATION,
                        querySpec
                ))
                .offset(dataTableLimitAndOffsetFactory.getOffset())
                .limit(dataTableLimitAndOffsetFactory.getLimit())
                .fetchResults();//optimized query does not work here. deprecated call cannot be avoided

        final DataTableFactory<Tuple> dataTableFactory = new DataTableFactory<>(
                listOfEntities,
                FIXED_COLUMN_USAGE_CONFIGURATION,
                "Dateiübersicht"
        );

        return dataTableFactory.buildTable();
    }


}
