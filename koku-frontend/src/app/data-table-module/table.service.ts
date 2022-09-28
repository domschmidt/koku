import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class TableService {

  constructor(private readonly httpClient: HttpClient) {
  }

  loadTable(endpoint: string, param: DataTableDto.DataQuerySpecDto, additionalQueryParams?: { [key: string]: string[] | string }) {
    return this.httpClient.post<DataTableDto.DataTableDto>(endpoint, param, {
      params: additionalQueryParams
    });
  }

}
