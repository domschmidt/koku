import {Component, Input} from '@angular/core';
import {FileSystemFileEntry, NgxFileDropEntry} from "ngx-file-drop";
import {DomSanitizer} from "@angular/platform-browser";
import {compressAccurately} from 'image-conversion';
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'user-info',
  templateUrl: './user-info.component.html',
  styleUrls: ['./user-info.component.scss']
})
export class UserInfoComponent {

  @Input() userDetails: KokuDto.KokuUserDetailsDto | undefined;
  private static readonly SUPPORTED_MIME_TYPES: string[] = ['image/png', 'image/jpeg', 'image/gif'];

  constructor(private readonly domSanitizer: DomSanitizer,
              private readonly matSnack: MatSnackBar) {
  }

  dropped(droppedFiles: NgxFileDropEntry[]) {
    if (!droppedFiles || droppedFiles.length > 1) {
      throw new Error('Unsupported number of file selections.');
    }
    const newPicture = <FileSystemFileEntry>droppedFiles[0].fileEntry;
    newPicture.file((file => {

      if (UserInfoComponent.SUPPORTED_MIME_TYPES.indexOf(file.type) < 0) {
        this.matSnack.open("Bildformat wird nicht unterstützt.", 'ok', {
          duration: 5000
        });
      } else {
        compressAccurately(file, {
          width: 400,
          size: 20
        }).then((blob) => {
          blob.arrayBuffer().then((arrayBuffer) => {
            let binary = '';
            const bytes = new Uint8Array(arrayBuffer);
            const len = bytes.byteLength;
            for (let i = 0; i < len; i++) {
              binary += String.fromCharCode(bytes[i]);
            }
            if (this.userDetails) {
              this.userDetails.avatarBase64 = btoa(binary);
            }
          }, () => {
            this.matSnack.open("Bild konnte nicht verarbeitet werden.", 'ok', {
              duration: 5000
            });
          });
        }, () => {
          this.matSnack.open("Bild konnte nicht verarbeitet werden.", 'ok', {
            duration: 5000
          });
        });
      }
    }));
  }
}
