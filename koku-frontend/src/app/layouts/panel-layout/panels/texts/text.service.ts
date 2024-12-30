import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class TextService {
  constructor(public httpClient: HttpClient) {
  }

  getText(sourceUrl: string) {
    return this.httpClient.get<KokuDto.TextPanelDto>(`/backend${sourceUrl}`);
  }

}
