import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {FileSystemFileEntry, NgxFileDropEntry} from "ngx-file-drop";
import {UploadWithProgress} from "../customer/customer-uploads/extended-upload.interface";
import {FileService} from "./file.service";
import * as FileSaver from "file-saver";
import {
  DocumentCaptureDialogComponent,
  DocumentCaptureDialogComponentData,
  DocumentCaptureDialogComponentResponseData
} from "../document/document-capture-dialog.component";

export interface FileUploadDialogComponentData {
  dynamicDocumentContext: KokuDto.DocumentContextEnumDto;
}

export interface FileUploadDialogComponentResponseData {
}

@Component({
  selector: 'file-upload-dialog',
  templateUrl: './file-upload-dialog.component.html',
  styleUrls: ['./file-upload-dialog.component.scss']
})
export class FileUploadDialogComponent {

  uploads: UploadWithProgress[] | undefined;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: FileUploadDialogComponentData,
    public dialogRef: MatDialogRef<FileUploadDialogComponent, FileUploadDialogComponentResponseData>,
    private readonly fileService: FileService,
    private readonly dialog: MatDialog
  ) {
  }

  dropped(droppedFiles: NgxFileDropEntry[]) {
    for (const currentUpload of droppedFiles) {
      if (!this.uploads) {
        this.uploads = [];
      }
      this.uploads.push(this.fileService.uploadFile(<FileSystemFileEntry>currentUpload.fileEntry));
    }
  }

  download(upload: UploadWithProgress) {
    if (upload.uuid && !upload.progress) {
      this.fileService.downloadFile(upload.uuid).subscribe((response) => {
        FileSaver.saveAs(response, upload.fileName);
      });
    }
  }

  documentSelected(selectedDocument: { [key: string]: any }) {
    if (selectedDocument !== undefined) {
      const documentCaptureDialogRef = this.dialog.open<DocumentCaptureDialogComponent, DocumentCaptureDialogComponentData, DocumentCaptureDialogComponentResponseData>(
        DocumentCaptureDialogComponent,
        {
          data: {
            documentId: selectedDocument.id
          },
          width: '100%',
          maxWidth: '90%',
          closeOnNavigation: false,
          position: {
            top: '20px'
          }
        }
      );
      documentCaptureDialogRef.afterClosed().subscribe((result) => {
        if (result && result.upload) {
          if (!this.uploads) {
            this.uploads = [];
          }
          this.uploads.push(result.upload);
        }
      });
    }
  }
}
