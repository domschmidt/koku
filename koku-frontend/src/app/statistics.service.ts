import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {

  constructor(public httpClient: HttpClient) {
  }

  loadStatistics() {
    return this.httpClient.get<KokuDto.StatisticsDto>('/api/statistics');
  }

}
