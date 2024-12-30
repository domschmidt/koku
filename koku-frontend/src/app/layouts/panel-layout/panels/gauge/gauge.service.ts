import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class GaugeService {
  constructor(public httpClient: HttpClient) {
  }

  getGauge(sourceUrl: string) {
    return this.httpClient.get<KokuDto.GaugePanelDto>(`/backend${sourceUrl}`);
  }

}
