package de.domschmidt.koku.controller.customer;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
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
import de.domschmidt.koku.persistence.model.Customer;
import de.domschmidt.koku.persistence.model.uploads.FileUpload;
import de.domschmidt.koku.persistence.model.uploads.QFileUpload;
import de.domschmidt.koku.persistence.model.uploads.QFileUploadTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;

@RestController
@Slf4j
public class CustomerDocumentsTableController {

    private static final QFileUpload qFileUpload = QFileUpload.fileUpload;
    private static final QFileUploadTag qFileUploadTag = QFileUploadTag.fileUploadTag;
    private static final ColumnUageConfiguration FIXED_COLUMN_USAGE_CONFIGURATION = new ColumnUageConfiguration(
            new ColumnUsageDescriptionBuilder<>(FileUpload.Fields.uuid, "ID", qFileUpload.uuid).hidden(true).build(),
            new ColumnUsageDescriptionBuilder<>(FileUpload.Fields.fileName, "Dateiname", qFileUpload.fileName).build(),
            new ColumnUsageDescriptionBuilder<>(FileUpload.Fields.customer + '.' + Customer.Fields.id, "Kundennummer", qFileUpload.customer.id).hidden(true).build(),
            new ColumnUsageDescriptionBuilder<>(FileUpload.Fields.customer + '.' + Customer.Fields.firstName, "Vorname", qFileUpload.customer.firstName).build(),
            new ColumnUsageDescriptionBuilder<>(FileUpload.Fields.customer + '.' + Customer.Fields.lastName, "Nachname", qFileUpload.customer.lastName).build(),
            new ColumnUsageDescriptionBuilder<>(FileUpload.Fields.creationDate, "Erstellungsdatum", qFileUpload.creationDate).defaultSort(DataQueryColumnSortDirDto.DESC, 0).build(),
            new ColumnUsageDescriptionBuilder<>(FileUpload.Fields.deleted, "Gelöscht", qFileUpload.deleted).defaultSearchValue(false).build(),
            new ColumnUsageDescriptionBuilder<>(FileUpload.Fields.tags, "Metadaten",
                    JPAExpressions.select(Expressions.stringTemplate("STRING_AGG({0}, ', ')", qFileUploadTag.value))
                            .from(qFileUploadTag)
                            .where(qFileUploadTag.fileUpload.eq(qFileUpload))
            ).hidden(true).build()
    );

    private final EntityManager entityManager;

    @Autowired
    public CustomerDocumentsTableController(
            final EntityManager entityManager
    ) {
        this.entityManager = entityManager;
    }

    @PostMapping(value = "/customers/documents/table")
    public DataTableDto getCustomerDocumentsTable(
            @RequestBody final DataQuerySpecDto querySpec
    ) {
        final DataTableLimitAndOffsetFactory dataTableLimitAndOffsetFactory = new DataTableLimitAndOffsetFactory(querySpec);
        final QueryResults<Tuple> listOfEntities = new JPAQuery<>(this.entityManager)
                .select(FIXED_COLUMN_USAGE_CONFIGURATION.getSelection())
                .where(new DataTableColumnExpressionFactory().buildExpressions(
                                FIXED_COLUMN_USAGE_CONFIGURATION,
                                querySpec
                        )
                )
                .from(qFileUpload)
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
                "Kundendokumente"
        );

        return dataTableFactory.buildTable();
    }

}
