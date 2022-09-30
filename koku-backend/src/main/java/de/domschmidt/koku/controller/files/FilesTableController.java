package de.domschmidt.koku.controller.files;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import de.domschmidt.datatable.dto.DataTableDto;
import de.domschmidt.datatable.dto.query.DataQueryColumnSortDirDto;
import de.domschmidt.datatable.dto.query.DataQuerySpecDto;
import de.domschmidt.datatable.factory.DataTableColumnExpressionFactory;
import de.domschmidt.datatable.factory.DataTableFactory;
import de.domschmidt.datatable.factory.DataTableLimitAndOffsetFactory;
import de.domschmidt.datatable.factory.DataTableOrderByFactory;
import de.domschmidt.datatable.factory.data.ColumnUageConfiguration;
import de.domschmidt.datatable.factory.data.ColumnUsageDescriptionBuilder;
import de.domschmidt.koku.persistence.model.QCustomer;
import de.domschmidt.koku.persistence.model.dynamic_documents.DynamicDocument;
import de.domschmidt.koku.persistence.model.dynamic_documents.QDynamicDocument;
import de.domschmidt.koku.persistence.model.uploads.FileUpload;
import de.domschmidt.koku.persistence.model.uploads.QFileUpload;
import de.domschmidt.koku.persistence.model.uploads.QFileUploadTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import java.util.TreeSet;

@RestController
@Slf4j
public class FilesTableController {

    private static final QFileUpload qFileUpload = QFileUpload.fileUpload;
    private static final QFileUploadTag qFileUploadTag = QFileUploadTag.fileUploadTag;
    private static final QCustomer qCustomer = QCustomer.customer;
    private static final QDynamicDocument qDynamicDocument = QDynamicDocument.dynamicDocument;
    public static final int GB = 1024 * 1024 * 1024;
    public static final int MB = 1024 * 1024;
    public static final int KB = 1024;

    private final EntityManager entityManager;

    @Autowired
    public FilesTableController(
            final EntityManager entityManager
    ) {
        this.entityManager = entityManager;
    }

    @PostMapping(value = "/files/table")
    public DataTableDto getFilesTable(
            @RequestBody final DataQuerySpecDto querySpec
    ) {
        final ColumnUageConfiguration columnConfig = new ColumnUageConfiguration(
                new ColumnUsageDescriptionBuilder<>(FileUpload.Fields.uuid, "ID", qFileUpload.uuid).hidden(true).build(),
                new ColumnUsageDescriptionBuilder<>(FileUpload.Fields.fileName, "Dateiname", qFileUpload.fileName).build(),
                new ColumnUsageDescriptionBuilder<>("Kunde", "Kunde", qCustomer.firstName.concat(" ").concat(qCustomer.lastName).trim()).build(),
                new ColumnUsageDescriptionBuilder<>(FileUpload.Fields.dynamicDocument + '.' + DynamicDocument.Fields.description, "Dokumentenvorlage", qDynamicDocument.description)
                        .possibleSelectValues(new TreeSet<>(new JPAQuery<>(this.entityManager)
                                .select(qDynamicDocument.description)
                                .from(qDynamicDocument)
                                .orderBy(qDynamicDocument.description.asc())
                                .fetch()
                        ))
                        .customDtoType("Select")
                        .build(),
                new ColumnUsageDescriptionBuilder<>(FileUpload.Fields.creationDate, "Erstellungsdatum", qFileUpload.creationDate).defaultSort(DataQueryColumnSortDirDto.DESC, 0).build(),
                new ColumnUsageDescriptionBuilder<>(FileUpload.Fields.size, "Größe",
                        new CaseBuilder()
                                .when(qFileUpload.size.gt(GB)).then(qFileUpload.size.doubleValue().divide(GB).multiply(100).round().divide(100).multiply(GB))
                                .when(qFileUpload.size.gt(MB)).then(qFileUpload.size.doubleValue().divide(MB).multiply(100).round().doubleValue().divide(100).multiply(MB))
                                .when(qFileUpload.size.gt(KB)).then(qFileUpload.size.doubleValue().divide(KB).multiply(100).round().doubleValue().divide(100).multiply(KB))
                                .otherwise(qFileUpload.size.doubleValue())
                ).customDtoType("FileSize").build(),
                new ColumnUsageDescriptionBuilder<>(FileUpload.Fields.deleted, "Gelöscht", qFileUpload.deleted).defaultSearchValue(false).build(),
                new ColumnUsageDescriptionBuilder<>(FileUpload.Fields.tags, "Metadaten",
                        JPAExpressions.select(Expressions.stringTemplate("STRING_AGG({0}, ', ')", qFileUploadTag.value))
                                .from(qFileUploadTag)
                                .where(qFileUploadTag.fileUpload.eq(qFileUpload))
                ).hidden(true).build()
        );

        final DataTableLimitAndOffsetFactory dataTableLimitAndOffsetFactory = new DataTableLimitAndOffsetFactory(querySpec);
        final QueryResults<Tuple> listOfEntities = new JPAQuery<>(this.entityManager)
                .select(columnConfig.getSelection())
                .where(new DataTableColumnExpressionFactory().buildExpressions(
                                columnConfig,
                                querySpec
                        )
                )
                .from(qFileUpload)
                .leftJoin(qFileUpload.customer, qCustomer)
                .leftJoin(qFileUpload.dynamicDocument, qDynamicDocument)
                .orderBy(new DataTableOrderByFactory().buildOrderBySpecifiers(
                        columnConfig,
                        querySpec
                ))
                .offset(dataTableLimitAndOffsetFactory.getOffset())
                .limit(dataTableLimitAndOffsetFactory.getLimit())
                .fetchResults();//optimized query does not work here. deprecated call cannot be avoided

        final DataTableFactory<Tuple> dataTableFactory = new DataTableFactory<>(
                listOfEntities,
                columnConfig,
                "Dateiübersicht"
        );

        return dataTableFactory.buildTable();
    }

}
