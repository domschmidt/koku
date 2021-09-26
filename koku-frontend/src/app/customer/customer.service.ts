import {Injectable} from '@angular/core';
import {HttpClient, HttpEvent, HttpEventType, HttpParams} from "@angular/common/http";
import {BehaviorSubject, Observable} from "rxjs";
import {FileSystemFileEntry} from "ngx-file-drop";
import {UploadWithProgress} from "./customer-uploads/extended-upload.interface";
import * as FileSaver from "file-saver";

@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  private _customers: BehaviorSubject<KokuDto.CustomerDto[]> = new BehaviorSubject(new Array<KokuDto.CustomerDto>());

  public readonly customers: Observable<KokuDto.CustomerDto[]> = this._customers.asObservable();

  private _documents: BehaviorSubject<KokuDto.FormularDto[]> = new BehaviorSubject(new Array<KokuDto.FormularDto>());

  public readonly documents: Observable<KokuDto.FormularDto[]> = this._documents.asObservable();

  constructor(public httpClient: HttpClient) {
  }

  public getCustomers(searchValue?: string) {
    this.loadCustomers(searchValue).subscribe((result) => {
      this._customers.next(result)
    });
    return this.customers;
  }

  public getCustomer(id: number) {
    return this.httpClient.get<KokuDto.CustomerDto>(`/api/customers/${id}`);
  }

  public getCustomerAppointments(id: number) {
    return this.httpClient.get<KokuDto.CustomerAppointmentDto[]>(`/api/customers/${id}/appointments`);
  }

  public getCustomerSales(id: number) {
    return this.httpClient.get<KokuDto.CustomerSalesDto[]>(`/api/customers/${id}/sales`);
  }

  updateCustomer(customer: KokuDto.CustomerDto) {
    return new Observable((observer) => {
      this.httpClient.put(`/api/customers/${customer.id}`, customer).subscribe(() => {
        this.loadCustomers().subscribe((newResult) => {
          this._customers.next(newResult);
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

  createCustomer(customer: KokuDto.CustomerDto) {
    return new Observable<KokuDto.CustomerDto>((observer) => {
      this.httpClient.post(`/api/customers`, customer).subscribe((result: KokuDto.CustomerDto) => {
        this.loadCustomers().subscribe((newResult) => {
          this._customers.next(newResult);
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

  deleteCustomer(customer: KokuDto.CustomerDto) {
    return new Observable((observer) => {
      this.httpClient.delete(`/api/customers/${customer.id}`).subscribe(() => {
        this.loadCustomers().subscribe((newResult) => {
          this._customers.next(newResult);
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

  public getCustomerUploads(customerId: number) {
    return this.httpClient.get<KokuDto.UploadDto[]>(`/api/customers/${customerId}/uploads`)
  }

  deleteCustomerUpload(customerId: number, item: KokuDto.UploadDto) {
    return this.httpClient.delete(`/api/customers/${customerId}/uploads/${item.uuid}`)
  }

  addCustomerUpload(customerId: number, currentUpload: FileSystemFileEntry) {
    const result: UploadWithProgress = {
      fileName: currentUpload.name,
      progress: 0
    }
    currentUpload.file((file) => {
      const formData: FormData = new FormData();
      formData.append('file', file);
      this.httpClient.post(`/api/customers/${customerId}/uploads`, formData, {
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

  createCustomerDocument(customerId: number, document: KokuDto.FormularDto) {
    return this.httpClient.post<KokuDto.UploadDto>(`/api/customers/${customerId}/documents`, document);
  }

  public getDocument(customerId: number, documentId: number) {
    return this.httpClient.get<KokuDto.FormularDto>(`/api/customers/${customerId}/documents/${documentId}`)
  }

  private loadCustomers(searchValue?: string) {
    const params = new HttpParams({
      fromObject: {
        search: searchValue || ''
      }
    });
    return this.httpClient.get<KokuDto.CustomerDto[]>('/api/customers', {params});
  }

  exportAllCustomers() {
    this.httpClient.get(`/api/customers/export`, {responseType: 'blob'}).subscribe((result) => {
      FileSaver.saveAs(result, "koku-customers.vcf");
    });
  }
}
