package de.domschmidt.koku.persistence.model.dynamic_documents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter

@DiscriminatorValue("SVG")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "field_definition_svg", schema = "koku")
public class SVGFieldDefinitionType extends FieldDefinitionType implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;

    @Column(length = 130000)
    String svgContentBase64encoded;
    Integer widthPercentage;
    Integer maxWidthInPx;

    // copy constructor
    public SVGFieldDefinitionType(final SVGFieldDefinitionType fieldDefintionTypeToBeCopied) {
        this.id = null;
        this.svgContentBase64encoded = fieldDefintionTypeToBeCopied.getSvgContentBase64encoded();
        this.widthPercentage = fieldDefintionTypeToBeCopied.getWidthPercentage();
        this.maxWidthInPx = fieldDefintionTypeToBeCopied.getMaxWidthInPx();
    }
}
