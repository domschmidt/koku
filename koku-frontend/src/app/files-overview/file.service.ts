import {Injectable} from '@angular/core';
import {HttpClient, HttpEvent, HttpEventType} from "@angular/common/http";
import {UploadWithProgress} from "../customer/customer-uploads/extended-upload.interface";
import {FileSystemFileEntry} from "ngx-file-drop";

@Injectable({
  providedIn: 'root'
})
export class FileService {

  constructor(public httpClient: HttpClient) {
  }

  uploadFile(fileEntry: FileSystemFileEntry) {
    const result: UploadWithProgress = {
      fileName: fileEntry.name,
      progress: 0
    }
    fileEntry.file((file) => {
      const formData: FormData = new FormData();
      formData.append('file', file);
      this.httpClient.post(`/backend/files`, formData, {
        reportProgress: true,
        observe: 'events'
      }).subscribe((httpEvent: HttpEvent<any>) => {
        switch (httpEvent.type) {
          case HttpEventType.UploadProgress: {
            result.progress = Math.round(httpEvent.loaded * 100 / (httpEvent.total || 1));
            break;
          }
          case HttpEventType.Response: {
            if (httpEvent.ok) {
              delete result.errorStatusText;
              delete result.progress;
              result.fileName = httpEvent.body.fileName;
              result.uuid = httpEvent.body.uuid;
              result.creationDate = httpEvent.body.creationDate;
            } else {
              result.errorStatusText = httpEvent.statusText;
            }
            break;
          }
          case HttpEventType.Sent:
            console.log("Sent");
            break;
          case HttpEventType.ResponseHeader:
            console.log("ResponseHeader");
            break;
        }
      });
    });
    return result;
  }

  downloadFile(id: string) {
    return this.httpClient.get('/backend/files/' + id, {responseType: 'blob'});
  }

  deleteFile(id: string) {
    return this.httpClient.delete('/backend/files/' + id);
  }

}
