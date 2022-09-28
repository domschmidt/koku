import {Injectable} from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class SummaryService {

  printSummary(advancedSearchSpec: DataTableDto.DataQueryAdvancedSearchDto[], formatter: (value: any) => string) {
    let result = '';

    for (const currentAdvancedSearchSpec of advancedSearchSpec || []) {
      let opPrefix = '';
      let opPostfix = '';
      switch (currentAdvancedSearchSpec.customOp) {
        case "EW":
          opPrefix = '...';
          break;
        case "EQ":
          opPrefix = '=';
          break;
        case "GOE":
          opPrefix = '>='
          break;
        case "GT":
          opPrefix = '>'
          break;
        case "LIKE":
          opPrefix = '...';
          opPostfix = '...';
          break;
        case "LOE":
          opPrefix = '<=';
          break;
        case "LT":
          opPrefix = '<';
          break;
        case "SW":
          opPostfix = '...';
          break;
      }

      result += `(${opPrefix}${formatter(currentAdvancedSearchSpec.search)}${opPostfix})`;
    }

    return result;
  }

}
