import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {NgForm} from '@angular/forms';
import {DocumentService} from '../../../document/document.service';
import {FileSystemFileEntry, NgxFileDropEntry} from 'ngx-file-drop';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {MatSnackBar} from '@angular/material/snack-bar';
import {DocumentFieldMeta} from '../../../document-designer-module/document-field-config';

@Component({
  selector: 'document-svg-field',
  templateUrl: './document-svg-config-field.component.html',
  styleUrls: ['./document-svg-config-field.component.scss']
})
export class DocumentSvgConfigFieldComponent {

  svgField: KokuDto.SVGFormularItemDto;
  saving = false;
  loading = true;
  createMode: boolean;
  trustedSVGContent: SafeHtml | undefined;
  acceptableMimeType = 'image/svg+xml';

  constructor(@Inject(MAT_DIALOG_DATA) public data: {
                field?: KokuDto.SVGFormularItemDto
                meta: DocumentFieldMeta
              },
              public dialogRef: MatDialogRef<DocumentSvgConfigFieldComponent>,
              public dialog: MatDialog,
              public domSanitizer: DomSanitizer,
              public matSnack: MatSnackBar,
              public documentService: DocumentService) {
    this.createMode = data.field === undefined;
    if (data.field === undefined) {
      this.svgField = {
        id: 0,
        ['@type']: 'SVGFormularItemDto',
        widthPercentage: 100,
        maxWidthInPx: 500
      };
    } else {
      this.svgField = {...data.field};
      this.trustedSVGContent = this.getTrustedSvg(this.svgField);
    }
  }

  save(form: NgForm): void {
    if (form.valid) {
      this.dialogRef.close(this.svgField);
    }
  }

  getTrustedSvg(formularItem: KokuDto.SVGFormularItemDto): SafeHtml {
    return this.domSanitizer.bypassSecurityTrustHtml(atob(formularItem.svgContentBase64encoded || ''));
  }

  dropped(droppedFiles: NgxFileDropEntry[]): void {
    if (!droppedFiles || droppedFiles.length > 1) {
      throw new Error('Unsupported number of file selections.');
    }
    const newPicture = droppedFiles[0].fileEntry as FileSystemFileEntry;
    newPicture.file((file => {
      if (file.type !== this.acceptableMimeType) {
        this.matSnack.open('Falsches Bildformat', 'ok', {
          duration: 5000
        });
      } else {
        file.arrayBuffer().then((arrayBuffer) => {
          let binary = '';
          const bytes = new Uint8Array(arrayBuffer);
          const len = bytes.byteLength;
          for (let i = 0; i < len; i++) {
            binary += String.fromCharCode(bytes[i]);
          }
          if (this.svgField) {
            this.svgField.svgContentBase64encoded = btoa(binary);
            this.trustedSVGContent = this.getTrustedSvg(this.svgField);
          }
        }, () => {
          this.matSnack.open('Bild konnte nicht erfasst werden.', 'ok', {
            duration: 5000
          });
        });
      }
    }));
  }

  getAlignStyle(align?: 'LEFT' | 'CENTER' | 'RIGHT'): string {
    let result = 'left';
    switch (align) {
      case 'CENTER':
        result = 'center';
        break;
      case 'RIGHT':
        result = 'flex-end';
        break;
      case 'LEFT':
        result = 'flex-start';
        break;
      default:
        break;
    }
    return result;
  }
}
