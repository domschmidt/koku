package de.domschmidt.koku.db_ext;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.spi.MetadataBuilderContributor;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class PostgresFunctionRegister implements MetadataBuilderContributor {

    @Override
    public void contribute(MetadataBuilder metadataBuilder) {
        metadataBuilder.applySqlFunction(
                "STRING_AGG",
                new StandardSQLFunction(
                        "STRING_AGG",
                        StandardBasicTypes.STRING
                )
        );
    }
}
