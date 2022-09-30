import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {BehaviorSubject, Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private _documents: BehaviorSubject<KokuDto.FormularDto[]> = new BehaviorSubject(new Array<KokuDto.FormularDto>());

  public readonly documents: Observable<KokuDto.FormularDto[]> = this._documents.asObservable();

  constructor(public httpClient: HttpClient) {
  }

  public getDocuments(searchValue?: string) {
    this.loadDocuments(searchValue).subscribe((result) => {
      this._documents.next(result)
    });
    return this.documents;
  }

  getDocument(documentId: number) {
    return this.httpClient.get<KokuDto.FormularDto>(`/api/documents/${documentId}`);
  }

  getDocumentsByContext(context: KokuDto.DocumentContextEnumDto) {
    return this.httpClient.get<KokuDto.FormularDto[]>(`/api/documents/context/${context}`);
  }

  getDocumentTextReplacementToken(context: KokuDto.DocumentContextEnumDto) {
    return this.httpClient.get<KokuDto.FormularReplacementTokenDto[]>(`/api/documents/contexts/${context}/replacementtoken/text`);
  }

  getDocumentDateReplacementToken(context: KokuDto.DocumentContextEnumDto) {
    return this.httpClient.get<KokuDto.FormularReplacementTokenDto[]>(`/api/documents/contexts/${context}/replacementtoken/date`);
  }

  getDocumentCheckboxReplacementToken(context: KokuDto.DocumentContextEnumDto) {
    return this.httpClient.get<KokuDto.FormularReplacementTokenDto[]>(`/api/documents/contexts/${context}/replacementtoken/checkbox`);
  }

  getDocumentQrcodeReplacementToken(context: KokuDto.DocumentContextEnumDto) {
    return this.httpClient.get<KokuDto.FormularReplacementTokenDto[]>(`/api/documents/contexts/${context}/replacementtoken/qrcode`);
  }

  createDocument(document: KokuDto.FormularDto) {
    return new Observable<KokuDto.FormularDto>((observer) => {
      return this.httpClient.post<KokuDto.FormularDto>(`/api/documents`, document).subscribe((result: KokuDto.FormularDto) => {
        this.loadDocuments().subscribe((newResult) => {
          this._documents.next(newResult);
          observer.next(result);
          observer.complete();
        }, (error) => {
          observer.error(error);
        })
      }, (error) => {
        observer.error(error);
      });
    });
  }

  duplicateDocument(document: KokuDto.FormularDto) {
    return new Observable<KokuDto.FormularDto>((observer) => {
      return this.httpClient.post<KokuDto.FormularDto>(`/api/documents/${document.id}`, document).subscribe((result: KokuDto.FormularDto) => {
        this.loadDocuments().subscribe((newResult) => {
          this._documents.next(newResult);
          observer.next(result);
          observer.complete();
        }, (error) => {
          observer.error(error);
        })
      }, (error) => {
        observer.error(error);
      });
    });
  }

  updateDocument(document: KokuDto.FormularDto) {
    return new Observable((observer) => {
      return this.httpClient.put(`/api/documents/${document.id}`, document).subscribe(() => {
        this.loadDocuments().subscribe((newResult) => {
          this._documents.next(newResult);
          observer.next();
          observer.complete();
        }, (error) => {
          observer.error(error);
        })
      }, (error) => {
        observer.error(error);
      });
    });
  }

  deleteDocument(document: KokuDto.FormularDto) {
    return new Observable((observer) => {
      return this.httpClient.delete(`/api/documents/${document.id}`).subscribe(() => {
        this.loadDocuments().subscribe((newResult) => {
          this._documents.next(newResult);
          observer.next();
          observer.complete();
        }, (error) => {
          observer.error(error);
        })
      }, (error) => {
        observer.error(error);
      });
    });
  }

  private loadDocuments(searchValue?: string) {
    const params = new HttpParams({
      fromObject: {
        search: searchValue || ''
      }
    });
    return this.httpClient.get<KokuDto.FormularDto[]>('/api/documents', {params});
  }

  getDocumentContexts() {
    return this.httpClient.get<KokuDto.DocumentContextDto[]>('/api/documents/contexts');
  }

  getDocumentCapture(documentId: number) {
    return this.httpClient.get<KokuDto.FormularDto>(`/api/documents/${documentId}/capture`);
  }

  saveCapturedDocument(document: KokuDto.FormularDto) {
    return this.httpClient.post<KokuDto.UploadDto>(`/api/documents/capture`, document);
  }
}
