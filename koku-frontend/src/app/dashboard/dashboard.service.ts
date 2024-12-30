import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  constructor(private readonly httpClient: HttpClient) {
  }

  getConfig() {
    return this.httpClient.get<KokuDto.DashboardConfigDto>(`/backend/dashboard/config`);
  }

  getDeferredPanelContent(href: string) {
    return this.httpClient.get<KokuDto.IDashboardColumnContent>(`/backend${href}`);
  }

}
