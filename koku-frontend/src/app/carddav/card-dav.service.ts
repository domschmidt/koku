import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class CardDavService {

  constructor(private readonly httpClient: HttpClient) {
  }

  public sync() {
    return this.httpClient.post(`/api/carddav/sync`, {});
  }

  public getInfo() {
    return this.httpClient.get<KokuDto.CardDavInfoDto>(`/api/carddav/info`);
  }

}
