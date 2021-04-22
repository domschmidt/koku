import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {NgForm} from "@angular/forms";
import {DocumentService} from "../../document.service";
import {FileSystemFileEntry, NgxFileDropEntry} from "ngx-file-drop";
import {DomSanitizer, SafeHtml} from "@angular/platform-browser";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'document-svg-field',
  templateUrl: './document-svg-field.component.html',
  styleUrls: ['./document-svg-field.component.scss']
})
export class DocumentSvgFieldComponent {

  svgField: KokuDto.SVGFormularItemDto;
  saving: boolean = false;
  loading: boolean = true;
  createMode: boolean;
  trustedSVGContent: SafeHtml | undefined;
  acceptableMimeType: string = 'image/svg+xml';

  constructor(@Inject(MAT_DIALOG_DATA) public data: KokuDto.SVGFormularItemDto,
              public dialogRef: MatDialogRef<DocumentSvgFieldComponent>,
              public dialog: MatDialog,
              public domSanitizer: DomSanitizer,
              public matSnack: MatSnackBar,
              public documentService: DocumentService) {
    this.createMode = data === null;
    if (this.createMode) {
      this.svgField = {
        ['@type']: 'SVGFormularItemDto',
        widthPercentage: 100,
        maxWidthInPx: 500
      };
    } else {
      this.svgField = {...data};
      this.trustedSVGContent = this.getTrustedSvg(this.svgField);
    }
  }

  save(form: NgForm) {
    if (form.valid) {
      this.dialogRef.close(this.svgField);
    }
  }

  getTrustedSvg(value: KokuDto.SVGFormularItemDto) {
    return this.domSanitizer.bypassSecurityTrustHtml(atob(value.svgContentBase64encoded || ''));
  }

  dropped(droppedFiles: NgxFileDropEntry[]) {
    if (!droppedFiles || droppedFiles.length > 1) {
      throw new Error('Unsupported number of file selections.');
    }
    const newPicture = <FileSystemFileEntry>droppedFiles[0].fileEntry;
    newPicture.file((file => {
      if (file.type !== this.acceptableMimeType) {
        this.matSnack.open("Falsches Bildformat", 'ok', {
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
          this.matSnack.open("Bild konnte nicht erfasst werden.", 'ok', {
            duration: 5000
          });
        });
      }
    }));
  }

  getAlignStyle(align?: "LEFT" | "CENTER" | "RIGHT") {
    let result = 'left'
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
