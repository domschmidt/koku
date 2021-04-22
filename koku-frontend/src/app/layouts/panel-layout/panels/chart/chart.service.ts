import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class ChartService {
  constructor(public httpClient: HttpClient) {
  }

  getChart(sourceUrl: string, params?: {
    [param: string]: string | string[];
  }) {
    return this.httpClient.get<KokuDto.ChartPanelDto>(`/api${sourceUrl}`, {
      params: params
    });
  }

}
