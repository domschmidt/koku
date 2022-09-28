import {Component, Inject, Input, ViewChild} from '@angular/core';
import {DocumentFieldHostDirective} from "./document-field-host.directive";
import {DOCUMENT_CONFIG, DocumentConfig} from "./document-field-config.injector";
import {DocumentBase} from "./document.interface";

@Component({
  selector: 'document-capture',
  templateUrl: './document-capture.component.html',
  styleUrls: ['./document-capture.component.scss']
})
export class DocumentCaptureComponent {
  @Input() document!: DocumentBase;

  @ViewChild(DocumentFieldHostDirective, {static: true}) fieldHost!: DocumentFieldHostDirective;

  constructor(
    @Inject(DOCUMENT_CONFIG) public readonly documentConfig: DocumentConfig
  ) {
  }

  getFxFlex(size: number) {
    return Math.round((size / this.documentConfig.gridSize) * 100) + '%';
  }

}
