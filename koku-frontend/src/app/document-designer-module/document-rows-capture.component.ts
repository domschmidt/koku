import {Component, Input, ViewChild} from '@angular/core';
import {DocumentFieldHostDirective} from './document-field-host.directive';
import {DocumentBaseRow} from './document.interface';
import {DocumentRenderFieldTypes} from './document-field-config';

@Component({
  selector: 'document-rows-capture',
  templateUrl: './document-rows-capture.component.html',
  styleUrls: ['./document-rows-capture.component.scss']
})
export class DocumentRowsCaptureComponent {
  @Input() rows!: DocumentBaseRow[];
  @Input() fieldConfig!: DocumentRenderFieldTypes;
  @Input() gridSize!: number;

  @ViewChild(DocumentFieldHostDirective, {static: true}) fieldHost!: DocumentFieldHostDirective;

  constructor() {
  }

  getFxFlex(size: number): string {
    return Math.round((size / this.gridSize) * 100) + '%';
  }

}
